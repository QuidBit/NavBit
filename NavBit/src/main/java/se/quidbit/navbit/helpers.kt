package se.quidbit.navbit

import android.view.View

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.showIfElseGone(boolean: Boolean) {
    if (boolean) visible() else gone()
}

fun View.showIfElseInvisible(boolean: Boolean) {
    if (boolean) visible() else invisible()
}