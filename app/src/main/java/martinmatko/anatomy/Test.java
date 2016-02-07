package martinmatko.anatomy;

import org.apache.http.cookie.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public void start() {
        questions.add(getFirstQuestion());

    }

    public Question getFirstQuestion() {
        JSONObject context = service.getContext("https://staging.anatom.cz/flashcards/practice/?avoid=[]&categories=[]&contexts=[]&limit=2&types=[]&without_contexts=1");
        JSONObject flashcard = null;
        try {
            context = context.getJSONArray("flashcards").getJSONObject(1);
            flashcard = service.getFlashcard("https://staging.anatom.cz/flashcards/context/" + context.getString("context_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parser.getQuestion(context, flashcard);
    }

    public Question getNextQuestion(String answer) {
        String newQuestion = service.post(answer);//("{\"answers\":[{\"flashcard_id\":1186,\"flashcard_answered_id\":1186,\"response_time\":1925,\"direction\":\"t2d\",\"option_ids\":[528],\"time\":1454838503018}]}");
        JSONObject context = null;
        JSONObject flashcard = null;
        try {
            context = new JSONObject(newQuestion);
            context = context.getJSONObject("data").getJSONArray("flashcards").getJSONObject(1);
            flashcard = service.getFlashcard("https://staging.anatom.cz/flashcards/context/" + context.getJSONObject("context").getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parser.getQuestion(context, flashcard);
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
