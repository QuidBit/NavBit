package se.quidbit.navbit

import android.content.Context
import android.view.View
import android.widget.FrameLayout

class InputBlocker(context : Context) : View(context) {
     init {
             layoutParams = FrameLayout.LayoutParams(
                 FrameLayout.LayoutParams.MATCH_PARENT,
                 FrameLayout.LayoutParams.MATCH_PARENT
             )
             block(false)
     }

    fun block(block : Boolean) {
        isClickable = block
        isFocusable = block
    }
}