package martinmatko.anatomy;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.TimingLogger;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {

    private static Context context;
    private final int FRAME_RATE = 30;
    public Test test = new Test();
    public Question question;
    public int goodAnswers = 0, numberOfQuestion = 0;
    public ArrayList<String> systemCategories = new ArrayList();
    public ArrayList<String> bodyCategories = new ArrayList();
    DrawView drawView;
    JSONObject profileData;
    private ViewFlipper viewFlipper;
    private Typeface tf;
    private int width;
    private Handler h;

    public static boolean isNetworkStatusAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfos = connectivityManager.getActiveNetworkInfo();
            if (netInfos != null)
                if (netInfos.isConnected())
                    return true;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = getApplicationContext();
        super.onCreate(savedInstanceState);
        // Set full screen view
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (isNetworkStatusAvailable(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "internet available", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "internet is not available", Toast.LENGTH_SHORT).show();

        }
        Configuration config = new Configuration();
        config.locale = new Locale("cs", "CZ");
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        setContentView(R.layout.my_layout);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        test.service.setUpCookies();
        TabHost tabhost = (TabHost) findViewById(R.id.tabHost);
        tabhost.setup();
        TabHost.TabSpec ts = tabhost.newTabSpec("tag1");

        ts.setContent(R.id.tab1);
        ts.setIndicator(getString(R.string.organSystems));
        tabhost.addTab(ts);

        ts = tabhost.newTabSpec("tag2");
        ts.setContent(R.id.tab2);
        ts.setIndicator(getString(R.string.bodyParts));
        tabhost.addTab(ts);
    }

    //Vyber
    public void getNextD2TdQuestion() {
        drawView.isHighlighted = false;
        Button highlightButton = (Button) findViewById(R.id.highlightButtonView);
        highlightButton.setVisibility(View.GONE);
        highlightButton.invalidate();

        if (question != null) {
            TextView captionView = (TextView) findViewById(R.id.captionView);
            captionView.setText(question.getCaption());
            TextView textView = (TextView) findViewById(R.id.textOfQuestionView);
            textView.setText(getString(R.string.choose) + " " + question.getCorrectAnswer().getName());
            setOptions(true);
        }
    }

    //Co je zvýrazněno?
    public void getNextt2dQuestion() {
        Button highlightButton = (Button) findViewById(R.id.highlightButtonView);
        highlightButton.setVisibility(View.VISIBLE);
        if (question != null) {
            TextView captionView = (TextView) findViewById(R.id.captionView);
            captionView.setText(question.getCaption());
            TextView textView = (TextView) findViewById(R.id.textOfQuestionView);
            textView.setText(R.string.highlighted);
            setOptions(false);
        }
    }

    public void setOptions(boolean isD2t) {
        RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
        options.setOnCheckedChangeListener(this);
        options.setBackgroundColor(getResources().getColor(R.color.optionsBackround));
        RadioButton button;
        for (Term option : question.getOptions()) {
            button = new RadioButton(this);
            if (isD2t) {
                button.setBackgroundColor(option.color);
            } else {
                button.setText(option.getName());
            }
            button.setTag(option.getIdentifier());
            options.addView(button, width, 100);
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton button = (RadioButton) group.getChildAt(i);
            button.setEnabled(false);
            button.setBackgroundColor(getResources().getColor(R.color.optionsBackround));
            if (button.getTag().equals(question.getCorrectAnswer().getIdentifier())) {
                button.setBackgroundColor(getResources().getColor(R.color.rightAnswer));
            }
            if (question.isD2T()) {
                for (Term option : question.getOptions()) {
                    if (option.getIdentifier().equals(button.getTag())) {
                        button.setText(option.getName());
                    }
                }
            }
        }
        RadioButton checked = (RadioButton) findViewById(checkedId);
        if (checked.getText().equals(question.getCorrectAnswer().getName())) {
            question.setAnswer(question.getCorrectAnswer());
            goodAnswers++;
        } else {
            checked.setBackgroundColor(getResources().getColor(R.color.wrongAnswer));
            for (Term option : question.getOptions()) {
                if (option.getIdentifier().equals(checked.getTag())) {
                    question.setAnswer(option);
                }
            }
        }
    }

    public void onNextClick(View v) {
        String answer = test.postAnswer(question.getAnswer(), question.getCorrectAnswer(), question.isD2T());
        while (!test.isPOSTCompleted) {
            try {
                Thread.currentThread().sleep(100); }
            catch (InterruptedException e) {
                e.printStackTrace(); }
        }
        question = test.questions.get(numberOfQuestion);
        drawView.question = question;
        drawView.totalScaleFactor = 1.0f;
        drawView.mode = DrawView.Mode.INITIAL;
        drawView.invalidate();
        RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
        options.removeAllViews();
        numberOfQuestion++;
        if (numberOfQuestion < 6) {
            if (question.isD2T()) {
                getNextD2TdQuestion();
            } else
                getNextt2dQuestion();
        } else {
            setContentView(R.layout.my_layout);
//            TextView captionView = (TextView) findViewById(R.id.captionView);
//            captionView.setText(question.getCaption());
//            int score = goodAnswers * 25;
//            captionView.setText(getString(R.string.rate) + " " + Integer.toString(score) + " %");
//            captionView.setTypeface(tf);
            numberOfQuestion = 0;
        }
        ServiceAsyncTask task = new ServiceAsyncTask(test);
        task.execute(answer);

        //test.getNextQuestion(answer);
    }

    public void onHighlightClick(View v) {
        drawView.isHighlighted = !drawView.isHighlighted;
        drawView.invalidate();
    }

    public void onTestClicked(View v) {
        setContentView(R.layout.activity_main);
        drawView = (DrawView) findViewById(R.id.drawView);
        test.start(systemCategories, bodyCategories);
        drawView.question = test.questions.get(numberOfQuestion);
        drawView.question.setD2T(true);
        question = drawView.question;
        getNextD2TdQuestion();
        numberOfQuestion++;
    }

    public void onRandomTestClicked(View v) {
        systemCategories.clear();
        bodyCategories.clear();
        onTestClicked(v);
    }

    public void onCategoryClicked(View v) {
        char firstLetter = v.getTag().toString().charAt(0);
        if (firstLetter == '1' || firstLetter == '0') {
            if (systemCategories.contains(v.getTag())) {
                systemCategories.remove(v.getTag());
                v.setBackgroundColor(getResources().getColor(R.color.gray));

            } else {
                systemCategories.add(v.getTag().toString());
                v.setBackgroundColor(getResources().getColor(R.color.buttonSelected));
            }
        } else {
            if (bodyCategories.contains(v.getTag())) {
                bodyCategories.remove(v.getTag());
                v.setBackgroundColor(getResources().getColor(R.color.gray));
            } else {
                bodyCategories.add(v.getTag().toString());
                v.setBackgroundColor(getResources().getColor(R.color.buttonSelected));
            }
        }
    }

    public void onSystemCategoriesClicked(View v) {
        setContentView(R.layout.categories_layout);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        TextView tv = (TextView) findViewById(R.id.bodyParts);
        tv.setTypeface(tf);
        tv = (TextView) findViewById(R.id.organSystems);
        tv.setTypeface(tf);
    }

    public void onBodyCategoriesClicked(View v) {
        onSystemCategoriesClicked(v);
        viewFlipper.showNext();
    }

    public void onSystemCategoriesLabelClicked(View v) {
        if (viewFlipper.getDisplayedChild() == 1) {
            viewFlipper.showPrevious();
        }
    }

    public void onBodyCategoriesLabelClicked(View v) {
        if (viewFlipper.getDisplayedChild() == 0) {
            viewFlipper.showPrevious();
        }
    }

    public void onExitClicked(View v) {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    public void onFABClicked(View v) {
        View button = findViewById(R.id.startCategoriesTest);
        button.setVisibility(View.VISIBLE);
        button = findViewById(R.id.startTest);
        button.setVisibility(View.VISIBLE);
        button = findViewById(R.id.fab);
        button.setVisibility(View.GONE);
    }
    //protected String fetchToken() throws IOException { try { return GoogleAuthUtil.getToken(this, "mmatko93@gmail.com", mScope); } catch (GooglePlayServicesAvailabilityException playEx) { // GooglePlayServices.apk is either old, disabled, or not present. } catch (UserRecoverableAuthException userRecoverableException) { // Unable to authenticate, but the user can fix this. // Forward the user to the appropriate activity. mActivity.startActivityForResult(userRecoverableException.getIntent(), mRequestCode); } catch (GoogleAuthException fatalException) { onError("Unrecoverable error " + fatalException.getMessage(), fatalException); } return null; }

}



