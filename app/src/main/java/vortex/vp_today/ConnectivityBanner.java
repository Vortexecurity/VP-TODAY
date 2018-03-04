package vortex.vp_today;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Simon Dräger
 * @version 2.3.18
 */

public class ConnectivityBanner extends View {
    private Paint paint;
    private boolean online = true;

    public ConnectivityBanner(Context ctx, AttributeSet att) {
        super(ctx, att);

        paint = new Paint();
        /* Initialize to red banner */
        paint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!online) {
            canvas.drawColor(Color.RED);
            canvas.drawText("Offline", this.getWidth() / 2, this.getHeight() / 2, paint);
        } else {
            canvas.drawColor(Color.GREEN);
            canvas.drawText("Online", this.getWidth() / 2, this.getHeight() / 2, paint);
        }
    }

    public void online() {
        online = true;
        invalidate();
    }

    public void offline() {
        online = false;
        invalidate();
    }
}