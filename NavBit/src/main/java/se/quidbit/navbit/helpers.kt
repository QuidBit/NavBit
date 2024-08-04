package se.quidbit.navbit

import android.view.View
import android.widget.FrameLayout.VISIBLE

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

fun View.fadeIn(delay : Long, length : Long) {
    animate().cancel()
    alpha = 0f
    visibility = VISIBLE
    animate()
        .alpha(1f)
        .setStartDelay(delay)
        .setDuration(length)
        .start()
}

fun View.fadeOut(delay : Long, length : Long) {
    animate().cancel()
    animate()
        .alpha(0f)
        .setStartDelay(delay)
        .setDuration(length)
        .start()
}