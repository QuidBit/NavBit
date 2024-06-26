# NavBit
NavBit is a comprehensive Android library that serves as the core backbone of your app, efficiently handling navigation between views, managing app states, and updating the UI seamlessly. It simplifies the development process by providing a robust framework for state management and view transitions, ensuring a smooth and responsive user experience.

## Usage

The main activity of the app needs to extend NavBitActivity:

```
class BaseActivity : NavBitActivity<Interaction, ScreenData>(
    InteractionHandler(),
    NavigationStateHandler(),
    ScreenGenerator()
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Perform other initializations here required before using the app

        initializeNavigation()
    }
}
```

In order to do that, five classes needs be implemented, defining all the logic of the application:

```
class Interaction : NavBitInteraction()
class InteractionHandler : NavBitInteractionHandler<Interaction>()

class NavigationStateHandler : NavBitNavigationStateHandler()

class ScreenData : NavBitScreenData()
class ScreenGenerator : NavBitScreenGenerator<ScreenData>()
```