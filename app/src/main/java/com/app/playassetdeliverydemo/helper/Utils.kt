package com.app.playassetdeliverydemo.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast

object Utils {
    /**
     * This method will check user is Connected to Internet or not
     *
     * @return true: Boolean if User is connected to Internet, false otherwise
     */
    @JvmStatic
    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    return when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }
                }
            }
        } else {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
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
