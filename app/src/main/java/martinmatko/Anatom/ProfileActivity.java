package martinmatko.Anatom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        //MenuActivity.this.finish();
        return super.onOptionsItemSelected(item);
    }

}
