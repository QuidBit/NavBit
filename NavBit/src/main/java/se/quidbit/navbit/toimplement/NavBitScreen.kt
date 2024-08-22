package se.quidbit.navbit.toimplement

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import se.quidbit.navbit.R
import se.quidbit.navbit.internal.InternalInteraction
import se.quidbit.navbit.internal.ScreenOverlay
import se.quidbit.navbit.internal.ScreenTransition
import se.quidbit.navbit.showIfElseGone
import se.quidbit.navbit.types.BackgroundWork
import se.quidbit.navbit.types.ScreenInsets
import se.quidbit.navbit.types.ScreenLayoutIds
import se.quidbit.navbit.types.ScreenType
import se.quidbit.navbit.types.TransitionType
import se.quidbit.navbit.types.TransitionDirection

// ------------------------------------------------------------------------------------------------------------------
// The Screen type to be used by all views that we navigate between with transactions
// -----------------------------------------------------------------------------------------------------------------
// Note: All functions are abstract for clarity, forcing all function the screen can/should implement being clearly visible

abstract class NavBitScreen<D : NavBitScreenData>(context: Context) : FrameLayout(context) {
    private lateinit var data: D

    private var transitionAnimation: ObjectAnimator? = null

    private var exiting = false
    private var covered = false
    private var waitingForCover : TransitionType? = null

    private var screenInsets : ScreenInsets? = null

    private var screenOverlay: ScreenOverlay? = null

    private var ownedByMainThread = false

    private var userLoadingSymbol : View? = null

    // -------------------------------------------------------------
    // Starting up
    // -----------------------------------------
    abstract fun getLayoutIds(type : ScreenType) : ScreenLayoutIds
    abstract fun prepareLayout(view: View, type : ScreenType, onPrepared : (ScreenInsets) -> Unit)

    internal fun initialize(type : ScreenType, onInitialized : () -> Unit) {
        val layoutIds = getLayoutIds(type)

        // Inflate layout
        // ------------------------------------------------------
        when (type) {
            ScreenType.Full -> {
                val layoutId = layoutIds.full ?:  throw IllegalStateException("Screen does not support Full type")
                LayoutInflater.from(context).inflate(layoutId, this, true)

                val color = ContextCompat.getColor(context, R.color._nb_screen_full_background)
                setBackgroundColor(color)
            }
            ScreenType.Sheet -> {
                LayoutInflater.from(context).inflate(R.layout._nb_sheet, this, true)
                val sheetContentArea = findViewById<FrameLayout>(R.id._nb_sheetContentArea)

                val layoutId = layoutIds.sheet ?:  throw IllegalStateException("Screen does not support Sheet type")
                LayoutInflater.from(context).inflate(layoutId, sheetContentArea, true)
            }
            ScreenType.PopUp -> {
                LayoutInflater.from(context).inflate(R.layout._nb_popup, this, true)
                val sheetContentArea = findViewById<FrameLayout>(R.id._nb_popup_content_area)

                val layoutId = layoutIds.popup ?:  throw IllegalStateException("Screen does not support PopUp type")
                LayoutInflater.from(context).inflate(layoutId, sheetContentArea, true)
            }
        }

        // Support closing
        // ------------------------------------------------------
        if (type == ScreenType.Sheet || type == ScreenType.PopUp) {
            val background = findViewById<View>(R.id._nb_background)
            background?.setOnClickListener {
                requestExit()
            }

            val closeIcon = findViewById<ImageView>(R.id._nb_closeButton)
            closeIcon?.setOnClickListener{
                requestExit()
            }
        }

        // Check for loading symbol
        // ------------------------------------------------------
        userLoadingSymbol = findViewById(R.id.loading)

        // Set up screen insets
        // ----------------------------------------------------
        prepareLayout(this, type) { rawInsets ->

            // Prepare the correct insets for the screen type
            screenInsets = when (type) {
                // Use as defined
                ScreenType.Full -> rawInsets

                ScreenType.Sheet -> {
                    // As the sheet never reaches the top, no topInset is needed
                    // Instead, a basic constant inset is added to the top to not overlap with the handle
                    val topPaddingId = rawInsets.topExtraPaddingId ?: R.dimen._nb_sheet_padding_top

                    rawInsets.topView?.let {
                        val topPadding = context.resources.getDimensionPixelSize(topPaddingId)
                        it.setPadding(
                            it.paddingLeft,
                            topPadding,
                            it.paddingRight,
                            it.paddingBottom
                        )
                    }

                    ScreenInsets(
                        null,
                        null,
                        rawInsets.bottomView,
                        rawInsets.bottomExtraPaddingId ?: R.dimen._nb_sheet_padding_bottom
                    )
                }

                // No insets needed for a popup as it is centered, if it fits on screen it fits!
                ScreenType.PopUp -> null
            }

            screenInsets?.setUpListeners()
            visibility = GONE

            onInitialized()
        }
    }

