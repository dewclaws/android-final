package week11.st451951.nearbuy.ui.screens.buy

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import week11.st451951.nearbuy.ui.components.SectionHeader

@Composable
fun BuyScreen(drawerState: DrawerState) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SectionHeader(
            title = "Browse",
            drawerState = drawerState
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Browse items for sale near you",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}