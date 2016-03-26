package martinmatko.anatomy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;

import utils.Constants;

public class MenuActivity extends AppCompatActivity {

    private static Context context;
    public Test test = new Test();
    public Question question;
    public ArrayList<String> systemCategories = new ArrayList();
    public ArrayList<String> bodyCategories = new ArrayList();
    DrawView drawView;
    private boolean isUserSigned;
    private String cookies = "";
    private SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

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
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        context = getApplicationContext();

        if (!isNetworkStatusAvailable(getApplicationContext())) {
            buildDialog(this).show();
        }
        super.onCreate(savedInstanceState);

        setCategoriesMenu();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("cookies");
            System.out.println("cookies" + value);
            cookies = value;
            test.service.setUpCookies(value);
            isUserSigned = true;
        }
    }

    public void setCategoriesMenu() {
        try {
            setContentView(R.layout.activity_menu);
        } catch (Exception e) {
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
    public void onTestClicked(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
        if (!cookies.isEmpty()) {
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
        Intent intent = new Intent(this, MenuActivity.class);
        switch (id) {
            case R.id.about:
                intent = new Intent(this, AboutActivity.class);
                break;
            case R.id.language:
                intent = new Intent(this, LanguageActivity.class);
                break;
            case R.id.profile:
                intent = new Intent(this, ProfileActivity.class);
                break;
            case R.id.sign:
                if (isUserSigned) {
                    new HTTPService().get(Constants.SERVER_NAME + "user/logout/");
                    isUserSigned = false;
                } else {
                    intent = new Intent(this, LoginActivity.class);
                }
                break;
            default:
                intent = new Intent(this, MenuActivity.class);
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
            if (tabNumber == 1) {
                rootView = inflater.inflate(R.layout.organsystems, container, false);
            } else {

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