package vortex.vp_today.logic;

/**
 * @author Simon Dräger
 * @version 5.3.18
 */

public enum VPKind {
    VERTRETUNG("Vertretung"),
    ENTFALL("Entfall"),
    RAUMVERTRETUNG("Raum-Vertretung"),
    EIGENVARBEITEN("Eigenv. Arbeiten"),
    PAUSENAUFSICHT("Pausenaufsicht"),
    KLAUSUR("Klausur"),
    BETREUUNG("Betreuung"),
    VERLEGUNG("Verlegung"),
    LEHRERTAUSCH("Lehrertausch"),
    SONDEREINSATZ("Sondereinsatz"),
    VORMERKUNG("Vormerkung");

    private final String name;

    VPKind (String strEquiv) {
        name = strEquiv;
    }

    public String getName() {
        return name;
    }

    public static VPKind fromString(String str) throws IllegalArgumentException {
        for (VPKind kind : VPKind.values()) {
            if (kind.getName().equalsIgnoreCase(str))
                return kind;
        }
        throw new IllegalArgumentException("No VPKind of value " + str + " found!");
    }
}