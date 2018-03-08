package vortex.vp_today.util;

import org.joda.time.LocalDate;

/**
 * @author Simon Dräger
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
}
