package martinmatko.anatomy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String firstName = "";
        String surname = "";
        String email = "";
        String userName = "";
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            firstName = extras.getString("first_name");
            surname = extras.getString("last_name");
            email = extras.getString("email");
            userName = extras.getString("username");
        }
        if (!firstName.isEmpty()) {
            TextView view = (TextView) findViewById(R.id.textViewFirstName);
            view.setText(firstName);
            view = (TextView) findViewById(R.id.textViewSurname);
            view.setText(surname);
            View labelView = findViewById(R.id.firstName);
            labelView.setVisibility(View.VISIBLE);
            labelView = findViewById(R.id.surname);
            labelView.setVisibility(View.VISIBLE);
        }
        TextView view = (TextView) findViewById(R.id.textViewEmail);
        view.setText(email);
        view = (TextView) findViewById(R.id.textViewUserName);
        view.setText(userName);
    }

}