    private fun requestExit() {
        if (!exiting) {
            EventBus.getDefault().post(InternalInteraction.Back)
        }
    }

    // -------------------------------------------------------------
    // Allow screens to post work asynchronously
    // -------------------------------------------------------------
    // Required since a finished Screen might be moved from background to main thread before the work is actually run

    // For use within any Screen implementations
    fun screenPost(work: () -> Unit) {
        currentHandler().post(work)
    }

    fun screenPostDelayed(delay : Long, work: () -> Unit) {
        currentHandler().postDelayed ({
            // Post again to use the currently correct handler after the delay (as it might have changed)
            screenPost(work)
        }, delay)
    }

    private fun currentHandler() : Handler {
        return when (ownedByMainThread) {
            true -> NavBitActivity.mainHandler
            false -> NavBitActivity.backgroundHandler
        }
    }

    // -------------------------------------------------------------
    // User customizable appearance
    // -------------------------------------------------------------

    fun hideCloseButton(hide: Boolean) {
        findViewById<ImageView>(R.id._nb_closeButton)?.showIfElseGone(!hide)
    }

    fun hideSheetHandle(hide: Boolean) {
        findViewById<CardView>(R.id._nb_sheetHandle)?.showIfElseGone(!hide)
    }

    // -------------------------------------------------------------
    // Loading
    // -------------------------------------------------------------
    private fun fadeInLoadingSymbolIfNotPresent() {
        if (userLoadingSymbol?.visibility != View.VISIBLE) {
            screenOverlay?.fadeInLoading()
        }
    }

    // -------------------------------------------------------------
    // Transitioning
    // -------------------------------------------------------------

    internal fun restoreScreen(newData : NavBitScreenData, onRestored : () -> Unit) {
        // This screen has been active before, so it is owned by the main thread
        ownedByMainThread = true
        setNewData(newData)

        entering(data) {
            currentHandler().post {
                startBackgroundWork()
                visibility = VISIBLE
                onRestored()
            }
        }
    }

    internal fun refreshScreenInsets() {
        screenInsets?.triggerRefresh()
    }

    internal fun initiateAppearingTransition(
        transition: ScreenTransition,
        newData: NavBitScreenData,
        newlyLoaded : Boolean,
        onDisplayed : () -> Unit,
        moveToMainThreadForDisplay : () -> Unit,
    ) {
        exiting = false
        waitingForCover = null
        covered = false

        val startOutside = transition.direction == TransitionDirection.Forward || newlyLoaded

        // Prepare appearing
        when (transition.type) {
            TransitionType.Full.Slide -> {
                x = when (startOutside) {
                    true -> Resources.getSystem().displayMetrics.widthPixels.toFloat()
                    false -> 0.0f
                }
            }
            TransitionType.Full.Fade -> {
                // When going to a new screen, we want to fade it in (starting from alpha 0)
                    // However, when backing, i.e. returning to a previous screen
                    // we want to retain the alpha as the previous view is revealed by fading out the current view
                if (transition.direction == TransitionDirection.Forward) {
                    alpha = 0.0f
                }
            }
            TransitionType.Sheet -> {
                y = when (startOutside) {
                    true -> Resources.getSystem().displayMetrics.heightPixels.toFloat()
                    false -> 0.0f
                }
            }
            TransitionType.PopUp -> {
                alpha = when (startOutside) {
                    true -> 0.0f
                    false -> 1.0f
                }
                scaleX = when (startOutside) {
                    true -> 0.8f
                    false -> 1.0f
                }
                scaleY = when (startOutside) {
                    true -> 0.8f
                    false -> 1.0f
                }
            }
        }

        fadeInLoadingSymbolIfNotPresent()

        // Populate the screen with the expected data
        when (transition.direction) {
            TransitionDirection.Forward -> {
                setNewData(newData)

                entering(data) {
                    performAppearingTransition(transition, moveToMainThreadForDisplay, onDisplayed)
                }
            }

            TransitionDirection.Backward -> {
                setNewData(newData)

                // The screen has been moved to the main container as it has already been displayed once
                // so any manipulation must be done on the main thread
                NavBitActivity.mainHandler.post {
                    visibility = VISIBLE

                    returning(data) {
                        performAppearingTransition(transition, moveToMainThreadForDisplay, onDisplayed)
                    }
                }
            }
        }
    }

