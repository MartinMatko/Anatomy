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

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Martin on 15.3.2016.
 */
public class ServiceAsyncTask extends AsyncTask<String, Void, JSONObject> {

    public DefaultHttpClient client;
    public org.apache.http.client.CookieStore cookieStore;
    public List<Cookie> cookies;
    private Test test;

    public ServiceAsyncTask(Test test) {
        this.test = test;
        cookies = test.service.cookies;
        cookieStore = test.service.cookieStore;
        client = test.service.client;
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
        long time0;
        long a, b, c;
        try {
            url = new URL("https://staging.anatom.cz/flashcards/practice/?avoid=%5B2064%5D&categories=%5B%5D&contexts=%5B%5D&limit=1&types=%5B%5D&without_contexts=1");
            //https://staging.anatom.cz/flashcards/practice/?avoid=%5B2064%5D&categories=%5B%5D&contexts=%5B%5D&limit=1&types=%5B%5D&without_contexts=1
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(60 * 1000);
            conn.setConnectTimeout(60 * 1000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
            String cookieString = cookies.get(0).getName().toString() + "=" + cookies.get(0).getValue().toString() + "; ";
            cookieString += cookies.get(1).getName().toString() + "=" + cookies.get(1).getValue().toString();
            conn.setRequestProperty("Cookie", cookieString);
            conn.setRequestProperty("X-" + cookies.get(0).getName(), cookies.get(0).getValue());
            conn.setRequestProperty("X-" + cookies.get(1).getName(), cookies.get(1).getValue());
            //conn.setRequestProperty("Accept", "application/json, text/plain, */*");
            //conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            //conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Length", Integer.toString(params[0].length()));
            conn.setRequestMethod("POST");
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(params[0]);
            writer.flush();
            writer.close();
            time0 = System.currentTimeMillis();
            conn.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            a = System.currentTimeMillis() - time0;
            while ((line = br.readLine()) != null) {
                response += line;
            }
            br.close();
            b = System.currentTimeMillis() - a - time0;
            JSONObject context = null;
            JSONObject flashcard = null;
            try {
                JSONObject question = new JSONObject(response);
                context = question;
                context = context.getJSONObject("data").getJSONArray("flashcards").getJSONObject(0);
                flashcard = test.service.get("https://staging.anatom.cz/flashcards/context/" + context.getString("context_id"));

            } catch (JSONException e) {
                System.out.println(context);
                System.out.println(flashcard);
                System.out.println(e.getMessage() + e.toString());
                e.printStackTrace();
            }
            c = System.currentTimeMillis() - b - a - time0;
            test.questions.add(new JSONParser().getQuestion(context, flashcard));
            test.isPOSTCompleted = true;
            //conn.disconnect();
            System.out.println("execute: " + a + "\ntoString: " + b + "\nJSONObject: " + c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
//        long time0;
//        long a, b, c;
//        try {
//            String postURL = "https://staging.anatom.cz/flashcards/practice/";
//            HttpPost post = new HttpPost(postURL);
//            post.addHeader("X-" + cookies.get(0).getName(), cookies.get(0).getValue());
//            post.addHeader("X-" + cookies.get(1).getName(), cookies.get(1).getValue());
//            post.addHeader("Connection", "Keep-Alive");
//            post.setHeader("Content-Type", "application/json");
//            post.setHeader("Accept", "application/json");
//
//            StringEntity entity = new StringEntity(params[0]);
//            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//            post.setEntity(entity);
//            time0 = System.currentTimeMillis();
//            client.setCookieStore(cookieStore);
//            HttpResponse responsePOST = client.execute(post);
//            client.getConnectionManager().shutdown();
//            a = System.currentTimeMillis() - time0;
//            HttpEntity resEntity = responsePOST.getEntity();
//            if (resEntity != null) {
//                String entityString  = EntityUtils.toString(resEntity);
//                responsePOST.getEntity().consumeContent();
//                b = System.currentTimeMillis() - a - time0;
//                JSONObject question = new JSONObject(entityString);
//                c = System.currentTimeMillis() - b - a - time0;
//                System.out.println("execute: " + a + "\ntoString: " + b + "\nJSONObject: " + c);
//                JSONObject context = null;
//                JSONObject flashcard = null;
//                try {
//                    context = question;
//                    context = context.getJSONObject("data").getJSONArray("flashcards").getJSONObject(1);
//                    flashcard = test.service.get("https://staging.anatom.cz/flashcards/context/" + context.getJSONObject("context").getString("id"));
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                test.questions.add(new JSONParser().getQuestion(context, flashcard));
//                test.isPOSTCompleted = true;
//                return question;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
    }

    protected void onPostExecute(JSONObject result) {

    }
}