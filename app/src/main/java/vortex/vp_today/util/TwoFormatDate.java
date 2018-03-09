package vortex.vp_today.util;

import org.joda.time.LocalDate;

/**
 * @author Simon Dräger
 * @author Melvin Zähl
 * @version 8.3.18
 */

public class TwoFormatDate {
    /* Datum im VP-Format (2018-03-01) */
    private final String vpDate;
    /* Datum im normalen Format (03-01-2018) */
    private final LocalDate actualDate;

    public TwoFormatDate(String vpDate, LocalDate actualDate) {
        this.vpDate = vpDate;
        this.actualDate = actualDate;
    }

    public String getVPDate() {
        return vpDate;
    }

    public LocalDate getActualDate() {
        return actualDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TwoFormatDate) {
            TwoFormatDate date = (TwoFormatDate) obj;
            if (date.getVPDate().equals(this.vpDate) &&
                    (date.getActualDate().equals(this.getActualDate()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return vpDate + " ; " + actualDate.toString();
    }

    public String getPrefix(){
        String prefix = "";
        switch (getActualDate().getDayOfWeek()){
            case 1:
                prefix = "[Mo] ";
                break;
            case 2:
                prefix = "[Di] ";
                break;
            case 3:
                prefix = "[Mi] ";
                break;
            case 4:
                prefix = "[Do] ";
                break;
            case 5:
                prefix = "[Fr] ";
                break;

        }
        return prefix;
    }

}
