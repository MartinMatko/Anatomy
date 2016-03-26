package martinmatko.anatomy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



        JSONObject userData = new HTTPService().get("https://staging.anatom.cz/user/profile/");
        try {
            userData = userData.getJSONObject("data").getJSONObject("user");
            String firstName = userData.getString("first_name");
            String surname = userData.getString("last_name");
            String email = userData.getString("email");
            TextView view = (TextView) findViewById(R.id.textViewFirstName);
            view.setText(firstName);
            view = (TextView) findViewById(R.id.textViewSurname);
            view.setText(surname);
            view = (TextView) findViewById(R.id.textViewEmail);
            view.setText(email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
