package vortex.vp_today.adapter;

import android.content.Context;
import android.support.transition.TransitionManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vplib.vortex.vplib.Util;
import com.vplib.vortex.vplib.logic.VPRow;

import java.util.List;

import vortex.vp_today.R;

/**
 * @author Simon Dräger
 * @version 18.3.18
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.VPRowViewHolder> {
    private Context ctx;
    private List<VPRow> rows;
    private String msgOTD;
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

    public RVAdapter(Context ctx, RecyclerView rv, List<VPRow> rows, String msgOTD) {
        this.ctx = ctx;
        this.rv = rv;
        this.msgOTD = msgOTD;
        this.rows = rows;
        expandedRows = new boolean[rows.size()];

        for (int i = 0; i < expandedRows.length; i++)
            expandedRows[i] = false;

        if (Util.D) Log.i("RVAdapter", "rows size: " + this.rows.size());
    }

    public void removeItem(int index) {
        rows.remove(index);
    }

    public void removeMsgOTD() {
        msgOTD = null;
        notifyDataSetChanged();
    }

    @Override
    public VPRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);

        VPRowViewHolder pvh = new VPRowViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final VPRowViewHolder vpViewHolder, final int i) {
        if (i == 0) {
            if (msgOTD != null) {
                vpViewHolder.vpText.setTextColor(ctx.getResources().getColor(R.color.colorPrimary));
                vpViewHolder.vpText.setText(msgOTD);
            }
        } else {
            VPRow r = rows.get(i - 1);

            if (Util.D) Log.i("onBindViewHolder", "row an " + i + ": " + r.toString());

            vpViewHolder.vpText.setVisibility(View.GONE);
            vpViewHolder.itemView.setActivated(false);

            vpViewHolder.vpTitle.setText(r.getStunde() + ". Std.: " + r.getArt());
            vpViewHolder.vpText.setText(r.isKurseVersion() ? r.getLinearContent() : r.getContent());
            vpViewHolder.vpImage.setImageResource(R.mipmap.ic_launcher);

            vpViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expandedRows[i - 1] = !expandedRows[i - 1];
                    vpViewHolder.vpText.setVisibility(expandedRows[i - 1] ? View.VISIBLE : View.GONE);
                    vpViewHolder.itemView.setActivated(expandedRows[i - 1]);
                    TransitionManager.beginDelayedTransition(rv);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (Util.D) Log.i("getItemCount", "" + (rows.size() + 1));

        if (msgOTD == null)
            return rows.size();

        /* + 1 für msgOTD */
        return rows.size() + 1;
    }
}
