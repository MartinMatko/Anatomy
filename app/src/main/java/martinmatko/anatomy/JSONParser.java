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

    private List<Term> terms = new ArrayList<>();
    public Question getQuestion() throws IOException, JSONException {
        Question question = new Question();
        String content;
        JSONArray paths;
        String caption;
        Context ctx = MainActivity.getAppContext();
        JSONObject flashcardContext;
        RESTService service = new RESTService();
        JSONObject dataFromREST = service.getTest("http://staging.anatom.cz/flashcards/practice/?avoid=[]&categories=[]&contexts=[]&limit=2&types=[]&without_contexts=1");
        flashcardContext = service.getFlashcard("http://staging.anatom.cz/flashcards/context/" + dataFromREST.getString("context_id"));

        JSONObject data = flashcardContext.getJSONObject("data");

            caption = data.getString("name");
            question.setCaption(caption);
            question.setCorrectAnswer(dataFromREST.getString("identifier"));
            content = data.getString("content");
        JSONObject JSONContent = new JSONObject(content);

        paths = JSONContent.getJSONArray("paths");
        SVGParser parser = new SVGParser();
        List<PartOfBody> parts = new ArrayList<>();

            // not possible to use foreach cycle
            for (int i = 0; i < paths.length(); i++){
                JSONObject path = paths.getJSONObject(i);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                String color = path.getString("color");
                if (color.charAt(0) == '#') {
                    paint.setColor(Color.parseColor(color));
                }
                String line = path.getString("d");
                String identifier = null;
                try{
                    identifier = path.getString("term");
                }
                catch (org.json.JSONException ex){
                }
                if (identifier != null)
                    parts.add(new PartOfBody(parser.doPath(line), paint, path.getString("term")) );

                else
                    color = toGrayScale(color);
                    paint.setColor(Color.parseColor(color));
                    parts.add(new PartOfBody(parser.doPath(line), paint) );
            }

            JSONArray flashcards = data.getJSONArray("flashcards");
            for (int i = 0; i < flashcards.length(); i++) {
                JSONObject flashcard = flashcards.getJSONObject(i);
                JSONObject term = flashcard.getJSONObject("term");
                String name = term.getString("name").split(",")[0];
                terms.add(new Term(name, term.getString("identifier")));
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


    public String rgbToHex (Integer[] rgb){
        String ret = "#";
        for (int i = 0; i < rgb.length; i++) {
            ret += Integer.toHexString(rgb[i]);
        }
        return ret;
    }
    public Integer[] HexTorgb (String colorStr){
        int r = Integer.valueOf( colorStr.substring( 1, 3 ), 16);
        int g = Integer.valueOf( colorStr.substring( 3, 5 ), 16);
        int b = Integer.valueOf( colorStr.substring( 5, 7 ), 16);
        Integer[] rgb = new Integer[]{r, g, b};
        return rgb;
    }

    public boolean isGray(String colorStr){
        Integer[] rgb  = HexTorgb(colorStr);
        return Math.max(Math.abs(rgb[0] - rgb[1]), Math.abs(rgb[0] - rgb[2])) < 10;
    }

    public String toGrayScale(String colorStr){
        if (isGray( colorStr)){
            return colorStr;
        }
        Integer[] rgb = HexTorgb(colorStr);

        Double[] weights = new Double[]{0.299, 0.587, 0.114};
        double graySum = 0;
        for (int i = 0; i < 3; i++) {
            graySum += 3.7 * rgb[i] * weights[i];
        }
        double grayAverage = Math.min(235, Math.round(lowerContrast(graySum / 3)));
        String grayAverageHex = Double.toHexString(grayAverage);
        if (grayAverageHex == "NaN") {
            return "#000000";
        }
        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = (int) Math.floor((rgb[i] + (int)grayAverage * 8) /9);
        }
        return rgbToHex(rgb);
    }

    public double lowerContrast(double grayValue) {
        grayValue = grayValue - 128;
        grayValue = grayValue * 0.5;
        grayValue = grayValue + 128;
        return grayValue;
    }


}
