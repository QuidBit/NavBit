package se.quidbit.navbit

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.provider.Settings

sealed class TransitionType {
    object Sheet : TransitionType()
    object PopUp : TransitionType()
    sealed class Full : TransitionType() {
        object Fade : Full()
        object Slide : Full()
    }

    fun hideOnExit() : Boolean {
        return when (this) {
            is Sheet,
            is PopUp -> false
            is Full -> true
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
        fun getTransitionLength(context: Context): Long {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
            return (250 * scale).toLong()
        }
    }

    fun asEnteringAnimation(view: View) : ObjectAnimator {
        return ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, view.translationY, 0.0f),
            PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 1.0f),
        ).setDuration(getTransitionLength(view.context))
    }

    fun asLeavingAnimation(view: View) : ObjectAnimator {
        val transitionLength = getTransitionLength(view.context)

        return when(type) {
            TransitionType.Full.Slide -> when (direction) {
                TransitionDirection.Forward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 0.8f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 0.8f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 1.0f),
                    ).setDuration(transitionLength)

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
                    ).setDuration(transitionLength)
            }
            TransitionType.Full.Fade ->
                ObjectAnimator.ofPropertyValuesHolder(
                    view,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                    PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f),
                ).setDuration(transitionLength)
            TransitionType.Sheet -> when (direction) {
                TransitionDirection.Forward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 1.0f),
                    ).setDuration(transitionLength)

                TransitionDirection.Backward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(
                            View.TRANSLATION_Y,
                            view.translationY,
                            Resources.getSystem().displayMetrics.heightPixels.toFloat()
                        ),
                        PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 1.0f)
                    ).setDuration(transitionLength)
            }
            TransitionType.PopUp -> when (direction) {
                TransitionDirection.Forward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 1.0f),
                    ).setDuration(transitionLength)

                TransitionDirection.Backward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 0.8f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 0.8f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, view.translationY, 0.0f),
                        PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 0.0f)
                    ).setDuration(transitionLength)
            }
        }
    }
}
