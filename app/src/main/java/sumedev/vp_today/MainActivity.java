package sumedev.vp_today;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
            }
        });

        Button btnKurse = findViewById(R.id.btnKurse);
        btnKurse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("[LOG]", "Kurse clicked");
            }
        });
    }

    private void update(){
        Log.e("[LOG]", "Update Clicked");
    }
}
