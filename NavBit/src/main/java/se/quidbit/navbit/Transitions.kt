package se.quidbit.navbit

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.TimeInterpolator
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.provider.Settings
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator

sealed class TransitionType {
    object Sheet : TransitionType()
    object PopUp : TransitionType()
    sealed class Full : TransitionType() {
        object Fade : Full()
        object Slide : Full()
    }

    fun previousIsVisible() : Boolean {
        return when (this) {
            is Sheet,
            is PopUp -> true
            is Full -> false
        }
    }
}

enum class TransitionDirection {
    Forward,
    Backward
}

const val BASE_TRANSITION_LENGTH = 250f

data class ScreenTransition (
    var type : TransitionType,
    var direction : TransitionDirection,
) {
    companion object {
        fun getBaseTransitionLength(context: Context) : Float {
            val transitionScale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )

            return BASE_TRANSITION_LENGTH * transitionScale
        }
    }

    private fun getTransitionLength(context: Context, leaving : Boolean): Long {

        val typeScale =
            // Faster popups
            if (type == TransitionType.PopUp) { 0.5f }

            // Slow down the leaving when sliding forward, to give the new screen more time to enter
                // Here we basically assume that backing is to a screen that already exists, which is not always true
                // However, slowing any backing in the other direction is too visible due to the nature of the slide animation
            else if (type is TransitionType.Full.Slide && leaving && direction == TransitionDirection.Forward) { 2f }

            // Default is just the base speed
            else { 1f }

        return (typeScale * getBaseTransitionLength(context)).toLong()
    }

    private fun getInterpolator(leaving : Boolean) : TimeInterpolator {
        return if (type is TransitionType.Full.Slide && leaving && direction == TransitionDirection.Forward) {
            DecelerateInterpolator()
        } else {
            LinearInterpolator()
        }
    }

    fun asEnteringAnimation(view: View) : ObjectAnimator {
        val animation = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, view.translationY, 0.0f),
            PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 1.0f),
        )

        animation.duration = getTransitionLength(view.context, false)
        animation.interpolator = getInterpolator(false)
        return animation
    }

    fun asLeavingAnimation(view: View) : ObjectAnimator {
        val animation = when(type) {
            TransitionType.Full.Slide -> when (direction) {
                TransitionDirection.Forward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 0.8f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 0.8f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 1.0f),
                    )

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
                    )
            }
            TransitionType.Full.Fade ->  when (direction) {
                TransitionDirection.Forward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 1.0f),
                    )

                TransitionDirection.Backward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f),
                    )
                }
            TransitionType.Sheet -> when (direction) {
                TransitionDirection.Forward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1.0f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 1.0f),
                    )

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
                    )
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
                    )

                TransitionDirection.Backward ->
                    ObjectAnimator.ofPropertyValuesHolder(
                        view,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 0.8f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 0.8f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0.0f),
                        PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, view.translationY, 0.0f),
                        PropertyValuesHolder.ofFloat(View.ALPHA, view.alpha, 0.0f)
                    )
            }
        }

        animation.duration = getTransitionLength(view.context, true)
        animation.interpolator = getInterpolator(true)
        return animation
    }
}
