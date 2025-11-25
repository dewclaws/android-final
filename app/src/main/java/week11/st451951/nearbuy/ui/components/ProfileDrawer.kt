package week11.st451951.nearbuy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import week11.st451951.nearbuy.auth.AuthViewModel

@Composable
fun ProfileDrawer(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.Companion.height(48.dp))

            // Monogram Avatar
            val userName = authViewModel.getCurrentUserName()
            val initials = userName.split(" ")
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .joinToString("")
                .ifEmpty { "U" }

            Box(
                modifier = Modifier.Companion
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Companion.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Companion.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.Companion.height(16.dp))

            // User Name
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Companion.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.Companion.height(32.dp))

            // Logout Button
            FilledTonalButton(
                onClick = onLogout,
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    modifier = Modifier.Companion.size(18.dp)
                )
                Spacer(modifier = Modifier.Companion.size(8.dp))
                Text("Logout")
            }
        }
    }
}