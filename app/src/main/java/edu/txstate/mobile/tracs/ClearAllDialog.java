package edu.txstate.mobile.tracs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

public class ClearAllDialog extends AlertDialog {
    private View.OnClickListener positiveClick;

    protected ClearAllDialog(Context context, View.OnClickListener positiveClick) {
        super(context);
        this.positiveClick = positiveClick;
        init();
    }

    private void init() {
        View dialog = getLayoutInflater().inflate(R.layout.clear_all_dialog, null);
        setView(dialog);

        Button positive = (Button) dialog.findViewById(R.id.clear_all_positive);
        Button negative = (Button) dialog.findViewById(R.id.clear_all_negative);

        positive.setOnClickListener(this::onConfirm);
        negative.setOnClickListener(this::onCancel);
    }

    private void onConfirm(View view) {
        this.positiveClick.onClick(view);
        dismiss();
    }

    private void onCancel(View view) {
        dismiss();
    }
}
