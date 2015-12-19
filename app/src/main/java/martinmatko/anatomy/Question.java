package martinmatko.anatomy;

import java.util.List;

/**
 * Created by Martin on 22.11.2015.
 */
public class Question {
    List<PartOfBody> bodyParts;
    String caption;
    String text;

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    String correctAnswer;
    List<Term> options;

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

}
