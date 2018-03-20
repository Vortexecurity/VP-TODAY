package vortex.vp_today.adapter;

import android.support.transition.TransitionManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import vortex.vp_today.R;
import vortex.vp_today.logic.VPRow;
import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @version 18.3.18
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.VPRowViewHolder> {
    private List<VPRow> rows;
    private RecyclerView rv;
    private boolean[] expandedRows;

    public static class VPRowViewHolder extends RecyclerView.ViewHolder {
        CardView cv;

        TextView vpTitle;
        TextView vpText;
        ImageView vpImage;

        VPRowViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            vpTitle = itemView.findViewById(R.id.vpTitle);
            vpText = itemView.findViewById(R.id.vpText);
            vpImage = itemView.findViewById(R.id.vpImage);
        }
    }

    public RVAdapter(RecyclerView rv, List<VPRow> rows) {
        this.rv = rv;
        this.rows = rows;
        expandedRows = new boolean[rows.size()];

        for (int i = 0; i < expandedRows.length; i++)
            expandedRows[i] = false;

        if (Util.D) Log.i("RVAdapter", "rows size: " + this.rows.size());
    }

    @Override
    public VPRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);

        VPRowViewHolder pvh = new VPRowViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final VPRowViewHolder vpViewHolder, final int i) {
        VPRow r = rows.get(i);

        if (Util.D) Log.i("onBindViewHolder", "row an " + i + ": " + r.toString());

        vpViewHolder.vpText.setVisibility(View.GONE);
        vpViewHolder.itemView.setActivated(false);

        vpViewHolder.vpTitle.setText(r.getStunde() + ". Std.: " + r.getArt());
        vpViewHolder.vpText.setText(r.isKurseVersion() ? r.getLinearContent() : r.getContent());
        vpViewHolder.vpImage.setImageResource(R.mipmap.ic_launcher);

        vpViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandedRows[i] = !expandedRows[i];
                vpViewHolder.vpText.setVisibility(expandedRows[i] ? View.VISIBLE : View.GONE);
                vpViewHolder.itemView.setActivated(expandedRows[i]);
                TransitionManager.beginDelayedTransition(rv);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (Util.D) Log.i("getItemCount", "" + rows.size());
        return rows.size();
    }
}
