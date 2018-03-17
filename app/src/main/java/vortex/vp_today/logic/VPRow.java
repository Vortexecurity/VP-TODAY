package vortex.vp_today.logic;

import android.support.annotation.Nullable;

import vortex.vp_today.util.Util;

/**
 * Eine VP-Reihe.
 *
 * @author Simon Dräger
 * @version 5.3.18
 */

public class VPRow {
    private VPKind art;
    private int stunde;
    private int[] stunden;
    private String fach;
    private String vertreter;
    private String klasse;
    private String raum;
    private String statt;
    private String bemerkung;
    private String content;

    public static final String DELIMITER = " | ";

    public VPRow() {
        art = null;
        stunde = 0;
        stunden = null;
        fach = null;
        vertreter = null;
        klasse = null;
        raum = null;
        statt = null;
        bemerkung = null;
        content = null;
    }

    @Nullable
    public String getLinearContent() {
        if (art != null) {
            String out = String.valueOf(stunde) + DELIMITER + fach + DELIMITER + art.getName() + DELIMITER;
            if (vertreter != null)
                out += vertreter + DELIMITER;
            if (!raum.equals("---"))
                out += raum + DELIMITER;
            if (!statt.equals("---"))
                out += statt + DELIMITER;
            if (!bemerkung.equals("---"))
                out += bemerkung + DELIMITER;
            return out.substring(0, out.length() - 2) + "\n\n";
        }
        return null;
    }

    @Override
    public String toString() {
        if (isKurseVersion())
            return getLinearContent();
        return getContent();
    }

    @Nullable
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isKurseVersion() {
        return content == null;
    }

    public VPKind getArt() {
        return art;
    }

    public void setArt(VPKind art) {
        this.art = art;
    }

    public int getStunde() {
        return stunde;
    }

    public void setStunde(int stunde) {
        this.stunde = stunde;
    }

    public int getStundenCount() {
        return stunden == null ? 1 : stunden.length;
    }

    @Nullable
    public int[] getStunden() {
        return stunden;
    }

    public void setStunden(int[] stunden) {
        this.stunden = stunden;
    }

    public String getFach() {
        return fach;
    }

    public void setFach(String fach) {
        this.fach = fach;
    }

    public String getVertreter() {
        return vertreter;
    }

    public void setVertreter(String vertreter) {
        this.vertreter = vertreter;
    }

    public String getKlasse() {
        return klasse;
    }

    public void setKlasse(String klasse) {
        this.klasse = klasse;
    }

    public String getRaum() {
        return raum;
    }

    public void setRaum(String raum) {
        this.raum = raum;
    }

    public String getStatt() {
        return statt;
    }

    public void setStatt(String statt) {
        this.statt = statt;
    }

    public String getBemerkung() {
        return bemerkung;
    }

    public void setBemerkung(String bemerkung) {
        this.bemerkung = bemerkung;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VPRow) {
            VPRow row = (VPRow) obj;
            if (row.getArt() == art &&
                    Util.equalsWithNulls(row.getBemerkung(), bemerkung) &&
                    Util.equalsWithNulls(row.getFach(), fach) &&
                    Util.equalsWithNulls(row.getKlasse(), klasse) &&
                    Util.equalsWithNulls(row.getRaum(), raum) &&
                    Util.equalsWithNulls(row.getStatt(), statt) &&
                    Util.equalsWithNulls(row.getVertreter(), vertreter) &&
                    Util.equalsWithNulls(row.getStunden(), stunden) &&
                    row.getStunde() == stunde) {
                return true;
            }
        }
        return false;
    }
}
