package week11.st451951.nearbuy.ui.screens.buy

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import week11.st451951.nearbuy.data.Category
import week11.st451951.nearbuy.data.LocationManager
import week11.st451951.nearbuy.ui.components.ListingItem
import week11.st451951.nearbuy.ui.components.SectionHeader

@Composable
fun BuyScreen(
    onListingClick: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: BuyViewModel = remember {
        BuyViewModel(locationManager = LocationManager(context))
    }
    val uiState by viewModel.uiState.collectAsState()

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BuyEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (uiState.error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = uiState.error ?: "An error occurred",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    } else {
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
                selectedCategory = uiState.selectedCategory,
                onCategoryClick = { category ->
                    viewModel.selectCategory(category)
                }
            )

            // Active filter chip
            if (uiState.selectedCategory != null) {
                FilterChip(
                    selected = true,
                    onClick = { viewModel.clearCategoryFilter() },
                    label = { Text(uiState.selectedCategory!!.displayName) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear filter",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Recent Listings header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.selectedCategory != null) {
                        "${uiState.selectedCategory!!.displayName} Listings"
                    } else {
                        "Recent Listings"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Listings list
            if (uiState.filteredListings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.selectedCategory != null) {
                            "No listings in ${uiState.selectedCategory!!.displayName}"
                        } else {
                            "No listings available"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.filteredListings) { listing ->
                        ListingItem(
                            listing = listing,
                            onClick = { onListingClick(listing.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryRow(
    selectedCategory: Category?,
    onCategoryClick: (Category) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(Category.entries) { category ->
            CategoryItem(
                category = category,
                isSelected = category == selectedCategory,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
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
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else {
                        Modifier
                    }
                ),
            contentScale = ContentScale.Crop
        )

        Text(
            text = category.displayName,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}