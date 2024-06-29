package se.quidbit.navbit

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.res.Resources
import android.view.View

sealed class TransitionType {
    object Sheet : TransitionType()
    sealed class Full : TransitionType() {
        object Fade : Full()
        object Slide : Full()
    }

    fun hideOnExit() : Boolean {
        return when (this) {
            is Sheet -> false
            else -> true
        }
    }
}

enum class TransitionDirection {
    Forward,
    Backward
}

data class ScreenTransition (
    var type : TransitionType,
    var direction : TransitionDirection,
) {
    companion object {
        const val TRANSITION_LENGTH = 250L
    }

    fun asEnteringAnimation(view: View) : ObjectAnimator {
        return ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, view.translationY, 0.0f),
            PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 1.0f),
        ).setDuration(TRANSITION_LENGTH)
    }


    fun asLeavingAnimation(view: View) : ObjectAnimator {

        return when(type) {
            TransitionType.Full.Slide -> when (direction) {
                TransitionDirection.Forward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 0.8f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 0.8f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 1.0f),
                    ).setDuration(TRANSITION_LENGTH)

                TransitionDirection.Backward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
                        PropertyValuesHolder.ofFloat(
                            View.TRANSLATION_X,
                            view.translationX,
                            Resources.getSystem().displayMetrics.widthPixels.toFloat()
                        ),
                        PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 1.0f)
                    ).setDuration(TRANSITION_LENGTH)
            }
            TransitionType.Full.Fade ->
                ObjectAnimator.ofPropertyValuesHolder(
                    view,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                    PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f),
                ).setDuration(TRANSITION_LENGTH)
            TransitionType.Sheet -> when (direction) {
                TransitionDirection.Forward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 1.0f),
                    ).setDuration(TRANSITION_LENGTH)

                TransitionDirection.Backward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(
                            View.TRANSLATION_Y,
                            view.translationY,
                            Resources.getSystem().displayMetrics.heightPixels .toFloat()
                        ),
                        PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 1.0f)
                    ).setDuration(TRANSITION_LENGTH)
            }
        }
    }
}
