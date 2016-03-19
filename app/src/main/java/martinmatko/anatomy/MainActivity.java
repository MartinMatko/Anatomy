package martinmatko.anatomy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {

    private static Context context;
    public Test test = new Test();
    public Question question;
    public int goodAnswers = 0, numberOfQuestion = 0;
    public ArrayList<String> systemCategories = new ArrayList();
    public ArrayList<String> bodyCategories = new ArrayList();
    DrawView drawView;
    private ViewFlipper viewFlipper;
    private int width;

    public static boolean isNetworkStatusAvailable(Context context) {
        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        long time0 = System.currentTimeMillis();
        long a, b, c;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = getApplicationContext();
        super.onCreate(savedInstanceState);
        // Set full screen view
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        Configuration config = new Configuration();
        config.locale = new Locale("cs", "CZ");
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        setCategoriesMenu(null);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        a = System.currentTimeMillis() - time0;
        //test.service.setUpCookies();

        b = System.currentTimeMillis() - a - time0;
        if (!isNetworkStatusAvailable(getApplicationContext())) {
            buildDialog(this).show();
        } else {
            Toast.makeText(getApplicationContext(), "internet is available", Toast.LENGTH_LONG).show();
        }
        c = System.currentTimeMillis() - b - a - time0;
        System.out.println("execute: " + a + "\ntoString: " + b + "\nJSONObject: " + c);
    }

    public void setCategoriesMenu(View v) {
        setContentView(R.layout.my_layout);
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec ts = tabHost.newTabSpec("tag1");

        ts.setContent(R.id.tab1);
        ts.setIndicator(getString(R.string.organSystems));
        tabHost.addTab(ts);

        ts = tabHost.newTabSpec("tag2");
        ts.setContent(R.id.tab2);
        ts.setIndicator(getString(R.string.bodyParts));
        tabHost.addTab(ts);
        tabHost.setOnTabChangedListener(new AnimatedTabHostListener(tabHost));

        for(int i=0;i<tabHost.getTabWidget().getChildCount();i++)
        {
            tabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.color.green); //unselected
        }
        tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundResource(R.color.grey_100); // selected

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
                button.setBackgroundColor(option.getColor());
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
        drawView.mode = DrawView.Mode.FINISH;
        drawView.selectedParts.add(new PartOfBody(null, null, checked.getTag().toString()));
        drawView.invalidate();
    }

    public void onNextClick(View v) {
        if (numberOfQuestion < 8) {
            RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
            options.removeAllViews();
            while (!test.isPOSTCompleted) {
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            question = test.questions.get(numberOfQuestion);
            drawView.clearVariables();
            drawView.question = question;
            drawView.invalidate();
            if (question.isD2T()) {
                getNextD2TdQuestion();
            } else{
                getNextt2dQuestion();
            }
            String answer = test.postAnswer(question.getAnswer(), question.getCorrectAnswer(), question.isD2T());
            numberOfQuestion++;

            ServiceAsyncTask task = new ServiceAsyncTask(test);
            task.execute(answer);

        } else {

            TextView captionView = (TextView) findViewById(R.id.captionView);
            captionView.setText(question.getCaption());
            int score = goodAnswers * 25;
            captionView.setText(getString(R.string.rate) + " " + Integer.toString(score) + " %");
            View fab = findViewById(R.id.multiple_actions);
            fab.setVisibility(View.VISIBLE);
            View next = findViewById(R.id.nextButtonView);
            next.setVisibility(View.GONE);
            numberOfQuestion = 0;
            test.questions.clear();
            goodAnswers = 0;
        }

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
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);

    }

    @Override
    public void onBackPressed(){
         setCategoriesMenu(null);
    }


    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet connection.");
        builder.setMessage("You have no internet connection");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                onExitClicked(null);
            }
        });

        return builder;
    }
}



