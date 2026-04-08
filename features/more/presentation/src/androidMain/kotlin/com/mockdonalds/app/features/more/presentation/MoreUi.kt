package com.mockdonalds.app.features.more.presentation

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mockdonalds.app.core.theme.MockDimens
import com.mockdonalds.app.core.theme.MockDonaldsTheme
import com.mockdonalds.app.core.theme.adaptiveBottomBarPadding
import com.mockdonalds.app.features.more.api.domain.MoreMenuItem
import com.mockdonalds.app.features.more.api.navigation.MoreScreen
import com.mockdonalds.app.features.more.api.ui.MoreTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(MoreScreen::class, AppScope::class)
@Inject
@Composable
fun MoreUi(state: MoreUiState, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    // Login bottom sheet
    LoginBottomSheet(
        loginSheet = state.loginSheet,
        onEmailChanged = { state.eventSink(MoreEvent.LoginEmailChanged(it)) },
        onSignInConfirmed = { state.eventSink(MoreEvent.LoginSignInConfirmed) },
        onDismissed = { state.eventSink(MoreEvent.LoginSheetDismissed) },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = MockDimens.SpacingXl)
            .padding(bottom = adaptiveBottomBarPadding())
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(40.dp),
    ) {
        // User Profile Section
        state.userProfile?.let { profile ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(MockDimens.RadiusMd))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .testTag(MoreTestTags.PROFILE_SECTION)
                    .clickable { state.eventSink(MoreEvent.ProfileClicked) }
                    .padding(MockDimens.SpacingXl),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val primary = MaterialTheme.colorScheme.primary
                    val secondary = MaterialTheme.colorScheme.secondary
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .drawWithContent {
                                drawCircle(
                                    brush = Brush.sweepGradient(
                                        0f to primary,
                                        0.45f to secondary,
                                        0.65f to secondary,
                                        1f to primary,
                                    ),
                                    style = Stroke(width = 3.dp.toPx()),
                                )
                                drawContent()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = profile.avatarUrl,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                        )
                    }
                    Column {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "${profile.tier} • ${profile.points}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        )
                    }
                }
                Text(text = ">", color = MaterialTheme.colorScheme.secondary)
            }
        }

        // Menu List
        if (state.menuItems.isNotEmpty()) {
            Column(
                modifier = Modifier.testTag(MoreTestTags.MENU_LIST),
                verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingXs),
            ) {
                state.menuItems.forEachIndexed { index, item ->
                    MenuItemRow(
                        item = item,
                        isOdd = index % 2 == 0,
                        onClick = { state.eventSink(MoreEvent.MenuItemClicked(item.id)) },
                        modifier = Modifier.testTag("${MoreTestTags.MENU_ITEM}-${item.id}"),
                    )
                }
            }
        }

        // Join the Team Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(MockDimens.RadiusMd))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .testTag(MoreTestTags.JOIN_TEAM_BANNER),
        ) {
            @Suppress("MaxLineLength")
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDtoKS4itUpfiQzJW9FGblMq9_3wzFqLR5CaS2eM929pYK-KWYvYqQiXcGfWz8ZVUlPcU1hmo0qseeHENBB_sP17bYCskdZ9VPfrIdYy7P63B5tGH6kgBQmn_i0RAanG3-y3r2F2U9G7IdqC5pgPPtd0CVRV-7jjEKtk7VGHqiwH40htvVQRSEZSqoJZ0hnlFw0FvqVNCM5k7pn_eI5N9zunkr86XGaaEl2qddd7Zld_sJOFnulnp_tJ8eqVDNAqvGdId-JcKf1t2s",
                contentDescription = "Kitchen Staff",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black,
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .padding(MockDimens.SpacingXxl),
                verticalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg),
            ) {
                Text(
                    text = "Join the Team",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 36.sp,
                )
                Text(
                    text = "Craft the future of late-night dining with us. We're looking for culinary masters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier.padding(top = MockDimens.SpacingSm),
                    shape = RoundedCornerShape(MockDimens.RadiusSm),
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MockDonaldsTheme.extendedColors.primaryDark,
                                    ),
                                ),
                            )
                            .padding(horizontal = MockDimens.SpacingXl, vertical = MockDimens.SpacingMd),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "View Openings",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemRow(item: MoreMenuItem, isOdd: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MockDimens.RadiusMd))
            .background(if (isOdd) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = item.icon, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(text = ">", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginBottomSheet(
    loginSheet: LoginSheetState?,
    onEmailChanged: (String) -> Unit,
    onSignInConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSignInDialog by remember { mutableStateOf(false) }

    // Animate show/hide based on loginSheet state
    LaunchedEffect(loginSheet) {
        if (loginSheet != null) {
            sheetState.show()
        } else {
            sheetState.hide()
        }
    }

    if (showSignInDialog) {
        AlertDialog(
            onDismissRequest = { showSignInDialog = false },
            title = { Text("Sign In") },
            text = {
                Text(
                    "Send a magic link to ${loginSheet?.email?.ifEmpty { "your email" } ?: "your email"}?",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSignInDialog = false
                    scope.launch {
                        sheetState.hide()
                        onSignInConfirmed()
                    }
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

    if (loginSheet != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissed,
            sheetState = sheetState,
        ) {
            LoginSheetContent(
                loginSheet = loginSheet,
                onEmailChanged = onEmailChanged,
                onSignInClick = { showSignInDialog = true },
            )
        }
    }
}

@Composable
private fun LoginSheetContent(
    loginSheet: LoginSheetState,
    onEmailChanged: (String) -> Unit,
    onSignInClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MockDimens.SpacingXxl)
            .padding(bottom = MockDimens.SpacingXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .padding(vertical = MockDimens.SpacingSm)
                .width(48.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        )

        Spacer(modifier = Modifier.height(MockDimens.SpacingXl))

        // Branding
        Text(
            text = "MockDonalds",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = (-1).sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(MockDimens.SpacingXxxl))

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
                value = loginSheet.email,
                onValueChange = onEmailChanged,
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
                    .height(56.dp),
            )
        }

        Spacer(modifier = Modifier.height(MockDimens.SpacingLg))

        // Sign In Button
        Button(
            onClick = onSignInClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
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

        Spacer(modifier = Modifier.height(MockDimens.SpacingXxl))

        // Divider
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

        Spacer(modifier = Modifier.height(MockDimens.SpacingXxl))

        // Google Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(MockDimens.RadiusMd))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable { },
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MockDimens.SpacingMd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "G",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "GOOGLE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
