package com.app.playassetdeliverydemo.helper

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast

object Utils {
    /**
     * This method will check user is Connected to Internet or not
     *
     * @return true: Boolean if User is connected to Internet, false otherwise
     */
    @JvmStatic
    fun isInternetConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                return true
            } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                return true
            }
        } else {
            // not connected to the internet
            return false
        }
        return false
    }

    /**
     * This method is used to show toast
     */
    @JvmStatic
    fun showToast(context: Context?, msg: String?) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}
