package vortex.vp_today;

import java.util.ArrayList;

/**
 * @author Simon Dräger
 * @version 5.3.18
 */

public class VPInfo {
    private ArrayList<VPRow> rows;

    public VPInfo() { }

    public VPRow[] getRows() {
        return rows.toArray(new VPRow[0]);
    }

    public void addRow(VPRow row) {
        rows.add(row);
    }
}