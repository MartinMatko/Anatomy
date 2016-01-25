package martinmatko.anatomy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {

    private static Context context;
    public Test test;
    public Question question;
    public int goodAnswers = 0, numberOfQuestion = 1;
    TextView textView;

    public static Context getAppContext() {
        return context;
    }

    public TextView gettextView() {
        return textView;
    }

    public void settextView(TextView t) {
        this.textView = t;
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
        getNextD2TdQuestion();
    }

    //Vyber
    public void getNextD2TdQuestion() {
        setContentView(R.layout.activity_main);
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
        drawView.setD2T(true);
        question = drawView.question;
        if (question != null) {
            TextView captionView = (TextView) findViewById(R.id.captionView);
            captionView.setText(question.getCaption());
            TextView textView = (TextView) findViewById(R.id.textOfQuestionView);
            textView.setText("Vyber");
            RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
            RadioButton button;
            button = new RadioButton(this);
            button.setText(question.getCorrectAnswer());
            options.addView(button);
        }
    }

    //Co je zvýrazněno?
    public void getNextt2dQuestion() {
        setContentView(R.layout.activity_main);
        RadioGroup rg = (RadioGroup) findViewById(R.id.optionsView);
        rg.setOnCheckedChangeListener(this);
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
        question = drawView.question;
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
            button.setText(question.getCorrectAnswer());
            options.addView(button);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setEnabled(false);
        }
        RadioButton checked = (RadioButton) findViewById(checkedId);
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
        if (checked.getText().equals(question.getCorrectAnswer())) {
            checked.setBackgroundColor(Color.GREEN);
            goodAnswers++;
        } else
            checked.setBackgroundColor(Color.RED);
    }

    public void onNextClick(View v) {
        if (numberOfQuestion < 5) {
            numberOfQuestion++;
            getNextD2TdQuestion();
        } else {
            if (numberOfQuestion < 5) {
                numberOfQuestion++;
                getNextD2TdQuestion();
            }

            TextView captionView = (TextView) findViewById(R.id.captionView);
            captionView.setText(question.getCaption());
            int score = goodAnswers * 25;
            captionView.setText("Úspěšnost: " + score);
        }
    }
}



