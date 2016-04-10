package martinmatko.anatomy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import utils.Constants;

public class DrawView extends View {
    private final int FRAME_RATE = 30;
    private Question question = new Question();
    private boolean isHighlighted = false;
    private float totalScaleFactor = 1.f;
    private List<PartOfBody> selectedParts = new ArrayList<>();
    private Matrix matrix;
    private boolean isHighlightFinished = false;
    private Mode mode = Mode.INITIAL;
    private float pointOfZoomX = 0;
    private float pointOfZoomY = 0;
    private Canvas canvas = new Canvas();
    private float scaleFactor = 1.f;
    private float zoomScaleFactor = 1.f;
    private ScaleGestureDetector detector;
    //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private float startX = 0f;
    private float startY = 0f;
    //These two variables keep track of the amount we need to translate the canvas along the X
    //and the Y coordinate
    private float translateX = 0f;
    private float translateY = 0f;
    private float x = 0f;
    private float y = 0f;
    private Handler h = new Handler();
    private Runnable r = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };
    private boolean isZoomed = false;

    public DrawView(Context context) {
        super(context);
        init(context);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setIsHighlighted(boolean isHighlighted) {
        this.isHighlighted = isHighlighted;
    }

    public List<PartOfBody> getSelectedParts() {
        return selectedParts;
    }

    private void init(Context context) {
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;

        if (isHighlighted) {
            isZoomed = true;
            RectF bordersOfSelectedArea = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, 0, 0);
            for (PartOfBody partOfBody : question.getBodyParts()) {
                if (partOfBody.getIdentifier() != null && partOfBody.getIdentifier().equals(question.getCorrectAnswer().getIdentifier())) {
                    question.setBounds(partOfBody.getBoundaries(), bordersOfSelectedArea);
                }
            }
            pointOfZoomX = (bordersOfSelectedArea.right + bordersOfSelectedArea.left) / 2;
            pointOfZoomY = (bordersOfSelectedArea.top + bordersOfSelectedArea.bottom) / 2;
            zoomScaleFactor = 1.1f;
            float heightScale = this.getWidth() / (bordersOfSelectedArea.right - bordersOfSelectedArea.left);
            float widthScale = this.getHeight() / (bordersOfSelectedArea.bottom - bordersOfSelectedArea.top);
            float minValue = heightScale < widthScale ? heightScale : widthScale;
            minValue = minValue < 2 ? minValue : 2f;

            if (totalScaleFactor < minValue) {
                h.postDelayed(r, FRAME_RATE);
            }
        }
        drawBodyParts();
        if (mode.equals(Mode.CONFIRM)) {
            drawButtons();
        }
    }

    public void drawBodyParts() {
        try {
            matrix = new Matrix();
            float x1 = (question.getBorders().right + question.getBorders().left) / 2;
            float y1 = (question.getBorders().bottom + question.getBorders().top) / 2;
            switch (mode) {
                case INITIAL:
                    //centering picture
                    scaleFactor = question.computeScaleFactorOfPicture(this.getWidth(), this.getHeight());
                    matrix.setTranslate(this.getWidth() / 2 - x1, this.getHeight() / 2 - y1);
                    matrix.postScale(scaleFactor, scaleFactor, this.getWidth() / 2, this.getHeight() / 2);
                    scaleFactor = 1.f;
                    if (isHighlighted && !isHighlightFinished) {
                        matrix.setScale(zoomScaleFactor, zoomScaleFactor, pointOfZoomX, pointOfZoomY);
                        totalScaleFactor += (zoomScaleFactor - 1);
                    }
                    break;
                case CONFIRM:
                    //centering area of selected items
                    if (!isZoomed) {
                        matrix.setTranslate(this.getWidth() / 2 - pointOfZoomX, this.getHeight() / 2 - pointOfZoomY);
                        matrix.postScale(scaleFactor, scaleFactor, this.getWidth() / 2, this.getHeight() / 2);
                        isZoomed = true;
                    }
                    break;
                case DRAG:
                    canvas.translate(translateX, translateY);
                    for (PartOfBody partOfBody : question.getBodyParts()) {
                        canvas.drawPath(partOfBody.getPath(), partOfBody.getPaint());
                    }
                    break;
                case DRAGFINISHED:
                    matrix.setTranslate(translateX, translateY);
                    if (question.isT2D()) {
                        mode = Mode.NOACTION;
                    } else {
                        mode = Mode.NOACTION;
                    }
                    break;
                case FINISH:
                    setColorOfWrongAnswer();
                    setColorOfRightAnswer();
                    scaleFactor = question.computeScaleFactorOfPicture(this.getWidth(), this.getHeight());
                    matrix.setTranslate(this.getWidth() / 2 - x1, this.getHeight() / 2 - y1);
                    matrix.postScale(scaleFactor, scaleFactor, this.getWidth() / 2, this.getHeight() / 2);
                    scaleFactor = 1.f;
                    mode = Mode.NOACTION;
                    if (question.getOptions().size() == 0) {
                        isZoomed = true;
                    }
            }
            matrix.mapRect(question.getBorders());
            if (mode != Mode.DRAG) {
                for (PartOfBody partOfBody : question.getBodyParts()) {
                    Path path = new Path();
                    partOfBody.getPath().transform(matrix, path);
                    RectF boundaries = new RectF();
                    path.computeBounds(boundaries, true);
                    partOfBody.setBoundaries(boundaries);
                    partOfBody.setPath(path);
                    canvas.drawPath(path, partOfBody.getPaint());
                }
            }
            if (question.getOptions().size() == 0 && mode.equals(Mode.CONFIRM)) {
                for (int i = 0; i < selectedParts.size(); i++) {
                    int color = Color.parseColor(Constants.COLORS.get(i));
                    Paint paint = new Paint(selectedParts.get(i).getPaint());
                    paint.setColor(color);
                    canvas.drawPath(selectedParts.get(i).getPath(), paint);
                }
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:

// First finger is on screen

                //We assign the current X and Y coordinate of the finger to startX and startY minus the previously translated
                //amount for each coordinates This works even when we are translating the first time because the initial
                //values for these two variables is zero.
                startX = getX() - event.getX();

                startY = getY() - event.getY();

                x = event.getX();
                y = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:

// first finger is moving on screen  
// update translation value to apply on Path
                translateX = event.getX() - getX() + startX;
                translateY = event.getY() - getY() + startY;
                if (isZoomed && (Math.abs(translateX) + Math.abs(translateY) > 10)) {
                    mode = Mode.DRAG;
                    invalidate();
                } else {
                    return false;
                }
                break;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mode == Mode.INITIAL || (mode == Mode.NOACTION && question.getAnswer() == null)) {
                showButtons();
                if (question.isT2D()) {
                    if (selectedParts.size() > 1) {
                        scaleFactor = 2;
                        totalScaleFactor += scaleFactor - 1;
                        mode = Mode.SELECT;
                    } else if (selectedParts.size() == 1) {
                        mode = Mode.FINISH;
                    } else {
                        mode = Mode.INITIAL;
                    }
                }
                invalidate();
            }
            if (mode == Mode.DRAG) {
                mode = Mode.DRAGFINISHED;
                invalidate();
            }
            x = event.getX();
            y = event.getY();
            pointOfZoomX = x;
            pointOfZoomY = y;
            switch (mode) {
                case SELECT: {
                    showButtons();
                    if (selectedParts.size() > 1) {
                        mode = Mode.CONFIRM;
                    } else if (selectedParts.size() == 1) {
                        mode = Mode.FINISH;
                    } else {
                        mode = Mode.INITIAL;
                    }
                    invalidate();
                    return true;
                }
                case CONFIRM: {
                    if (isButtonPressed()) {
                        mode = Mode.FINISH;
                    } else {
                        mode = Mode.SELECT;
                    }
                }
                invalidate();
                return true;
            }
        }
        return true;
    }

    public void setColorOfWrongAnswer() {
        MainActivity host = (MainActivity) getContext();

        if (selectedParts.size() == 1) {
            String identifierOfAnswer = selectedParts.get(0).getIdentifier();
            if (question.getAnswer() == null) {
                host.setAnswersInOptions(identifierOfAnswer);
            }
            for (PartOfBody partOfBody : question.getBodyParts()) {
                Paint paint = partOfBody.getPaint();
                if (identifierOfAnswer.equals(partOfBody.getIdentifier())) {
                    paint.setColor(getResources().getColor(R.color.wrongAnswer));
                } else {
                    int color = paint.getColor();
                    JSONParser parser = new JSONParser();
                    String colorString = parser.toGrayScale(String.format("#%06X", 0xFFFFFF & color));
                    try {
                        paint.setColor(Color.parseColor(colorString));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        //System.out.println(colorString);
                    }
                    partOfBody.setPaint(paint);
                }
            }
            return;
        }

        for (Term option : question.getOptions()) {
            RectF button = option.getButton();
            if (button != null && button.contains(x, y)) {
                for (PartOfBody partOfBody : question.getBodyParts()) {
                    Paint paint = partOfBody.getPaint();
                    if (option.getIdentifier().equals(partOfBody.getIdentifier())) {
                        paint.setColor(getResources().getColor(R.color.wrongAnswer));

                        if (question.getAnswer() == null) {
                            host.setAnswersInOptions(option.getIdentifier());
                        }
                    }
                }
            }
        }
        for (Term option : question.getTerms()) {
            RectF button = option.getButton();
            if (button != null && button.contains(x, y)) {
                for (PartOfBody partOfBody : question.getBodyParts()) {
                    Paint paint = partOfBody.getPaint();
                    if (option.getIdentifier().equals(partOfBody.getIdentifier())) {
                        paint.setColor(getResources().getColor(R.color.wrongAnswer));
                        if (question.getAnswer() == null) {
                            host.setAnswersInOptions(option.getIdentifier());
                        }
                    }
                }
            }
        }
    }

    public void setColorOfRightAnswer() {
        for (Term option : question.getOptions()) {
            if (option.getIdentifier().equals(question.getCorrectAnswer().getIdentifier())) {
                for (PartOfBody partOfBody : question.getBodyParts()) {
                    if (partOfBody.getIdentifier() != null && partOfBody.getIdentifier().equals(option.getIdentifier())) {
                        Paint paint = partOfBody.getPaint();
                        paint.setColor(getResources().getColor(R.color.rightAnswer));
                    }
                }
            }
        }
        if (question.getOptions().size() == 0) {
            for (PartOfBody partOfBody : question.getBodyParts()) {
                if (partOfBody.getIdentifier() != null && partOfBody.getIdentifier().equals(question.getCorrectAnswer().getIdentifier())) {
                    Paint paint = partOfBody.getPaint();
                    paint.setColor(getResources().getColor(R.color.rightAnswer));
                }
            }
        }
    }

    private boolean isButtonPressed() {
        for (Term option : question.getTerms()) {
            RectF button = option.getButton();
            if (button != null && button.contains(x, y)) {
                return true;
            }
        }
        for (Term option : question.getOptions()) {
            RectF button = option.getButton();
            if (button != null && button.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    public void showButtons() {
        selectedParts.clear();
        for (PartOfBody partOfBody : question.getBodyParts()) {
            Region region = new Region();
            if (partOfBody.getBoundaries() != null) {
                RectF rectF = partOfBody.getBoundaries();
                region.setPath(partOfBody.getPath(), new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
                boolean isTerm = (question.containsIdentifier(partOfBody.getIdentifier()));
                boolean isNotInButtons = true;
                for (int i = 0; i < selectedParts.size(); i++) {
                    if (selectedParts.get(i).getIdentifier() != null &&
                            selectedParts.get(i).getIdentifier().equals(partOfBody.getIdentifier())) {
                        isNotInButtons = false;
                    }
                }
                if (rectF.contains(Math.abs(x), Math.abs(y)) && isTerm && isNotInButtons) {
                    selectedParts.add(partOfBody);
                    partOfBody.setSelected(true);
                }
            }

        }
    }

    public void clearVariables() {
        totalScaleFactor = 1.f;
        selectedParts = new ArrayList<>();
        question = new Question();
        isHighlighted = false;
        mode = Mode.INITIAL;
        pointOfZoomX = 0;
        pointOfZoomY = 0;
        canvas = new Canvas();
        scaleFactor = 1.f;
        zoomScaleFactor = 1.f;
        startX = 0f;
        startY = 0f;
        translateX = 0f;
        translateY = 0f;
        x = 0f;
        y = 0f;
        isZoomed = false;
    }

    public void drawButtons() {

        float angle = 0f;
        for (int i = 0; i < selectedParts.size(); i++) {
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            float radiusOfButtons = Math.min(this.getHeight(), this.getWidth());
            float radius = radiusOfButtons / 10;
            float xCenter = radiusOfButtons / 2;
            float yCenter = radiusOfButtons / 2;
            xCenter += Math.cos(angle) * xCenter / 1.5;
            yCenter += Math.sin(angle) * yCenter / 1.5;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            canvas.drawCircle(xCenter, yCenter, radius, p);
            if (question.getOptions().size() == 0) {
                int color = Color.parseColor(Constants.COLORS.get(i));
                Paint paint = new Paint(selectedParts.get(i).getPaint());
                paint.setColor(color);
                p = paint;
            } else {
                p = selectedParts.get(i).getPaint();
            }
            p.setStyle(Paint.Style.FILL);
            RectF button = new RectF();
            button.bottom = yCenter + radius;
            button.top = yCenter - radius;
            button.left = xCenter - radius;
            button.right = xCenter + radius;
            canvas.drawCircle(xCenter, yCenter, radius, p);
            List<Term> options = question.getOptions();
            for (Term term : options) {
                if (term.getIdentifier().equals(selectedParts.get(i).getIdentifier())) {
                    term.setButton(button);
                }
            }
            if (options.size() == 0) {
                for (Term term : question.getTerms()) {
                    if (term.getIdentifier().equals(selectedParts.get(i).getIdentifier())) {
                        term.setButton(button);
                    }
                }
            }
            angle += (2 * Math.PI) / selectedParts.size();
        }
    }

    public enum Mode {
        INITIAL, DRAG, SELECT, CONFIRM, NOACTION, FINISH, DRAGFINISHED
    }
}