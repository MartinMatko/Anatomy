package martinmatko.anatomy;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.http.AndroidHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Martin on 7.11.2015.
 */
public class JSONParser {
    public List<String> getTerms() {
        return terms;
    }
    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    private List<String> terms = new ArrayList<>();
    public Question getQuestion() throws IOException, JSONException {
        Question question = new Question();
        Context ctx = MainActivity.getAppContext();
        InputStream is;
        RESTService service = new RESTService();
        String contextID = service.get("http://anatom.cz/flashcards/practice/?avoid=[]&categories=[]&contexts=[]&limit=2&types=[]&without_contexts=1");
        is = new DownloadFilesTask().doInBackground(new URL("http://anatom.cz/flashcards/context/"+contextID));
        //is = ctx.getResources().openRawResource(R.raw.krajinyhlavy);
        JSONObject body = new JSONObject();
        String json = readFully(is, "UTF-8");
        String content = null, caption;
            body = new JSONObject(json);
            JSONObject data = body.getJSONObject("data");

            caption = data.getString("name");
            question.setCaption(caption);
            content = data.getString("content");


        SVGParser parser = new SVGParser();
        List<PartOfBody> parts = new ArrayList<>();
        List<String> parsed;
        //parsed = new ArrayList<>(Arrays.asList(body.split("d\\\\\": \\\\\"")));
        parsed = new ArrayList<>(Arrays.asList(content.split("bbox")));
        //parsed.remove(0);
        parsed.remove(parsed.size()-1);
        parsed.remove(parsed.size()-1);
        try{
            for (String line : parsed){
                //line = line.substring(0, line.indexOf("\\"));
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                int startOfColor = line.indexOf("color")+9;
                String color = line.substring(startOfColor, startOfColor + 7);
                if (color.charAt(0) == '#') {
                    paint.setColor(Color.parseColor(color));
                }

                if (line.contains("z")){
                    line = line.substring(line.indexOf('M'), line.indexOf('z')+1);
                }
                else
                    line = line.substring(line.indexOf('M'), line.length()-1);
                if (line.startsWith("M")){
                    parts.add(new PartOfBody(parser.doPath(line), paint) );
                }
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

            JSONArray flashcards = data.getJSONArray("flashcards");
            for (int i = 0; i < flashcards.length(); i++) {
                JSONObject flashcard = flashcards.getJSONObject(i);
                JSONObject term = flashcard.getJSONObject("term");
                terms.add(term.getString("name"));
                //getTerm(term.getString("url"));
            }
        question.setOptions(terms);
        question.setBodyParts(parts);
        return question;
    }
    public String readFully(InputStream inputStream, String encoding)
            throws IOException {
        return new String(readFully(inputStream), encoding);
    }

    private byte[] readFully(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toByteArray();
    }


    private boolean isValidColor(String color){
        if (color.equals(null))
            return false;
        if (color.contains("none"))
            return false;
        return color.length()==7;
    }

    public void getTerm(String urlOfTerm) {
        InputStream is;
        try {
            is = new DownloadFilesTask().doInBackground(new URL("http://anatom.cz" + urlOfTerm));
            JSONObject term = new JSONObject(readFully(is, "UTF-8")).getJSONObject("data");
            JSONArray parents = term.getJSONArray("parents");
            for (int i = 0; i < parents.length(); i++) {
                JSONObject parent = parents.getJSONObject(i);
                if(isValidTermOfQuestion(parent)){
                    terms.add(term.getString("name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean isValidTermOfQuestion(JSONObject jsonObject) throws JSONException {
        boolean isNotNumeric = !android.text.TextUtils.isDigitsOnly(jsonObject.getString("identifier"));
        boolean isInModel = !jsonObject.getBoolean("not-in-model");
        return isInModel && isNotNumeric;
    }
}
