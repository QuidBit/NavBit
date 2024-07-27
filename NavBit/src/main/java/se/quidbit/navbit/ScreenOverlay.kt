package se.quidbit.navbit

import android.content.Context
import android.widget.FrameLayout
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.ProgressBar

class ScreenOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var screenCover : FrameLayout
    private var progressBar : ProgressBar

    init {
        LayoutInflater.from(context).inflate(R.layout.screen_overlay, this, true)
        screenCover = findViewById(R.id.screen_cover)
        progressBar = findViewById(R.id.progress_bar)

        screenCover.setOnClickListener {
            Log.i("NavBit", "Input Blocked")
        }

        progressBar.hideLoading()
        blockInput(false)
    }

    fun blockInput(block: Boolean) {
        screenCover.isClickable = block
        screenCover.isFocusable = block
    }

    fun fadeInLoading() {
        val baseTransitionLength = ScreenTransition.getBaseTransitionLength(context).toLong()
        progressBar.fadeInLoading(baseTransitionLength, baseTransitionLength * 2)
    }

    fun fadeOutLoading() {
        val baseTransitionLength = ScreenTransition.getBaseTransitionLength(context).toLong()
        progressBar.fadeOutLoading(0, baseTransitionLength / 2)
    }
}
