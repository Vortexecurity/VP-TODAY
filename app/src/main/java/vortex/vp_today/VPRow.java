package vortex.vp_today;

/**
 * Eine VP-Reihe.
 * @author Simon Dräger
 * @version 5.3.18
 */

public class VPRow {
    private VPKind art;
    private int stunde;
    private String fach;
    private String vertreter;
    private String klasse;
    private String raum;
    private String statt;
    private String bemerkung;

    public VPRow() {
        art = null;
        stunde = 0;
        fach = null;
        vertreter = null;
        klasse = null;
        raum = null;
        statt = null;
        bemerkung = null;
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
}