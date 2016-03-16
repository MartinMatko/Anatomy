package martinmatko.anatomy;

import org.apache.http.cookie.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 17.1.2016.
 */
public class Test {

    JSONParser parser = new JSONParser();
    HTTPService service = new HTTPService();
    List<Question> questions = new ArrayList<>();
    Question question;
    List<Cookie> cookies = new ArrayList<>();
    boolean isPOSTCompleted = true;

    public void start(ArrayList systemCategories, ArrayList bodyCategories) {
        getFirstQuestion(systemCategories, bodyCategories);

    }

    public void getFirstQuestion(ArrayList<String> systemCategories, ArrayList<String> bodyCategories) {
        JSONObject context;
        if (systemCategories.isEmpty() && bodyCategories.isEmpty()) {
            context = service.get("https://staging.anatom.cz/flashcards/practice/?avoid=[]&categories=[]&contexts=[]&limit=2&types=[]&without_contexts=1");
            try {
                context = context.getJSONObject("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            StringBuilder systemCategoriesTags = new StringBuilder();
            for (String tag : systemCategories) {
                systemCategoriesTags.append("\"" + tag + "\",");
            }
            StringBuilder bodyCategoriesTags = new StringBuilder();
            for (String tag : bodyCategories) {
                bodyCategoriesTags.append("\"" + tag + "\",");
            }
            String url = "";
            String tags = "";
            if (systemCategoriesTags.length() > 0) {
                systemCategoriesTags.deleteCharAt(systemCategoriesTags.length() - 1);
                tags = "[" + systemCategoriesTags.toString() + "]";
            }

            if (bodyCategoriesTags.length() > 0) {

                bodyCategoriesTags.deleteCharAt(bodyCategoriesTags.length() - 1);
                tags = tags + ",[" + bodyCategoriesTags.toString() + "]";
            }
            try {
                url = URLEncoder.encode(tags, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            url = "https://staging.anatom.cz/flashcards/practice/?avoid=[]&categories=[" + url + "]&contexts=[]&limit=2&types=[]&without_contexts=1";
            context = service.get(url);
            try {
                context = context.getJSONObject("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject flashcard = null;
        try {
            JSONObject context1 = context.getJSONArray("flashcards").getJSONObject(0);
            flashcard = service.get("https://staging.anatom.cz/flashcards/context/" + context1.getString("context_id"));
            Question firstQuestion = parser.getQuestion(context1, flashcard);
            questions.add(firstQuestion);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            context = context.getJSONArray("flashcards").getJSONObject(1);
            flashcard = service.get("https://staging.anatom.cz/flashcards/context/" + context.getString("context_id"));
            Question secondQuestion = parser.getQuestion(context, flashcard);
            questions.add(secondQuestion);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getNextQuestion(String answer) {
        JSONObject context = null;
        JSONObject flashcard = null;
        try {
            context = service.post(answer);
            context = context.getJSONObject("data").getJSONArray("flashcards").getJSONObject(1);
            flashcard = service.get("https://staging.anatom.cz/flashcards/context/" + context.getJSONObject("context").getString("id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        questions.add(parser.getQuestion(context, flashcard));
    }


    public String postAnswer(Term answered, Term rightAnswer, boolean isd2t) {
        JSONObject response = new JSONObject();
        String direction = isd2t ? "d2t" : "t2d";

        JSONObject answer = new JSONObject();
        JSONArray answers = new JSONArray();
        try {
            answer.put("flashcard_id", Integer.parseInt(rightAnswer.getId()));
            if (answered != null) {
                answer.put("flashcard_answered_id", Integer.parseInt(answered.getId()));
            }
            answer.put("response_time", 500);
            answer.put("direction", direction);
            answer.put("option_ids", new JSONArray().put(5));
            answer.put("time", 14541545);
            answers.put(answer);
            response.put("answers", answers);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return response.toString();
    }
}
