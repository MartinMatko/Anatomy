package martinmatko.Anatom;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import utils.Constants;

/**
 * Created by Martin on 7.11.2015.
 */
public class HTTPService {
    private Map<String, String> cookies = new HashMap<>();
    private String cookieString = "";

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public String getCookieString() {
        return cookieString;
    }

    public void setCookieString(String cookieString) {
        this.cookieString = cookieString;
    }

    public JSONObject get(String urlString) {
        URL url;
        StringBuilder response = new StringBuilder();
        String line;
        JSONObject data;
        try {
            url = new URL(urlString);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestProperty("Cookie", cookieString);
            conn.setRequestProperty("X-" + "csrftoken", cookies.get("csrftoken"));
            conn.setRequestProperty("X-" + "sessionid", cookies.get("sessionid"));
            conn.setDoInput(true);
            BufferedReader br;
            int code = conn.getResponseCode();
            if (code == 200 || code == 202) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
            } else if (code != -1) {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
            }
            data = new JSONObject(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
        return data;
    }

    public int post(String urlString, String postData) {
        String response = "";
        int status = 0;
        try {
            URL url = new URL(urlString);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Cookie", cookieString);
            conn.setRequestProperty("X-" + "csrftoken", cookies.get("csrftoken"));
            conn.setRequestProperty("X-" + "sessionid", cookies.get("sessionid"));
            conn.setRequestProperty("Accept", "application/json, text/plain, */*");
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length()));
            conn.setRequestMethod("POST");
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(postData);
            writer.flush();
            writer.close();
            conn.connect();
            status = conn.getResponseCode();
            BufferedReader br;
            String line;
            if (status == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                List<String> cookies = conn.getHeaderFields().get("Set-Cookie");
                String[] token = cookies.get(0).split("=");
                String[] sesionid = cookies.get(1).split("=");
                setUpCookies(sesionid[0] + "=" + sesionid[1].split(";")[0] + "; " + token[0] + "=" + token[1].split(";")[0]);
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            while ((line = br.readLine()) != null) {
                response += line;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }


    public int createSesion() {
        URL url;
        String response = "";
        String line;
        int responseCode = 0;
        try {
            url = new URL(Constants.SERVER_NAME + "user/session/");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            BufferedReader br;
            responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
                //System.out.println(response);
            }
            br.close();
            List<String> cookies = conn.getHeaderFields().get("Set-Cookie");
            String[] token = cookies.get(0).split("=");
            String[] sesionid = cookies.get(1).split("=");
            setUpCookies(sesionid[0] + "=" + sesionid[1].split(";")[0] + "; " + token[0] + "=" + token[1].split(";")[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseCode;
    }

    public Map<String, String> setUpCookies(String cookieString) {
        String[] cookiesArray = cookieString.split("; ");//sessionid and csrftoken
        String[] cookie1 = cookiesArray[0].split("=");
        String[] cookie2 = cookiesArray[1].split("=");
        cookies.put(cookie1[0], cookie1[1]);
        cookies.put(cookie2[0], cookie2[1]);
        this.cookieString = cookieString;
        return cookies;
    }
}
