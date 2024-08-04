package se.quidbit.navbit.internal

import android.view.View
import android.widget.FrameLayout.INVISIBLE
import android.widget.FrameLayout.VISIBLE

internal fun View.hide() {
    alpha = 0f
    visibility = INVISIBLE
}
