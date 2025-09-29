package com.example.scmptest.ext

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.scmptest.R
import com.example.scmptest.data.model.ApiError
import com.google.gson.Gson
import okhttp3.ResponseBody

fun Int?.orZero() = this ?: 0

fun Boolean?.orFalse() = this ?: false

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visibleElseGone(shouldVisible: Boolean) {
    if (shouldVisible) visible() else gone()
}

fun Context.showErrorDialog(msg: String) {
    AlertDialog.Builder(this)
        .setTitle(R.string.common_generic_error_header)
        .setMessage(msg)
        .setPositiveButton(R.string.common_confirm_btn) { dialog, _ ->
            dialog.dismiss()
        }
        .setCancelable(false)
        .create()
        .show()
}

fun ResponseBody?.getErrorMsg(genericError: String): String =
    this?.string()?.let { body ->
        try {
            val gson = Gson()
            val apiError = gson.fromJson(body, ApiError::class.java)
            apiError.error ?: genericError
        } catch (e: Exception) {
            e.message ?: genericError
        }
    } ?: genericError