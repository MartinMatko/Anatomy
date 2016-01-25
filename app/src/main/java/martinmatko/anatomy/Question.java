package martinmatko.anatomy;

import android.graphics.RectF;

import java.util.List;

/**
 * Created by Martin on 22.11.2015.
 */
public class Question {

    RectF borders = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, 0, 0);;
    private List<PartOfBody> bodyParts;
    private String caption;
    private String text;
    private String correctAnswer;
    private float scaleFactorOfPicture;
    private String correctAnswerIdentifier;
    private List<Term> options;

    public Question() {
    }

    public Question(List<PartOfBody> bodyParts, String caption, String text, List<Term> options) {
        this.bodyParts = bodyParts;
        this.caption = caption;
        this.text = text;
        this.options = options;
    }

    public float getScaleFactorOfPicture() {
        return scaleFactorOfPicture;
    }

    public void setScaleFactorOfPicture(float scaleFactorOfPicture) {
        this.scaleFactorOfPicture = scaleFactorOfPicture;
    }

    public String getCorrectAnswerIdentifier() {
        return correctAnswerIdentifier;
    }

    public void setCorrectAnswerIdentifier(String correctAnswerIdentifier) {
        this.correctAnswerIdentifier = correctAnswerIdentifier;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
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

    public void setBounds(RectF rectF) {

        if (rectF.right > borders.right)
            borders.right = rectF.right;
        if (rectF.left < borders.left)
            borders.left = rectF.left;
        if (rectF.bottom > borders.bottom)
            borders.bottom = rectF.bottom;
        if (rectF.top < borders.top)
            borders.top = rectF.top;
    }

    public float computeScaleFactorOfPicture(float width, float height) {
        float xFactor = width / (borders.right - borders.left);
        float yFactor = height / (borders.bottom - borders.top);
        if (xFactor < yFactor)
            return xFactor;
        else
            return yFactor;
    }

    public boolean containsIdentifier(String identifier) {
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i).getIdentifier().equals(identifier)) {
                return true;
            }
        }
        return false;
    }
}
