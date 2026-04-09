package com.mockdonalds.app.konsist.circuit

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.core.spec.style.BehaviorSpec

/**
 * Validates Circuit-specific conventions:
 * - Events must be sealed class (not interface) for iOS interop
 * - Screen objects must reside in api/navigation and carry @Parcelize
 * - ProtectedScreen implementations must reside in api/navigation
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
                .assertTrue { it.resideInPath("..impl/presentation..") }
        }
    }

    Given("Circuit screen declarations") {
        val productionScreens = Konsist.scopeFromProject()
            .objects()
            .filter {
                it.hasParent { p ->
                    p.name == "Screen" || p.name == "ProtectedScreen" || p.name == "TabScreen"
                }
            }
            .filter {
                !it.resideInPath("..test..") &&
                    !it.resideInPath("..Test..") &&
                    !it.resideInPath("..commonTest..") &&
                    !it.resideInPath("..androidTest..") &&
                    !it.resideInPath("..androidDeviceTest..")
            }

        Then("all Screen objects should reside in api navigation packages") {
            productionScreens.assertTrue { it.resideInPath("..api..") }
        }

        Then("all Screen objects should have @Parcelize annotation") {
            productionScreens.assertTrue { it.hasAnnotation { a -> a.name == "Parcelize" } }
        }
    }

    Given("TabScreen declarations") {
        Then("TabScreen implementations should have a tag property") {
            Konsist.scopeFromProject()
                .objects()
                .filter { it.hasParent { p -> p.name == "TabScreen" } }
                .filter {
                    !it.resideInPath("..test..") &&
                        !it.resideInPath("..Test..") &&
                        !it.resideInPath("..commonTest..") &&
                        !it.resideInPath("..androidTest..") &&
                        !it.resideInPath("..androidDeviceTest..")
                }
                .assertTrue(
                    additionalMessage = "TabScreen objects must override the tag property — " +
                        "this tag is the single source of truth for tab identification " +
                        "across Android, iOS Kotlin, and Swift.",
                ) { it.hasProperty { p -> p.name == "tag" } }
        }
    }

    Given("ProtectedScreen declarations") {
        Then("ProtectedScreen implementations should reside in api navigation packages") {
            Konsist.scopeFromProject()
                .objects()
                .filter { it.hasParent { p -> p.name == "ProtectedScreen" } }
                .filter {
                    !it.resideInPath("..test..") &&
                        !it.resideInPath("..Test..") &&
                        !it.resideInPath("..commonTest..") &&
                        !it.resideInPath("..androidTest..") &&
                        !it.resideInPath("..androidDeviceTest..")
                }
                .assertTrue(
                    additionalMessage = "ProtectedScreen objects must reside in " +
                        "api/navigation packages — same as regular Screen objects. " +
                        "ProtectedScreen is a marker interface that triggers auth " +
                        "interception via AuthInterceptor.",
                ) { it.resideInPath("..api..") }
        }
    }


})
