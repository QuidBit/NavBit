package se.quidbit.navbit.types

import se.quidbit.navbit.toimplement.NavBitInteraction
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class InteractionReceiver<I : NavBitInteraction> (
    private val interactionQueue : BlockingQueue<QueuedInteraction<I>> = LinkedBlockingQueue()
) {
    fun send(interaction: I) {
        interactionQueue.add(QueuedInteraction.Custom(interaction))
    }

    fun sendBack() {
        interactionQueue.add(QueuedInteraction.Back())
    }
}