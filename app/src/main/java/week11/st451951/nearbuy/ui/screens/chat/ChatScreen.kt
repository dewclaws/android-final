package week11.st451951.nearbuy.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import week11.st451951.nearbuy.ui.components.SectionHeader

@Composable
fun ChatScreen() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SectionHeader(
            title = "Messages",
            onLeadingIconClick = { /* TODO: Handle account click */ },
            onTrailingIconClick = { /* TODO: Handle settings click */ }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Your conversations",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}