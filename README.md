# NavBit
NavBit is a Android library that serves as the core backbone of your app. It efficiently handles navigation between views, managing app states, and updating the UI seamlessly, ensuring a smooth and responsive user experience.

## Structure

An app built with NavBit contains three pieces of data:
 - Interaction - What the user can do
 - NavigationState - The different states the app can be in
 - ScreenData - The different type of screens that can be shown

 These three are in turn manipulated using the three handler classes:
 - InteractionHandler
 - NavigationStateHandler
 - ScreenDataHandler

## Flow

In general, the flow of the app is:
    Interaction -> NavigationState -> ScreenData

The app can emit Interactions when for example a user touches a button.

This Interaction is then handled by NavBit:
    Interaction + current NavigationState = new NavigationState

Depending on the new state, the screen is either updated or transitioned to a new screen.

The new NavigationState data is used to generate the ScreenData, what is to be displayed, which is then shown.

## Example Usage

The main activity of the app needs to extend NavBitActivity:

```
class BaseActivity : NavBitActivity<Interaction, NavigationState, ScreenData>(
    InteractionHandler(),
    NavigationStateHandler(),
    ScreenHandler()
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Perform other initializations here required before using the app

        initializeNavigation()
    }

    companion object {
        fun getNavBit() : NavBitActivity<Interaction, NavigationState, ScreenData> {
            return getNavBitInstance()
        }
    }
}
```

In order to do that, six classes needs be implemented, defining all the data and logic of the application:

```
class Interaction : NavBitInteraction()
class NavigationState : NavBitNavigationState()
class ScreenData : NavBitScreenData()

class InteractionHandler : NavBitInteractionHandler<Interaction>()
class NavigationStateHandler : NavBitNavigationStateHandler()
class ScreenHandler : NavBitScreenGenerator<ScreenData>()
```