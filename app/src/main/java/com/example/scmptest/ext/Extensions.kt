package com.example.scmptest.ext

import android.view.View

fun Int?.orZero() = this ?: 0

fun Boolean?.orFalse() = this ?: false

fun Boolean?.orTrue() = this ?: true

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visibleElseGone(shouldVisible: Boolean) {
    if (shouldVisible) visible() else gone()
}