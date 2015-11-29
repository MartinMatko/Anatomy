package martinmatko.anatomy;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 7.11.2015.
 */
public class RESTService {
    List <Cookie> cookies;
    CookieStore cookieStore;

    public String get (String url){
        String contextID = null;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);
            HttpResponse responseGet = client.execute(get);
            HttpEntity resEntityGet = responseGet.getEntity();
            cookieStore = client.getCookieStore();
            cookies =  cookieStore.getCookies();

            if (resEntityGet != null) {
                //do something with the response
                InputStream is;
                is = resEntityGet.getContent();
                resEntityGet.toString();
                String json = new JSONParser().readFully(is, "UTF-8");
                JSONObject question = new JSONObject(json);
                JSONArray array = question.getJSONObject("data").getJSONArray("flashcards");
                contextID = array.getJSONObject(0).getString("context_id");


                post();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contextID;
    }
    public void post(){
        try {
            HttpClient client = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();

            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            String postURL = "http://anatom.cz/user/session/";
            HttpPost post = new HttpPost(postURL);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user", "Martin"));

            UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            post.setEntity(ent);
            HttpResponse responsePOST = client.execute(post, localContext);
            HttpEntity resEntity = responsePOST.getEntity();
            if (resEntity != null) {
                Log.i("RESPONSE",EntityUtils.toString(resEntity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}