package se.quidbit.navbit

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

//NOTE: Only items at the top and bottom edge of the screen needs to be inset, to not overlap with the bars/keyboard
//If the top of the item is in the middle of the screen, the top padding can be set directly in the XML
//And vice versa, if the bottom of the item is in the middle of the screen, the bottom padding can be set directly in the XM

data class ScreenInsets (
    val topView : View?,
    val topExtraPaddingId: Int?,
    val bottomView : View?,
    val bottomExtraPaddingId: Int?,
) {
    constructor() : this(null, null, null, null)
    constructor(contentView: View?) : this(contentView, null, contentView, null)
    constructor(topView: View?, bottomView: View?) : this(topView, null, bottomView, null)

    constructor(topView: View?, topExtraPaddingId: Int, bottomView: View?) : this(topView, topExtraPaddingId, bottomView, null)

    constructor(topView: View?, bottomView: View?, bottomExtraPaddingId: Int) : this(topView, null, bottomView, bottomExtraPaddingId)

    fun setUpListeners() {
        topView?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
                var topPadding = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                topExtraPaddingId?.let {
                    topPadding += v.resources.getDimension(it).toInt()
                }

                v.setPadding(v.paddingLeft, topPadding, v.paddingRight, v.paddingBottom)
                WindowInsetsCompat.CONSUMED
            }
        }

        bottomView?.let {view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
                val barInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
                val keyboardInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

                var bottomPadding = barInsets.bottom + keyboardInsets.bottom
                bottomExtraPaddingId?.let {
                    bottomPadding += v.resources.getDimension(it).toInt()
                }

                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottomPadding)
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    fun triggerRefresh() {
        topView?.requestApplyInsets()
        bottomView?.requestApplyInsets()
    }
}

fun ViewGroup.setTopPadding(paddingID : Int) {
    this.setPadding(this.paddingLeft, resources.getDimension(paddingID).toInt(), this.paddingRight, this.paddingBottom)
}

fun ViewGroup.setBottomPadding(paddingID : Int) {
    this.setPadding(this.paddingLeft, this.paddingTop, this.paddingRight, resources.getDimension(paddingID).toInt())
}

// TO BE REMOVED AFTER SHEET CONVERSION
// -----------------------------------------------


fun View.insetTop(extraPaddingID : Int? = null) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        var topPadding = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top

        extraPaddingID?.let {
            topPadding += resources.getDimension(it).toInt()
        }

        view.setPadding(view.paddingLeft, topPadding, view.paddingRight, view.paddingBottom)
        WindowInsetsCompat.CONSUMED
    }
}
fun View.insetBottom(extraPaddingID : Int? = null) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val barInsets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val keyboardInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

        var bottomPadding = barInsets.bottom + keyboardInsets.bottom

        extraPaddingID?.let {
            bottomPadding += resources.getDimension(it).toInt()
        }

        view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottomPadding)
        WindowInsetsCompat.CONSUMED
    }
}