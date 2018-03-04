package vortex.vp_today;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Simon Dräger
 */

public final class SharedLogic {
    private static List<String> lstStufen;
    private static List<String> lstKlassen;

    static {
        lstStufen = new ArrayList<>();
        lstKlassen = new ArrayList<>();

        if (lstStufen.isEmpty()) {
            lstStufen.addAll(Arrays.asList("5", "6", "7", "8", "9", "EF", "Q1", "Q2"));
        }

        if (lstKlassen.isEmpty()) {
            lstKlassen.addAll(Arrays.asList("A", "B", "C", "D"));
        }
    }

    /**
     * @return Die Stufenliste
     */
    @NonNull
    public static String[] getStufen() {
        return lstStufen.toArray(new String[0]);
    }

    /**
     * @return Die Klassenliste in Buchstaben
     */
    @NonNull
    public static String[] getKlassen() {
        return lstKlassen.toArray(new String[0]);
    }
}