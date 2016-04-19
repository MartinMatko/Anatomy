package martinmatko.anatomy;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import utils.Constants;

/**
 * Created by Martin on 15.3.2016.
 */
public class PostAsyncTask extends AsyncTask<String, Void, JSONObject> {

    private Map<String, String> cookies;
    private Test test;
    private String cookieString;

    public PostAsyncTask(Test test) {
        this.test = test;
        cookies = test.getService().getCookies();
        cookieString = test.getService().getCookieString();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        //Debug.waitForDebugger();
        test.setIsPOSTCompleted(false);
        URL url;
        StringBuilder response = new StringBuilder();
        String line;
        JSONObject data = null;
        try {
            url = new URL(Constants.SERVER_NAME + "flashcards/practice/?avoid=[" + URLEncoder.encode(params[1], "UTF-8") +
                    "]&categories=[" + test.getCategories() + "]&contexts=[]&limit=1&types=[]&without_contexts=1");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Cookie", cookieString);
            conn.setRequestProperty("X-" + "csrftoken", cookies.get("csrftoken"));
            conn.setRequestProperty("X-" + "sessionid", cookies.get("sessionid"));
            conn.setRequestProperty("Accept", "application/json, text/plain, */*");
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(params[0].length()));
            conn.setRequestMethod("POST");
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(params[0]);
            writer.flush();
            writer.close();
            conn.connect();
            int status = conn.getResponseCode();
            BufferedReader br;
            if (status == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
            JSONObject context = null;
            JSONObject flashcard = null;
            try {
                JSONObject question = new JSONObject(response.toString());
                context = question.getJSONObject("data").getJSONArray("flashcards").getJSONObject(0);
                flashcard = test.getService().get(Constants.SERVER_NAME + "flashcards/context/" + context.getString("context_id"));
                test.getQuestions().add(new JSONParser().getQuestion(context, flashcard));
                test.setIsPOSTCompleted(true);
            } catch (Exception e) {
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