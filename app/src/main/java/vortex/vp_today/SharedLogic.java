package vortex.vp_today;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Simon Dräger
 */

public final class SharedLogic {
    private static List<String> lstStufen;

    static {
        lstStufen = new ArrayList<>();
        if (lstStufen.isEmpty()) {
            lstStufen.addAll(Arrays.asList("5", "6", "7", "8", "9", "EF", "Q1", "Q2"));
        }
    }

    /**
     * May return null, no @link NullPointerException (@link Nullable)
     * @return Die Stufenliste
     */
    @Nullable
    public static List<String> getStufen() {
        return lstStufen;
    }
}