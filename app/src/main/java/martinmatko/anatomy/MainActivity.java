package martinmatko.anatomy;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {

    private static Context context;
    public Test test = new Test();
    public Question question;
    public int goodAnswers = 0, numberOfQuestion = 1;
    public ArrayList<String> systemCategories = new ArrayList();
    public ArrayList<String> bodyCategories = new ArrayList();
    DrawView drawView;
    JSONObject profileData;
    private ViewFlipper viewFlipper;
    private Typeface tf;
    private int width;

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
        setContentView(R.layout.menu_layout);
        TextView tv = (TextView) findViewById(R.id.captionView);
        tf = Typeface.createFromAsset(context.getAssets(), "webfont.ttf");
        tv.setTypeface(tf);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
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
            RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
            options.removeAllViews();
            RadioButton button;
            options.setBackgroundColor(Color.BLACK);
            for (Term option : question.getOptions()) {
                button = new RadioButton(this);
                button.setBackgroundColor(option.color);
                button.setText(option.getName());
                options.addView(button, width, 130);
            }
            invalidateOptionsMenu();
        }
    }

    //Co je zvýrazněno?
    public void getNextt2dQuestion() {
        Button highlightButton = (Button) findViewById(R.id.highlightButtonView);
        highlightButton.setVisibility(View.VISIBLE);
        RadioGroup rg = (RadioGroup) findViewById(R.id.optionsView);
        rg.setOnCheckedChangeListener(this);
        if (question != null) {
            TextView captionView = (TextView) findViewById(R.id.captionView);
            captionView.setText(question.getCaption());
            TextView textView = (TextView) findViewById(R.id.textOfQuestionView);
            textView.setText(R.string.highlighted);
            RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
            options.removeAllViews();
            RadioButton button;
            for (int i = 0; i < question.getOptions().size(); i++) {
                button = new RadioButton(this);
                button.setText(question.getOptions().get(i).getName());
                options.addView(button, width, 130);
            }
            button = new RadioButton(this);
            button.setText(question.getCorrectAnswer().getName());
            options.addView(button, width, 130);
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton button = (RadioButton) group.getChildAt(i);
            button.setEnabled(false);
        }
        RadioButton checked = (RadioButton) findViewById(checkedId);
        if (checked.getText().equals(question.getCorrectAnswer().getName())) {
            checked.setBackgroundColor(Color.GREEN);
            question.setAnswer(question.getCorrectAnswer());
            goodAnswers++;
        } else {
            checked.setBackgroundColor(Color.RED);
            for (Term option : question.getOptions()) {
                if (option.getName().equals(checked.getText())) {
                    question.setAnswer(option);
                }
            }
        }
    }

    public void onNextClick(View v) {
        String answer = test.postAnswer(question.getAnswer(), question.getCorrectAnswer(), question.isD2T());
        System.out.println(answer);
        question = test.getNextQuestion(answer);
        drawView.question = question;
        drawView.mode = DrawView.Mode.INITIAL;
        drawView.invalidate();
        RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
        options.removeAllViews();
        numberOfQuestion++;
        if (numberOfQuestion < 4) {
            if (question.isD2T()) {
                getNextD2TdQuestion();
            } else
                getNextt2dQuestion();
        } else {
            setContentView(R.layout.menu_layout);
            TextView captionView = (TextView) findViewById(R.id.captionView);
            captionView.setText(question.getCaption());
            int score = goodAnswers * 25;
            captionView.setText(getString(R.string.rate) + " " + Integer.toString(score) + " %");
            captionView.setTypeface(tf);
            numberOfQuestion = 0;
        }
    }

    public void onHighlightClick(View v) {
        drawView.isHighlighted = !drawView.isHighlighted;
        drawView.invalidate();
    }

    public void onTestClicked(View v) {
        setContentView(R.layout.activity_main);
        drawView = (DrawView) findViewById(R.id.drawView);
        drawView.question = test.getFirstQuestion(systemCategories, bodyCategories);
        drawView.invalidate();
        TextView tv = (TextView) findViewById(R.id.captionView);
        tv.setTypeface(tf);
        question = drawView.question;
        if (question.isD2T()) {
            getNextD2TdQuestion();
        } else
            getNextt2dQuestion();
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
    //protected String fetchToken() throws IOException { try { return GoogleAuthUtil.getToken(this, "mmatko93@gmail.com", mScope); } catch (GooglePlayServicesAvailabilityException playEx) { // GooglePlayServices.apk is either old, disabled, or not present. } catch (UserRecoverableAuthException userRecoverableException) { // Unable to authenticate, but the user can fix this. // Forward the user to the appropriate activity. mActivity.startActivityForResult(userRecoverableException.getIntent(), mRequestCode); } catch (GoogleAuthException fatalException) { onError("Unrecoverable error " + fatalException.getMessage(), fatalException); } return null; }

}



