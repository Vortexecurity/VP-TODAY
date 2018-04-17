package vortex.vp_today.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import com.google.gson.reflect.TypeToken;
import com.vplib.vortex.vplib.Tuple;
import com.vplib.vortex.vplib.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vortex.vp_today.AppCompatPreferenceActivity;
import vortex.vp_today.R;
import vortex.vp_today.net.RetrieveKurseFromServer;

public class SettActivity extends AppCompatPreferenceActivity {
    private SharedPreferences prefs;

    public static void show(@NonNull Context context) {
        Intent intent = new Intent(context, SettActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || UpdatePreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        MultiSelectListPreference mKurse;
        ListPreference lStufe;
        ListPreference lKlasse;
        private Tuple<String[], Boolean[]> currentKurseChanges = null;
        private SharedPreferences prefs;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            prefs = getActivity().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_general);
            setHasOptionsMenu(true);

            mKurse = (MultiSelectListPreference) findPreference("slctKurse");
            lStufe = (ListPreference) findPreference("slctStufe");
            lKlasse = (ListPreference) findPreference("slctKlasse");

            if (mKurse != null) {
                try {
                    final String[] kurse = new RetrieveKurseFromServer().execute().get();
                    Log.i("kurse", "kurse: null " + (kurse == null));
                    mKurse.setEntries(kurse);
                    mKurse.setEntryValues(kurse);
                    mKurse.setSummary(Arrays.asList(mKurse.getValues()).toString());
                    mKurse.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            MultiSelectListPreference newPref = (MultiSelectListPreference) preference;
                            if (newPref != null) {
                                HashSet<String> valSet = (HashSet<String>) newValue;
                                Boolean[] boolSelects = new Boolean[kurse.length];

                                for (int i = 0; i < kurse.length; i++) {
                                    if (valSet.contains(kurse[i])) {
                                        boolSelects[i] = true;
                                    } else {
                                        boolSelects[i] = false;
                                    }
                                }

                                currentKurseChanges = new Tuple<>(valSet.toArray(new String[0]), boolSelects);

                                Tuple<String[], Boolean[]> tup = new Tuple(valSet.toArray(new String[0]), boolSelects);

                                Util.putGsonObject(getActivity().getApplicationContext(), getString(R.string.settingkurse), tup, new TypeToken<Tuple<String[], Boolean[]>>() {});

                                if (Util.D) Log.i("onPrefChange", "Gson obj y size: " +
                                        ((Tuple<String[], ArrayList<Boolean>>)Util.getGsonObject(getActivity().getApplicationContext(), getString(R.string.settingkurse), Tuple.class)).y.size());

                                newPref.setSummary(newValue.toString());
                                newPref.setValues((Set<String>) newValue);
                                return true;
                            }

                            return false;
                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (lStufe != null) {
                String sVal = lStufe.getValue();
                if (sVal != null)
                    lStufe.setSummary(sVal);
                lStufe.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        ListPreference newPref = (ListPreference) preference;
                        String strVal = newValue.toString();
                        if (newPref != null) {
                            newPref.setSummary(strVal);
                            newPref.setValue(strVal);

                            prefs.edit().putString(getString(R.string.settingstufe), strVal).apply();

                            if (strVal.equals("EF") || strVal.equals("Q1") || strVal.equals("Q2")) {
                                lKlasse.setEnabled(false);
                                mKurse.setEnabled(true);
                            } else {
                                lKlasse.setEnabled(true);
                                mKurse.setEnabled(false);
                            }

                            return true;
                        }

                        return false;
                    }
                });
            }

            if (lKlasse != null) {
                String kVal = lKlasse.getValue();
                if (kVal != null)
                    lKlasse.setSummary(kVal);
                lKlasse.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        ListPreference newPref = (ListPreference) preference;
                        String strVal = newValue.toString();

                        if (newPref != null) {
                            prefs.edit().putString(getString(R.string.settingklasse), strVal).apply();

                            newPref.setSummary(strVal);
                            newPref.setValue(strVal);
                            return true;
                        }

                        return false;
                    }
                });
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        SwitchPreference sReceivePushes;
        SwitchPreference sVibrate;
        private SharedPreferences prefs;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            prefs = getActivity().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_notifications);
            setHasOptionsMenu(true);

            sReceivePushes = (SwitchPreference) findPreference("receiveNotifs");
            sVibrate = (SwitchPreference) findPreference("vibrateOnNotifReceive");

            sReceivePushes.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SwitchPreference newPref = (SwitchPreference) preference;
                    boolean bVal = (boolean) newValue;
                    if (newPref != null) {
                        newPref.setSummary(newPref.toString());

                        prefs.edit().putBoolean(getString(R.string.settingpushes), bVal).apply();

                        sVibrate.setEnabled(bVal);

                        return true;
                    }
                    return false;
                }
            });

            sVibrate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SwitchPreference newPref = (SwitchPreference) preference;
                    boolean bVal = (boolean) newValue;
                    if (newPref != null) {
                        prefs.edit().putBoolean(getString(R.string.settingvibrateLS), bVal).apply();

                        newPref.setSummary(newPref.toString());
                        newPref.setEnabled(bVal);
                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class UpdatePreferenceFragment extends PreferenceFragment {
        ListPreference lInterval;
        private SharedPreferences prefs;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            prefs = getActivity().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_update);
            setHasOptionsMenu(true);

            lInterval = (ListPreference) findPreference("update_frequency");

            if (lInterval != null) {
                // TODO: cleanup
                lInterval.setDefaultValue(getResources().getStringArray(R.array.listIntervalValuesMin)[2]);
                //lInterval.setSummary(getResources().getStringArray(R.array.listIntervals)[lInterval.findIndexOfValue(lInterval.getValue())]);
                lInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (preference != null) {
                            ListPreference newPref = (ListPreference) preference;
                            int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());

                            prefs.edit().putInt(getString(R.string.settingRefreshIntervalMin),
                                    Integer.parseInt(getResources().getStringArray(R.array.listIntervalValuesMin)[index]))
                                    .apply();

                            newPref.setSummary(getResources().getStringArray(R.array.listIntervals)[index]);
                            newPref.setValue(newValue.toString());
                            return true;
                        }
                        return false;
                    }
                });
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
