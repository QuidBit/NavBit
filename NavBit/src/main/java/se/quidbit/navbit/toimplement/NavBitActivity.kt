package se.quidbit.navbit.toimplement

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import se.quidbit.navbit.internal.InternalInteraction
import se.quidbit.navbit.internal.MainController

abstract class NavBitActivity<I : NavBitInteraction, S : NavBitNavigationState, D : NavBitScreenData>(
    val interactionHandler: NavBitInteractionHandler<I, S>,
    private val navigationStateHandler: NavBitNavigationStateHandler<S, D>,
    private val screenHandler: NavBitScreenHandler<S,D>
)  : AppCompatActivity() {

    private lateinit var mainController : MainController<I, S, D>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        val backgroundThread = HandlerThread("NavBitThread")
        backgroundThread.start()

        backgroundHandler = Handler(backgroundThread.looper)
        mainHandler = Handler(Looper.getMainLooper())
    }

    internal fun getScreenGenerator() : NavBitScreenHandler<S,D> {
        return screenHandler
    }

    fun getCurrentState() : S {
        return navigationStateHandler.getCurrentState()
    }

    fun initializeNavBit() {
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        actionBar?.hide()

        mainController = MainController(
        this,
            interactionHandler,
            navigationStateHandler,
            screenHandler
        )

        // Handle back presses
        // --------------------------------------------------------
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Backing is always allowed regardless if we are mid screen switching
                mainController.handleInteraction(interactionHandler.getBackInteraction())
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    internal fun onMessageEvent(interaction: I) {
        mainController.handleInteraction(interaction)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    internal fun onMessageEvent(interaction: InternalInteraction) {
        val appInteraction = when (interaction) {
            InternalInteraction.Back -> interactionHandler.getBackInteraction()
        }
        mainController.handleInteraction(appInteraction)
    }


    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    public override fun onDestroy() {
        super.onDestroy()
        mainController.onDestroy()
    }

    // Access functions
    // ----------------------------------------------------
    companion object {
        internal lateinit var backgroundHandler: Handler
        internal lateinit var mainHandler : Handler

        private lateinit var instance : NavBitActivity<*, *, *>
        fun <I : NavBitInteraction, S : NavBitNavigationState, D : NavBitScreenData>getNavBitInstance() : NavBitActivity<I, S, D> {
            return instance as NavBitActivity<I, S, D>
        }
    }
}