package vortex.vp_today.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import vortex.vp_today.BuildConfig;
import vortex.vp_today.R;

/**
 * @author Simon Dräger
 * @version 3.3.18
 */

public class AboutActivity extends AppCompatActivity {

    /* Main TextView */
    private EditText txtV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        txtV = findViewById(R.id.txtAbout);

        int buildnr = 0;

        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            buildnr = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        txtV.setText(String.format(getString(R.string.abouttxt), buildnr, BuildConfig.buildTime.toString(),
                getString(R.string.melvinemail),
                getString(R.string.simonemail),
                getString(R.string.florianemail)
        ));
        txtV.setFocusable(false);
    }

    public static void show(@NonNull Context context){
        Intent intent = new Intent(context, AboutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}