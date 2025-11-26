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

@Composable
private fun ListingItem(
    listing: Listing,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // #####
        // IMAGE
        // #####
        AsyncImage(
            model = listing.imageUrls.firstOrNull(),
            contentDescription = listing.title,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        // #######
        // CONTENT
        // #######
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ###########
            // TITLE
            // DESCRIPTION
            // ###########
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // #############
                // TITLE & PRICE
                // #############
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = listing.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$${listing.price.toInt()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // ###########
                // DESCRIPTION
                // ###########
                Text(
                    text = listing.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ######
            // FOOTER
            // ######
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestamp(listing.createdAt.toDate()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun formatTimestamp(date: Date): String {
    val now = Date()
    val diff = now.time - date.time

    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        minutes < 60 -> "Today • $minutes min ago"
        hours < 24 -> "Today • $hours hour${if (hours > 1) "s" else ""} ago"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        else -> {
            val format = SimpleDateFormat("MMM d", Locale.getDefault())
            format.format(date)
        }
    }
}