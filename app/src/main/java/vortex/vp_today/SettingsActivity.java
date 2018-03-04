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

    private Spinner spinStufen;
    private Spinner spinKlassen;
    private Button btnApply;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        spinStufen = findViewById(R.id.spinStufen);
        spinKlassen = findViewById(R.id.spinKlasse);
        btnApply = findViewById(R.id.btnApply);
        btnCancel = findViewById(R.id.btnCancel);

        spinStufen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hasChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinStufen.setSelection(0);
                hasChanged();
            }
        });

        spinKlassen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hasChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinKlassen.setSelection(0);
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

        ArrayAdapter<String> stufenAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                SharedLogic.getStufen()
        );

        ArrayAdapter<String> klassenAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                SharedLogic.getKlassen()
        );

        stufenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        klassenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinStufen.setAdapter(stufenAdapter);
        spinKlassen.setAdapter(klassenAdapter);

        load();
    }

    public static void show(@NonNull Context context){
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    private synchronized void save() {
        SharedPreferences settings = getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = settings.edit();

        e.putString("stufe", spinStufen.getSelectedItem().toString());
        e.putString("klasse", spinKlassen.getSelectedItem().toString());

        if (e.commit())
            Toast.makeText(getApplicationContext(), "Einstellungen gesichert!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), "Speichern fehlgeschlagen!", Toast.LENGTH_SHORT).show();
    }

    private synchronized void load() {
        /* Spinner Stufen setting */
        String stufe = Util.getSettingStufe(getApplicationContext());

        switch (stufe) {
            case "EF":
                spinStufen.setSelection(5);
                break;
            case "Q1":
                spinStufen.setSelection(6);
                break;
            case "Q2":
                spinStufen.setSelection(7);
                break;
            default:
                if (stufe == null || stufe.equals("")) {
                    spinStufen.setSelection(0);
                    break;
                }
                int i = Integer.parseInt(stufe);
                spinStufen.setSelection(i - 5);
                break;
        }

        /* Spinner Klassen setting */
        String klasse = Util.getSettingKlasse(getApplicationContext());

        switch (klasse) {
            case "A":
                spinKlassen.setSelection(0);
                break;
            case "B":
                spinKlassen.setSelection(1);
                break;
            case "C":
                spinKlassen.setSelection(2);
                break;
            case "D":
                spinKlassen.setSelection(3);
                break;
            default:
                spinKlassen.setSelection(0);
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