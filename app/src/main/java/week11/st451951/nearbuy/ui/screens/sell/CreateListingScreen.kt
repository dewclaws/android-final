package week11.st451951.nearbuy.ui.screens.sell

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import week11.st451951.nearbuy.data.ImageUploader
import week11.st451951.nearbuy.data.ListingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    onNavigateBack: () -> Unit,
    onListingCreated: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ListingsRepository() }
    val imageUploader = remember { ImageUploader() }

    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (selectedImages.size < 4) {
                selectedImages = selectedImages + it
            } else {
                Toast.makeText(context, "Maximum 4 images allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun validateAndCreateListing() {
        when {
            title.isBlank() -> {
                Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
            }
            priceText.isBlank() -> {
                Toast.makeText(context, "Please enter a price", Toast.LENGTH_SHORT).show()
            }
            priceText.toDoubleOrNull() == null || priceText.toDouble() < 0 -> {
                Toast.makeText(context, "Please enter a non-negative price", Toast.LENGTH_SHORT).show()
            }
            description.isBlank() -> {
                Toast.makeText(context, "Please enter a description", Toast.LENGTH_SHORT).show()
            }
            selectedImages.isEmpty() -> {
                Toast.makeText(context, "Please add at least one image", Toast.LENGTH_SHORT).show()
            }
            else -> {
                isLoading = true
                scope.launch {
                    try {
                        // Upload images
                        val uploadResult = imageUploader.uploadImages(selectedImages)
                        if (uploadResult.isFailure) {
                            Toast.makeText(
                                context,
                                "Failed to upload images: ${uploadResult.exceptionOrNull()?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            isLoading = false
                            return@launch
                        }

                        val imageUrls = uploadResult.getOrThrow()

                        // Create listing
                        val createResult = repository.createListing(
                            title = title,
                            price = priceText.toDouble(),
                            description = description,
                            imageUrls = imageUrls
                        )

                        if (createResult.isSuccess) {
                            Toast.makeText(context, "Listing created!", Toast.LENGTH_SHORT).show()
                            onListingCreated(createResult.getOrThrow())
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to create listing: ${createResult.exceptionOrNull()?.message}",
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
                        isLoading = false
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Listing") },
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
            if (!isLoading) {
                FloatingActionButton(
                    onClick = { validateAndCreateListing() },
                    modifier = Modifier.padding(bottom = 112.dp, end = 16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Create listing"
                    )
                }
            }
        }
    ) { paddingValues ->
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
                //
                // IMAGE GRID
                //
                ImageGrid(
                    images = selectedImages,
                    onAddImage = { imagePickerLauncher.launch("image/*") }
                )

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
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price") },
                        leadingIcon = { Text("$") },
                        modifier = Modifier.width(120.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
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
                    maxLines = 10
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ImageGrid(
    images: List<Uri>,
    onAddImage: () -> Unit
) {
    when (images.size) {
        0 -> {
            // ###########################
            // 0 IMAGES ADDED
            //
            // SHOW FULL-WIDTH PLACEHOLDER
            // ###########################
            AddImagePlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                onClick = onAddImage
            )
        }
        1 -> {
            // ###########################################
            // 1 IMAGE ADDED
            //
            // SHOW FIRST IMAGE + PLACEHOLDER SIDE-BY-SIDE
            // ###########################################
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ImageThumbnail(
                    uri = images[0],
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                )
                AddImagePlaceholder(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    onClick = onAddImage
                )
            }
        }
        in 2..3 -> {
            // #######################################
            // 2-3 IMAGES ADDED
            //
            // SHOW 2x2 GRID WITH IMAGES + PLACEHOLDER
            // #######################################
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ImageThumbnail(
                        uri = images[0],
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                    )
                    ImageThumbnail(
                        uri = images[1],
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (images.size >= 3) {
                        ImageThumbnail(
                            uri = images[2],
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp)
                        )
                    }
                    if (images.size < 4) {
                        AddImagePlaceholder(
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp),
                            onClick = onAddImage
                        )
                    }
                }
            }
        }
        else -> {
            // ################################
            // 4 IMAGES ADDED
            //
            // SHOW 2x2 GRID W/ NO PLACEHOLDERS
            // ################################
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ImageThumbnail(
                        uri = images[0],
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                    )
                    ImageThumbnail(
                        uri = images[1],
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ImageThumbnail(
                        uri = images[2],
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                    )
                    ImageThumbnail(
                        uri = images[3],
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddImagePlaceholder(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add image",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ImageThumbnail(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = "Selected image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}