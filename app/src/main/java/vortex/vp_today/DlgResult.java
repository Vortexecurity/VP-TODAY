package vortex.vp_today;

/**
 * @author Simon Dräger
 * @version 4.3.18
 */

public class DlgResult {
    private DialogResult result;

    public DlgResult() {
        result = DialogResult.OK;
    }

    public void setResult(DialogResult result) {
        this.result = result;
    }

    public DialogResult getResult() {
        return result;
    }
}