package week11.st451951.nearbuy.ui.screens.sell

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import week11.st451951.nearbuy.data.LocationManager
import week11.st451951.nearbuy.ui.components.LocationWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    onNavigateBack: () -> Unit,
    onListingCreated: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: CreateListingViewModel = remember {
        CreateListingViewModel(locationManager = LocationManager(context))
    }
    val uiState by viewModel.uiState.collectAsState()

    // Permission launcher for location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.fetchLocation()
        } else {
            Toast.makeText(context, "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateListingEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is CreateListingEvent.ListingCreated -> {
                    onListingCreated(event.listingId)
                }
                is CreateListingEvent.RequestLocationPermission -> {
                    locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addImage(it) }
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
            if (!uiState.isLoading) {
                FloatingActionButton(
                    onClick = { viewModel.createListing() },
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
                    images = uiState.selectedImages,
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
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text("Title") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = uiState.titleError != null,
                        supportingText = uiState.titleError?.let { { Text(it) } }
                    )

                    OutlinedTextField(
                        value = uiState.priceText,
                        onValueChange = { viewModel.updatePrice(it) },
                        label = { Text("Price") },
                        leadingIcon = { Text("$") },
                        modifier = Modifier.width(120.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = uiState.priceError != null
                    )
                }

                // #################
                // DESCRIPTION INPUT
                // #################
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10,
                    isError = uiState.descriptionError != null,
                    supportingText = uiState.descriptionError?.let { { Text(it) } }
                )

                // ###############
                // LOCATION PICKER
                // ###############
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (uiState.location != null) {
                        LocationWidget(
                            location = uiState.location!!,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.requestLocation() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoadingLocation
                        ) {
                            if (uiState.isLoadingLocation) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (uiState.isLoadingLocation) "Getting location..." else "Add Location")
                        }
                    }
                }
            }

            if (uiState.isLoading) {
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