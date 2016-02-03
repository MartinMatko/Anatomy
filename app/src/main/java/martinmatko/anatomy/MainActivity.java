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

import org.json.JSONException;

import java.io.IOException;

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
        drawView.question = test.getNextQuestion();
        drawView.invalidate();
        question = drawView.question;
        if (question.isD2T()){
            getNextD2TdQuestion();
        }
        else
            getNextt2dQuestion();
    }

    //Vyber
    public void getNextD2TdQuestion() {
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
        question = drawView.question;
        if (question != null) {
            TextView captionView = (TextView) findViewById(R.id.captionView);
            captionView.setText(question.getCaption());
            TextView textView = (TextView) findViewById(R.id.textOfQuestionView);
            textView.setText("Vyber");
            RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
            options.removeAllViews();
            RadioButton button;
            button = new RadioButton(this);
            button.setText(question.getCorrectAnswer().getName());
            options.addView(button);
        }
    }

    //Co je zvýrazněno?
    public void getNextt2dQuestion() {
        RadioGroup rg = (RadioGroup) findViewById(R.id.optionsView);
        //rg.setOnCheckedChangeListener(this);
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
        question = drawView.question;
        if (question != null) {
            TextView captionView = (TextView) findViewById(R.id.captionView);
            captionView.setText(question.getCaption());
            TextView textView = (TextView) findViewById(R.id.textOfQuestionView);
            textView.setText("Co je zvýrazněno?");
            RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
            options.clearCheck();
            options.removeAllViews();
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
        if (checked.getText().equals(question.getCorrectAnswer())) {
            checked.setBackgroundColor(Color.GREEN);
            question.setAnswer(question.getCorrectAnswer());
            goodAnswers++;
        } else{
            checked.setBackgroundColor(Color.RED);
            for (Term option : question.getOptions()){
                if (option.getName().equals(checked.getText())){
                    question.setAnswer(option);
                }
            }
        }

    }

    public void onNextClick(View v) {
        Test test = new Test();
        DrawView drawView = (DrawView) findViewById(R.id.drawView);
        drawView.question = test.getNextQuestion();
        question = drawView.question;
        drawView.invalidate();
//        String answer = test.postAnswer(question.getCorrectAnswer().getId(), question.getAnswer().getId(), question.isD2T());
//        service.post(answer, question.getCookies());
        if (numberOfQuestion < 10) {
            numberOfQuestion++;
            getNextD2TdQuestion();
        } else {
            if (numberOfQuestion < 10) {
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



