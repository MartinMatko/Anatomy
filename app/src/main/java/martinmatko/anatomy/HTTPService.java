package martinmatko.anatomy;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by Martin on 7.11.2015.
 */
public class HTTPService {
    List<Cookie> cookies;
    DefaultHttpClient client;
    org.apache.http.client.CookieStore cookieStore;

    public void setUpCookies() {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        params.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        client = new DefaultHttpClient(params);
        HttpParams httpParameters = client.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 100000);
        HttpConnectionParams.setSoTimeout(httpParameters, 100000);
        HttpConnectionParams.setTcpNoDelay(httpParameters, true);
        HttpGet get = new HttpGet("https://staging.anatom.cz/user/session/");
        get.addHeader("Connection", "Keep-Alive");
        HttpResponse responseGet = null;
        try {
            responseGet = client.execute(get);
            responseGet.getHeaders("cookie");
            System.out.println(responseGet.getHeaders("cookie").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (responseGet != null) {
            try {
                responseGet.getEntity().consumeContent();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cookieStore = client.getCookieStore();
        cookies = client.getCookieStore().getCookies();
    }

    public JSONObject get(String url) {
        JSONObject flashcard = null;
        try {
            HttpGet get = new HttpGet(url);
            if (cookies != null) {
                get.addHeader("X-" + cookies.get(0).getName(), cookies.get(0).getValue());
                get.addHeader("X-" + cookies.get(1).getName(), cookies.get(1).getValue());
            }
            get.addHeader("Connection", "Keep-Alive");
            HttpResponse responseGet = client.execute(get);
            if (responseGet != null) {
                flashcard = new JSONObject(EntityUtils.toString(responseGet.getEntity()));
                responseGet.getEntity().consumeContent();
            }
            if (cookies == null) {
                cookieStore = client.getCookieStore();
                cookies = client.getCookieStore().getCookies();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flashcard;
    }


    public JSONObject post(String answer) {
        String response = "";
        long time0;
        long a, b, c;
        try {

            String postURL = "https://staging.anatom.cz/flashcards/practice/";
            HttpPost post = new HttpPost(postURL);
            post.setHeader("X-" + cookies.get(0).getName(), cookies.get(0).getValue());
            post.setHeader("X-" + cookies.get(1).getName(), cookies.get(1).getValue());
            String cookieString = cookies.get(0).getName().toString() + "=" + cookies.get(0).getValue().toString() + "; ";
            cookieString += cookies.get(1).getName().toString() + "=" + cookies.get(1).getValue().toString();
            post.setHeader("Cookie", cookieString);
            post.setHeader("Connection", "Keep-Alive");
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");

            StringEntity entity = new StringEntity(answer);
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            post.setEntity(entity);
            time0 = System.currentTimeMillis();
            client = new DefaultHttpClient();
            //client.setCookieStore(cookieStore);
            HttpResponse responsePOST = client.execute(post);

            a = System.currentTimeMillis() - time0;
            HttpEntity resEntity = responsePOST.getEntity();
            if (resEntity != null) {
                String entityString = EntityUtils.toString(resEntity);
                responsePOST.getEntity().consumeContent();
                b = System.currentTimeMillis() - a - time0;
                JSONObject question = new JSONObject(entityString);
                Log.i("RESPONSE ", response);
                c = System.currentTimeMillis() - b - a - time0;
                System.out.println("execute: " + a + "\ntoString: " + b + "\nJSONObject: " + c);
                return question;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
