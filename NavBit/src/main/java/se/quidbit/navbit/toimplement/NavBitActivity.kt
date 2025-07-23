package se.quidbit.navbit.toimplement

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import se.quidbit.navbit.internal.AppThemeHolder
import se.quidbit.navbit.internal.InfoLog
import se.quidbit.navbit.internal.MainHolder
import se.quidbit.navbit.internal.MasterController
import se.quidbit.navbit.internal.MasterControllerFactory
import se.quidbit.navbit.types.QueuedInteraction
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

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

    @Volatile
    private var isProcessingInteraction = false
    private var interactionJob : Job? = null

    private val interactionQueue : BlockingQueue<QueuedInteraction<I>> = LinkedBlockingQueue()

    fun send(interaction : I) {
        InfoLog.i( "Interaction Received: $interaction")
        interactionQueue.add(QueuedInteraction.Custom(interaction))
    }
    fun sendBack() {
        InfoLog.i( "Interaction Received: [Back]")
        interactionQueue.add(QueuedInteraction.Back())
    }
    fun sendClose() {
        InfoLog.i( "Interaction Received: [Close]")
        interactionQueue.add(QueuedInteraction.Close())
    }
    // --------------------------------------------------------

    public override fun onResume() {
        super.onResume()
        startInteractionProcessing()
    }

    public override fun onPause() {
        super.onPause()
         stopInteractionProcessing()
    }

    private fun startInteractionProcessing() {
        if (interactionJob == null || interactionJob?.isCompleted == true) {
            interactionJob = CoroutineScope(Dispatchers.IO).launch {
                // Process interactions until cancel has been called
                while (isActive) {
                    // Make sure no interactions are processed before the masterController is ready
                    masterController?.let {
                        val interaction = interactionQueue.take()
                        isProcessingInteraction = true
                        masterController?.processInteraction(this@NavBitActivity, interaction)
                        isProcessingInteraction = false
                    }
                }
            }
        }
    }

    private fun stopInteractionProcessing() {
        interactionJob?.let { job ->
            if (!isProcessingInteraction) {
                job.cancel()
            } else {
                runBlocking {
                    job.cancelAndJoin()
                }
            }
            interactionJob = null
        }
    }

    // ---------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        instance = this

        enableEdgeToEdge()
        setContent {
            // Prepare the main state and controller of the app
            val controller : MasterController<I,S> = viewModel(
                factory =  MasterControllerFactory(interactionHandler, navigationStateHandler)
            )

            // Display the screen
            val theme = screenHandler.getTheme(LocalContext.current, isSystemInDarkTheme())
            AppThemeHolder<S>(theme) {
                MainHolder(this, controller, screenHandler, interactionQueue, theme)
            }

            masterController = controller

            // Setup back press handling
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    interactionQueue.add(QueuedInteraction.Back())
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