package week11.st451951.nearbuy.ui.screens.sell

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import week11.st451951.nearbuy.data.Listing
import week11.st451951.nearbuy.data.ListingsRepository
import week11.st451951.nearbuy.ui.components.SectionHeader
import java.text.SimpleDateFormat
import java.util.*
import week11.st451951.nearbuy.ui.components.ListingItem

@Composable
fun SellScreen(
    onNavigateToCreateListing: () -> Unit,
    onListingClick: (String) -> Unit
) {
    val repository = remember { ListingsRepository() }
    val listings by repository.getUserListings().collectAsState(initial = emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            SectionHeader(
                title = "Your Listings"
            )

            if (listings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No listings yet. Tap + to create your first listing!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                ) {
                    items(listings) { listing ->
                        ListingItem(
                            listing = listing,
                            onClick = { onListingClick(listing.id) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNavigateToCreateListing,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 146.dp, end = 32.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Create new listing"
            )
        }
    }
}

