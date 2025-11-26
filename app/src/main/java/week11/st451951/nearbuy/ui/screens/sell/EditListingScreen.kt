package week11.st451951.nearbuy.ui.screens.sell

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import week11.st451951.nearbuy.data.Listing
import week11.st451951.nearbuy.data.ListingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListingScreen(
    listingId: String,
    onNavigateBack: () -> Unit,
    onListingUpdated: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ListingsRepository() }

    var listing by remember { mutableStateOf<Listing?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(listingId) {
        val result = repository.getListing(listingId)
        if (result.isSuccess) {
            listing = result.getOrNull()
            listing?.let {
                title = it.title
                priceText = it.price.toString()
                description = it.description
            }
        }
        isLoading = false
    }

    fun validateAndUpdateListing() {
        when {
            title.isBlank() -> {
                Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
            }
            priceText.isBlank() -> {
                Toast.makeText(context, "Please enter a price", Toast.LENGTH_SHORT).show()
            }
            priceText.toDoubleOrNull() == null || priceText.toDouble() < 0 -> {
                Toast.makeText(context, "Please enter a valid non-negative price", Toast.LENGTH_SHORT).show()
            }
            description.isBlank() -> {
                Toast.makeText(context, "Please enter a description", Toast.LENGTH_SHORT).show()
            }
            else -> {
                isSaving = true
                scope.launch {
                    try {
                        val updateResult = repository.updateListing(
                            listingId = listingId,
                            title = title,
                            price = priceText.toDouble(),
                            description = description,
                            imageUrls = listing?.imageUrls ?: emptyList()
                        )

                        if (updateResult.isSuccess) {
                            Toast.makeText(context, "Listing edited", Toast.LENGTH_SHORT).show()
                            onListingUpdated(listingId)
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to update listing: ${updateResult.exceptionOrNull()?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    } finally {
                        isSaving = false
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Listing") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isSaving && !isLoading) {
                FloatingActionButton(
                    onClick = { validateAndUpdateListing() },
                    modifier = Modifier.padding(bottom = 96.dp, end = 16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save changes"
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (listing == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Listing not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ##############
                    // IMAGE CAROUSEL
                    // ##############
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listing!!.imageUrls) { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Listing image",
                                modifier = Modifier
                                    .width(280.dp)
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // ######################
                    // TITLE AND PRICE INPUTS
                    // ######################
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = !isSaving
                        )

                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Price") },
                            leadingIcon = { Text("$") },
                            modifier = Modifier.width(120.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            enabled = !isSaving
                        )
                    }

                    // #################
                    // DESCRIPTION INPUT
                    // #################
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10,
                        enabled = !isSaving
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }

                if (isSaving) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
