package se.quidbit.navbit

import android.view.View
import android.widget.FrameLayout.INVISIBLE
import android.widget.FrameLayout.VISIBLE

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

fun View.hide() {
    alpha = 0f
    visibility = INVISIBLE
}

fun View.show() {
    alpha = 0f
    visibility = INVISIBLE
}
