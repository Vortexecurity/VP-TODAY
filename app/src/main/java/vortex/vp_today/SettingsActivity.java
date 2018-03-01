package vortex.vp_today;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private boolean changed = false;

    Spinner spin;
    Button btnApply;
    Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        spin = findViewById(R.id.spinStufen);
        btnApply = findViewById(R.id.btnApply);
        btnCancel = findViewById(R.id.btnCancel);

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hasChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spin.setSelection(0);
                hasChanged();
            }
        });

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                onBackPressed();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                SharedLogic.getStufen()
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = findViewById(R.id.spinStufen);
        sItems.setAdapter(adapter);
        load();
    }

    private void save() {
        SharedPreferences settings = getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);
        if(settings.edit().putString("stufe", spin.getSelectedItem().toString()).commit())
            Toast.makeText(getApplicationContext(), "Einstellungen gesichert!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), "Speichern fehlgeschlagen!", Toast.LENGTH_SHORT).show();
    }

    private void load(){
        String s = getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getString("stufe", "");

        switch (s) {
            case "EF":
                spin.setSelection(5);
                break;
            case "Q1":
                spin.setSelection(6);
                break;
            case "Q2":
                spin.setSelection(7);
                break;
            default:
                if (s == null || s == ""){
                    spin.setSelection(0);
                    break;
                }
                int i = Integer.parseInt(s);
                spin.setSelection(i - 5);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        DoDialog();
        super.onBackPressed();
    }

    private void DoDialog() {
        if (changed) {
            if (Util.ShowYesNoDialog(getApplicationContext(), "Möchten Sie die ungespeicherten Änderungen speichern?") == DialogInterface.BUTTON_POSITIVE) {
                save();
            }
        }
    }

    private void hasChanged() {
        changed = true;
    }
}