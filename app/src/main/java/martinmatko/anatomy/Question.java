package martinmatko.anatomy;

import android.graphics.RectF;

import java.util.List;

/**
 * Created by Martin on 22.11.2015.
 */
public class Question {

    RectF borders = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, 0, 0);
    private List<PartOfBody> bodyParts;
    private String caption;
    private String text;
    private float scaleFactorOfPicture;
    private Term correctAnswer;
    private Term answer;
    private List<Term> options;
    private List<Term> terms;
    private boolean isT2D;
    private boolean isRandomWithoutOptions = false;
    private String flashcardId;
    public Question() {
    }

    public List<Term> getTerms() {
        return terms;
    }

    public void setTerms(List<Term> terms) {
        this.terms = terms;
    }

    public boolean isRandomWithoutOptions() {
        return isRandomWithoutOptions;
    }

    public void setIsRandomWithoutOptions(boolean isRandomWithoutOptions) {
        this.isRandomWithoutOptions = isRandomWithoutOptions;
    }

    public String getFlashcardId() {
        return flashcardId;
    }

    public void setFlashcardId(String flashcardId) {
        this.flashcardId = flashcardId;
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

    public boolean isT2D() {
        return isT2D;
    }

    public void setT2D(boolean isD2T) {
        this.isT2D = isD2T;
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

    public void setBounds(RectF rectF, RectF borders) {

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
        if (options.size() == 0 && isT2D && identifier != null) { // for free choice questions
            return true;
        }
        return false;
    }
}
