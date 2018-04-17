package com.vplib.vortex.vplib.logic;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;

/**
 * Beschreibt einen Tag vom VP.
 *
 * @author Simon Dräger
 * @version 20.3.18
 */

public class VPInfo {
    private ArrayList<VPRow> rows;
    private Date date;

    public VPInfo(Date date) {
        rows = new ArrayList<>();
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public ArrayList<VPRow> getRows() {
        return rows;
    }

    public void addRow(VPRow row) {
        rows.add(row);
    }

    public void addAll(VPRow[] additionalRows) {
        for (VPRow r : additionalRows) {
            rows.add(r);
        }
    }

    public void removeRow(int index) {
        rows.remove(index);
    }

    public void removeRow(VPRow row) {
        rows.remove(row);
    }

    public boolean isEmpty() {
        if (rows == null ||
                rows.isEmpty() ||
                rows.size() == 0) {
            return true;
        }
        return false;
    }

    public boolean contains(VPRow row) {
        return rows.contains(row);
    }

    public boolean assumeKursVersion() {
        for (VPRow r : rows) {
            if (r.isKurseVersion())
                return true;
        }
        return false;
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
