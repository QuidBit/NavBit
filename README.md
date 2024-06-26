# NavBit
NavBit is a comprehensive Android library that serves as the core backbone of your app, efficiently handling navigation between views, managing app states, and updating the UI seamlessly. It simplifies the development process by providing a robust framework for state management and view transitions, ensuring a smooth and responsive user experience.

## Usage

The main activity of the app needs to be a NavBitActivity:

```
class BaseActivity : NavBitActivity<ScreenData>(
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

In order to do that, four classes needs be implemented, defining all the logic of the application:

```
class InteractionHandler : NavBitInteractionHandler()
class NavigationStateHandler : NavBitNavigationStateHandler()
class ScreenData : NavBitScreenData()
class ScreenGenerator : NavBitScreenGenerator<ScreenData>()
```