package com.mockdonalds.app.konsist.circuit

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates Circuit-specific conventions:
 * - Events must be sealed class (not interface) for iOS interop
 * - Screen objects must reside in api/navigation and carry @Parcelize
 */
class CircuitConventionsTest : BehaviorSpec({

    Given("Circuit event types") {
        Then("no sealed interface should be named *Event — must be sealed class for iOS interop") {
            val sealedEventInterfaces = Konsist.scopeFromProject()
                .interfaces()
                .withNameEndingWith("Event")
                .filter { it.hasSealedModifier }

            assert(sealedEventInterfaces.isEmpty()) {
                val violators = sealedEventInterfaces.joinToString { "${it.name} (${it.path})" }
                "Circuit events must be sealed class (not sealed interface) for iOS interop — " +
                    "sealed interface exports as Obj-C protocol which breaks Event.Subtype() syntax in Swift. " +
                    "Violators: $violators"
            }
        }

        Then("all Event sealed classes should reside in presentation modules") {
            Konsist.scopeFromProject()
                .classes()
                .withNameEndingWith("Event")
                .filter { it.hasSealedModifier }
                .assertTrue { it.resideInPath("..presentation..") }
        }
    }

    Given("Circuit screen declarations") {
        Then("all Screen objects should reside in api navigation packages") {
            Konsist.scopeFromProject()
                .objects()
                .filter { it.hasParent { p -> p.name == "Screen" } }
                .assertTrue { it.resideInPath("..api..") }
        }

        Then("all Screen objects should have @Parcelize annotation") {
            Konsist.scopeFromProject()
                .objects()
                .filter { it.hasParent { p -> p.name == "Screen" } }
                .assertTrue { it.hasAnnotation { a -> a.name == "Parcelize" } }
        }
    }
})
