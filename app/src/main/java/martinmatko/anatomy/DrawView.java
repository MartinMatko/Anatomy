package martinmatko.anatomy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.media.Image;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class DrawView extends View {

    static final String TAG = "DrawView";
    //These two constants specify the minimum and maximum zoom
    private static float MIN_ZOOM = -5f;
    private static float MAX_ZOOM = 5f;
    private final int FRAME_RATE = 30;
    public float totalScaleFactor = 1.f;
    public List<PartOfBody> selectedParts = new ArrayList<>();
    Context ctx;
    Question question = new Question();
    Matrix matrix;
    Matrix oldMatrix = new Matrix();
    boolean isHighlighted = false;
    Mode mode = Mode.INITIAL;
    private float pointOfZoomX = 0;
    private float pointOfZoomY = 0;
    private boolean isD2T = true;
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
    //These two variables keep track of the amount we translated the X and Y coordinates, the last time we
    //panned.
    private float previousTranslateX = 0f;
    private float previousTranslateY = 0f;
    private float x = 0f;
    private float y = 0f;
    private Handler h = new Handler();
    private Runnable r = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

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
        setFocusable(true);
        setFocusableInTouchMode(true);
        scaleFactor = question.computeScaleFactorOfPicture(this.getWidth(), this.getHeight());
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;

        if (isHighlighted) {
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
            minValue = minValue < 2 ? minValue : 2;

            if (totalScaleFactor < minValue) {
                h.postDelayed(r, FRAME_RATE);
            }
            totalScaleFactor = 1f;
        }
        drawBodyParts();
        if (mode.equals(Mode.CONFIRM)) {
            drawButtons();
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
                    scaleFactor = question.computeScaleFactorOfPicture(this.getWidth(), this.getHeight());
                    matrix.setTranslate(this.getWidth() / 2 - x1, this.getHeight() / 2 - y1);
                    matrix.postScale(scaleFactor, scaleFactor, this.getWidth() / 2, this.getHeight() / 2);
                    scaleFactor = 1.f;
                    //mode = Mode.NOACTION;
                    if (isHighlighted) {
                        matrix.setScale(zoomScaleFactor, zoomScaleFactor, pointOfZoomX, pointOfZoomY);
                        totalScaleFactor += (zoomScaleFactor - 1);
                    }
                    break;
                case NOACTION:
//                    scaleFactor = 1.f;
//                    pointOfZoomX = 0;
//                    pointOfZoomY = 0;
                    break;
                case CONFIRM:
                    //centering area of elected items
                    matrix.setTranslate(this.getWidth() / 2 - pointOfZoomX, this.getHeight() / 2 - pointOfZoomY);
                    matrix.postScale(scaleFactor, scaleFactor, this.getWidth() / 2, this.getHeight() / 2);
                    break;
                case DRAG:
                    //matrix.postTranslate(translateX/2, translateY/2);
                    canvas.translate(translateX, translateY);
                    break;
                case SELECT:
                    break;
                case TAPTOZOOM:
                    matrix.setScale(scaleFactor, scaleFactor, pointOfZoomX, pointOfZoomY);
                    if (question.isD2T()) {
                        mode = Mode.SELECT;
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
            }
            RectF originalBorders = new RectF(question.borders);
            matrix.mapRect(originalBorders);
            float displayHeight = this.getHeight()/5;
            float displayWidth = this.getWidth()/5;
            RectF bordersOfPicture = originalBorders;
            if (mode != Mode.DRAG && bordersOfPicture.bottom > displayHeight && bordersOfPicture.right > displayWidth && bordersOfPicture.left < displayWidth*4 && bordersOfPicture.top < displayHeight*4){
                question.borders = originalBorders;
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
            else {
                for (PartOfBody partOfBody : question.getBodyParts()) {
                    canvas.drawPath(partOfBody.getPath(), partOfBody.getPaint());
                }
            }
            if (question.getOptions().size() == 0 && mode.equals(Mode.CONFIRM)) {
                for (PartOfBody partOfBody : selectedParts) {
                    if (partOfBody.getOriginalPaint() != null) {
                        canvas.drawPath(partOfBody.getPath(), partOfBody.getOriginalPaint());
                    }
                }
            }
            //used for debugging - showing path boundaries
//            for (PartOfBody partOfBody : question.getBodyParts()) {
//                if (partOfBody.getIdentifier() != null) {
//                    if (partOfBody.getIdentifier().equals(question.getCorrectAnswer().getIdentifier()) || isD2T) {
//                        Paint p = new Paint();
//                        p.setStyle(Paint.Style.STROKE);
//                        p.setStrokeWidth(1);
//                        canvas.drawRect(partOfBody.getBoundaries(), p);
//                    }
//                }
//            }
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
                MainActivity host = (MainActivity) getContext();
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                translateX = event.getX() - getX() + startX;
                translateY = event.getY() - getY() + startY;
                if (Math.abs(translateX) + Math.abs(translateY) > 10 && mode != Mode.PINCHTOZOOM)
            {
                mode = Mode.DRAG;
                canvas.translate(translateX, translateY);
                invalidate();
                System.out.println("ooo");
                invalidate();
            }
                else if (mode.equals(Mode.PINCHTOZOOM) ){
                    canvas.translate(translateX, translateY);
                    //canvas.scale(.5f, .5f);
                    System.out.println("zooooooooooooooooooom");
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

        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mode == Mode.INITIAL) {
                showButtons();
                if (question.isD2T()) {
                    if (selectedParts.size() != 1) {
                        scaleFactor = 2;
                        totalScaleFactor += scaleFactor-1;
                        mode = Mode.SELECT;
                    } else mode = Mode.FINISH;
                } else {
                    mode = Mode.TAPTOZOOM;
                }
            }
            if (mode == Mode.NOACTION) {
                //mode = Mode.TAPTOZOOM;
            }
            if (mode == Mode.DRAG) {
                mode = Mode.NOACTION;
                }
            x = event.getX();
            y = event.getY();
            pointOfZoomX = x;
            pointOfZoomY = y;
            switch (mode) {
                case PINCHTOZOOM: {
                    if (question.isD2T()) {
                        //mode = Mode.SELECT;
                    }
                    break;
                }
                case TAPTOZOOM: {
                    scaleFactor = 2;
                    totalScaleFactor += scaleFactor;
                    break;
                }
                case SELECT: {
                    showButtons();
                    if (selectedParts.size() != 1) {
//                        scaleFactor = 2;
//                        totalScaleFactor += scaleFactor;
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
        return true;
    }

    public void setColorOfWrongAnswer() {
        MainActivity host = (MainActivity) getContext();

        if (selectedParts.size() == 1) {
            String identifierOfAnswer = selectedParts.get(0).getIdentifier();
            for (Term option : question.getOptions()) {
                if (option.getIdentifier().equals(identifierOfAnswer)) {
                    question.setAnswer(option);
                }
            }

            host.setAnswersInOptions(identifierOfAnswer);
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
                        paint.setColor(getResources().getColor(R.color.wrongAnswer));
                        question.setAnswer(option);
                        host.setAnswersInOptions(option.getIdentifier());
                    } else {
//                        int color = paint.getColor();
//                        String colorString = new JSONParser().toGrayScale(String.format("#%06X" + "\n", 0xFFFFFF & color));
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
                        paint.setColor(getResources().getColor(R.color.rightAnswer));
                    }
                }
            }
            if (question.getAnswer() == null) {
                question.setAnswer(question.getCorrectAnswer());
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
        isD2T = true;
        canvas = new Canvas();
        scaleFactor = 1.f;
        zoomScaleFactor = 1.f;
        startX = 0f;
        startY = 0f;
        translateX = 0f;
        translateY = 0f;
        previousTranslateX = 0f;
        previousTranslateY = 0f;
        x = 0f;
        y = 0f;
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
            if (question.getOptions().size() == 0 && partOfBody.getOriginalPaint() != null) {
                p = partOfBody.getOriginalPaint();
            } else {
                p = partOfBody.getPaint();
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
}