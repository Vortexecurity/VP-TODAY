package vortex.vp_today.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import vortex.vp_today.R;
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
                    btnKurse.setEnabled(true);
                } else {
                    spinKlassen.setEnabled(true);
                    btnKurse.setEnabled(false);
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
                final Resources res = getApplicationContext().getResources();
                final int q1Len = res.getStringArray(R.array.KurseQ1).length;
                final ArrayList<String> selectedItems = new ArrayList<>(q1Len);
                final ArrayList<Boolean> boolSelectedItems = new ArrayList<>(q1Len);
                final String[] items = res.getStringArray(R.array.KurseQ1);

                Tuple<String[], ArrayList<Boolean>> tupSelects = Util.getGsonObject(getString(R.string.settingkurse), Tuple.class);

                if (tupSelects != null) {
                    Log.i("btnKurseClick", "tupSelects not null");
                    Log.i("btnKurseClick", "tupleSelects type: " + tupSelects.getClass().toString());
                    //Log.i("btnKurseClick", "tupSelects.y type: " + tupSelects.y.getClass().toString());
                    selects = Util.BoolToTypeBool(tupSelects.y.toArray(new Boolean[0]));

                    Log.i("btnKurseClick", "tupSelects.y lenght " + tupSelects.y.toArray(new Boolean[0]).length);
                    for (int i = 0; i < q1Len; i++) {
                        selects[i] = tupSelects.y.get(i);
                        selectedItems.add(items[i]);
                        boolSelectedItems.add(selects[i]);
                        Log.i("btnKurseClick", "i = " + i);
                    }
                } else {
                    Log.i("btnKurseClick", "tupSelects is null");
                    selects = new boolean[q1Len];

                    for (int i = 0; i < q1Len; i++)
                        selectedItems.add("");

                    for (int i = 0; i < q1Len; i++) {
                        //selects[i] = false;
                        boolSelectedItems.add(false);
                    }
                }

                Util.ShowKurseDialogQ1(selects, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {
                            selectedItems.set(indexSelected, items[indexSelected]);
                            boolSelectedItems.set(indexSelected, true);
                        } else {
                            selectedItems.remove(items[indexSelected]);
                            boolSelectedItems.set(indexSelected, false);
                        }
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentKurseChanges = new Tuple<>(selectedItems.toArray(new String[0]), boolSelectedItems.toArray(new Boolean[0]));
                        hasChanged();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });

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

        ArrayAdapter<String> stufenAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Util.getStufen()
        );

        ArrayAdapter<String> klassenAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                Util.getKlassen()
        );

        stufenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        klassenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinStufen.setAdapter(stufenAdapter);
        spinKlassen.setAdapter(klassenAdapter);

        Util.setup(this);
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
            Util.putGsonObject(getString(R.string.settingkurse), currentKurseChanges);
        }

        if (e.commit())
            Toasty.success(getApplicationContext(), "Einstellungen gesichert!").show();
        else
            Toasty.error(getApplicationContext(), "Speichern fehlgeschlagen!").show();
    }

    private synchronized void load() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);

        /* Spinner Stufen setting */
        String stufe = Util.getSettingStufe();

        switch (stufe) {
            case "EF":
                spinStufen.setSelection(5);
                btnKurse.setEnabled(true);
                break;
            case "Q1":
                spinStufen.setSelection(6);
                btnKurse.setEnabled(true);
                break;
            case "Q2":
                spinStufen.setSelection(7);
                btnKurse.setEnabled(true);
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
        String klasse = Util.getSettingKlasse();

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
                                    Util.ShowYesNoDialog("Möchten Sie die ungesicherten Änderungen speichern?",
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
