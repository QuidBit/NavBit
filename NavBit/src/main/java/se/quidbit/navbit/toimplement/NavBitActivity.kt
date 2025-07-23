package se.quidbit.navbit.toimplement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import se.quidbit.navbit.internal.AppThemeHolder
import se.quidbit.navbit.internal.InfoLog
import se.quidbit.navbit.internal.MainHolder
import se.quidbit.navbit.internal.MasterController
import se.quidbit.navbit.internal.MasterControllerFactory
import se.quidbit.navbit.types.QueuedInteraction
import java.util.concurrent.atomic.AtomicInteger

abstract class NavBitActivity<I : NavBitInteraction, S : NavBitNavigationState>(
    private val interactionHandler: NavBitInteractionHandler<I, S>,
    private val navigationStateHandler: NavBitNavigationStateHandler<S>,
    private val screenHandler: NavBitScreenHandler<S>,
    loggingEnabled : Boolean
)  : ComponentActivity() {

    init {
        InfoLog.LOGGING_ENABLED = loggingEnabled
    }

    private var masterController : MasterController<I, S>? = null

    // --------------------------------------------------------
    // Interaction handling
    // --------------------------------------------------------
    // NOTE: Interactions are processed sequentially one by one to avoid concurrent issues on modifying state/data

    private val interactionChannel = Channel<QueuedInteraction<I>>(Channel.UNLIMITED)
    private val interactionQueueSize = AtomicInteger(0)

    fun send(interaction: I) {
        InfoLog.i("Interaction Received [$interactionQueueSize]: $interaction")
        interactionChannel.trySend(QueuedInteraction.Custom(interaction))
        interactionQueueSize.incrementAndGet()
    }

    fun sendBack() {
        InfoLog.i("Interaction Received [${interactionQueueSize}]: [Back]")
        interactionChannel.trySend(QueuedInteraction.Back())
        interactionQueueSize.incrementAndGet()
    }

    fun sendClose() {
        InfoLog.i("Interaction Received [${interactionQueueSize}]: [Close]")
        interactionChannel.trySend(QueuedInteraction.Close())
        interactionQueueSize.incrementAndGet()
    }

    // ---------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this

        // ----------------------------------------------
        // Start the interaction processing directly
        // ----------------------------------------------
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                while (isActive) {
                    if (masterController == null) {
                        InfoLog.d("Delaying Interaction Processing - Waiting for MasterController")
                        delay(50)
                        continue
                    }

                    val interaction = interactionChannel.receive()
                    interactionQueueSize.decrementAndGet()
                    masterController?.processInteraction(this@NavBitActivity, interaction)
                }
            }
        }
        // ----------------------------------------------

        enableEdgeToEdge()
        setContent {
            // Prepare the main state and controller of the app
            val controller : MasterController<I,S> = viewModel(
                factory =  MasterControllerFactory(interactionHandler, navigationStateHandler)
            )

            // Display the screen
            val theme = screenHandler.getTheme(LocalContext.current, isSystemInDarkTheme())
            AppThemeHolder<S>(theme) {
                MainHolder(this, controller, screenHandler, interactionChannel, theme)
            }

            masterController = controller

            // Setup back press handling
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    interactionChannel.trySend(QueuedInteraction.Back())
                }
            })
        }
    }

    companion object {
        private lateinit var instance : NavBitActivity<*, *>
        fun <I : NavBitInteraction, S : NavBitNavigationState> getNavBitInstance() : NavBitActivity<I, S> {
            return instance as NavBitActivity<I, S>
        }
    }
}