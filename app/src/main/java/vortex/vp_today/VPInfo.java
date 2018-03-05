package vortex.vp_today;

import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * @author Simon Dräger
 * @version 5.3.18
 */

public class VPInfo {
    private ArrayList<VPRow> rows;

    public VPInfo() {
        rows = new ArrayList<>();
    }

    public ArrayList<VPRow> getRows() {
        return rows;
    }

    public void addRow(VPRow row) {
        rows.add(row);
    }

    @Nullable
    public String[] getContent() {
        ArrayList<String> temp = new ArrayList<>(rows.size());

        for (VPRow row : rows) {
            temp.add(row.getContent());
        }

        return temp.toArray(new String[0]);
    }
}