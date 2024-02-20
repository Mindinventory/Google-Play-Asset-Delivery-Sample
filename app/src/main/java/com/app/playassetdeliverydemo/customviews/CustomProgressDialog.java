package com.app.playassetdeliverydemo.customviews;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import androidx.annotation.NonNull;

import com.app.playassetdeliverydemo.R;

public class CustomProgressDialog extends Dialog {

    Context context;

    public CustomProgressDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(R.layout.custom_progress_layout);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    public void showProgresDialog() {
        if (!isShowing()) {
            show();
        }
    }

    public void hideProgresDialog() {
        if (isShowing()) {
            dismiss();
        }
    }
}
