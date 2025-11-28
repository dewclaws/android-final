package week11.st451951.nearbuy.ui.screens.buy

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import week11.st451951.nearbuy.data.Category
import week11.st451951.nearbuy.data.ListingsRepository
import week11.st451951.nearbuy.ui.components.ListingItem
import week11.st451951.nearbuy.ui.components.SectionHeader

@Composable
fun BuyScreen(
    onListingClick: (String) -> Unit
) {
    val repository = remember { ListingsRepository() }
    val listings by repository.getAllListings().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SectionHeader(
            title = "Browse"
        )

        // Categories label
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Category row
        CategoryRow(
            onCategoryClick = { category ->
                // TODO: Handle category selection
            }
        )

        // Recent Listings header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Listings",
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View all",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Listings list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

@Composable
fun CategoryRow(
    onCategoryClick: (Category) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(Category.entries) { category ->
            CategoryItem(
                category = category,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = category.drawableRes),
            contentDescription = category.displayName,
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Text(
            text = category.displayName,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}