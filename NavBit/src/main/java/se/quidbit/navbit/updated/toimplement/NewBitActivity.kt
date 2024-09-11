package se.quidbit.navbit.updated.toimplement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import se.quidbit.navbit.toimplement.NavBitInteraction
import se.quidbit.navbit.toimplement.NavBitNavigationState
import se.quidbit.navbit.updated.internal.MainHolder
import se.quidbit.navbit.updated.internal.NewBitController
import se.quidbit.navbit.updated.internal.NewBitControllerFactory
import se.quidbit.navbit.updated.internal.QueuedInteraction
import se.quidbit.navbit.updated.types.InteractionReceiver
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

abstract class NewBitActivity<I : NavBitInteraction, S : NavBitNavigationState>(
    private val interactionHandler: NewBitInteractionHandler<I, S>,
    private val navigationStateHandler: NewBitNavigationStateHandler<S>,
    private val screenHandler: NewBitScreenHandler<I, S>
)  : ComponentActivity() {

    private lateinit var viewModel : NewBitController<I, S>

    // Interaction handling
    // --------------------------------------------------------
    @Volatile
    private var isProcessingInteraction = false
    private val interactionQueue : BlockingQueue<QueuedInteraction<I>> = LinkedBlockingQueue()
    private var interactionJob : Job? = null

    private lateinit var interactionReceiver : InteractionReceiver<I>

    // --------------

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
                    val interaction = interactionQueue.take()
                    isProcessingInteraction = true
                    viewModel.processInteraction(this@NewBitActivity, interaction)
                    isProcessingInteraction = false
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

    // --------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        instance = this
        interactionReceiver = InteractionReceiver(interactionQueue)

        enableEdgeToEdge()
        setContent {
            // Prepare the main state and controller of the app
            viewModel = viewModel(
                factory =  NewBitControllerFactory(interactionHandler, navigationStateHandler)
            )

            // Display the screen
            MainHolder(this, interactionReceiver, viewModel, screenHandler)

            // Setup back press handling
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    interactionQueue.add(QueuedInteraction.Back())
                }
            })
        }
    }

    companion object {
        private lateinit var instance : NewBitActivity<*, *>
        fun <I : NavBitInteraction, S : NavBitNavigationState> getNavBitInstance() : NewBitActivity<I, S> {
            return instance as NewBitActivity<I, S>
        }
    }
}