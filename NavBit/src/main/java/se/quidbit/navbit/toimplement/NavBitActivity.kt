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




    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    public override fun onResume() {
        super.onResume()
        mainController.startInteractionProcessing()
    }

    public override fun onPause() {
        super.onPause()
        mainController.stopInteractionProcessing()
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