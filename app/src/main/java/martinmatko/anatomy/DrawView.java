package martinmatko.anatomy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class DrawView extends View {

    static final String TAG = "DrawView";
    //These two constants specify the minimum and maximum zoom
    private static float MIN_ZOOM = -5f;
    private static float MAX_ZOOM = 5f;
    Context ctx;
    Question question = new Question();
    Matrix matrix;
    boolean isHighlighted = false;
    Mode mode = Mode.INITIAL;
    private float pointOfZoomX = 0;
    private float pointOfZoomY = 0;
    private boolean isD2T = true;
    private Canvas canvas = new Canvas();
    private float scaleFactor = 1.f;
    private float totalScaleFactor = 1.f;
    private ScaleGestureDetector detector;
    //These two variables keep track of the X and Y coordinate of the finger when it first
    //touches the screen
    private float startX = 0f;
    private float startY = 0f;
    //These two variables keep track of the amount we need to translate the canvas along the X
    //and the Y coordinate
    private float translateX = 0f;
    private float translateY = 0f;
    //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
    //panned.
    private float previousTranslateX = 0f;
    private float previousTranslateY = 0f;
    private float x = 0f;
    private float y = 0f;
    private List<PartOfBody> selectedParts = new ArrayList<>();

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


    private void init(Context context) {
        ctx = context;

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        DisplayMetrics metrics = new DisplayMetrics();

        display.getMetrics(metrics);

        detector = new ScaleGestureDetector(context, new ScaleListener());

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        this.canvas = canvas;
        drawBodyParts();
        if (mode.equals(Mode.CONFIRM)) {
            drawButtons();
        }
        if (isHighlighted){
            RectF bordersOfSelectedArea = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, 0, 0);
            for (PartOfBody partOfBody : question.getBodyParts()){
                if (partOfBody.getIdentifier() != null && partOfBody.getIdentifier().equals(question.getCorrectAnswer().getIdentifier())){
                    question.setBounds(partOfBody.getBoundaries(), bordersOfSelectedArea);
                }
            }
            float xCenter = (bordersOfSelectedArea.right + bordersOfSelectedArea.left)/2;
            float yCenter = (bordersOfSelectedArea.top + bordersOfSelectedArea.bottom)/2;
            float width = (bordersOfSelectedArea.right - bordersOfSelectedArea.left)/2;
            float height = (bordersOfSelectedArea.bottom - bordersOfSelectedArea.top)/2;
            float radius = width > height ? width : height;
            Paint p = new Paint();
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2);
            canvas.drawCircle(xCenter, yCenter, radius, p);
        }
    }

    public void drawBodyParts() {
        try {
            matrix = new Matrix();
            float x1 = (question.borders.right + question.borders.left) / 2;
            float y1 = (question.borders.bottom + question.borders.top) / 2;
            switch (mode) {
                case INITIAL:
                    //centering picture
                    matrix = new Matrix();
                    scaleFactor = question.computeScaleFactorOfPicture(this.getWidth(), this.getHeight());
                    matrix.setTranslate(this.getWidth() / 2 - x1, this.getHeight() / 2 - y1);
                    matrix.postScale(scaleFactor, scaleFactor, this.getWidth() / 2, this.getHeight() / 2);
                    scaleFactor = 1.f;
                    mode = Mode.NOACTION;
                    break;
                case NOACTION:
                    //centering picture
                    //matrix.setScale(1/totalScaleFactor, 1/totalScaleFactor, this.getWidth()/2, this.getHeight()/2);
                    //matrix.postTranslate(this.getWidth()/2 - x1, this.getHeight()/2 - y1);
                    scaleFactor = 1.f;
                    pointOfZoomX = 0;
                    pointOfZoomY = 0;
                    break;
                case CONFIRM:
                    //centering area of elected items
                    matrix.setTranslate(this.getWidth() / 2 - pointOfZoomX, this.getHeight() / 2 - pointOfZoomY);
                    break;
                case DRAG:
                    matrix.setTranslate(translateX / 5, translateY / 5);
                    mode = Mode.SELECT;
                    break;
                case SELECT:
                    break;
                case TAPTOZOOM:
                    matrix.setScale(scaleFactor, scaleFactor, pointOfZoomX, pointOfZoomY);
                    mode = Mode.SELECT;
                    break;
                case FINISH:
                    setColorOfWrongAnswer();
                    setColorOfRightAnswer();
                    matrix.setTranslate(this.getWidth() / 2 - x1, this.getHeight() / 2 - y1);
                    matrix.postScale(1 / totalScaleFactor, 1 / totalScaleFactor, this.getWidth() / 2, this.getHeight() / 2);
                    mode = Mode.NOACTION;
            }
            matrix.mapRect(question.borders);
            for (PartOfBody partOfBody : question.getBodyParts()) {
                Path path = new Path();
                partOfBody.getPath().transform(matrix, path);
                RectF boundaries = new RectF();
                path.computeBounds(boundaries, true);
                partOfBody.setBoundaries(boundaries);

                partOfBody.setPath(path);
                canvas.drawPath(path, partOfBody.getPaint());
            }
            for (PartOfBody partOfBody : question.getBodyParts()) {
                if (partOfBody.getIdentifier() != null) {
                    if (partOfBody.getIdentifier().equals(question.getCorrectAnswer().getIdentifier()) || isD2T) {
                        Paint p = new Paint();
                        p.setStyle(Paint.Style.STROKE);
                        p.setStrokeWidth(1);
                        canvas.drawRect(partOfBody.getBoundaries(), p);
                    }

                    canvas.drawPath(partOfBody.getPath(), partOfBody.getPaint());
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
                startX = event.getX() - previousTranslateX;
                startY = event.getY() - previousTranslateY;
                x = event.getX();
                y = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:

// first finger is moving on screen  
// update translation value to apply on Path
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                translateX = event.getX() - startX;
                translateY = event.getY() - startY;
                if (Math.abs(translateX) + Math.abs(translateY) > 50 && mode != Mode.PINCHTOZOOM) {
                    mode = Mode.DRAG;
                }

                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mode = Mode.PINCHTOZOOM;
                pointOfZoomX = (x + event.getX(1)) / 2;
                pointOfZoomY = (y + event.getY(1)) / 2;
                break;

            case MotionEvent.ACTION_POINTER_UP:

// No more fingers on screen
                // All fingers went up, so let's save the value of translateX and translateY into previousTranslateX and
                //previousTranslate
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                //mode = Mode.PINCHTOZOOM;
                return true;

// All touch events are sended to ScaleListener
        }
        detector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mode == Mode.INITIAL || mode == Mode.NOACTION) {
                mode = Mode.TAPTOZOOM;
            }
            x = event.getX();
            y = event.getY();
            pointOfZoomX = x;
            pointOfZoomY = y;
            switch (mode) {
                case PINCHTOZOOM: {
                    mode = Mode.SELECT;
                    break;
                }
                case TAPTOZOOM: {
                    scaleFactor = 3;
                    totalScaleFactor = scaleFactor;
                    break;
                }
                case SELECT: {
                    showButtons();
                    if (selectedParts.size() > 1) {
                        mode = Mode.CONFIRM;
                    } else {
                        mode = Mode.FINISH;
                    }
                    invalidate();
                    return true;
                }
                case CONFIRM: {
                    mode = Mode.FINISH;

                }
                invalidate();
                return true;
            }
        }
        invalidate();
        return true;
    }

    public void setColorOfWrongAnswer() {

        if (selectedParts.size() == 1) {
            String identifierOfAnswer = selectedParts.get(0).getIdentifier();
            for (Term option : question.getOptions()) {
                if (option.getIdentifier().equals(identifierOfAnswer)) {
                    question.setAnswer(option);
                }
            }
            for (PartOfBody partOfBody : question.getBodyParts()) {
                Paint paint = partOfBody.getPaint();
                if (identifierOfAnswer.equals(partOfBody.getIdentifier())) {
                    paint.setColor(Color.RED);
                } else {
                    int color = paint.getColor();
                    JSONParser parser = new JSONParser();
                    String colorString = parser.toGrayScale(String.format("#%06X", 0xFFFFFF & color));
                    try {
                        paint.setColor(Color.parseColor(colorString));
                    } catch (Exception ex) {
                        System.out.println("Zla farba: " + colorString);
                    }
                    partOfBody.setPaint(paint);
                }
            }
            return;
        }

        for (Term option : question.getOptions()) {
            //oznacena odpoved
            RectF button = option.getButton();
            if (button != null && button.contains(x, y)) {
                for (PartOfBody partOfBody : question.getBodyParts()) {
                    Paint paint = partOfBody.getPaint();
                    if (option.getIdentifier().equals(partOfBody.getIdentifier())) {
                        paint.setColor(Color.RED);
                        question.setAnswer(option);
                    } else {
//                        int color = paint.getColor();
//                        String colorString = parser.toGrayScale(String.format("#%06X" + "\n", 0xFFFFFF & color));
//                        paint.setColor(Color.parseColor(colorString));
//                        partOfBody.setPaint(paint);
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
                        paint.setColor(Color.GREEN);
                    }
                }
            }
            if (question.getAnswer() == null) {
                question.setAnswer(question.getCorrectAnswer());
            }
        }
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
                    if (selectedParts.get(i).getIdentifier().equals(partOfBody.getIdentifier())) {
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

    public void drawButtons() {

        float angle = 0f;
        for (PartOfBody partOfBody : selectedParts) {
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
            p = partOfBody.getPaint();
            p.setStyle(Paint.Style.FILL);
            RectF button = new RectF();
            button.bottom = yCenter + radius;
            button.top = yCenter - radius;
            button.left = xCenter - radius;
            button.right = xCenter + radius;
            canvas.drawCircle(xCenter, yCenter, radius, p);
            List<Term> options = question.getOptions();
            for (Term term : options) {
                if (term.getIdentifier().equals(partOfBody.getIdentifier())) {
                    term.setButton(button);
                }
            }
            angle += (2 * Math.PI) / selectedParts.size();
        }
    }

    public enum Mode {
        INITIAL, DRAG, PINCHTOZOOM, TAPTOZOOM, SELECT, CONFIRM, NOACTION, FINISH
    }


    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor = detector.getScaleFactor();
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));

            invalidate();
            return true;
        }
    }
}