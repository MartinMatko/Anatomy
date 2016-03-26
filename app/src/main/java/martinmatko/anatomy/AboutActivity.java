package martinmatko.anatomy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bluejamesbond.text.DocumentView;
import com.bluejamesbond.text.style.TextAlignment;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        DocumentView documentView = new DocumentView(this, DocumentView.PLAIN_TEXT);  // Support plain text
        documentView.getDocumentLayoutParams().setTextAlignment(TextAlignment.JUSTIFIED);
        documentView.setText(getResources().getText(R.string.aboutustext));
    }
}
