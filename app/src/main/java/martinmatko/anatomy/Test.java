package martinmatko.anatomy;

import org.apache.http.cookie.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Martin on 17.1.2016.
 */
public class Test {

    JSONParser parser = new JSONParser();
    HTTPService service = new HTTPService();

    public Question getNextQuestion(){
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

    public String postAnswer(String answerid, String rightAnswerid, boolean isd2t){
        JSONObject response = new JSONObject();
        String direction = isd2t ? "d2t" : "t2d";

        JSONObject answer = new JSONObject();
        JSONArray answers = new JSONArray();
        try {
            answer.put("flashcard_id", rightAnswerid);
            answer.put("flashcard_answered_id", answerid);
            answer.put("response_time", "500");
            answer.put("direction", direction);
            answer.put("option_ids", new JSONArray().put(5));
            answer.put("time", "1454154569798");
            answers.put(answer);
            response.put("answers", answers);
        }
        catch (JSONException ex){
            ex.printStackTrace();
        }
        return response.toString();
        }

}
