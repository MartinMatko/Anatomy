package martinmatko.Anatom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import utils.Constants;

/**
 * Created by Martin on 17.1.2016.
 */
public class Test {

    private JSONParser parser = new JSONParser();
    private HTTPService service = new HTTPService();
    private List<Question> questions = new ArrayList<>();
    private boolean isPOSTCompleted = true;
    private String categories;

    public JSONParser getParser() {
        return parser;
    }

    public void setParser(JSONParser parser) {
        this.parser = parser;
    }

    public HTTPService getService() {
        return service;
    }

    public void setService(HTTPService service) {
        this.service = service;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public boolean isPOSTCompleted() {
        return isPOSTCompleted;
    }

    public void setIsPOSTCompleted(boolean isPOSTCompleted) {
        this.isPOSTCompleted = isPOSTCompleted;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public boolean start(String categories) {
        return getFirstQuestion(categories);

    }

    public boolean getFirstQuestion(String categories) {
        JSONObject context;
        if (categories.isEmpty()) {
            context = service.get(Constants.SERVER_NAME + "models/practice/?avoid=[]&filter=[[\"category/images\"]]&contexts=[]&limit=2&types=[]&without_contexts=1");

        } else {

            String url = Constants.SERVER_NAME + "models/practice/?avoid=[]&filter=[" + categories + ",[\"category/images\"]]&contexts=[]&limit=2&types=[]&without_contexts=1";
            context = service.get(url);
        }
        JSONObject flashcard;
        try {
            JSONObject context1 = context.getJSONArray("data").getJSONObject(0);
            flashcard = service.get(Constants.SERVER_NAME + "flashcards/context/" + context1.getJSONObject("payload").getString("context_id"));
            Question firstQuestion = parser.getQuestion(context1, flashcard);
            questions.add(firstQuestion);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            context = context.getJSONArray("data").getJSONObject(1);
            flashcard = service.get(Constants.SERVER_NAME + "flashcards/context/" + context.getJSONObject("payload").getString("context_id"));
            Question secondQuestion = parser.getQuestion(context, flashcard);
            questions.add(secondQuestion);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    public String convertCategoriesToUrl(ArrayList<String> systemCategories, ArrayList<String> bodyCategories) {
        if (systemCategories.isEmpty() && bodyCategories.isEmpty()) {
            return "";
        } else {
            StringBuilder systemCategoriesTags = new StringBuilder();
            for (String tag : systemCategories) {
                systemCategoriesTags.append("\"").append("category/").append(tag).append("\",");
            }
            StringBuilder bodyCategoriesTags = new StringBuilder();
            for (String tag : bodyCategories) {
                bodyCategoriesTags.append("\"").append("category/").append(tag).append("\",");
            }
            String url = "";
            String tags = "";
            if (systemCategoriesTags.length() > 0) {
                systemCategoriesTags.deleteCharAt(systemCategoriesTags.length() - 1);
                tags = "[" + systemCategoriesTags.toString() + "]";
            }

            if (bodyCategoriesTags.length() > 0 && systemCategoriesTags.length() > 0) {

                bodyCategoriesTags.deleteCharAt(bodyCategoriesTags.length() - 1);
                tags = tags + ",[" + bodyCategoriesTags.toString() + "]";
            } else if (systemCategoriesTags.length() == 0) {

                bodyCategoriesTags.deleteCharAt(bodyCategoriesTags.length() - 1);
                tags = tags + "[" + bodyCategoriesTags.toString() + "]";
            }
            try {
                url = URLEncoder.encode(tags, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return url;
        }
    }


    public String postAnswer(Question question, long timeOfAnswer) {
        JSONObject response = new JSONObject();
        String direction = question.isT2D() ? "t2d" : "d2t";

        JSONObject answer = new JSONObject();
        JSONArray answers = new JSONArray();
        try {
            answer.put("flashcard_id", Integer.parseInt(question.getCorrectAnswer().getId()));
            if (!question.getAnswer().getIdentifier().isEmpty()) {
                answer.put("flashcard_answered_id", Integer.parseInt(question.getAnswer().getId()));
                JSONArray optionIds = new JSONArray();
                for (Term option : question.getOptions()) {
                    if (!option.getId().equals(question.getCorrectAnswer().getId())){
                        optionIds.put(Integer.parseInt(option.getId()));
                    }
                }
                if (optionIds.length() != 0) {
                    answer.put("option_ids", optionIds);
                }
            }
            answer.put("answer_class", "flashcard_answer");
            answer.put("response_time", timeOfAnswer);
            answer.put("question_type", direction);
            JSONObject metadata = new JSONObject();
            metadata.put("client", "android");
            if (question.isRandomWithoutOptions()) {
                metadata.put("test", "random_without_options");
            }
            answer.put("meta", metadata);
            answers.put(answer);
            response.put("answers", answers);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        System.out.println(response.toString());
        return response.toString();
    }
}
