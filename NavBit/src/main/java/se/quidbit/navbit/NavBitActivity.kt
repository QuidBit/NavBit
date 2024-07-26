package se.quidbit.navbit

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

abstract class NavBitActivity<T : NavBitInteraction, U : NavBitNavigationState, V : NavBitScreenData>(
    val interactionHandler: NavBitInteractionHandler<T, U>,
    private val navigationStateHandler: NavBitNavigationStateHandler<U, V>,
    private val screenHandler: NavBitScreenHandler<V>
)  : AppCompatActivity() {

    private lateinit var navBitMainController : NavBitMainController<T, U, V>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        val backgroundThread = HandlerThread("NavBitThread")
        backgroundThread.start()

        backgroundHandler = Handler(backgroundThread.looper)
        mainHandler = Handler(Looper.getMainLooper())
    }

    fun getScreenGenerator() : NavBitScreenHandler<V> {
        return screenHandler
    }

    fun getCurrentState() : U {
        return navigationStateHandler.getCurrentState()
    }

    fun initializeNavBit() {
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        actionBar?.hide()

        navBitMainController = NavBitMainController(
        this,
            interactionHandler,
            navigationStateHandler,
            screenHandler
        )

        // Handle back presses
        // --------------------------------------------------------
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Backing is always allowed regardless if we are mid fragment switching
                navBitMainController.handleInteraction(interactionHandler.getBackInteraction())
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(interaction: T) {
        navBitMainController.handleInteraction(interaction)
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onMessageEvent(interaction: InternalInteraction) {
        val appInteraction = when (interaction) {
            InternalInteraction.Back -> interactionHandler.getBackInteraction()
        }
        navBitMainController.handleInteraction(appInteraction)
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
        navBitMainController.onDestroy()
    }

    // Access functions
    // ----------------------------------------------------
    companion object {
        internal lateinit var backgroundHandler: Handler
        internal lateinit var mainHandler : Handler

        private lateinit var instance : NavBitActivity<*,*,*>
        fun <T : NavBitInteraction, U : NavBitNavigationState, V : NavBitScreenData>getNavBitInstance() : NavBitActivity<T,U,V> {
            return instance as NavBitActivity<T, U, V>
        }
    }
}