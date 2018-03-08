package vortex.vp_today.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import vortex.vp_today.R;
import vortex.vp_today.logic.SharedLogic;
import vortex.vp_today.util.Tuple;
import vortex.vp_today.util.Util;

/**
 * @author Melvin Zähl
 * @author Simon Dräger
 * @version 5.3.18
 */

public class SettingsActivity extends AppCompatActivity {
    private boolean changed = false;
    private static final Object lockObj = new Object();
    private volatile boolean _continue = false;

    private Spinner spinStufen;
    private Spinner spinKlassen;
    private Button btnApply;
    private Button btnCancel;
    private Button btnKurse;
    private Switch switchVibrate;
    private Switch switchPushes;

    private Tuple<String[], Boolean[]> currentKurseChanges = null;

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
        btnKurse = findViewById(R.id.btnKurse);
        switchPushes = findViewById(R.id.switchPushes);
        switchVibrate = findViewById(R.id.switchVibrate);

        spinStufen.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = spinStufen.getSelectedItem().toString();

                if (item.equals("EF") || item.equals("Q1") || item.equals("Q2")) {
                    spinKlassen.setEnabled(false);
                } else {
                    spinKlassen.setEnabled(true);
                }

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

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                NavUtils.navigateUpFromSameTask(SettingsActivity.this);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(SettingsActivity.this);
            }
        });

        btnKurse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean[] selects = null;

                // FIXME: crasht beim laden

                Tuple<String[], Boolean[]> tupSelects = (Tuple<String[], Boolean[]>) Util.getGsonObject(getApplicationContext(), getString(R.string.settingkurse));

                if (tupSelects != null)
                    selects = Util.BoolToTypeBool(tupSelects.y);

                currentKurseChanges = Util.ShowKurseDialogQ1(SettingsActivity.this, selects);
                hasChanged();
            }
        });

        switchPushes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchVibrate.setEnabled(isChecked);
                hasChanged();
            }
        });

        switchVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hasChanged();
            }
        });

        ArrayAdapter<String> stufenAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                SharedLogic.getStufen()
        );

        ArrayAdapter<String> klassenAdapter = new ArrayAdapter<String>(
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

    public static void show(@NonNull Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private synchronized void save() {
        SharedPreferences settings = getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = settings.edit();

        String selStufe = spinStufen.getSelectedItem().toString();
        String selKlasse = spinKlassen.isEnabled() ? spinKlassen.getSelectedItem().toString() : null;

        e.putString(getString(R.string.settingstufe), selStufe);

        if (selKlasse != null)
            e.putString(getString(R.string.settingklasse), spinKlassen.getSelectedItem().toString());
        else
            e.remove(getString(R.string.settingklasse));

        e.putBoolean(getString(R.string.settingpushes), switchPushes.isChecked());
        e.putBoolean(getString(R.string.settingvibrateLS), switchVibrate.isChecked());

        if (currentKurseChanges != null) {
            Util.putGsonObject(getApplicationContext(), getString(R.string.settingkurse), currentKurseChanges);
        }

        if (e.commit())
            Toast.makeText(getApplicationContext(), "Einstellungen gesichert!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), "Speichern fehlgeschlagen!", Toast.LENGTH_SHORT).show();
    }

    private synchronized void load() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);

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

        /* Switch push notifications setting */
        switchPushes.setChecked(
                prefs.getBoolean(getString(R.string.settingpushes), getResources().getBoolean(R.bool.switchPushesEnabled))
        );

        /* Switch vibrate setting */
        switchVibrate.setChecked(
                prefs.getBoolean(getString(R.string.settingvibrateLS), getResources().getBoolean(R.bool.switchPushVibrateEnabled))
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                try {
                    synchronized (lockObj) {
                        if (changed) {
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    Util.ShowYesNoDialog(SettingsActivity.this, "Möchten Sie die ungesicherten Änderungen speichern?",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    save();
                                                    goBack();
                                                }
                                            },
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    goBack();
                                                }
                                            });
                                }
                            });
                        }
                    }
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        }

        return super.onOptionsItemSelected(item);
    }

    private synchronized void goBack() {
        NavUtils.navigateUpFromSameTask(this);
    }

    private synchronized void hasChanged() {
        changed = true;
    }
}
