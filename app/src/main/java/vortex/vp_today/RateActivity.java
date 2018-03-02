package vortex.vp_today;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

/**
 * @author Simon Dräger
 * @version 2.3.18
 */

public class RateActivity extends AppCompatActivity {
    private ProgressBar progBar;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        progBar = findViewById(R.id.progressBar);
        btnSend = findViewById(R.id.btnSend);

        progBar.setVisibility(View.INVISIBLE);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}