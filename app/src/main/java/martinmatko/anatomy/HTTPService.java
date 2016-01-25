package martinmatko.anatomy;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Martin on 7.11.2015.
 */
public class HTTPService {
    List<Cookie> cookies;
    CookieStore cookieStore;
    Header[] setCookies;

    public JSONObject getTest(String url) {
        String contextID = null;
        JSONObject data = null;
        DefaultHttpClient client = new DefaultHttpClient();
        try {
            HttpGet get = new HttpGet(url);
            HttpResponse responseGet = client.execute(get);
            HttpEntity resEntityGet = responseGet.getEntity();

            cookieStore = client.getCookieStore();
            cookies = cookieStore.getCookies();
            setCookies = responseGet.getHeaders("Set-Cookie");
            if (resEntityGet != null) {
                //do something with the response
                InputStream is;
                is = resEntityGet.getContent();
                resEntityGet.toString();
                String json = new JSONParser().readFully(is, "UTF-8");
                JSONObject question = new JSONObject(json);
                JSONArray array = question.getJSONObject("data").getJSONArray("flashcards");
                JSONObject termToDescription = null;
                for (int i = 0; i < array.length(); i++) {
                    if (array.getJSONObject(i).getString("direction").equals("t2d")) {
                        termToDescription = array.getJSONObject(i);
                    }
                }
                contextID = termToDescription.getString("context_id");
                data = new JSONObject();
                data.put("context_id", contextID);
                data.put("name", termToDescription.getJSONObject("term").getString("name"));
                data.put("description", termToDescription.getString("description"));
                data.put("identifier", termToDescription.getString("identifier"));
                //post(client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public JSONObject getFlashcard(String url) {
        String contextID = null;
        JSONObject data = null;
        JSONObject flashcard = null;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);
            HttpResponse responseGet = client.execute(get);
            HttpEntity resEntityGet = responseGet.getEntity();
            cookieStore = client.getCookieStore();
            cookies = cookieStore.getCookies();

            if (resEntityGet != null) {
                //do something with the response
                InputStream is;
                is = resEntityGet.getContent();
                resEntityGet.toString();
                String json = new JSONParser().readFully(is, "UTF-8");
                flashcard = new JSONObject(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flashcard;
    }


    public void post(HttpClient client) {
        try {
            String body = "{\"answers\":[{\"flashcard_id\":370,\"flashcard_answered_id\":734,\"response_time\":59203,\"direction\":\"t2d\",\"option_ids\":[734],\"time\":1453658532305}]}";
            String postURL = "https://staging.anatom.cz/flashcards/practice/";
            HttpPost post = new HttpPost(postURL);
            post.addHeader("X-CSRFTOKEN", cookies.get(0).getValue());
            post.addHeader("X-sessionid", cookies.get(1).getValue());
            StringEntity entity = new StringEntity(body);
            entity.setContentType(new BasicHeader("Content-Type",
                    "raw"));
            post.setEntity(entity);

//            HttpContext localContext = new BasicHttpContext();
//            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
//            List<NameValuePair> params = new ArrayList<NameValuePair>();
//             //   PASSING THE TOKEN GOTTEN FROM THE CODE ABOVE
//            post.addHeader("x-sessionid", cookies.get(1).getValue()); //   PASSING THE SESSIONID
//            StringEntity entity = new StringEntity(body);
//            entity.setContentType(new BasicHeader("Content-Type",
//                    "application/atom+xml"));
//            post.setEntity(entity);
//
//            post.addHeader("Cookie", "_ga=GA1.2.977886069.1448194905; __utmx=125757914.UerhSQbmRoi890TNRKcmtg$0:1; __utmxx=125757914.UerhSQbmRoi890TNRKcmtg$0:1448804348:8035200; csrftoken=XUItNU1gPFNWbXDcOpVedqxZrFdjhABF; sessionid=707natnhs8cei2dvd4scfbu2d89x8ajr");
//            params.add(new BasicNameValuePair("X-CSRFTOKEN", cookies.get(0).getValue()));
//            params.add(new BasicNameValuePair("X-sessionid", cookies.get(1).getValue()));
//            UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
//            post.setEntity(ent);
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