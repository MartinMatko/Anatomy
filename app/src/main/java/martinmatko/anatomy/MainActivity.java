package martinmatko.anatomy;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;


import android.app.Activity;
import android.os.StrictMode;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends Activity {

    private static Context context;
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

      //  RESTService restService = new RESTService();
     //   restService.doInBackground("nothing");
        try {
            drawView = new DrawView(MainActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setContentView(drawView);
    }

    public static Context getAppContext() {
        return context;
    }



}



