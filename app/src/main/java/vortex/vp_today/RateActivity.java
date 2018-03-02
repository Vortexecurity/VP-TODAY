package vortex.vp_today;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

/**
 * @author Simon Dräger
 * @version 2.3.18
 */

public class RateActivity extends AppCompatActivity {
    private ProgressBar progBar;
    private Button btnSend;
    private EditText txtSuggest;
    private RatingBar rateBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        progBar = findViewById(R.id.progressBar);
        btnSend = findViewById(R.id.btnSend);
        txtSuggest = findViewById(R.id.txtSuggest);
        rateBar = findViewById(R.id.rateBar);

        progBar.setVisibility(View.INVISIBLE);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: check if valid
                if (txtSuggest.getText().toString().trim().equals("")) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Bitte geben Sie einen Bericht ein!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    if (Util.isInternetConnected(getApplicationContext())) {
                        Intent in = Util.sendEmail(getString(R.string.simonemail),
                                getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getString("clientid", "0x0"),
                                "Sendete ein Rating von " + rateBar.getNumStars() + " Sternen.");
                        RateActivity.this.startActivity(in);
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Feedback erfolgreich gesendet!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Es besteht keine Internetverbindung.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }
}