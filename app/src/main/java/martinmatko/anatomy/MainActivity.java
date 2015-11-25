package martinmatko.anatomy;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;


import android.app.Activity;
import android.os.StrictMode;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends Activity {

    private static Context context;

    public TextView gettextView() {
        return textView;
    }

    public void settextView(TextView t) {
        this.textView = t;
    }

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

      //  RESTService restService = new RESTService();
     //   restService.doInBackground("nothing");
        //drawView = new DrawView(MainActivity.this);

        setContentView(R.layout.activity_main);

        TextView captionView = (TextView) findViewById(R.id.captionView);
        captionView.setText("Přehled znalostí");
        TextView textView = (TextView) findViewById(R.id.textOfQuestionView);
        //View view = findViewById(R.id.textView);

        //textView = (TextView) findViewById(R.id.textView);

//        RelativeLayout ll = (RelativeLayout)findViewById(R.id.textView);
//
//        Button btn = new Button(this);
//        btn.setText("Manual Add");
//        btn.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
//        ll.addView(btn);
    }

    public static Context getAppContext() {
        return context;
    }



}



