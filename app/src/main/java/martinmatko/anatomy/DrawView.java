package martinmatko.anatomy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static martinmatko.anatomy.DrawView.Mode.INITIAL;
import static martinmatko.anatomy.DrawView.Mode.NOACTION;

public class DrawView extends View {

    Context ctx;

    static final String TAG = "DrawView";
    private float pointOfZoomX = 0;
    private float pointOfZoomY = 0;
    private boolean isReadyToAnswer = false;

    public boolean isD2T() {
        return isD2T;
    }

    public void setD2T(boolean isD2T) {
        this.isD2T = isD2T;
    }

    private boolean isD2T = true;

    private Canvas canvas = new Canvas();
    Paint paint = new Paint();
    Question question;
    //These two constants specify the minimum and maximum zoom
    private static float MIN_ZOOM = -5f;
    private static float MAX_ZOOM = 5f;
    Matrix matrix = new Matrix();

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

    private boolean dragged = false;
    private boolean touched = false;
    private boolean selectedMode = false;
    private List<PartOfBody> selectedParts = new ArrayList<>();

    private Mode mode = INITIAL;

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
        try {
            this.question = new JSONParser().getQuestion(isD2T);
        } catch (Exception e) {
            e.printStackTrace();
        }
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


        this.canvas  = canvas;

        if (!touched && dragged){
            pointOfZoomX = translateX ;
            pointOfZoomY = translateY ;
        }

