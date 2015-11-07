package martinmatko.anatomy;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Martin on 7.11.2015.
 */
public class RESTService extends AsyncTask<String, Void, String>{

    @Override
    protected String doInBackground(String... params) {
        //make connection
        URL url = null;
        try {
            url = new URL("http://staging.anatom.cz/flashcards/context/106");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String query = "hello Anatomy";
        URLConnection urlc = null;
        try {
            urlc = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get result
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(urlc
                    .getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String l = null;
        try {
            while ((l=br.readLine())!=null) {
                System.out.println(l);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //use post mode
        urlc.setDoOutput(true);
        urlc.setAllowUserInteraction(false);

        //send query
        PrintStream ps = null;
        try {
            ps = new PrintStream(urlc.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ps.print(query);
        ps.close();
        return "You are at PostExecute";
    }
}