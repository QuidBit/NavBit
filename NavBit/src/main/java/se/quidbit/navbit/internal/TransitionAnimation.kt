package se.quidbit.navbit.internal

import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.TwoWayConverter
import se.quidbit.navbit.types.TransitionTransform

internal data class TransitionAnimation(
    val offset: Float = 0f,
    val scale: Float = 1f,
    val alpha: Float = 1f,
    val z : Float = 0f
) {

    fun toTransform() : TransitionTransform {
        return TransitionTransform(offset, scale, alpha)
    }

    companion object {
        fun new(z : Float, transform: TransitionTransform) : TransitionAnimation {
            return TransitionAnimation(transform.offset, transform.scale, transform.alpha, z)
        }
    }
}

internal class TransitionAnimationTypeConverter :
    TwoWayConverter<TransitionAnimation, AnimationVector4D> {
    override val convertToVector: (TransitionAnimation) -> AnimationVector4D
        get() = { v ->
            AnimationVector4D(v.offset, v.scale, v.alpha, v.z)
        }
    override val convertFromVector: (AnimationVector4D) -> TransitionAnimation
        get() = { v ->
            TransitionAnimation(v.v1, v.v2, v.v3, v.v4)
        }
}

