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
// The Fragment type to be used by all views that we navigate between with transactions
// -----------------------------------------------------------------------------------------------------------------
// Note: All functions are abstract for clarity, forcing all function the fragment can/should implement being clearly visible

abstract class Screen<T : NavBitScreenData>(context: Context) : FrameLayout(context) {
    private lateinit var data: T

    private var transitionAnimation: ObjectAnimator? = null

    private var exiting = false

    private lateinit var inputBlocker: InputBlocker

    private var ownedByMainThread = false

    // -------------------------------------------------------------
    // Starting up
    // -----------------------------------------
    abstract fun getLayoutIds(type : ScreenType) : ScreenLayoutIds
    abstract fun prepareLayout(view: View, type : ScreenType) : ScreenInsets

    fun initialize(type : ScreenType) {
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

                val layoutId = layoutIds.popup ?:  throw IllegalStateException("Screen does not support Sheet type")
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
        val screenInsets = prepareLayout(this, type)

        // Prepare the correct insets for the screen type
        val finalScreenInsets = when (type) {
            // Use as defined
            ScreenType.Full -> screenInsets

            ScreenType.Sheet -> {
                // As the sheet never reaches the top, no topInset is needed
                // Instead, a basic constant inset is added to the top to not overlap with the handle
                val topPaddingId = screenInsets.topExtraPaddingId ?: R.dimen.sheet_padding_top

                screenInsets.topView?.let {
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
                    screenInsets.bottomView,
                    screenInsets.bottomExtraPaddingId ?: R.dimen.sheet_padding_bottom
                )
            }

            // No insets needed for a popup as it is centered, if it fits on screen it fits!
            ScreenType.PopUp -> null
        }

        finalScreenInsets?.setUpListeners()

        // Set up input blocker
        // ---------------------------------------------------------------------
        // In order to consume all touch/input when the screen is leaving
        inputBlocker = InputBlocker(context)
        inputBlocker.z = 10f
        addView(inputBlocker)

        this.visibility = View.INVISIBLE
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

    fun restoreScreen(newData : NavBitScreenData) {
        storeNewData(newData)
        entering(data) { postReleaseWork ->
            screenHandler().post {
                startBackgroundWork()
                this.visibility = View.VISIBLE

                postReleaseWork()
            }
        }
    }

    fun initiateAppearingTransition(transition: ScreenTransition, newState: NavBitScreenData, newlyLoaded : Boolean, moveToMainThreadForDisplay : () -> Unit) {
        exiting = false

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

        // Populate the screen with the expected data
        when (transition.direction) {
            TransitionDirection.Forward -> {
                storeNewData(newState)
                entering(data) { afterRelease ->
                    performAppearingTransition(transition, moveToMainThreadForDisplay)

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
                    returning(oldData, data) {
                        performAppearingTransition(transition, moveToMainThreadForDisplay)
                    }
                }
            }
        }
    }

    private fun performAppearingTransition(
        transition: ScreenTransition,
        moveToMainThreadForDisplay : () -> Unit
    ) {
        this.visibility = View.VISIBLE
        inputBlocker.block(false)

        startBackgroundWork()

        // Add the screen to the main container for display
        moveToMainThreadForDisplay()
        ownedByMainThread = true

        // Any further interactions must be done on the main thread
        NavBitActivity.mainHandler.post {
            val newAnimation = transition.asEnteringAnimation(this)

            transitionAnimation?.cancel()
            transitionAnimation = newAnimation
            transitionAnimation?.start()
        }
    }

    // -------------------------------------------------------------

    fun initiateLeavingTransition(transition: ScreenTransition) {
        visibility = VISIBLE
        inputBlocker.block(true)
        exiting = true
        stopBackgroundWork()

        val screen = this
        // Generate the new animation first to guarantee that the current position is retained
        val newAnimation = transition.asLeavingAnimation(this).apply {
            addListener(object : AnimatorListener {
                override fun onAnimationEnd(p0: Animator) {
                    if (exiting &&
                        (transition.direction == TransitionDirection.Backward || transition.type.hideOnExit())
                    ) {
                        screen.visibility = INVISIBLE
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
    }

    // -------------------------------------------------------------

    fun isBackgroundScreen() : Boolean {
        // NOTE: Should be improved with a more explicit state
            // It might not be part of the background after the animation is finished since we look at the visibility
        return visibility == View.VISIBLE && exiting
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

    // Entering the fragment (user going forwards)
    abstract fun entering(data: T, releaseForDisplay: (workAfter : () -> Unit) -> Unit)

    // Updated state on the current fragment
    abstract fun updating(oldData: T, data: T)

    // Returning to the fragment (user going backwards)
    // NOTE: The screen might not have been loaded before, in which there is no oldData available
    abstract fun returning(oldData: T?, data: T, notifyReady: () -> Unit)

    // Background work - Used to correctly start/stop any background work done by the fragment
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