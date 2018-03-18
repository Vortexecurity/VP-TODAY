package vortex.vp_today.logic;

import android.widget.TextView;

/**
 * @author Simon Dräger
 * @version 18.3.18
 */

public class VPCardItem {
    private final TextView stundeWithArt;
    private final TextView subText;

    public VPCardItem(TextView stundeWithArt, TextView subText) {
        this.stundeWithArt = stundeWithArt;
        this.subText = subText;
    }

    public TextView getStundeWithArt() {
        return stundeWithArt;
    }

    public TextView getSubText() {
        return subText;
    }
}
