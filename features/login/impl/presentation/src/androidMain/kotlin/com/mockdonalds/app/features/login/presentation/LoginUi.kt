package com.mockdonalds.app.features.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mockdonalds.app.core.theme.MockDimens
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.core.theme.isCompactHeight
import com.mockdonalds.app.features.login.api.navigation.LoginScreen
import com.mockdonalds.app.features.login.api.ui.LoginTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(LoginScreen::class, AppScope::class)
@Inject
@Composable
fun LoginUi(state: LoginUiState, modifier: Modifier = Modifier) {
    val landscape = isCompactHeight()
    var showSignInDialog by remember { mutableStateOf(false) }

    if (showSignInDialog) {
        AlertDialog(
            onDismissRequest = { showSignInDialog = false },
            modifier = Modifier.testTag(LoginTestTags.SIGN_IN_DIALOG),
            title = { Text("Sign In") },
            text = {
                Text(
                    "Send a magic link to ${state.email.ifEmpty { "your email" }}?",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSignInDialog = false
                    state.eventSink(LoginEvent.SignInConfirmed)
                }) {
                    Text("Send Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignInDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MockDimens.SpacingXxl, vertical = MockDimens.SpacingXl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .padding(vertical = MockDimens.SpacingSm)
                    .width(48.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .testTag(LoginTestTags.DRAG_HANDLE),
            )

            Spacer(modifier = Modifier.height(MockDimens.SpacingXl))

            if (landscape) {
                // Two-column: branding left, form + social right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingXxl),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        BrandingSection(
                            logoUrl = state.logoUrl,
                            modifier = Modifier.testTag(LoginTestTags.BRANDING),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingXl),
                    ) {
                        LoginForm(state = state, onSignInClick = { showSignInDialog = true })
                        OrDivider()
                        SocialButtons(state = state)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(MockDimens.SpacingMd))

                BrandingSection(
                    logoUrl = state.logoUrl,
                    modifier = Modifier.testTag(LoginTestTags.BRANDING),
                )

                Spacer(modifier = Modifier.height(MockDimens.SpacingXxxl))

                LoginForm(state = state, onSignInClick = { showSignInDialog = true })

                Spacer(modifier = Modifier.height(MockDimens.SpacingXxxl))

                OrDivider()

                Spacer(modifier = Modifier.height(MockDimens.SpacingXxxl))

                SocialButtons(state = state)
            }

            Spacer(modifier = Modifier.height(MockDimens.SpacingXxl))
        }
    }
}

@Composable
private fun BrandingSection(logoUrl: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg),
    ) {
        AsyncImage(
            model = logoUrl,
            contentDescription = "MockDonalds Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(64.dp),
        )
        Text(
            text = "MockDonalds",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-1).sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun LoginForm(state: LoginUiState, onSignInClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg)) {
        // Email Field
        Column(verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingSm)) {
            Text(
                text = "EMAIL ADDRESS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.padding(start = MockDimens.SpacingXs),
            )
            TextField(
                value = state.email,
                onValueChange = { state.eventSink(LoginEvent.EmailChanged(it)) },
                placeholder = {
                    Text(
                        text = "gourmet@night.com",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(MockDimens.RadiusMd),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.secondary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag(LoginTestTags.EMAIL_INPUT),
            )
        }

        Spacer(modifier = Modifier.height(MockDimens.SpacingSm))

        // Sign In Button
        Button(
            onClick = onSignInClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag(LoginTestTags.SIGN_IN_BUTTON),
            shape = RoundedCornerShape(MockDimens.RadiusMd),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MockDonaldsTheme.extendedColors.primaryDark,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Sign In",
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

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
        )
        Text(
            text = "OR CONTINUE WITH",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.padding(horizontal = MockDimens.SpacingLg),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
        )
    }
}

@Composable
private fun SocialButtons(state: LoginUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg),
    ) {
        SocialButton(
            label = "GOOGLE",
            icon = "G",
            onClick = { state.eventSink(LoginEvent.GoogleSignInClicked) },
            modifier = Modifier
                .weight(1f)
                .testTag(LoginTestTags.GOOGLE_BUTTON),
        )
    }
}

@Composable
private fun SocialButton(
    label: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(MockDimens.RadiusMd))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