        drawBodyParts();
        if (!mode.equals(NOACTION)){
            drawButtons();
        }
    }
    public void drawBodyParts(){
        try {
            if (mode == INITIAL){
                scaleFactor = question.computeScaleFactorOfPicture(this.getWidth(), this.getHeight());
                //totalScaleFactor *= scaleFactor;
            }
            for (PartOfBody partOfBody: question.getBodyParts()){

                float x1 = (question.right+question.left)/2;
                float y1 = (question.bottom+question.top)/2;
                switch (mode){
                    case INITIAL:
                        //centering picture
                        matrix.setTranslate(this.getWidth()/2 - x1, this.getHeight()/2 - y1);
                        matrix.postScale(scaleFactor, scaleFactor, this.getWidth()/2, this.getHeight()/2);
                        scaleFactor = 1.f;
                        break;
                    case NOACTION:
                        //centering picture
                        matrix.setScale(1/totalScaleFactor, 1/totalScaleFactor, this.getWidth()/2, this.getHeight()/2);
                        //matrix.postTranslate(this.getWidth()/2 - x1, this.getHeight()/2 - y1);
                        scaleFactor = 1.f;
                        break;
                    case CONFIRM:
                        //centering area of elected items
                        scaleFactor = 1.f;
                        matrix.setTranslate(this.getWidth()/2 - pointOfZoomX, this.getHeight()/2 - pointOfZoomY);
                        //matrix.postScale(scaleFactor, scaleFactor, this.getWidth()/2, this.getHeight()/2);
                        break;
                    default:
                        matrix.setScale(scaleFactor, scaleFactor, pointOfZoomX, pointOfZoomY);
                }

                Path path = new Path();
                partOfBody.getPath().transform(matrix, path);
                RectF boundaries = new RectF();
                path.computeBounds(boundaries, true);
                partOfBody.setBoundaries(boundaries);

                partOfBody.setPath(path);
                //matrix.setTranslate(-canvasTranslateX/scaleFactor, -canvasTranslateY/scaleFactor);
                canvas.drawPath(path, partOfBody.getPaint());
            }
            for (PartOfBody partOfBody: question.getBodyParts()){
                if (partOfBody.getIdentifier()!= null)
                {
                    if (partOfBody.getIdentifier().equals(question.getCorrectAnswerIdentifier()) || isD2T){
                        Paint p = new Paint();
                        p.setStyle(Paint.Style.STROKE);
                        p.setStrokeWidth(3);
//                setBounds(partOfBody.getBoundaries());
                        //canvas.drawRect(partOfBody.getBoundaries(), p);
                    }

                    canvas.drawPath(partOfBody.getPath(), partOfBody.getPaint());
                }
            }
        }catch (NullPointerException ex){
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
                dragged = false;
                if (mode == INITIAL){
                    mode = Mode.TAPTOZOOM;
                }
                startX = event.getX() - previousTranslateX;
                startY = event.getY() - previousTranslateY;
                x = event.getX();
                y = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:

// first finger is moving on screen  
// update translation value to apply on Path

                translateX = event.getX() - startX;
                translateY = event.getY() - startY;
                mode = Mode.DRAG;

                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mode = Mode.PINCHTOZOOM;
                pointOfZoomX = (x + event.getX(1))/2;
                pointOfZoomY = (y + event.getY(1))/2;
                break;

            case MotionEvent.ACTION_POINTER_UP:

// No more fingers on screen

                // All fingers went up, so let's save the value of translateX and translateY into previousTranslateX and
                //previousTranslate
                previousTranslateX = translateX;
                previousTranslateY = translateY;
                dragged = true;
                mode = Mode.PINCHTOZOOM;
                return true;

// All touch events are sended to ScaleListener
        }
        detector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP && mode == Mode.TAPTOZOOM){
            float x = event.getX();
            float y = event.getY();
            scaleFactor = 3;
            totalScaleFactor *= scaleFactor;
            pointOfZoomX = x;
            pointOfZoomY = y;
            dragged = false;
            touched = true;
            mode = Mode.SELECT;
            invalidate();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP && mode == Mode.SELECT){
            x = event.getX();
            y = event.getY();
            pointOfZoomX = x;
            pointOfZoomY = y;
            showButtons();
            isReadyToAnswer = true;
            if (selectedParts.size() > 1){
                mode = Mode.CONFIRM;
            }
            else{
                mode = NOACTION;
            }
            invalidate();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP && isReadyToAnswer && mode == Mode.CONFIRM){
            mode = NOACTION;
            x = event.getX();
            y = event.getY();
            pointOfZoomX = x;
            pointOfZoomY = y;
            for (Term option : question.getOptions()) {
                //oznacena odpoved
                RectF button = option.getButton();
                if (button!= null && button.contains(x, y)){
                    if (option.getIdentifier().equals(question.getCorrectAnswerIdentifier())){
                        for (PartOfBody partOfBody : question.getBodyParts()){
                            if (partOfBody.getIdentifier().equals(option.getIdentifier())){
                                Paint paint = partOfBody.getPaint();
                                paint.setColor(Color.GREEN);
                            }
                        }
                    }
                    else{
                        for (PartOfBody partOfBody : question.getBodyParts()){

                            if (option.getIdentifier().equals(partOfBody.getIdentifier())){
                                Paint paint = partOfBody.getPaint();
                                paint.setColor(Color.RED);
                            }
                        }
                    }
                }
            }
            invalidate();
            return true;
        }
        return true;
    }

    public void showButtons(){
        selectedParts.clear();
        for (PartOfBody partOfBody : question.getBodyParts()){
            Region region = new Region();
            if (partOfBody.getBoundaries()!=null){
                RectF rectF = partOfBody.getBoundaries();
                region.setPath(partOfBody.getPath(), new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
                boolean isTerm = (question.containsIdentifier(partOfBody.getIdentifier()));
                boolean isNotInButtons = true;
                for (int i = 0; i < selectedParts.size(); i++) {
                    if (selectedParts.get(i).getIdentifier().equals(partOfBody.getIdentifier())){
                        isNotInButtons = false;
                    }
                }
                if (rectF.contains(Math.abs(x ), Math.abs(y )) && isTerm && isNotInButtons)
                {
                    selectedParts.add(partOfBody);
                    partOfBody.setSelected(true);
                }
            }

        }
    }

    public void drawButtons(){

        float angle = 0f;
        for (PartOfBody partOfBody: selectedParts){
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            float radiusOfButtons = Math.min(this.getHeight(), this.getWidth());
            float radius = radiusOfButtons/10;
            float xCenter = radiusOfButtons/2;
            float yCenter = radiusOfButtons/2;
            xCenter += Math.cos(angle) * xCenter/1.5;
            yCenter += Math.sin(angle) * yCenter/1.5;
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
            for ( Term term : options){
                if (term.getIdentifier().equals(partOfBody.getIdentifier())){
                    term.setButton(button);
                }
            }
            angle += (2 * Math.PI)/selectedParts.size();
        }
    }

    public enum Mode{
        INITIAL, DRAG, PINCHTOZOOM, TAPTOZOOM, SELECT, CONFIRM, NOACTION
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