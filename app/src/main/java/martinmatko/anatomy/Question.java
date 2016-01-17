package martinmatko.anatomy;

import java.util.List;

/**
 * Created by Martin on 22.11.2015.
 */
public class Question {
    private List<PartOfBody> bodyParts;
    private String caption;
    private String text;
    private String correctAnswer;

    public String getCorrectAnswerIdentifier() {
        return correctAnswerIdentifier;
    }

    public void setCorrectAnswerIdentifier(String correctAnswerIdentifier) {
        this.correctAnswerIdentifier = correctAnswerIdentifier;
    }

    private String correctAnswerIdentifier;
    private List<Term> options;

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }


    public Question() {
    }

    public Question(List<PartOfBody> bodyParts, String caption, String text, List<Term> options) {
        this.bodyParts = bodyParts;
        this.caption = caption;
        this.text = text;
        this.options = options;
    }

    public List<PartOfBody> getBodyParts() {
        return bodyParts;
    }

    public void setBodyParts(List<PartOfBody> bodyParts) {
        this.bodyParts = bodyParts;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Term> getOptions() {
        return options;
    }

    public void setOptions(List<Term> options) {
        this.options = options;
    }

    public boolean containsIdentifier(String identifier){
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).getIdentifier().equals(identifier)){
                return true;
            }
        }
        return false;
    }
}
