package vortex.vp_today.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.EditText;

import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import vortex.vp_today.BuildConfig;
import vortex.vp_today.R;
import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @author Melvin Zähl
 * @version 11.3.18
 */

public class AboutActivity extends MaterialAboutActivity {
    /* Main TextView */
    private EditText txtV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setup(this);
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
        MaterialAboutCard c1 = new MaterialAboutCard.Builder()
                .addItem(new MaterialAboutTitleItem.Builder()
                .text("VP-TODAY")
                .desc("© 2018 VorteX Dev. Corp.")
                .icon(R.mipmap.ic_launcher)
                .build())
                .titleColor(getResources().getColor(R.color.colorPrimaryDark))
                .addItem(new MaterialAboutActionItem.Builder()
                        .text("Version")
                        .subText(BuildConfig.buildVersion)
                        .icon(new IconicsDrawable(getApplicationContext())
                                .icon(CommunityMaterial.Icon.cmd_information_outline)
                                .sizeDp(18))
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                .text("Lizenz")
                .icon(new IconicsDrawable(getApplicationContext())
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .sizeDp(18))
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        Util.ShowLicenseDialog(AboutActivity.this);
                    }
                })
                .build())
                .build();

        MaterialAboutCard cAuthor = new MaterialAboutCard.Builder()
                .title("Entwickler")
                .addItem(new MaterialAboutTitleItem.Builder()
                .text("VorteX Dev. Corp.:")
                .desc("© 2018")
                .icon(R.mipmap.logo)
                .build())
                .addItem(new MaterialAboutActionItem.Builder()
                .text("Simon Dräger")
                .subText(R.string.simonemail)
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        Log.e("CLICK", "CLICK SIMON");
                    }
                })
                .build())
                .addItem(new MaterialAboutActionItem.Builder()
                .text("Melvin Zähl")
                .subText(R.string.melvinemail)
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {
                            @Override
                            public void onClick() {
                                Log.e("CLICK", "CLICK MELVIN");
                            }
                        })
                .build())
                .addItem(new MaterialAboutActionItem.Builder()
                .text("Florian Koll")
                .subText(R.string.florianemail)
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {
                            @Override
                            public void onClick() {
                                Log.e("CLICK", "CLICK FLORIAN");
                            }
                        })
                .build())
                .build();

        return new MaterialAboutList.Builder()
                .addCard(c1)
                .addCard(cAuthor)
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