# NavBit
NavBit is a Android library that serves as the core backbone of your app. It efficiently handles navigation between views, managing app states, and updating the UI seamlessly, ensuring a smooth and responsive user experience.

## Structure

An app built with NavBit contains three pieces of data:
 - `Interaction` - What the user can do
 - `NavigationState` - The different states the app can be in
 - `ScreenData` - The different type of screens that can be shown

 These three are in turn manipulated using the three handler classes:
 - `InteractionHandler`
 - `NavigationStateHandler`
 - `ScreenDataHandler`

## Flow

In general, the flow of the app is:
    `Interaction` -> `NavigationState` -> `ScreenData`

The app can emit Interactions when for example a user touches a button.

This Interaction is then handled by NavBit:
    `Interaction` + current `NavigationState` = new `NavigationState`

Depending on the new state, the screen is either updated or transitioned to a new screen.

The new `NavigationState` data is used to generate the `ScreenData`, what is to be displayed, which is then shown.

## Example Usage

The main activity of the app needs to extend `NavBitActivity`:

```
class BaseActivity : NavBitActivity<Interaction, NavigationState, ScreenData>(
    interactionHandler,
    stateHandler,
    screenHandler
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeNavBit()
    }

    companion object {
        // Retain all handlers between rotations (in which this activity is recreated)
            // Especially important for the stateHandler so the app state is not lost
        val interactionHandler = InteractionHandler()
        val stateHandler = NavigationStateHandler()
        val screenHandler = ScreenHandler()

        // Allow easy access to NavBit from anywhere in the app
        fun getNavBit() : NavBitActivity<Interaction, NavigationState, ScreenData> {
            return getNavBitInstance()
        }
    }
}
```
In order to extend `NavBitActivity`, six classes needs be implemented, defining all the data and logic of the application:

***Data***
```
class Interaction : NavBitInteraction()
class NavigationState : NavBitNavigationState()
class ScreenData : NavBitScreenData()
```
**Handlers**
```
class InteractionHandler : NavBitInteractionHandler<Interaction>()
class NavigationStateHandler : NavBitNavigationStateHandler()
class ScreenHandler : NavBitScreenGenerator<ScreenData>()
```

## Other Info
 - When posting work inside Screens, use the functions  `screenPost()` and  `screenPostDelay()`, which make sure to run them on the appropriate thread (important for View manipulation)
 - During slow transitions, a spinning wheel appears. If your Screen already has an animating progress bar present, make sure to name it `R.id.loading`. If present and visible, no spinning wheel is shown to preven.t a duplicate.