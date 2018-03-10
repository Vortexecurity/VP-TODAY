package vortex.vp_today.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.EditText;

import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;

import vortex.vp_today.R;

/**
 * @author Simon Dräger
 * @author Melvin Zähl
 * @version 3.3.18
 */

public class AboutActivity extends MaterialAboutActivity {
    /* Main TextView */
    private EditText txtV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.Theme_Mal_Light_DarkActionBar);
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_about);

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
        txtV.setFocusable(false);*/
    }

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull Context context) {
        MaterialAboutList list = new MaterialAboutList();

        MaterialAboutCard c1 = new MaterialAboutCard.Builder()
                .title("VP-TODAY")
                .titleColor(getResources().getColor(R.color.colorPrimaryDark))
                .addItem(new MaterialAboutActionItem.Builder()
                .text("VP-TODAY").build()).build();

        return new MaterialAboutList.Builder()
                .addCard(c1)
                .build();
    }

    @Nullable
    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.about);
    }

    public static void show(@NonNull Context context){
        Intent intent = new Intent(context, AboutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}