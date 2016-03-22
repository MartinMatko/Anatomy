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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Martin on 7.11.2015.
 */
public class HTTPService {
    Map<String, String> cookies = new HashMap<>();
    DefaultHttpClient client;
    org.apache.http.client.CookieStore cookieStore;
    String cookieString = "";

    public JSONObject get(String urlString) {
        URL url;
        String response = "";
        String line;
        JSONObject data = null;
        try {
            url = new URL(urlString);
            HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3Factory());
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("Cookie", cookieString);
            conn.setRequestProperty("X-" + "csrftoken", cookies.get("csrftoken"));
            conn.setRequestProperty("X-" + "sessionid", cookies.get("sessionid"));
            conn.setSSLSocketFactory(new NoSSLv3Factory());
            conn.setInstanceFollowRedirects(false);
            conn.setReadTimeout(60 * 1000);
            conn.setConnectTimeout(60 * 1000);
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(0);
            conn.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = br.readLine()) != null) {
                response += line;
            }

            br.close();
            data = new JSONObject(response);
            if (cookieString.isEmpty()){
                List<String> cookies = conn.getHeaderFields().get("Set-Cookie");
                String[] token = cookies.get(0).split("=");
                String[] sesionid = cookies.get(1).split("=");
                setUpCookies(sesionid[0] + "=" +  sesionid[1].split(";")[0]  + ";" +token[0] + "=" +  token[1].split(";")[0] );
            }
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public void setUpCookies(String cookieString){
        String[] cookiesArray = cookieString.split(";");
        String[] token = cookiesArray[0].split("=");
        String[] sessionid = cookiesArray[1].split("=");
        cookies.put(token[0], token[1]);
        cookies.put(sessionid[0], sessionid[1]);
        this.cookieString = cookieString;
    }
}
