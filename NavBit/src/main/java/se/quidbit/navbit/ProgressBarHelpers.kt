package se.quidbit.navbit

import android.widget.FrameLayout.INVISIBLE
import android.widget.FrameLayout.VISIBLE
import android.widget.ProgressBar

fun ProgressBar.fadeInLoading(delay : Long, length : Long) {
    animate().cancel()
    alpha = 0f
    visibility = VISIBLE
    animate()
        .alpha(1f)
        .setStartDelay(delay)
        .setDuration(length)
        .start()
}

fun ProgressBar.fadeOutLoading(delay : Long, length : Long) {
    animate().cancel()
    animate()
        .alpha(0f)
        .setStartDelay(delay)
        .setDuration(length)
        .start()
}

fun ProgressBar.hideLoading() {
    alpha = 0f
    visibility = INVISIBLE
}
