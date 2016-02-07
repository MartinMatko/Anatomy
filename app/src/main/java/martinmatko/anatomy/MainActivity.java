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
    public Test test = new Test();
    public Question question;
    public int goodAnswers = 0, numberOfQuestion = 1;
    TextView textView;

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

        setContentView(R.layout.activity_main);
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
        drawView.question = test.getFirstQuestion();
        drawView.invalidate();
        question = drawView.question;
        if (question.isD2T()) {
            getNextD2TdQuestion();
        } else
            getNextt2dQuestion();
    }

    //Vyber
    public void getNextD2TdQuestion() {
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
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
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
}



