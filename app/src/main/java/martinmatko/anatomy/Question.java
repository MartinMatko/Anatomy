package martinmatko.anatomy;

import android.graphics.RectF;

import java.util.List;

/**
 * Created by Martin on 22.11.2015.
 */
public class Question {
    private List<PartOfBody> bodyParts;
    private String caption;
    private String text;
    private String correctAnswer;
     float left = Float.MAX_VALUE;
     float top = Float.MAX_VALUE;
     float right = 0;
     float bottom = 0;

    public float getScaleFactorOfPicture() {
        return scaleFactorOfPicture;
    }

    public void setScaleFactorOfPicture(float scaleFactorOfPicture) {
        this.scaleFactorOfPicture = scaleFactorOfPicture;
    }

    private float scaleFactorOfPicture;

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

    public void setBounds(RectF rectF){
        if(rectF.right > right)
            right = rectF.right;
        if(rectF.left < left)
            left = rectF.left;
        if(rectF.bottom > bottom)
            bottom = rectF.bottom;
        if(rectF.top < top)
            top = rectF.top;
    }

    public float computeScaleFactorOfPicture(float width,float height){
        float xFactor = width/(right - left);
        float yFactor = height/(bottom - top);
        if (xFactor < yFactor)
            return xFactor;
        else
            return yFactor;
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
