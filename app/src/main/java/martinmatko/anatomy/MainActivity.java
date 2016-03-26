package martinmatko.anatomy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.apache.http.cookie.Cookie;

import java.security.spec.ECField;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private static Context context;
    private boolean isUserSigned;
    private String cookies = "";
    private Menu menu;
    public Test test = new Test();
    public Question question;
    public int goodAnswers = 0, numberOfQuestion = 0;
    public ArrayList<String> systemCategories = new ArrayList();
    public ArrayList<String> bodyCategories = new ArrayList();
    DrawView drawView;
    private ViewFlipper viewFlipper;
    private int width;
    private View backgroundDimmer;
    private FloatingActionsMenu floatingActionsMenu;

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
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        long time0 = System.currentTimeMillis();
        long a, b, c;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = getApplicationContext();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        a = System.currentTimeMillis() - time0;
        //test.service.setUpCookies();

        if (!isNetworkStatusAvailable(getApplicationContext())) {
            b = System.currentTimeMillis() - a - time0;
            buildDialog(this).show();
        } else {
            b = System.currentTimeMillis() - a - time0;
            //Toast.makeText(getApplicationContext(), "internet is available", Toast.LENGTH_LONG).show();
        }
        c = System.currentTimeMillis() - b - a - time0;
         System.out.println("execute: " + a + "\ntoString: " + b + "\nJSONObject: " + c);
        super.onCreate(savedInstanceState);

        setCategoriesMenu(null);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("cookies");
            System.out.println("cookies" + value);
            cookies = value;
            test.service.setUpCookies(value);
            isUserSigned = true;
        }
    }

    public void setCategoriesMenu(View v) {
        try{
            setContentView(R.layout.categories_layout);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

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
            } else{
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

        //test.getNextQuestion(answer);
    }

    public void onFABClicked(View v){
        View backgroundDimmer = findViewById(R.id.background_dimmer);
        backgroundDimmer.setVisibility(View.VISIBLE);
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

    public void onExitClicked(View v) {
        finish();
        System.exit(0);
    }

    public void onCaptionClick(View view) {
        Toast.makeText(context, question.getCaption(), Toast.LENGTH_LONG).show();
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        if (!cookies.isEmpty()){
            MenuItem menuItem = menu.findItem(R.id.sign);
            menuItem.setTitle(R.string.signout);
            menuItem = menu.findItem(R.id.profile);
            menuItem.setVisible(true);
        }
        invalidateOptionsMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent = new Intent(this, MainActivity.class);
        switch (id){
            case R.id.about:
                intent = new Intent(this,AboutActivity.class);
                break;
            case R.id.language:
                intent = new Intent(this,LanguageActivity.class);
                break;
            case R.id.profile:
                intent = new Intent(this,ProfileActivity.class);
                break;
            case R.id.sign:
                if (isUserSigned){
                    new HTTPService().get("https://staging.anatom.cz/user/logout/");
                    isUserSigned = false;
                }
                else {
                    intent = new Intent(this,LoginActivity.class);
                }
                break;
            default:
                intent = new Intent(this, MainActivity.class);
                break;
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int tabNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView;
            if (tabNumber == 1){
                rootView = inflater.inflate(R.layout.organsystems, container, false);
            }
            else {

                rootView = inflater.inflate(R.layout.bodyorgans, container, false);
            }

            return rootView;
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.organSystems);
                case 1:
                    return getResources().getString(R.string.bodyParts);
            }
            return null;
        }
    }
}



