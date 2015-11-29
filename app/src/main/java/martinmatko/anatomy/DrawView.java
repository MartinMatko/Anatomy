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
import java.util.List;

public class DrawView extends View {

    Context ctx;

    static final String TAG = "DrawView";
    public float left = Float.MAX_VALUE;
    public float top = Float.MAX_VALUE;
    public float right = 0;
    public float bottom = 0;
    public float pointOfZoomX = 0f;
    public float pointOfZoomY = 0f;

    private Canvas canvas = new Canvas();
    Paint paint = new Paint();
    Question question;
    //These two constants specify the minimum and maximum zoom
    private static float MIN_ZOOM = -1f;
    private static float MAX_ZOOM = 5f;

    private float scaleFactor = 2.f;
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

    // Used for set first translate to a quarter of screen
    private float displayWidth;
    private float displayHeight;

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
        try {
            this.question = new JSONParser().getQuestion();
        } catch (Exception e) {
            e.printStackTrace();
        }
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        DisplayMetrics metrics = new DisplayMetrics();

        display.getMetrics(metrics);

        translateX = displayWidth/4;
        translateY = displayHeight/4;

        previousTranslateX = displayWidth/4;
        previousTranslateY = displayHeight/4;

        detector = new ScaleGestureDetector(context, new ScaleListener());

        setFocusable(true);
        setFocusableInTouchMode(true);
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

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        displayWidth = this.getWidth();
        displayHeight = this.getHeight();

        this.canvas  = canvas;

        if (!touched && dragged){
            pointOfZoomX = translateX ;
            pointOfZoomY = translateY ;
        }
        canvas.save();
        if (selectedMode){
            scaleFactor = 1f;
        }
        try {
            for (PartOfBody partOfBody: question.getBodyParts()){
                Paint p = new Paint();
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(1);
                //setBounds(partOfBody.getBoundaries());
                //canvas.drawRect(partOfBody.getBoundaries(), p);
                Matrix matrix = new Matrix();
                matrix.setScale(scaleFactor, scaleFactor, pointOfZoomX, pointOfZoomY);

                Path path = new Path();
                partOfBody.getPath().transform(matrix, path);
                RectF boundaries = new RectF();
                path.computeBounds(boundaries, true);

                partOfBody.setPath(path);
                //matrix.setTranslate(-canvasTranslateX/scaleFactor, -canvasTranslateY/scaleFactor);
                //touched = false;

                canvas.drawPath(path, partOfBody.getPaint());
                partOfBody.setBoundaries(boundaries);
                canvas.drawRect(boundaries, p);
            }
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
        float angle = 0f;
        for (PartOfBody partOfBody: selectedParts){
                Paint p = new Paint();
                RectF button = new RectF();
                button.left += 200 * Math.cos(angle) + x;
                button.right = button.left + 300;
                button.top += 200 * Math.sin(angle) + y;
                button.bottom = button.top + 200;
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(10);
                canvas.drawRect(button, p);
                p = partOfBody.getPaint();
                p.setStyle(Paint.Style.FILL);
                canvas.drawRect(button, p);
            angle += (2 * Math.PI)/selectedParts.size();
        }
        if (question != null && findViewById(R.id.captionView) != null){
            TextView captionView = (TextView) findViewById(R.id.captionView);
            captionView.setText(question.getCaption());
            captionView.invalidate();
            TextView textView = (TextView) findViewById(R.id.textOfQuestionView);
            textView.setText("Zvolte nos");
            textView.invalidate();
            RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
            RadioButton button;
            for(int i = 0; i < 5; i++) {
                button = new RadioButton(ctx);
                button.setText(question.getOptions().get(i));
                options.addView(button);
            }
            options.invalidate();
        }
        //canvas.translate(Math.abs(translateX - previousTranslateX) / scaleFactor, Math.abs(translateY - previousTranslateY) / scaleFactor);
        canvas.restore();
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

                break;

            case MotionEvent.ACTION_POINTER_DOWN:
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
                return true;

// All touch events are sended to ScaleListener
        }
        detector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP && !dragged && touched){
            x = event.getX();
            y = event.getY();
            selectedMode = true;
            selectedParts.clear();
            for (PartOfBody partOfBody : question.getBodyParts()){
                Region region = new Region();
                RectF rectF = partOfBody.getBoundaries();
                region.setPath(partOfBody.getPath(), new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
                if (rectF.contains(Math.abs(x ), Math.abs(y )))
                {
                    selectedParts.add(partOfBody);
                    partOfBody.setSelected(true);
                }
            }
            invalidate();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP && !dragged && !touched){
            final int MAX_DURATION = 200;
            float x = event.getX();
            float y = event.getY();
            RectF rectF2 = new RectF();
            scaleFactor = 3;
            pointOfZoomX = x;
            pointOfZoomY = y;
            dragged = false;
            touched = true;
            invalidate();
        }

        return true;
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