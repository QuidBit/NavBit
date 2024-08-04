package se.quidbit.navbit.internal

import android.content.Context
import android.widget.FrameLayout
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.ProgressBar
import se.quidbit.navbit.R
import se.quidbit.navbit.fadeIn
import se.quidbit.navbit.fadeOut

internal class ScreenOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var screenCover : FrameLayout
    private var progressBar : ProgressBar

    init {
        LayoutInflater.from(context).inflate(R.layout._nb_screen_overlay, this, true)
        screenCover = findViewById(R.id.screen_cover)
        progressBar = findViewById(R.id._nb_loading)

        screenCover.setOnClickListener {
            Log.i("NavBit-Screen", "Input Blocked")
        }

        progressBar.hide()
        blockInput(false)
    }

    fun blockInput(block: Boolean) {
        screenCover.isClickable = block
        screenCover.isFocusable = block
    }

    fun fadeInLoading() {
        val baseTransitionLength = ScreenTransition.getBaseTransitionLength(context).toLong()
        progressBar.fadeIn(baseTransitionLength, baseTransitionLength * 2)
    }

    fun fadeOutLoading() {
        val baseTransitionLength = ScreenTransition.getBaseTransitionLength(context).toLong()
        progressBar.fadeOut(0, baseTransitionLength / 2)
    }
}
