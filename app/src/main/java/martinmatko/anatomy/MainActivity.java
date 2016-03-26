package martinmatko.anatomy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

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

        }
        setContentView(R.layout.activity_main);
        drawView = (DrawView) findViewById(R.id.drawView);
        test.start(systemCategories, bodyCategories);
        drawView.question = test.questions.get(numberOfQuestion);
        drawView.question.setD2T(true);
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
            String answer = test.postAnswer(question.getAnswer(), question.getCorrectAnswer(), question.isD2T());
            numberOfQuestion++;

            ServiceAsyncTask task = new ServiceAsyncTask(test);
            task.execute(answer);

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

}



