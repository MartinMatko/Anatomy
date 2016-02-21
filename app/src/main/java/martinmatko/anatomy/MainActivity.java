package martinmatko.anatomy;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {

    private static Context context;
    public Test test = new Test();
    public Question question;
    public int goodAnswers = 0, numberOfQuestion = 1;
    DrawView drawView;

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
        setContentView(R.layout.menu_layout);

        Locale locale = new Locale("en_US");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getApplicationContext().getResources().updateConfiguration(config, null);
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
            textView.setText("Vyber: " + question.getCorrectAnswer().getName());
            RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
            RadioButton button;
            for (Term option : question.getOptions()) {
                button = new RadioButton(this);

                button.setBackgroundColor(option.color);
                options.addView(button, options.getWidth(), 100);
            }
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
            textView.setText("Co je zvýrazněno?");
            RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
            RadioButton button;
            for (int i = 0; i < question.getOptions().size(); i++) {
                button = new RadioButton(this);
                button.setText(question.getOptions().get(i).getName());
                options.addView(button);
            }
            button = new RadioButton(this);
            button.setText(question.getCorrectAnswer().getName());
            options.addView(button);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setEnabled(false);
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
        options.invalidate();
        if (numberOfQuestion < 10) {
            if (question.isD2T()) {
                getNextD2TdQuestion();
            } else
                getNextt2dQuestion();
        } else {
            TextView captionView = (TextView) findViewById(R.id.captionView);
            captionView.setText(question.getCaption());
            int score = goodAnswers * 25;
            captionView.setText("Úspěšnost: " + score);
        }
    }
    public void onHighlightClick(View v){
        drawView.isHighlighted = !drawView.isHighlighted;
        drawView.invalidate();
    }

    public void onRandomTestClicked(View v){
        setContentView(R.layout.activity_main);
        drawView = (DrawView) findViewById(R.id.drawView);
        drawView.question = test.getFirstQuestion();
        drawView.invalidate();
        question = drawView.question;
        if (question.isD2T()) {
            getNextD2TdQuestion();
        } else
            getNextt2dQuestion();
    }

    public void onSystemClicked (View v){
        setContentView(R.layout.categories_layout);
        Display display = getWindowManager().getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();

        ImageButton yourBtn = (ImageButton) findViewById(R.id.button1);
        int btnSize=yourBtn.getLayoutParams().width;
        yourBtn.setLayoutParams(new TableRow.LayoutParams(screenWidth/3, screenWidth/3));
        ImageButton yourBtn2 = (ImageButton) findViewById(R.id.button2);
        int btnSize2=yourBtn2.getLayoutParams().width;
        yourBtn2.setLayoutParams(new TableRow.LayoutParams(screenWidth/3, screenWidth/3));
        ImageButton yourBtn3 = (ImageButton) findViewById(R.id.button3);
        int btnSize3=yourBtn3.getLayoutParams().width;
        yourBtn3.setLayoutParams(new TableRow.LayoutParams(screenWidth/3, screenWidth/3));

        //GridLayout ll = (GridLayout) findViewById(R.id.layout);
        JSONArray categories = test.getCategories();
        for (int i = 0; i < categories.length(); i++){
            CheckBox checkBox = new CheckBox(this);
            String text = null;
            try {
                text = categories.getJSONObject(i).getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            checkBox.setText(text);
            //checkBox.setBackground(ContextCompat.getDrawable(context, R.drawable.one));
            //ll.addView(checkBox);
        }
    }
}



