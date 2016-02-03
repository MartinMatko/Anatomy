package martinmatko.anatomy;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Martin on 7.11.2015.
 */
public class HTTPService {
    List<Cookie> cookies;

    public JSONObject getContext(String url) {
        JSONObject data = null;
        DefaultHttpClient client = new DefaultHttpClient();
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse responseGet = client.execute(get);
            HttpEntity resEntityGet = responseGet.getEntity();
            cookies = client.getCookieStore().getCookies();
            if (resEntityGet != null) {
                //do something with the response
                InputStream is;
                is = resEntityGet.getContent();
                resEntityGet.toString();
                String json = new JSONParser().readFully(is, "UTF-8");
                JSONObject question = new JSONObject(json);
                return question.getJSONObject("data");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public JSONObject getFlashcard(String url) {
        JSONObject flashcard = null;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);
            HttpResponse responseGet = client.execute(get);
            cookies = client.getCookieStore().getCookies();
            HttpEntity resEntityGet = responseGet.getEntity();

            if (resEntityGet != null) {
                //do something with the response
                InputStream is;
                is = resEntityGet.getContent();
                resEntityGet.toString();
                String json = new JSONParser().readFully(is, "UTF-8");
                flashcard = new JSONObject(json);
            }
            //post("hulahop", cookies);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flashcard;
    }


    public void post(String answer, List<Cookie> cookies) {
        try {
            String postURL = "https://staging.anatom.cz/flashcards/practice/";
            HttpPost post = new HttpPost(postURL);
            post.addHeader("X-CSRFTOKEN", cookies.get(0).getValue());
            post.addHeader("X-sessionid", cookies.get(1).getValue());
            StringEntity entity = new StringEntity(answer);
            entity.setContentType(new BasicHeader("Content-Type",
                    "raw"));
            post.setEntity(entity);
            DefaultHttpClient client = new DefaultHttpClient();
            HttpResponse responsePOST = client.execute(post);
            StringBuilder sb = new StringBuilder();
            for (Header header : post.getAllHeaders()) {
                sb.append(header.getName() + ": " + header.getValue());
            }
            Log.i("Cookies: ", sb.toString());
            HttpEntity resEntity = responsePOST.getEntity();
            if (resEntity != null) {
                Log.i("RESPONSE ", EntityUtils.toString(resEntity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}