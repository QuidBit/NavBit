package se.quidbit.navbit.internal

import android.view.View
import android.widget.FrameLayout.INVISIBLE
import android.widget.FrameLayout.VISIBLE

internal fun View.fadeIn(delay : Long, length : Long) {
    animate().cancel()
    alpha = 0f
    visibility = VISIBLE
    animate()
        .alpha(1f)
        .setStartDelay(delay)
        .setDuration(length)
        .start()
}

internal fun View.fadeOut(delay : Long, length : Long) {
    animate().cancel()
    animate()
        .alpha(0f)
        .setStartDelay(delay)
        .setDuration(length)
        .start()
}

internal fun View.hide() {
    alpha = 0f
    visibility = INVISIBLE
}
