package se.quidbit.navbit

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.doOnLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.atomic.AtomicInteger

// ------------------------------------------------------------------------------------------------------------------
// The Fragment type to be used by all views that we navigate between with transactions
// -----------------------------------------------------------------------------------------------------------------
// Note: All functions are abstract for clarity, forcing all function the fragment can/should implement being clearly visible

abstract class Screen<T : NavBitScreenData>(context: Context) : FrameLayout(context) {
    private lateinit var data: T

    private var transitionAnimation: ObjectAnimator? = null

    private var exiting = false

    private lateinit var inputBlocker: InputBlocker

    // -------------------------------------------------------------
    // Starting up
    // -----------------------------------------
    abstract fun getLayoutIds(type : ScreenType) : ScreenLayoutIds
    abstract fun prepareLayout(view: View, type : ScreenType) : ScreenInsets

    fun initialize(type : ScreenType) {
        val layoutIds = getLayoutIds(type)

        when (type) {
            ScreenType.Full -> {
                // Inflate layout
                // ----------------------------------------------------
                val layoutId = layoutIds.full ?:  throw IllegalStateException("Screen does not support Full type")
                LayoutInflater.from(context).inflate(layoutId, this, true)
            }
            ScreenType.Sheet -> {
                // Inflate layout
                // ----------------------------------------------------
                LayoutInflater.from(context).inflate(R.layout.sheet, this, true)
                val sheetContentArea = findViewById<FrameLayout>(R.id.sheet_content_area)

                val layoutId = layoutIds.sheet ?:  throw IllegalStateException("Screen does not support Sheet type")
                LayoutInflater.from(context).inflate(layoutId, sheetContentArea, true)

                // Support closing
                // ----------------------------------------------------
                val background = findViewById<View>(R.id.background)
                background?.setOnClickListener{
                    EventBus.getDefault().post(InternalInteraction.Back)
                }

                val closeIcon = findViewById<ImageView>(R.id.bottomSheetClose)
                closeIcon?.setOnClickListener{
                    EventBus.getDefault().post(InternalInteraction.Back)
                }
            }
        }

        // Set insets
        // ----------------------------------------------------
        val screenInsets = prepareLayout(this, type)
        screenInsets.setUpListeners()

        // In order to consume all touch/input when the screen is leaving
        inputBlocker = InputBlocker(context)
        inputBlocker.z = 10f
        addView(inputBlocker)

        this.visibility = View.INVISIBLE
    }

    // -------------------------------------------------------------
    // Transitioning
    // -------------------------------------------------------------

    fun restoreScreen(data : NavBitScreenData, visible : Boolean) {
        storeNewData(data)
        entering(data as T) {
            this.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        }
    }

    fun initiateAppearingTransition(transition: ScreenTransition, newState: NavBitScreenData, newlyLoaded : Boolean) {
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
        }

        // Populate the screen with the expected data
        val readyCounter = AtomicInteger(0)
        when (transition.direction) {
            TransitionDirection.Forward -> {
                storeNewData(newState)
                entering(data) {
                    checkReadyForAppearingTransition(readyCounter, transition)
                }
            }

            TransitionDirection.Backward -> {
                val oldData = storeNewData(newState, true)

                returning(oldData, data) {
                    checkReadyForAppearingTransition(readyCounter, transition)
                }
            }
        }

        this.doOnLayout {
            checkReadyForAppearingTransition(readyCounter, transition)
        }
    }

    private fun checkReadyForAppearingTransition(
        readyCounter: AtomicInteger,
        transition: ScreenTransition
    ) {
        if (readyCounter.incrementAndGet() != 2) {
            return
        }

        this.visibility = View.VISIBLE
        inputBlocker.block(false)

        startBackgroundWork()

        val newAnimation = transition.asEnteringAnimation(this)

        transitionAnimation?.cancel()
        transitionAnimation = newAnimation
        transitionAnimation?.start()
    }

    // -------------------------------------------------------------

    fun initiateLeavingTransition(transition: ScreenTransition) {
        visibility = VISIBLE
        inputBlocker.block(true)
        exiting = true
        stopBackgroundWork()

        val screenFull = this
        // Generate the new animation first to guarantee that the current position is retained
        val newAnimation = transition.asLeavingAnimation(this).apply {
            addListener(object : AnimatorListener {
                override fun onAnimationEnd(p0: Animator) {
                    if (exiting &&
                        transition.direction == TransitionDirection.Backward || transition.type.hideOnExit()
                    ) {
                        screenFull.visibility = INVISIBLE
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

    fun getData(): T {
        return data
    }

    private fun storeNewData(state: NavBitScreenData, returnOld: Boolean = false): T? {
        // Dangerous cast here!
        // However, it is safe as long as this function is only called with the same type as was used to find the fragment
        // Making them guaranteed to match
        // As currently done in BaseActivity
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
            Handler(Looper.getMainLooper()).post {
                updating(oldData, data)
            }
        }
    }

    // The three main navigation functions
    //------------------------------------------------

    // Entering the fragment (user going forwards)
    abstract fun entering(data: T, notifyReady: () -> Unit)

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
    val sheet : Int?
)

data class BackgroundWork (
    val periodMs : Long? = null,
    val work : () -> UIwork
)

data class UIwork (
    val work : () -> Unit
)