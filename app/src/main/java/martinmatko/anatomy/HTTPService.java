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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Martin on 7.11.2015.
 */
public class HTTPService {
    static java.net.CookieManager msCookieManager = new java.net.CookieManager();
    List<Cookie> cookies;
    HttpURLConnection connection;
    DefaultHttpClient client = new DefaultHttpClient();


    //    public void setUpCookies() {
//        URL url = null;
//        HttpsURLConnection conn = null;
//        try {
//            url = new URL("https://staging.anatom.cz/user/session/");
//            conn = (HttpsURLConnection) url.openConnection();
//            conn.setRequestProperty("Connection","Keep-Alive");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Map<String, List<String>> headerFields = conn.getHeaderFields();
//        List<String> cookiesHeader = headerFields.get("Set-Cookie");
//
//        if(cookiesHeader != null)
//        {
//            for (String cookie : cookiesHeader)
//            {
//                msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
//            }
//        }
//        cookies = msCookieManager.getCookieStore().getCookies();
//    }
    public void setUpCookies() {
        HttpGet get = new HttpGet("https://staging.anatom.cz/user/session/");
        HttpResponse responseGet = null;
        try {
            responseGet = client.execute(get);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpEntity resEntityGet = responseGet.getEntity();

        try {
            resEntityGet.consumeContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cookies = client.getCookieStore().getCookies();
    }


    public JSONObject get(String requestURL) {
        URL url;
        String response = "";
        String line;
        JSONObject data = null;
        try {
            url = new URL(requestURL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            //conn.connect();
            conn.setRequestProperty("X-" + cookies.get(0).getName(), cookies.get(0).getValue());
            conn.setRequestProperty("X-" + cookies.get(1).getName(), cookies.get(1).getValue());
            conn.setRequestMethod("GET");


            Map<String, List<String>> headerFields = conn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }
            //cookies = msCookieManager.getCookieStore().getCookies();
            int status = conn.getResponseCode();
            if (status != 403) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            }
            conn.disconnect();
            JSONObject question = new JSONObject(response);
            return question;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public JSONObject post(String answer) {
        String response = "Shit happnes";
        try {
            String postURL = "https://staging.anatom.cz/flashcards/practice/";
            HttpPost post = new HttpPost(postURL);
            post.addHeader("X-" + cookies.get(0).getName(), cookies.get(0).getValue());
            post.addHeader("X-" + cookies.get(1).getName(), cookies.get(1).getValue());
            StringEntity entity = new StringEntity(answer);
            entity.setContentType(new BasicHeader("Content-Type",
                    "raw"));
            post.setEntity(entity);

            HttpResponse responsePOST = client.execute(post);

            StringBuilder sb = new StringBuilder();
            for (Header header : post.getAllHeaders()) {
                sb.append(header.getName() + ": " + header.getValue());
            }
            Log.i("Cookies: ", sb.toString());
            HttpEntity resEntity = responsePOST.getEntity();

            if (resEntity != null) {
                response = EntityUtils.toString(resEntity);
                resEntity.consumeContent();
                Log.i("RESPONSE ", response);
                JSONObject question = new JSONObject(response);
                return question;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


//    public JSONObject post(String answer) {
//        URL url;
//        String response = "";
//        String line;
//        JSONObject data = null;
//
//        try {
//            url = new URL("https://staging.anatom.cz/flashcards/practice/");
//            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
//            conn.setReadTimeout(60 * 1000);
//            conn.setConnectTimeout(60 * 1000);
//
//            conn.setDoOutput(true);
//            conn.setChunkedStreamingMode(0);
//            conn.setRequestProperty("X-csrftoken", cookies.get(0).getValue());
//            conn.setRequestProperty ("X-" + cookies.get(1).getName(), cookies.get(1).getValue());
//            conn.setRequestProperty ("Content-Type", "raw");
//            conn.setRequestMethod("POST");
//            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
//            writer.write(answer);
//            writer.flush();
//            writer.close();
//            int status = conn.getResponseCode();
//            if (status != 403){
//                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                while ((line=br.readLine()) != null) {
//                    response+=line;
//                }
//            }
//            else {
//                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
//                while ((line=br.readLine()) != null) {
//                    response+=line;
//                }
//            }
//            conn.disconnect();
//            JSONObject question = new JSONObject(response);
//            return question;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return data;
//    }
}