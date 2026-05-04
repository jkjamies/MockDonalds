package com.mockdonalds.app.features.kiosk.identify.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mockdonalds.app.core.centerpost.CenterPostDispatchers
import com.mockdonalds.app.core.centerpost.CenterPostResult
import com.mockdonalds.app.core.presentation.centerpost.collectAsState
import com.mockdonalds.app.core.presentation.centerpost.rememberCenterPost
import com.mockdonalds.app.features.kiosk.identify.api.domain.ContinueAsGuest
import com.mockdonalds.app.features.kiosk.identify.api.domain.GetIdentifyContent
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyByPhoneNumber
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyByQrCode
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyResult
import com.mockdonalds.app.features.kiosk.identify.api.navigation.IdentifyScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(IdentifyScreen::class, AppScope::class)
@Inject
@Composable
fun IdentifyPresenter(
    screen: IdentifyScreen,
    navigator: Navigator,
    getIdentifyContent: GetIdentifyContent,
    identifyByPhoneNumber: IdentifyByPhoneNumber,
    identifyByQrCode: IdentifyByQrCode,
    continueAsGuest: ContinueAsGuest,
    dispatchers: CenterPostDispatchers,
): IdentifyUiState {
    val centerPost = rememberCenterPost(dispatchers)
    getIdentifyContent(Unit)
    val content by getIdentifyContent.collectAsState()

    var phoneInput by rememberSaveable { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val eventSink: (IdentifyEvent) -> Unit = remember(screen) {
        sink@{ event ->
            if (isSubmitting) return@sink
            when (event) {
                is IdentifyEvent.DigitPressed -> {
                    if (phoneInput.length < MaxPhoneDigits) {
                        phoneInput += event.digit
                    }
                }
                IdentifyEvent.DeletePressed -> {
                    if (phoneInput.isNotEmpty()) {
                        phoneInput = phoneInput.dropLast(1)
                    }
                }
                IdentifyEvent.SubmitPhonePressed -> {
                    if (phoneInput.length < MinPhoneDigits) {
                        errorMessage = "Enter at least $MinPhoneDigits digits"
                        return@sink
                    }
                    isSubmitting = true
                    centerPost {
                        handleResult(identifyByPhoneNumber(phoneInput), navigator, screen) { msg ->
                            errorMessage = msg
                            isSubmitting = false
                        }
                    }
                }
                is IdentifyEvent.QrCodeDetected -> {
                    isSubmitting = true
                    centerPost {
                        handleResult(identifyByQrCode(event.payload), navigator, screen) { msg ->
                            errorMessage = msg
                            isSubmitting = false
                        }
                    }
                }
                IdentifyEvent.SkipPressed -> {
                    isSubmitting = true
                    centerPost {
                        handleResult(continueAsGuest(Unit), navigator, screen) { msg ->
                            errorMessage = msg
                            isSubmitting = false
                        }
                    }
                }
                IdentifyEvent.DismissError -> errorMessage = null
            }
        }
    }

    return IdentifyUiState(
        phoneInput = phoneInput,
        isSubmitting = isSubmitting,
        errorMessage = errorMessage,
        skipEnabled = content?.skipEnabled ?: true,
        qrScannerEnabled = content?.qrScannerEnabled ?: true,
        countryDialCode = content?.countryDialCode ?: "+1",
        eventSink = eventSink,
    )
}

private fun handleResult(
    result: CenterPostResult<IdentifyResult>,
    navigator: Navigator,
    screen: IdentifyScreen,
    onFailure: (String) -> Unit,
) {
    when (result) {
        is CenterPostResult.Success -> when (val payload = result.data) {
            is IdentifyResult.Identified, IdentifyResult.GuestSession -> {
                val next = screen.next
                if (next != null) {
                    navigator.resetRoot(next)
                } else {
                    navigator.pop()
                }
            }
            is IdentifyResult.Failed -> onFailure(payload.reason)
        }
        is CenterPostResult.Failure -> onFailure("Identify failed — please try again.")
    }
}

private const val MinPhoneDigits = 7
private const val MaxPhoneDigits = 10
