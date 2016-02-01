package martinmatko.anatomy;

import android.graphics.RectF;

import org.apache.http.cookie.Cookie;

import java.util.List;

/**
 * Created by Martin on 22.11.2015.
 */
public class Question {

    RectF borders = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, 0, 0);;
    private List<PartOfBody> bodyParts;
    private String caption;
    private String text;
    private float scaleFactorOfPicture;
    private Term correctAnswer;
    private Term answer;
    private List<Term> options;
    private boolean isD2T;

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    List<Cookie> cookies;


    public Question() {
    }

    public Question(List<PartOfBody> bodyParts, String caption, String text, List<Term> options) {
        this.bodyParts = bodyParts;
        this.caption = caption;
        this.text = text;
        this.options = options;
    }
    public Term getAnswer() {
        return answer;
    }

    public void setAnswer(Term answer) {
        this.answer = answer;
    }

    public Term getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(Term correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public boolean isD2T() {
        return isD2T;
    }

    public void setD2T(boolean isD2T) {
        this.isD2T = isD2T;
    }
    public float getScaleFactorOfPicture() {
        return scaleFactorOfPicture;
    }

    public void setScaleFactorOfPicture(float scaleFactorOfPicture) {
        this.scaleFactorOfPicture = scaleFactorOfPicture;
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
