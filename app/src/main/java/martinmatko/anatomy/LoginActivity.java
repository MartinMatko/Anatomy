package martinmatko.anatomy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import utils.Constants;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private UserLoginTask mAuthTask = null;
    private HTTPService service = new HTTPService();

    // UI references.
    private AutoCompleteTextView usernameView;
    private EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Bundle extras = getIntent().getExtras();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Set up the login form.
        usernameView = (AutoCompleteTextView) findViewById(R.id.email);

        passwordView = (EditText) findViewById(R.id.password);

        usernameView.setText(preferences.getString("username", ""));
        passwordView.setText(preferences.getString("password", ""));
        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        if (extras != null) {
            String value = extras.getString("cookies");
            service.setUpCookies(value);
            if (extras.getString("automaticLogin", "").equals("true")) {
                switch (preferences.getString("preferredLogin", "")) {
                    case "google":
                        onGoogleClicked(null);
                        break;
                    case "facebook":
                        onFacebookClicked(null);
                        break;
                    case "email":
                        attemptLogin();//need for sending post request with login data
                        break;
                }
            }
        }

    }

    public void onFacebookClicked(View v) {
        try {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            WebView myWebView = new WebView(this);
            WebViewClient webViewClient = new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.equals(Constants.SERVER_NAME + "overview/#_=_") || url.equals("https://anatom.cz/overview/#_=_")) {
                        CookieManager cookieManager = CookieManager.getInstance();
                        String cookiesString = cookieManager.getCookie(url);
                        String[] cookies = cookiesString.split("; ");
                        StringBuilder sb = new StringBuilder();
                        for (String cookie : cookies) {
                            if (cookie.startsWith("sessionid") || cookie.startsWith("csrftoken")) {
                                sb.append(cookie).append("; ");
                            }
                        }
                        sb.deleteCharAt(sb.length() - 1);
                        sb.deleteCharAt(sb.length() - 1);
                        LoginActivity host = (LoginActivity) view.getContext();
                        Intent intent = new Intent(view.getContext(), MenuActivity.class);
                        intent.putExtra("cookies", sb.toString());
                        host.startActivity(intent);
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(host);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("preferredLogin", "facebook");
                        editor.apply();
                        host.finish();
                        return true;
                    }
                    return false;
                }
            };
            myWebView.setWebViewClient(webViewClient);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (!preferences.getString("preferredLogin", "").equals("facebook")) {
                setContentView(myWebView);
            } else {
                setContentView(R.layout.splash);
            }
            myWebView.loadUrl(Constants.SERVER_NAME + "login/facebook/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onGoogleClicked(View v) {
        try {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            WebView myWebView = new WebView(this);
            WebViewClient webViewClient = new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.equals("https://anatom.cz/overview/#")) {
                        CookieManager cookieManager = CookieManager.getInstance();
                        String cookiesString = cookieManager.getCookie(url);
                        LoginActivity host = (LoginActivity) view.getContext();
                        Intent intent = new Intent(view.getContext(), MenuActivity.class);
                        intent.putExtra("cookies", cookiesString);
                        host.startActivity(intent);
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(host);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("preferredLogin", "google");
                        editor.apply();
                        host.finish();
                        return true;
                    }
                    return false;
                }
            };
            myWebView.setWebViewClient(webViewClient);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (!preferences.getString("preferredLogin", "").equals("google")) {
                setContentView(myWebView);
            } else {
                setContentView(R.layout.splash);
            }
            myWebView.loadUrl(Constants.SERVER_NAME + "login/google-oauth2/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void signUpClicked(View v) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("cookies", service.getCookieString());
        startActivity(intent);
        LoginActivity.this.finish();
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        usernameView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            try {
                JSONObject loginData = new JSONObject();
                loginData.put("username", username);
                loginData.put("password", password);
                int status = service.post(Constants.SERVER_NAME + "user/login/", loginData.toString());
                BufferedReader br;
                if (status == 200) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("username", username);
                    editor.putString("password", password);
                    editor.putString("preferredLogin", "email");
                    editor.apply();
                    Intent intent = new Intent(this, MenuActivity.class);
                    intent.putExtra("cookies", service.getCookieString());
                    startActivity(intent);
                    LoginActivity.this.finish();
                } else {
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

        private final String username;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            this.username = username;
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
                passwordView.setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}

