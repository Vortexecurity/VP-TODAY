package vortex.vp_today.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.widget.TextView;

import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.vplib.vortex.vplib.Util;

import vortex.vp_today.BuildConfig;
import vortex.vp_today.R;

/**
 * @author Simon Dräger
 * @author Melvin Zähl
 * @version 11.3.18
 */

public class AboutActivity extends MaterialAboutActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                        .subText("Build: " + BuildConfig.buildVersion)
                        .icon(new IconicsDrawable(getApplicationContext())
                                .icon(CommunityMaterial.Icon.cmd_information_outline)
                                .sizeDp(18))
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                .text("Lizenz")
                .icon(new IconicsDrawable(getApplicationContext())
                        .icon(CommunityMaterial.Icon.cmd_comment_text)
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
                .icon(R.mipmap.logo)
                .build())
                .addItem(new MaterialAboutActionItem.Builder()
                .text("Simon Dräger")
                .subText("Head-Admin | Developer | " + getString(R.string.simonemail))
                .setOnClickAction(new MaterialAboutItemOnClickAction() {
                    @Override
                    public void onClick() {
                        greetDialog("Simon", "Florian ist der Kapitän aufm Schiff", "Ay ay");
                    }
                })
                .build())
                .addItem(new MaterialAboutActionItem.Builder()
                .text("Melvin Zähl")
                .subText("Head-Admin | Developer | " + getString(R.string.melvinemail))
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {
                            @Override
                            public void onClick() {
                                greetDialog("Melvin", "Bei mir zählt jede Sekunde", "Hihi.");
                            }
                        })
                .build())
                .addItem(new MaterialAboutActionItem.Builder()
                .text("Florian Koll")
                .subText("Kundensupport | Projektleitung | Bug-Hunter | " + getString(R.string.florianemail))
                        .setOnClickAction(new MaterialAboutItemOnClickAction() {
                            @Override
                            public void onClick() {
                                greetDialog("Florian", "Ich bin toll und heiße Koll", "Ja so und nicht anders!");
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

    private void greetDialog(String name, String text, String btnText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
        builder.setTitle("Gruß vom " + name);

        TextView myMsg = new TextView(AboutActivity.this);
        myMsg.setText(text);
        myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(myMsg);

        builder.setPositiveButton(btnText, null);

        builder.show();
    }

    public static void show(@NonNull Context context){
        Intent intent = new Intent(context, AboutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
