package martinmatko.anatomy;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Locale;

import utils.Constants;

public class LanguageActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        RadioGroup options = (RadioGroup) findViewById(R.id.languages);
        options.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        RadioButton checked = (RadioButton) findViewById(checkedId);
        checked.setBackgroundColor(getResources().getColor(R.color.grey_500));
        switch (checked.getId()) {
            case R.id.czech:
                Configuration config = new Configuration();
                config.locale = new Locale("cs", "CZ");
                getBaseContext().getResources().updateConfiguration(config,
                        getBaseContext().getResources().getDisplayMetrics());
                Constants.SERVER_NAME = "https://staging.anatom.cz/";
                break;
            case R.id.english:
                Configuration config1 = new Configuration();
                config1.locale = new Locale("en", "US");
                getBaseContext().getResources().updateConfiguration(config1,
                        getBaseContext().getResources().getDisplayMetrics());
                Constants.SERVER_NAME = "https://staging.practiceanatomy.com/";
                break;
        }
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }
}
