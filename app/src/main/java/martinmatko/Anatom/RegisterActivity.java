package martinmatko.Anatom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.BufferedReader;

import utils.Constants;

public class RegisterActivity extends AppCompatActivity {

    private HTTPService service = new HTTPService();

    // UI references.
    private AutoCompleteTextView usernameView;
    private AutoCompleteTextView emailView;
    private EditText passwordView;
    private EditText passwordAgainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("cookies");
            service.setUpCookies(value);
        }
        // Set up the login form.
        usernameView = (AutoCompleteTextView) findViewById(R.id.userName);
        emailView = (AutoCompleteTextView) findViewById(R.id.email);
        passwordView = (EditText) findViewById(R.id.password);
        passwordAgainView = (EditText) findViewById(R.id.passwordAgain);

        Button signInButton = (Button) findViewById(R.id.sign_up_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });
    }


    private void attemptRegistration() {
        usernameView.setError(null);
        passwordView.setError(null);
        emailView.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();
        String email = emailView.getText().toString();
        String passwordAgain = passwordAgainView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password, passwordAgain)) {
            focusView = passwordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        }
        if (!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            try {
                JSONObject registerData = new JSONObject();
                registerData.put("username", username);
                registerData.put("email", email);
                registerData.put("password", password);
                registerData.put("password_check", passwordAgain);
                int status = service.post(Constants.SERVER_NAME + "user/signup/", registerData.toString());
                //{"username":"MartinMatko1","email":"fakemail@sk.sk","password":"1234","password_check":"1234"}
                BufferedReader br;
                if (status == 201) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("username", username);
                    editor.putString("password", password);
                    editor.apply();
                    Intent intent = new Intent(this, MenuActivity.class);
                    intent.putExtra("cookies", service.getCookieString());
                    startActivity(intent);
                    RegisterActivity.this.finish();
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
        builder.setTitle(getResources().getString(R.string.registrationFailed));
        builder.setMessage(getResources().getString(R.string.registrationFailed));

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

    private boolean isPasswordValid(String password, String passwordAgain) {
        if (password.length() < 6) {
            passwordView.setError(getString(R.string.error_invalid_password));
            return false;
        }
        if (!password.equals(passwordAgain)) {
            passwordView.setError(getString(R.string.error_passwords_do_not_match));
            return false;
        }
        return true;
    }
}
