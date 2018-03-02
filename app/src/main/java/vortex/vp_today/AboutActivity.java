package vortex.vp_today;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

/**
 * @author Simon Dräger
 */

public class AboutActivity extends AppCompatActivity {

    /* Main TextView */
    private EditText txtV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        txtV = findViewById(R.id.txtAbout);

        //txtV.setText(getString(R.string.abouttxt));
    }
}