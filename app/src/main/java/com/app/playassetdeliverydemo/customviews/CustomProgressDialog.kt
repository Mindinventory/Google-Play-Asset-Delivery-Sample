package com.app.playassetdeliverydemo.customviews

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.app.playassetdeliverydemo.R

class CustomProgressDialog(context: Context) : Dialog(context) {
    init {
        init()
    }

    private fun init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setContentView(R.layout.custom_progress_layout)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun showProgressDialog() {
        if (!isShowing) {
            show()
        }
    }

    fun hideProgressDialog() {
        if (isShowing) {
            dismiss()
        }
    }
}
