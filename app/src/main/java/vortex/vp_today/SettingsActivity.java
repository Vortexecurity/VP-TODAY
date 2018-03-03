package vortex.vp_today;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * @author Melvin Zähl
 * @author Simon Dräger
 * @version 3.3.18
 */

public class SettingsActivity extends AppCompatActivity {
    private boolean changed = false;
    private static final Object lockObj = new Object();
    private volatile boolean _continue = false;

    Spinner spin;
    Button btnApply;
    Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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

        btnApply.setBackgroundColor(Color.GREEN);

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                NavUtils.navigateUpFromSameTask(SettingsActivity.this);
            }
        });

        btnCancel.setBackgroundColor(Color.GREEN);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(SettingsActivity.this);
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

    public static void show(@NonNull Context context){
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    private synchronized void save() {
        SharedPreferences settings = getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);
        if(settings.edit().putString("stufe", spin.getSelectedItem().toString()).commit())
            Toast.makeText(getApplicationContext(), "Einstellungen gesichert!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), "Speichern fehlgeschlagen!", Toast.LENGTH_SHORT).show();
    }

    private synchronized void load() {
        /* Spinner setting */
        String s = Util.getSettingStufe(this);

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
        switch ( item.getItemId() ) {
            case android.R.id.home:
                try {
                    synchronized (lockObj) {
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                DoDialog();
                                SettingsActivity.this._continue = true;
                            }
                        };
                        new Handler().post(r);

                        r.wait();

                        NavUtils.navigateUpFromSameTask(this);
                    }
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }

        return super.onOptionsItemSelected(item);
    }

    private void DoDialog() {
        if (changed) {
            if (Util.ShowYesNoDialog(SettingsActivity.this, "Möchten Sie die ungespeicherten Änderungen speichern?") == DialogInterface.BUTTON_POSITIVE) {
                save();
            }
        }
    }

    private void hasChanged() {
        changed = true;
    }
}