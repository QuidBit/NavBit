# NavBit
NavBit is a Android library that serves as the core backbone of your app. It efficiently handles managing app states, updating on changes from outside sources, navigation between screens (including sheets and popups), ensuring a smooth and responsive user experience. 

Built on Compose, in which all UI is defined using Composables.

## Structure

An app built with NavBit contains two pieces of data:
 - `Interaction` - What the user can do
 - `NavigationState` - The different states the app can be in

 All logic is then handled in three different classes:
 - `InteractionHandler`
 - `NavigationStateHandler`
 - `ScreenHandler`

## Flow

In general, the flow of the app is:
    `Interaction` -> `NavigationState` -> `@Composable`

The app can emit Interactions when for example a user touches a button.

This Interaction is then handled by NavBit:
    `Interaction` + current `NavigationState` = new `NavigationState`

Depending on the new state, the screen is either updated or transitioned to a new screen.

The new `NavigationState` data is used to generate the corresponding `@Composable`, which is then shown.

## Example Usage

The main activity of the app needs to extend `NavBitActivity`:

```
class BaseActivity : NavBitActivity<Interaction, NavigationState> (
    InteractionHandler(),
    NavigationStateHandler(),
    ScreenHandler(),
) {
    // Allow easy access to NavBit from anywhere in the app
    companion object {
        fun getNavBit() : NavBitActivity<Interaction, NavigationState> {
            return getNavBitInstance()
        }
    }
}
```
In order to extend `NavBitActivity`, five classes needs be implemented, defining all the data and logic of the application:

***Data***
```
class Interaction : NavBitInteraction()
class NavigationState : NavBitNavigationState()
```
**Handlers**
```
class InteractionHandler : NavBitInteractionHandler<Interaction>()
class NavigationStateHandler : NavBitNavigationStateHandler<NavigationState>()
class ScreenHandler : NavBitScreenHandler<Interaction, NavigationState>()
```

## Other info

A demo app is included, showing general navigation and state handling, as well generating and updating screens.