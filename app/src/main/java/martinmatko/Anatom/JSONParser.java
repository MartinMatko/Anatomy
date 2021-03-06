package martinmatko.Anatom;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import utils.Constants;
import utils.SVGParser;

/**
 * Created by Martin on 7.11.2015.
 */
public class JSONParser {


    public Question getQuestion(JSONObject context, JSONObject flashcard) {
        List<Term> optionsTerms = new ArrayList<>();
        List<Term> terms = new ArrayList<>();
        Question question = new Question();
        String content;
        JSONArray paths;
        String caption;
        List<PartOfBody> parts = new ArrayList<>();
        String color = "";
        try {
            String directionOfQuestion = context.getString("question_type");
            context = context.getJSONObject("payload");
            String nameOfCorrectAnswer = context.getJSONObject("term").getString("name").split(";")[0];
            String identifierOfCorrectAnswer = context.getJSONObject("term").getString("identifier");
            String idOfCorrectAnswer = context.getString("id");
            Term correctAnswer = new Term(nameOfCorrectAnswer, identifierOfCorrectAnswer, idOfCorrectAnswer);
            question.setCorrectAnswer(correctAnswer);
            question.setFlashcardId(context.getString("id"));
            question.setItemId(context.getString("item_id"));
            JSONArray options = context.getJSONArray("options");
            if (context.has("practice_meta")) {
                JSONObject meta = context.getJSONObject("practice_meta");
                if (meta.has("test")) {
                    question.setIsRandomWithoutOptions(meta.getString("test").equals("random_without_options"));
                }
            }
            if (options.length() > 0) {
                question.setT2D(directionOfQuestion.equals("t2d"));
            } else {
                question.setT2D(true);
            }
            JSONObject data = flashcard.getJSONObject("data");
            content = data.getString("content");
            JSONObject JSONContent = new JSONObject(content);
            caption = data.getString("name");
            question.setCaption(caption);
            paths = JSONContent.getJSONArray("paths");
            if (options.length() > 0) {
                for (int i = 0; i < options.length(); i++) {
                    JSONObject option = options.getJSONObject(i);
                    JSONObject termJSON = option.getJSONObject("term");
                    String name = termJSON.getString("name").split(";")[0];
                    Term term = new Term(name, termJSON.getString("identifier"), option.getString("id"));
                    term.setColor(Color.parseColor(Constants.COLORS.get(i)));
                    optionsTerms.add(term);
                }
            } else {
                options = data.getJSONArray("flashcards");
                for (int i = 0; i < options.length(); i++) {
                    JSONObject option = options.getJSONObject(i);
                    JSONObject termJSON = option.getJSONObject("term");
                    String name = termJSON.getString("name").split(";")[0];
                    Term term = new Term(name, termJSON.getString("identifier"), option.getString("id"));
                    terms.add(term);
                }
            }
            question.setOptions(optionsTerms);
            question.setTerms(terms);
            // not possible to use foreach cycle
            for (int i = 0; i < paths.length(); i++) {
                JSONObject path = paths.getJSONObject(i);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                color = path.getString("color");
                if (color.length() > 1) {
                    paint.setColor(Color.parseColor(color));
                }
                String line = path.getString("d");
                if (path.has("term")) {
                    String identifier = path.getString("term");
                    if (isInT2DOptions(question, identifier)) {

                        PartOfBody partOfBody = new PartOfBody(SVGParser.doPath(line), paint, path.getString("term"));
                        for (Term option : optionsTerms) {
                            if (identifier.equals(option.getIdentifier())) {
                                option.getPartOfBodyList().add(partOfBody);
                                partOfBody.getPaint().setColor(option.getColor());
                            }
                        }
                        parts.add(partOfBody);
                    } else {
                        PartOfBody partOfBody = new PartOfBody(SVGParser.doPath(line), paint, path.getString("term"));
                        color = toGrayScale(color);
                        paint.setColor(Color.parseColor(color));
                        if (!question.isT2D() && partOfBody.getIdentifier().equals(question.getCorrectAnswer().getIdentifier())) {
                            paint.setColor(Color.parseColor(Constants.COLORS.get(1)));
                        }
                        partOfBody.setPaint(paint);
                        parts.add(partOfBody);
                    }
                } else {
                    if (!color.equals("")) {
                        color = toGrayScale(color);
                        paint.setColor(Color.parseColor(color));
                    } else {
                        paint.setColor(Color.TRANSPARENT);
                    }
                    parts.add(new PartOfBody(SVGParser.doPath(line), paint));
                }
                RectF boundaries = new RectF();
                parts.get(i).getPath().computeBounds(boundaries, true);
                parts.get(i).setBoundaries(boundaries);
                question.setBounds(boundaries, question.getBorders());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        question.setBodyParts(parts);
        return question;
    }

    public boolean isInT2DOptions(Question question, String identifier) {
        if (!question.isT2D())
            return false;
        if (question.getOptions() != null) {
            for (Term option : question.getOptions()) {
                if (option.getIdentifier().equals(identifier))
                    return true;
            }
        }

        return false;
    }

    public String rgbToHex(Integer[] rgb) {
        String ret = "#";
        for (int color : rgb) {
            ret += Integer.toHexString(color);
        }
        return ret;
    }

    public Integer[] HexTorgb(String colorStr) {
        int r = 0, g = 0, b = 0;
        try {
            r = Integer.valueOf(colorStr.substring(1, 3), 16);
            g = Integer.valueOf(colorStr.substring(3, 5), 16);
            b = Integer.valueOf(colorStr.substring(5, 7), 16);
        } catch (NumberFormatException ex) {
            return new Integer[]{0, 0, 0};
        } catch (StringIndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
        return new Integer[]{r, g, b};
    }

    public boolean isGray(String colorStr) {
        Integer[] rgb = HexTorgb(colorStr);
        return Math.max(Math.abs(rgb[0] - rgb[1]), Math.abs(rgb[0] - rgb[2])) < 10;
    }

    public String toGrayScale(String colorStr) {
        if (colorStr.equals("none") || colorStr.length() < 5) {
            return "#000000";
        }
        if (isGray(colorStr)) {
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
        if (grayAverageHex.equals("NaN")) {
            return "#000000";
        }
        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = (int) Math.floor((rgb[i] + (int) grayAverage * 8) / 9);
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
