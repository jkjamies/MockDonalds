package com.mockdonalds.app.features.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mockdonalds.app.core.theme.MockDimens
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.features.login.api.navigation.WelcomeScreen
import com.mockdonalds.app.features.login.api.ui.WelcomeTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(WelcomeScreen::class, AppScope::class)
@Inject
@Composable
fun WelcomeUi(state: WelcomeUiState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = MockDimens.SpacingXxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "M",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    fontSize = 72.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(100.dp)
                    .testTag(WelcomeTestTags.LOGO),
            )

            Spacer(modifier = Modifier.height(MockDimens.SpacingXxl))

            Text(
                text = "Welcome!",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag(WelcomeTestTags.TITLE),
            )

            Spacer(modifier = Modifier.height(MockDimens.SpacingMd))

            Text(
                text = "You're all set",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.testTag(WelcomeTestTags.SUBTITLE),
            )

            Spacer(modifier = Modifier.height(MockDimens.SpacingXxxl))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(MockDimens.RadiusMd))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MockDonaldsTheme.extendedColors.primaryDark,
                            ),
                        ),
                    )
                    .clickable { state.eventSink(WelcomeEvent.ContinueClicked) }
                    .testTag(WelcomeTestTags.CONTINUE_BUTTON),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = MockDonaldsTheme.extendedColors.onPrimaryButton,
                )
            }
        }
    }
}
