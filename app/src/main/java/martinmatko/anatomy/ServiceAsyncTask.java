package martinmatko.anatomy;

import android.os.AsyncTask;
import android.os.Debug;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Martin on 15.3.2016.
 */
public class ServiceAsyncTask extends AsyncTask<String, Void, JSONObject> {

    private Map<String, String> cookies;
    private Test test;
    private String cookieString;

    public ServiceAsyncTask(Test test) {
        this.test = test;
        cookies = test.service.cookies;
        cookieString = test.service.cookieString;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        //Debug.waitForDebugger();
        test.isPOSTCompleted = false;
        URL url;
        String response = "";
        String line;
        JSONObject data = null;
        try {
            url = new URL("https://staging.anatom.cz/flashcards/practice/?avoid=%5B2064%5D&categories=%5B%5D&contexts=%5B%5D&limit=1&types=%5B%5D&without_contexts=1");
            //https://staging.anatom.cz/flashcards/practice/?avoid=%5B2064%5D&categories=%5B%5D&contexts=%5B%5D&limit=1&types=%5B%5D&without_contexts=1
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
            conn.setRequestProperty("Cookie", cookieString);
            conn.setRequestProperty("X-" + "csrftoken", cookieString.split(";")[1].split("=")[1] );
            conn.setRequestProperty("X-" + "sessionid", cookies.get("sessionid"));
            conn.setRequestProperty("Accept", "application/json, text/plain, */*");
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Length", Integer.toString(params[0].length()));
            conn.setRequestMethod("POST");
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(params[0]);
            writer.flush();
            writer.close();
            conn.connect();
            int status = conn.getResponseCode();
            BufferedReader br;
            if (status == 200){
                 br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            }
            else {
                 br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            while ((line = br.readLine()) != null) {
                response += line;
            }
            br.close();
            JSONObject context = null;
            JSONObject flashcard = null;
            try {
                System.out.println(response);
                JSONObject question = new JSONObject(response);
                context = question.getJSONObject("data").getJSONArray("flashcards").getJSONObject(0);
                flashcard = test.service.get("https://staging.anatom.cz/flashcards/context/" + context.getString("context_id"));
                test.questions.add(new JSONParser().getQuestion(context, flashcard));
                test.isPOSTCompleted = true;
            } catch (Exception e) {
                System.out.println(context);
                System.out.println(flashcard);
                System.out.println(e.getMessage() + e.toString());
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    protected void onPostExecute(JSONObject result) {

    }
}