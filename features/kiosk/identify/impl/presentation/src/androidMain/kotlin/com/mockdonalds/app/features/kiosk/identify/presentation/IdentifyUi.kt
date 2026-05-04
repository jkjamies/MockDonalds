package com.mockdonalds.app.features.kiosk.identify.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mockdonalds.app.core.theme.MockDimens
import com.mockdonalds.app.features.kiosk.identify.api.navigation.IdentifyScreen
import com.mockdonalds.app.features.kiosk.identify.api.ui.IdentifyTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@CircuitInject(IdentifyScreen::class, AppScope::class)
@Composable
fun IdentifyUi(state: IdentifyUiState, modifier: Modifier = Modifier) {
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            delay(ErrorAutodismissSeconds.seconds)
            state.eventSink(IdentifyEvent.DismissError)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(IdentifyTestTags.SCREEN),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            IdentifyHeader()

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(MockDimens.SpacingLg),
                horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg),
            ) {
                if (state.qrScannerEnabled) {
                    QrScannerPanel(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onSimulatedScan = {
                            state.eventSink(
                                IdentifyEvent.QrCodeDetected("simulated-payload-${currentMillis()}"),
                            )
                        },
                    )
                }
                PhoneEntryPanel(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    state = state,
                )
            }

            if (state.skipEnabled) {
                SkipCta(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MockDimens.SpacingLg, vertical = MockDimens.SpacingMd),
                    onClick = { state.eventSink(IdentifyEvent.SkipPressed) },
                )
            }
        }

        if (state.isSubmitting) {
            LoadingOverlay()
        }

        state.errorMessage?.let { msg ->
            ErrorBanner(
                message = msg,
                onDismiss = { state.eventSink(IdentifyEvent.DismissError) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = MockDimens.SpacingLg, start = MockDimens.SpacingLg, end = MockDimens.SpacingLg),
            )
        }
    }
}

@Composable
private fun IdentifyHeader() {
    Surface(
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier
            .fillMaxWidth()
            .height(HeaderHeightDp.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MockDimens.SpacingLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Sign in to earn points",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "or continue without an account",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun QrScannerPanel(
    modifier: Modifier = Modifier,
    onSimulatedScan: () -> Unit,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(MockDimens.RadiusLg))
            .testTag(IdentifyTestTags.QR_SCANNER),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MockDimens.SpacingLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingMd, Alignment.CenterVertically),
        ) {
            Text(
                text = "Scan rewards code",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Box(
                modifier = Modifier
                    .size(QrViewfinderSizeDp.dp)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(MockDimens.RadiusMd),
                    )
                    .padding(MockDimens.SpacingMd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "QR scanner placeholder",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            OutlinedButton(
                onClick = onSimulatedScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SimulateScanHeightDp.dp),
                shape = RoundedCornerShape(MockDimens.RadiusMd),
            ) {
                Text(
                    text = "Simulate scan",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "Hold your rewards QR code in front of the scanner.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PhoneEntryPanel(modifier: Modifier = Modifier, state: IdentifyUiState) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(MockDimens.RadiusLg)),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(MockDimens.SpacingLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingMd),
        ) {
            Text(
                text = "Enter phone number",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            PhoneDisplay(
                dialCode = state.countryDialCode,
                digits = state.phoneInput,
            )
            KeypadGrid(state = state, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PhoneDisplay(dialCode: String, digits: String) {
    val formatted = formatUsPhone(digits)
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .height(PhoneDisplayHeightDp.dp)
            .testTag(IdentifyTestTags.PHONE_DISPLAY),
        shape = RoundedCornerShape(MockDimens.RadiusMd),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MockDimens.SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingMd),
        ) {
            Text(
                text = dialCode,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = formatted.ifEmpty { "(   )    -    " },
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 56.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun KeypadGrid(state: IdentifyUiState, modifier: Modifier = Modifier) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("delete", "0", "submit"),
    )
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingSm),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingSm),
            ) {
                row.forEach { key ->
                    KeypadKey(
                        key = key,
                        state = state,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(key: String, state: IdentifyUiState, modifier: Modifier = Modifier) {
    when (key) {
        "delete" -> Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = modifier
                .clip(RoundedCornerShape(MockDimens.RadiusMd))
                .testTag(IdentifyTestTags.DELETE),
        ) {
            IconButton(
                onClick = { state.eventSink(IdentifyEvent.DeletePressed) },
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        "submit" -> Button(
            onClick = { state.eventSink(IdentifyEvent.SubmitPhonePressed) },
            modifier = modifier.testTag(IdentifyTestTags.SUBMIT),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(MockDimens.RadiusMd),
        ) {
            Text(
                text = "OK",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        else -> Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = modifier
                .clip(RoundedCornerShape(MockDimens.RadiusMd))
                .testTag("${IdentifyTestTags.KEYPAD_DIGIT_PREFIX}$key"),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = { state.eventSink(IdentifyEvent.DigitPressed(key.first())) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SkipCta(modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(SkipHeightDp.dp)
            .testTag(IdentifyTestTags.SKIP),
        shape = RoundedCornerShape(MockDimens.RadiusMd),
    ) {
        Text(
            text = "Continue without account",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .testTag(IdentifyTestTags.LOADING_OVERLAY),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(MockDimens.RadiusMd))
            .testTag(IdentifyTestTags.ERROR_BANNER),
        color = MaterialTheme.colorScheme.error,
    ) {
        Row(
            modifier = Modifier.padding(MockDimens.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingMd),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onError,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onError,
                )
            }
        }
    }
}

private fun formatUsPhone(digits: String): String = when {
    digits.isEmpty() -> ""
    digits.length <= 3 -> "(${digits}"
    digits.length <= 6 -> "(${digits.substring(0, 3)}) ${digits.substring(3)}"
    else -> "(${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6)}"
}

private fun currentMillis(): Long = kotlin.time.TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds

// Kiosk dimensions — sized for a vertical ~32" touchscreen at standing distance,
// not a phone. Keys, displays, and CTAs are large enough to read from 0.5m away
// and to tap reliably with no hand-stabilization (no resting wrist on a surface).
private const val HeaderHeightDp = 200
private const val PhoneDisplayHeightDp = 160
private const val SkipHeightDp = 140
private const val QrViewfinderSizeDp = 520
private const val SimulateScanHeightDp = 96
private const val ErrorAutodismissSeconds = 4
