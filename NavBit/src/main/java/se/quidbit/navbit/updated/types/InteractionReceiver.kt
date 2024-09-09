package se.quidbit.navbit.updated.types

import se.quidbit.navbit.toimplement.NavBitInteraction
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class InteractionReceiver<I : NavBitInteraction> (
    private val interactionQueue : BlockingQueue<I> = LinkedBlockingQueue()
) {
    fun send(interaction: I) {
        interactionQueue.add(interaction)
    }
}