    private fun performAppearingTransition(
        transition: ScreenTransition,
        moveToMainThreadForDisplay : () -> Unit,
        onDisplayed : () -> Unit
    ) {
        // Make sure the appearing has not been cancelled
        if (exiting) { return }
        
        startBackgroundWork()

        // Add the screen to the main container for display
        moveToMainThreadForDisplay()
        ownedByMainThread = true

        // Any further interactions must be done on the main thread
        NavBitActivity.mainHandler.post {

            visibility = VISIBLE
            screenOverlay?.blockInput(false)

            // Set up Screen Overlay
            // ---------------------------------------------------------------------
            if (screenOverlay == null) {
                screenOverlay = ScreenOverlay(context)
                screenOverlay?.z = 10f
                addView(screenOverlay)
            }

            // Start the entering
            val newAnimation = transition.asEnteringAnimation(this).apply {
                addListener(object : AnimatorListener {
                    override fun onAnimationEnd(p0: Animator) {
                        onDisplayed()
                    }
                    override fun onAnimationStart(p0: Animator) {}
                    override fun onAnimationCancel(p0: Animator) {}
                    override fun onAnimationRepeat(p0: Animator) {}
                })
            }

            transitionAnimation?.cancel()
            transitionAnimation = newAnimation
            transitionAnimation?.start()

            screenOverlay?.fadeOutLoading()
        }
    }

    // -------------------------------------------------------------

    internal fun initiateLeavingTransition(transition: ScreenTransition) {
        exiting = true
        waitingForCover = null
        covered = false

        visibility = VISIBLE
        screenOverlay?.blockInput(true)
        stopBackgroundWork()

        // Generate the new animation first to guarantee that the current position is retained
        val newAnimation = transition.asLeavingAnimation(this).apply {
            addListener(object : AnimatorListener {
                override fun onAnimationEnd(p0: Animator) {
                    // Check so the screen has not been brought back during the animation
                    if (exiting) {
                        // When backing away, there can be no delays, so hide directly OR if we are covered
                        if (transition.direction == TransitionDirection.Backward) {
                            visibility = GONE
                        }
                        // If going forward to a screen, we have to wait until we get covered (i.e., it is ready to display)
                        else if (covered) {
                            finishLeaving(transition.type)
                        } else {
                            waitingForCover = transition.type
                        }
                    }
                }
                override fun onAnimationStart(p0: Animator) {}
                override fun onAnimationCancel(p0: Animator) {}
                override fun onAnimationRepeat(p0: Animator) {}
            })
        }

        transitionAnimation?.cancel()
        transitionAnimation = newAnimation
        transitionAnimation?.start()

        fadeInLoadingSymbolIfNotPresent()
    }

    internal fun notifyCovered() {
        covered = true
        waitingForCover?.let {
            finishLeaving(it)
        }
    }

    private fun finishLeaving(type : TransitionType) {
        if (type.previousIsVisible()) {
            screenOverlay?.fadeOutLoading()
        } else {
            visibility = GONE
        }
    }

    // -------------------------------------------------------------

    internal fun isBackgroundScreen() : Boolean {
        return visibility == View.VISIBLE && covered
    }

    internal fun getScreenTag(): String {
        return data.tag()
    }

    // --------------------------------------------------------------------------------------------------
    // Dangerous casts here!
    // However, it is safe as long as this function is only called with the same type as was used to find the screen
    // Making them guaranteed to match, so it has to be enforced within the library
    // --------------------------------------------------------------------------------------------------

    internal fun <X : NavBitScreenData>getData(): X {
        return data as X
    }
    private fun setNewData(newData: NavBitScreenData) {
        data = newData as D
    }

    // -------------------------------------------------------------
    // Updating
    // -----------------------------------------
    internal fun notifyUpdatedData(updatedData: NavBitScreenData) {

        setNewData(updatedData)

        // Make any view changes on the main thread
        NavBitActivity.mainHandler.post {
            updating(data)
        }
    }

    // The three main navigation functions
    //------------------------------------------------

    // Entering the screen (user going forwards)
    abstract fun entering(data: D, notifyDone: () -> Unit)

    // Updated state on the current screen
    abstract fun updating(data: D)

    // Returning to the screen (user going backwards)
    // NOTE: The screen might not have been loaded before, in which there is no oldData available
    abstract fun returning(data: D, notifyDone: () -> Unit)

    // Background work - Used to correctly start/stop any background work done by the screen
    // ------------------------------------------------------------------------------
    abstract fun getBackgroundWork() : BackgroundWork?

    private var backgroundWorkCounter = 0

    private fun startBackgroundWork() {
        val backgroundWork = getBackgroundWork() ?: return
        val startUpdateQrCounter = backgroundWorkCounter
        val lifecycle = (context as? LifecycleOwner)?.lifecycleScope

        lifecycle?.launch(Dispatchers.IO) {
            while (startUpdateQrCounter == backgroundWorkCounter) {
                // Perform preparing background work
                val uiWork = backgroundWork.work()

                // Perform resulting UI work
                lifecycle.launch(Dispatchers.Main) {
                    uiWork.work()
                }

                // Repeat if a period was given
                when (backgroundWork.periodMs) {
                    null -> return@launch
                    else -> delay(backgroundWork.periodMs)
                }
            }
        }
    }

    private fun stopBackgroundWork() {
        backgroundWorkCounter += 1
    }

    open fun onStopBackgroundWork() {}
}