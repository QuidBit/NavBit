package se.quidbit.navbit.toimplement

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
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
import se.quidbit.navbit.internal.ThemeViewModel
import se.quidbit.navbit.types.ThemeMode
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
    // Theme Mode support
    // ---------------------------------------------------------------------------------------------

    // Must be kept in a viewModel to retain between activity recreations (which happen if the user for example uses an action toggle to switch dark/light mode)
    private val themeViewModel: ThemeViewModel by viewModels()

    fun setThemeMode(mode : ThemeMode) {
        InfoLog.i("Setting ThemeMode: $mode]")
        themeViewModel.setThemeMode(mode)
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
            // ---------------------------------------------------------
            // Prepare the main state and controller of the app
            // ---------------------------------------------------------
            val controller : MasterController<I,S> = viewModel(
                factory =  MasterControllerFactory(interactionHandler, navigationStateHandler)
            )

            // ---------------------------------------------------------
            // Create a custom Configuration to support dark/light override
            // ---------------------------------------------------------
            val baseConfig = LocalConfiguration.current
            val isDark = themeViewModel.themeMode.value.isCurrentlyDark()

            val customConfig = remember(isDark) {
                Configuration(baseConfig).apply {
                    uiMode = if (isDark) {
                        (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_YES
                    } else {
                        (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
                    }
                }
            }

            val context = LocalContext.current
            val overriddenContext = remember(customConfig) {
                context.createConfigurationContext(customConfig)
            }

            // ---------------------------------------------------------
            // Display the screen
            // ---------------------------------------------------------
            CompositionLocalProvider(
                LocalConfiguration provides customConfig,
                LocalContext provides overriddenContext
            ) {
                val theme = screenHandler.getTheme(LocalContext.current, isSystemInDarkTheme())

                AppThemeHolder(theme) {
                    MainHolder(this, controller, screenHandler, interactionChannel, theme)
                }
            }
            // ---------------------------------

            masterController = controller

            // ---------------------------------
            // Setup back press handling
            // ---------------------------------
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