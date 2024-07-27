package se.quidbit.navbit

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
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

// ------------------------------------------------------------------------------------------------------------------
// The Screen type to be used by all views that we navigate between with transactions
// -----------------------------------------------------------------------------------------------------------------
// Note: All functions are abstract for clarity, forcing all function the screen can/should implement being clearly visible

abstract class Screen<T : NavBitScreenData>(context: Context) : FrameLayout(context) {
    private lateinit var data: T

    private var transitionAnimation: ObjectAnimator? = null

    private var exiting = false
    private var covered = false
    private var waitingForCover : TransitionType? = null

    private var screenInsets : ScreenInsets? = null

    private var screenOverlay: ScreenOverlay? = null

    private var ownedByMainThread = false

    // -------------------------------------------------------------
    // Starting up
    // -----------------------------------------
    abstract fun getLayoutIds(type : ScreenType) : ScreenLayoutIds
    abstract fun prepareLayout(view: View, type : ScreenType, onPrepared : (ScreenInsets) -> Unit)

    fun initialize(type : ScreenType, onInitialized : () -> Unit) {
        val layoutIds = getLayoutIds(type)

        // Inflate layout
        // ------------------------------------------------------
        when (type) {
            ScreenType.Full -> {
                val layoutId = layoutIds.full ?:  throw IllegalStateException("Screen does not support Full type")
                LayoutInflater.from(context).inflate(layoutId, this, true)

                val color = ContextCompat.getColor(context, R.color.screen_full_background)
                setBackgroundColor(color)
            }
            ScreenType.Sheet -> {
                LayoutInflater.from(context).inflate(R.layout.sheet, this, true)
                val sheetContentArea = findViewById<FrameLayout>(R.id.sheet_content_area)

                val layoutId = layoutIds.sheet ?:  throw IllegalStateException("Screen does not support Sheet type")
                LayoutInflater.from(context).inflate(layoutId, sheetContentArea, true)
            }
            ScreenType.PopUp -> {
                LayoutInflater.from(context).inflate(R.layout.popup, this, true)
                val sheetContentArea = findViewById<FrameLayout>(R.id.popup_content_area)

                val layoutId = layoutIds.popup ?:  throw IllegalStateException("Screen does not support PopUp type")
                LayoutInflater.from(context).inflate(layoutId, sheetContentArea, true)
            }
        }

        // Support closing
        // ------------------------------------------------------
        if (type == ScreenType.Sheet || type == ScreenType.PopUp) {
            val background = findViewById<View>(R.id.background)
            background?.setOnClickListener {
                requestExit()
            }

            val closeIcon = findViewById<ImageView>(R.id.closeButton)
            closeIcon?.setOnClickListener{
                requestExit()
            }
        }

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
                    val topPaddingId = rawInsets.topExtraPaddingId ?: R.dimen.sheet_padding_top

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
                        rawInsets.bottomExtraPaddingId ?: R.dimen.sheet_padding_bottom
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

    // For use within any Screen implementations
    fun screenHandler() : Handler {
        return when (ownedByMainThread) {
            true -> NavBitActivity.mainHandler
            false -> NavBitActivity.backgroundHandler
        }
    }

    // -------------------------------------------------------------
    // Transitioning
    // -------------------------------------------------------------

    fun restoreScreen(newData : NavBitScreenData, onRestored : () -> Unit) {
        // This screen has been active before, so it is owned by the main thread
        ownedByMainThread = true
        storeNewData(newData)

        entering(data) { postReleaseWork ->
            screenHandler().post {
                startBackgroundWork()
                visibility = VISIBLE

                postReleaseWork()
                onRestored()
            }
        }
    }

    fun refreshScreenInsets() {
        screenInsets?.triggerRefresh()
    }

    fun initiateAppearingTransition(
        transition: ScreenTransition,
        newState: NavBitScreenData,
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
                alpha = 0.0f
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

        screenOverlay?.fadeInLoading()

        // Populate the screen with the expected data
        when (transition.direction) {
            TransitionDirection.Forward -> {
                storeNewData(newState)

                entering(data) { afterRelease ->

                    performAppearingTransition(transition, moveToMainThreadForDisplay, onDisplayed)

                    screenHandler().post {
                        afterRelease()
                    }
                }
            }

            TransitionDirection.Backward -> {
                val oldData = storeNewData(newState, true)

                // The screen has been moved to the main container as it has already been displayed once
                // so any manipulation must be done on the main thread
                NavBitActivity.mainHandler.post {
                    visibility = VISIBLE

                    returning(oldData, data) {
                        performAppearingTransition(transition, { }, onDisplayed)
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

    fun initiateLeavingTransition(transition: ScreenTransition) {
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

        screenOverlay?.fadeInLoading()
    }

    fun notifyCovered() {
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

    fun isBackgroundScreen() : Boolean {
        return visibility == View.VISIBLE && covered
    }

    fun getScreenTag(): String {
        return data.tag()
    }

    fun <X : NavBitScreenData>getData(): X {
        // Dangerous cast here!
        // However, it is safe as long as this function is only called with the same type as was used to find the screen
        // Making them guaranteed to match, so it has to be enforced within the library
        return data as X
    }

    private fun storeNewData(state: NavBitScreenData, returnOld: Boolean = false): T? {
        // Dangerous cast here!
        // However, it is safe as long as this function is only called with the same type as was used to find the screen
        // Making them guaranteed to match, so it has to be enforced within the library
        val newData = state as T
        val oldData = if (returnOld && this::data.isInitialized) {
            NavBitActivity.getNavBitInstance<NavBitInteraction, NavBitNavigationState, T>().getScreenGenerator().screenDataDeepCopy(data)
        } else {
            null
        }

        data = NavBitActivity.getNavBitInstance<NavBitInteraction, NavBitNavigationState, T>().getScreenGenerator().screenDataDeepCopy(newData)
        return oldData
    }

    // -------------------------------------------------------------
    // Updating
    // -----------------------------------------
    fun notifyUpdatedData(updatedData: NavBitScreenData) {

        storeNewData(updatedData, true)?.let { oldData ->

            // Make any view changes on the main thread
            NavBitActivity.mainHandler.post {
                updating(oldData, data)
            }
        }
    }

    // The three main navigation functions
    //------------------------------------------------

    // Entering the screen (user going forwards)
    abstract fun entering(data: T, releaseForDisplay: (workAfter : () -> Unit) -> Unit)

    // Updated state on the current screen
    abstract fun updating(oldData: T, data: T)

    // Returning to the screen (user going backwards)
    // NOTE: The screen might not have been loaded before, in which there is no oldData available
    abstract fun returning(oldData: T?, data: T, notifyReady: () -> Unit)

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

data class ScreenLayoutIds (
    val full : Int?,
    val sheet : Int?,
    val popup : Int?
) {
    constructor(id: Int) : this(id, id, id)
}

data class BackgroundWork (
    val periodMs : Long? = null,
    val work : () -> UIwork
)

data class UIwork (
    val work : () -> Unit
)