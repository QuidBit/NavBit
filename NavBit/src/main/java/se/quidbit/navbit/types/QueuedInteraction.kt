package se.quidbit.navbit.types

import se.quidbit.navbit.toimplement.NavBitInteraction

// NOTE: Should ideally be internal, but must be exposed since it used in InteractionReceiver

sealed class QueuedInteraction<I : NavBitInteraction> {
    class Back<I : NavBitInteraction>: QueuedInteraction<I>()
    data class Custom<I : NavBitInteraction>(val interaction: I) : QueuedInteraction<I>()
}