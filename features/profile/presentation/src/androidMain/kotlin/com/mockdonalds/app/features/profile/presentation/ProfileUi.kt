package com.mockdonalds.app.features.profile.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mockdonalds.app.core.theme.MockDimens
import com.mockdonalds.app.features.profile.api.navigation.ProfileScreen
import com.mockdonalds.app.features.profile.api.ui.ProfileTestTags
import com.slack.circuit.codegen.annotations.CircuitInject
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@CircuitInject(ProfileScreen::class, AppScope::class)
@Inject
@Composable
fun ProfileUi(state: ProfileUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(MockDimens.SpacingXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(MockDimens.SpacingXxl))

        AsyncImage(
            model = state.avatarUrl,
            contentDescription = "Profile avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .testTag(ProfileTestTags.AVATAR),
        )

        Spacer(modifier = Modifier.height(MockDimens.SpacingXl))

        Text(
            text = state.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag(ProfileTestTags.NAME),
        )

        Spacer(modifier = Modifier.height(MockDimens.SpacingSm))

        Text(
            text = state.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(ProfileTestTags.EMAIL),
        )

        Spacer(modifier = Modifier.height(MockDimens.SpacingLg))

        Text(
            text = "${state.tier} \u2022 ${state.points}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag(ProfileTestTags.TIER_POINTS),
        )

        Spacer(modifier = Modifier.height(MockDimens.SpacingSm))

        Text(
            text = state.memberSince,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(ProfileTestTags.MEMBER_SINCE),
        )

        Spacer(modifier = Modifier.height(MockDimens.SpacingXxxl))

        Button(
            onClick = { state.eventSink(ProfileEvent.LogoutClicked) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(ProfileTestTags.LOGOUT_BUTTON),
        ) {
            Text(
                text = "Log Out",
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
