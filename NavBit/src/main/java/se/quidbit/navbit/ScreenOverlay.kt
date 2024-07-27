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
            Log.i("NavBit", "INPUT BLOCKED")
        }

        hideLoading()
        blockInput(false)
    }

    fun blockInput(block: Boolean) {
        screenCover.isClickable = block
        screenCover.isFocusable = block
    }

    fun fadeInLoading() {
        val baseTransitionLength = ScreenTransition.getBaseTransitionLength(context).toLong()

        progressBar.apply {
            animate().cancel()
            alpha = 0f
            visibility = VISIBLE
            animate()
                .alpha(1f)
                .setStartDelay(baseTransitionLength)
                .setDuration(baseTransitionLength * 2)
                .start()
        }
    }

    fun fadeOutLoading() {
        val baseTransitionLength = ScreenTransition.getBaseTransitionLength(context).toLong()

        progressBar.apply {
            animate().cancel()
            animate()
                .alpha(0f)
                .setStartDelay(0)
                .setDuration(baseTransitionLength / 2)
                .start()
        }
    }

    private fun hideLoading() {
        progressBar.alpha = 0f
        progressBar.visibility = INVISIBLE
    }
}
