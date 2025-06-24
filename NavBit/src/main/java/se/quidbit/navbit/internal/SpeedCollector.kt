package se.quidbit.navbit.internal

import kotlin.math.min


const val SAMPLE_POINTS = 10
const val AVERAGE_SAMPLES = 5

class SpeedCollector {
    private var speeds : ArrayList<Float> = ArrayList()

    fun collect(speed: Float) {
        speeds.add(speed)
        if (speeds.size > SAMPLE_POINTS) {
            speeds.removeAt(0)
        }
    }

    fun completeForAverage() : Float {
        val averageSpeed =
            if (speeds.isNotEmpty()) {
                speeds
                    .takeLast(AVERAGE_SAMPLES)
                    .sum() / min(AVERAGE_SAMPLES, speeds.size)
            } else 0f

        speeds.clear()

        return averageSpeed
    }
}