package martinmatko.Anatom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import utils.Constants;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private static Context context;
    private Test test = new Test();
    private Question question;
    private int goodAnswers = 0, numberOfQuestion = 0;
    private int height;
    private DrawView drawView;
    private long startTime;
    private long endTime;
    private int width;
    private boolean isAnswered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = getApplicationContext();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        String categories = "";
        if (extras != null) {
            String value = extras.getString("cookies");
            test.getService().setUpCookies(value);
            categories = extras.getString("categories");
        }
        test.setCategories(categories);
        if (!test.start(categories)) {//problem with getting first question
            buildDialog(this).show();
            return;
        }
        setContentView(R.layout.activity_main);
        drawView = (DrawView) findViewById(R.id.drawView);
        drawView.setQuestion(test.getQuestions().get(numberOfQuestion));
        question = drawView.getQuestion();
        TextView captionView = (TextView) findViewById(R.id.captionView);
        captionView.setText(question.getCaption());
        if (question.isT2D()) {
            getNextT2DQuestion();
        } else {
            getNextD2TQuestion();
        }
        numberOfQuestion++;
        startTime = System.currentTimeMillis();
    }

    //Vyber
    public void getNextT2DQuestion() {
        Button highlightButton = (Button) findViewById(R.id.highlightButtonView);
        highlightButton.setVisibility(View.GONE);
        highlightButton.invalidate();

        if (question != null) {
            TextView textView = (TextView) findViewById(R.id.textOfQuestionView);
            SpannableString text = new SpannableString(getString(R.string.choose) + " " + question.getCorrectAnswer().getName());
            int lengthOfFirstPart = getString(R.string.choose).length();
            text.setSpan(new RelativeSizeSpan(0.8f), 0, lengthOfFirstPart, 0);
            text.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.grey_500)), 0, lengthOfFirstPart, 0);
            text.setSpan(new ForegroundColorSpan(Color.BLACK), lengthOfFirstPart + 1, text.length(), 0);
            textView.setText(text, TextView.BufferType.SPANNABLE);
            setOptions(true);
        }
    }

    //Co je zvýrazněno?
    public void getNextD2TQuestion() {
        Button highlightButton = (Button) findViewById(R.id.highlightButtonView);
        highlightButton.setVisibility(View.VISIBLE);
        if (question != null) {
            TextView textView = (TextView) findViewById(R.id.textOfQuestionView);
            textView.setText(R.string.highlighted);
            setOptions(false);
        }
    }

    public void setOptions(boolean isT2D) {
        RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
        options.setOnCheckedChangeListener(this);
        options.setBackgroundColor(getResources().getColor(R.color.optionsBackround));
        RadioButton button;
        for (Term option : question.getOptions()) {
            button = new RadioButton(this);
            if (isT2D) {
                button.setBackgroundColor(option.getColor());
            } else {
                button.setText(option.getName());
            }
            button.setTag(option.getIdentifier());
            button.setButtonDrawable(new StateListDrawable());
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            button.setPadding(30, 0, 0, 0);
            options.addView(button, width, height / Constants.RADIO_BUTTON_HEIGHT);
        }
    }

    public void setAnswersInOptions(String identifier) {
        isAnswered = true;
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
            if (button.getTag().equals(identifier)) {
                button.setBackgroundColor(getResources().getColor(R.color.wrongAnswer));
            }
            if (button.getTag().equals(question.getCorrectAnswer().getIdentifier())) {
                button.setBackgroundColor(getResources().getColor(R.color.rightAnswer));
                if (identifier.equals(button.getTag())) {
                    goodAnswers++;
                }
            }
            if (question.isT2D() && question.getOptions().size() != 0) {
                button.setText(question.getOptions().get(i).getName());
            }
        }
        if (question.getOptions().size() == 0 && drawView.getMode().equals(DrawView.Mode.FINISH)) {
            RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
            //options.removeAllViews();
            options.setBackgroundColor(getResources().getColor(R.color.optionsBackround));
            RadioButton button = new RadioButton(this);
            button.setEnabled(false);
            button.setBackgroundColor(getResources().getColor(R.color.rightAnswer));
            button.setText(question.getCorrectAnswer().getName());
            button.setTag(question.getCorrectAnswer().getIdentifier());
            button.setButtonDrawable(new StateListDrawable());
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            button.setPadding(30, 0, 0, 0);
            options.addView(button, width, height / Constants.RADIO_BUTTON_HEIGHT);
            if (!identifier.isEmpty() && !identifier.equals(question.getCorrectAnswer().getIdentifier())) {
                RadioButton buttonOfIncorrectAnswer = new RadioButton(this);
                buttonOfIncorrectAnswer.setEnabled(false);
                buttonOfIncorrectAnswer.setBackgroundColor(getResources().getColor(R.color.wrongAnswer));
                Term answer = null;
                for (Term term : question.getTerms()) {
                    if (identifier.equals(term.getIdentifier())) {
                        answer = term;
                        break;
                    }
                }
                buttonOfIncorrectAnswer.setText(answer.getName());
                buttonOfIncorrectAnswer.setTag(answer.getIdentifier());
                buttonOfIncorrectAnswer.setButtonDrawable(new StateListDrawable());
                buttonOfIncorrectAnswer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                buttonOfIncorrectAnswer.setPadding(30, 0, 0, 0);
                options.addView(buttonOfIncorrectAnswer, width, height / Constants.RADIO_BUTTON_HEIGHT);
                question.setAnswer(answer);
            } else if (!identifier.isEmpty()) {
                question.setAnswer(question.getCorrectAnswer());
                goodAnswers++;
            }
        }
        drawView.setMode(DrawView.Mode.FINISH);
        drawView.getSelectedParts().add(new PartOfBody(null, null, identifier.toString()));
        drawView.invalidate();
        Button button = (Button) findViewById(R.id.nextButtonView);
        button.setText(R.string.continueToNext);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton checked = (RadioButton) findViewById(checkedId);
        setAnswersInOptions(checked.getTag().toString());
    }

    public void onNextClick(View v) {
        if (!isAnswered) {
            setAnswersInOptions("");
            question.setAnswer(new Term("", "", ""));
        } else {
            isAnswered = false;
            String answer = test.postAnswer(question, endTime - startTime);
            PostAsyncTask task = new PostAsyncTask(test);
            if (numberOfQuestion < 10) {
                float timeout = 0;
                while (numberOfQuestion + 1 != test.getQuestions().size()) {
                    try {
                        timeout += 100;
                        if (timeout < 10000) {
                            Thread.currentThread().sleep(100);
                        } else throw new InterruptedException();
                    } catch (InterruptedException e) {
                        buildDialog(this).show();
                        this.finish();
                        e.printStackTrace();
                        return;
                    }
                }
                question = test.getQuestions().get(numberOfQuestion);
                task.execute(answer, question.getItemId());
                TextView captionView = (TextView) findViewById(R.id.captionView);
                captionView.setText(question.getCaption());
                RadioGroup options = (RadioGroup) findViewById(R.id.optionsView);
                options.removeAllViews();
                Button button = (Button) findViewById(R.id.nextButtonView);
                button.setText(R.string.doNotKnow);
                drawView.clearVariables();
                drawView.setQuestion(question);
                drawView.invalidate();
                if (question.isT2D()) {
                    getNextT2DQuestion();
                } else {
                    getNextD2TQuestion();
                }
                numberOfQuestion++;
                startTime = System.currentTimeMillis();

            } else {
                TextView captionView = (TextView) findViewById(R.id.captionView);
                captionView.setText(getString(R.string.testFinished));
                Toast.makeText(this, getString(R.string.testFinished), Toast.LENGTH_SHORT).show();
                TextView labelView = (TextView) findViewById(R.id.textOfQuestionView);
                int score = goodAnswers * 10;
                labelView.setText(getString(R.string.rate) + " " + Integer.toString(score) + " %");
                View fab = findViewById(R.id.multiple_actions);
                fab.setVisibility(View.VISIBLE);
                View button = findViewById(R.id.nextButtonView);
                button.setVisibility(View.GONE);
                button = findViewById(R.id.highlightButtonView);
                button.setVisibility(View.GONE);
                numberOfQuestion = 0;
                test.getQuestions().clear();
                goodAnswers = 0;
            }
        }
    }

    public void onHighlightClick(View v) {
        drawView.setMode(DrawView.Mode.INITIAL);
        drawView.setIsHighlighted(!drawView.isHighlighted());
        drawView.invalidate();
    }

    public void onCaptionClick(View view) {
        Toast.makeText(context, question.getCaption(), Toast.LENGTH_LONG).show();
    }

    public void goToMenu(View view) {
        onBackPressed();
        MainActivity.this.finish();
    }

    public void onTestClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("cookies", test.getService().getCookieString());
        intent.putExtra("categories", test.getCategories());
        startActivity(intent);
        MainActivity.this.finish();
    }

    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(getResources().getString(R.string.error));
        builder.setMessage(getResources().getString(R.string.errorMessage));

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBackPressed();
            }
        });

        return builder;
    }
}



