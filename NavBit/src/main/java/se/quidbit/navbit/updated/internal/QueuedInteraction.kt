package se.quidbit.navbit.updated.internal

import se.quidbit.navbit.toimplement.NavBitInteraction

sealed class QueuedInteraction<I : NavBitInteraction> {
    class Back<I : NavBitInteraction>: QueuedInteraction<I>()
    data class Custom<I : NavBitInteraction>(val interaction: I) : QueuedInteraction<I>()
}