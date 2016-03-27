package martinmatko.anatomy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private static Context context;
    public Test test = new Test();
    public Question question;
    public int goodAnswers = 0, numberOfQuestion = 0;
    public ArrayList<String> systemCategories = new ArrayList();
    public ArrayList<String> bodyCategories = new ArrayList();
    DrawView drawView;
    private int width;
    long startTime;
    long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = getApplicationContext();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("cookies");
            System.out.println("cookies" + value);
            test.service.setUpCookies(value);
        }
        setContentView(R.layout.activity_main);
        drawView = (DrawView) findViewById(R.id.drawView);
        test.start(systemCategories, bodyCategories);
        drawView.question = test.questions.get(numberOfQuestion);
        question = drawView.question;
        getNextD2TdQuestion();
        numberOfQuestion++;
    }

    //Vyber
    public void getNextD2TdQuestion() {
        drawView.isHighlighted = false;
        Button highlightButton = (Button) findViewById(R.id.highlightButtonView);
        highlightButton.setVisibility(View.GONE);
        highlightButton.invalidate();

        if (question != null) {
            TextView textView = (TextView) findViewById(R.id.textOfQuestionView);
            SpannableString text = new SpannableString(getString(R.string.choose) + " " + question.getCorrectAnswer().getName());
            int lengthOfFirstPart = getString(R.string.choose).length();
            text.setSpan(new ForegroundColorSpan(Color.BLACK), 0, lengthOfFirstPart, 0);
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.grey_500)), lengthOfFirstPart + 1, text.length(), 0);
            text.setSpan(new RelativeSizeSpan(0.8f), lengthOfFirstPart + 1, text.length(), 0);
            textView.setText(text, TextView.BufferType.SPANNABLE);
            setOptions(true);
        }
    }

    //Co je zvýrazněno?
    public void getNextt2dQuestion() {
        Button highlightButton = (Button) findViewById(R.id.highlightButtonView);
        highlightButton.setVisibility(View.VISIBLE);
        if (question != null) {
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
            button.setButtonDrawable(new StateListDrawable());
            //button.setPadding(20, 5, 5, 5);
            options.addView(button, width, 120);
        }
        invalidateOptionsMenu();
    }

    public void setAnswersInOptions(String identifier){
        endTime = System.currentTimeMillis();
        RadioGroup group = (RadioGroup) findViewById(R.id.optionsView);
        for (Term option : question.getOptions()) {
            if (option.getIdentifier().equals(identifier)) {
                question.setAnswer(option);
            }
        }
        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton button = (RadioButton) group.getChildAt(i);
            button.setEnabled(false);
            button.setBackgroundColor(getResources().getColor(R.color.optionsBackround));
            if (button.getTag().equals(question.getAnswer().getIdentifier())) {
                button.setBackgroundColor(getResources().getColor(R.color.wrongAnswer));
            }
            if (button.getTag().equals(question.getCorrectAnswer().getIdentifier())) {
                button.setBackgroundColor(getResources().getColor(R.color.rightAnswer));
                if (identifier.equals(button.getTag())){
                    goodAnswers++;
                }
            }
            if (question.isD2T()) {
                button.setText(question.getOptions().get(i).getName());
            }
        }
        drawView.mode = DrawView.Mode.FINISH;
        drawView.selectedParts.add(new PartOfBody(null, null, identifier.toString()));
        drawView.invalidate();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton checked = (RadioButton) findViewById(checkedId);
        setAnswersInOptions(checked.getTag().toString());
    }

    public void onNextClick(View v) {
        if (numberOfQuestion < 8) {
            while (numberOfQuestion + 1 != test.questions.size()) {
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            question = test.questions.get(numberOfQuestion);
            TextView captionView = (TextView) findViewById(R.id.captionView);
            captionView.setText(question.getCaption());
            RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
            options.removeAllViews();
            drawView.clearVariables();
            drawView.question = question;
            drawView.invalidate();
            if (question.isD2T()) {
                getNextD2TdQuestion();
            } else {
                getNextt2dQuestion();
            }
            boolean isWithoutOptions = (question.getOptions().size() == 0);
            String answer = test.postAnswer(question.getAnswer(), question.getCorrectAnswer(), question.isD2T(), isWithoutOptions, startTime - endTime);
            numberOfQuestion++;
            PostAsyncTask task = new PostAsyncTask(test);
            task.execute(answer);
            startTime = System.currentTimeMillis();

        } else {
            TextView captionView = (TextView) findViewById(R.id.captionView);
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
    }

    public void onHighlightClick(View v) {
        drawView.isHighlighted = !drawView.isHighlighted;
        drawView.invalidate();
    }

    public void onCaptionClick(View view) {
        Toast.makeText(context, question.getCaption(), Toast.LENGTH_LONG).show();
    }
    public void goToMenu(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }
    public void onTestClicked(View view) {
        test.start(systemCategories, bodyCategories);
        drawView.question = test.questions.get(numberOfQuestion);
        question = drawView.question;
        getNextD2TdQuestion();
        numberOfQuestion++;
    }
}



