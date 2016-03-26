package martinmatko.anatomy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import utils.Constants;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEmailView.setText(preferences.getString("username", ""));
        mPasswordView.setText(preferences.getString("password", ""));

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    public void onFacebookClicked(View v) {
        loginWithSocialNetwork(Constants.SERVER_NAME + "login/facebook/");
    }

    public void onGoogleClicked(View v) {
        loginWithSocialNetwork(Constants.SERVER_NAME + "login/google-oauth2/");
    }

    public void signUpClicked(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void loginWithSocialNetwork(String url) {
        try {
            WebView myWebView = new WebView(this);
            WebSettings webSettings = myWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setBuiltInZoomControls(true);
            myWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.equals(Constants.SERVER_NAME + "overview/#_=_")) {
                        return true;
                    }
                    return false;
                }

            });
            setContentView(myWebView);
            myWebView.loadUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String cookies = CookieManager.getInstance().getCookie(url);
        System.out.println("///////////////" + cookies);

        try {
            WebView myWebView = new WebView(this);
            WebSettings webSettings = myWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setBuiltInZoomControls(true);
            JSONObject userData;
            myWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    //finish();
                    String url1 = view.getUrl();
                    if (url.equals(Constants.SERVER_NAME + "user/profile/")) {
                        JSONObject userData = new HTTPService().get(Constants.SERVER_NAME + "user/profile/");
                        System.out.println("///////////////" + userData.toString());
                        return true;
                    }
                    return false;
                }

            });
            setContentView(myWebView);
            myWebView.loadUrl(Constants.SERVER_NAME + "user/profile/");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("cookies", cookies);
        //startActivity(intent);
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
//        else if (!isEmailValid(email)) {
//            mEmailView.setError(getString(R.string.error_invalid_email));
//            focusView = mEmailView;
//            cancel = true;
//        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            try {
                URL url = new URL(Constants.SERVER_NAME + "user/session/");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.getHeaderFields();
                List<String> cookies = conn.getHeaderFields().get("Set-Cookie");
                String[] token = cookies.get(0).split("=");
                conn.disconnect();
                JSONObject loginData = new JSONObject();
                loginData.put("username", email);
                loginData.put("password", password);
                String loginDataString = "{" + "username: \"" + email.toString() + "\", password: \"" + password.toString() + "\"}";
                url = new URL(Constants.SERVER_NAME + "user/login/");
                conn = (HttpsURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setChunkedStreamingMode(0);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Cookie", "activeType=system; csrftoken=wm25pmh5Co4QjVKKUHrc4W9dXz4v6WMB; activeType=system; sessionid=9qi3bv4ct3r9p4zxy43ebxmpj743o6t5; " + "csrftoken=" + token[1].split(";")[0]);
                conn.setRequestProperty("X-csrftoken", token[1].split(";")[0]);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(loginData.toString());
                writer.flush();
                writer.close();
                int status = conn.getResponseCode();
                BufferedReader br;
                if (status == 200) {
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    String response = "";
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                    br.close();

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("username", email);
                    editor.putString("password", password);
                    editor.apply();
                    Intent intent = new Intent(this, MenuActivity.class);
                    intent.putExtra("cookies", conn.getHeaderFields().get("Set-Cookie").toString());
                    startActivity(intent);
                } else {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String line;
                    String response = "";
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                    br.close();
                    buildDialog(this).show();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(getResources().getString(R.string.failedLogin));
        builder.setMessage(getResources().getString(R.string.failedLoginText));

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                URL url = new URL(Constants.SERVER_NAME + "user/login/");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setChunkedStreamingMode(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}

