package martinmatko.anatomy;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 7.11.2015.
 */
public class JSONParser {

    private List<Term> terms = new ArrayList<>();
    public Question getQuestion(boolean isD2T) throws IOException, JSONException {
        Question question = new Question();
        String content;
        JSONArray paths;
        String caption;
        Context ctx = MainActivity.getAppContext();
        JSONObject flashcardContext;
        HTTPService service = new HTTPService();
        JSONObject dataFromREST = service.getTest("https://staging.anatom.cz/flashcards/practice/?avoid=[]&categories=[]&contexts=[]&limit=2&types=[]&without_contexts=1");
        flashcardContext = service.getFlashcard("https://staging.anatom.cz/flashcards/context/" + dataFromREST.getString("context_id"));

        JSONObject data = flashcardContext.getJSONObject("data");

            caption = data.getString("name");
            question.setCaption(caption);
            String nameOfCorrectAnswer = dataFromREST.getString("name");
            String identifierOfCorrectAnswer = dataFromREST.getString("description");
            question.setCorrectAnswer(nameOfCorrectAnswer);
            question.setCorrectAnswerIdentifier(identifierOfCorrectAnswer);
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
                if (identifier != null){
                    if (identifier.equals(identifierOfCorrectAnswer) || isD2T){
                        try{
                            paint.setColor(Color.parseColor(color));
                        }
                        catch (IllegalArgumentException ex){
                            System.out.println("Unknown color" + color);
                            ex.printStackTrace();
                        }
                        parts.add(new PartOfBody(parser.doPath(line), paint, path.getString("term")) );
                    }
                    else{
                        color = toGrayScale(color);
                        paint.setColor(Color.parseColor(color));
                        parts.add(new PartOfBody(parser.doPath(line), paint, path.getString("term")) );
                    }
                }

                else
                {
                    color = toGrayScale(color);
                    try{
                        paint.setColor(Color.parseColor(color));
                    }
                    catch (IllegalArgumentException ex)
                    {
                        ex.printStackTrace();
                    }
                    parts.add(new PartOfBody(parser.doPath(line), paint) );
                }
                RectF boundaries = new RectF();
                parts.get(i).getPath().computeBounds(boundaries, true);
                parts.get(i).setBoundaries(boundaries);
                question.setBounds(boundaries);
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
        int r=0, g=0, b=0;
        try{
            r = Integer.valueOf( colorStr.substring( 1, 3 ), 16);
            g = Integer.valueOf( colorStr.substring( 3, 5 ), 16);
            b = Integer.valueOf( colorStr.substring( 5, 7 ), 16);
        }
        catch (NumberFormatException ex){
            System.out.println(colorStr);
            ex.printStackTrace();
            return new Integer[]{0, 0, 0};
        }
        catch (StringIndexOutOfBoundsException ex){
            System.out.println(colorStr);
            ex.printStackTrace();
        }
        Integer[] rgb = new Integer[]{r, g, b};
        return rgb;
    }

    public boolean isGray(String colorStr){
        Integer[] rgb  = HexTorgb(colorStr);
        return Math.max(Math.abs(rgb[0] - rgb[1]), Math.abs(rgb[0] - rgb[2])) < 10;
    }

    public String toGrayScale(String colorStr){
        if (colorStr == "none" || colorStr.length() < 5){
            return "#000000";
        }
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
