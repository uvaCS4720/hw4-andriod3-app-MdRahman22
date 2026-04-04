package edu.nd.pmcburne.hello

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.nd.pmcburne.hello.ui.theme.MyApplicationTheme

// UVA-inspired colors
private val UvaBlue = Color(0xFF232D4B)
private val UvaOrange = Color(0xFFE57200)

// Darker overall background so the change is easy to see
private val AppBackground = Color(0xFFDCE3F0)

// Card colors
private val LightBlueCard = Color(0xFFD4DEF3)
private val LightOrangeCard = Color(0xFFFFE7D1)

// Clear button color
private val ClearRed = Color(0xFFB23A48)

class MainActivity : ComponentActivity() {

    // This connects MainActivity to your ViewModel
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = AppBackground
                ) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    // Get the current UI state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    CampusMapContent(
        uiState = uiState,
        onTagSelected = viewModel::selectTag,
        modifier = modifier
    )
}

@Composable
fun CampusMapContent(
    uiState: MainUiState,
    onTagSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Controls whether dropdown is open
    var expanded by remember { mutableStateOf(false) }

    // Stores whichever marker the user last tapped
    var selectedLocation by remember { mutableStateOf<LocationEntity?>(null) }

    // Fallback map center for UVA
    val defaultLocation = LatLng(38.0356, -78.5034)

    // Move the map camera in code
    val cameraPositionState = rememberCameraPositionState()

    // Detects portrait vs landscape
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Whenever the filtered locations change, move the camera
    LaunchedEffect(uiState.filteredLocations) {
        val firstLocation = uiState.filteredLocations.firstOrNull()

        val target = if (firstLocation != null) {
            LatLng(firstLocation.latitude, firstLocation.longitude)
        } else {
            defaultLocation
        }

        // Clears old selected card when filter changes
        selectedLocation = null

        cameraPositionState.move(
            CameraUpdateFactory.newLatLngZoom(target, 15f)
        )
    }

    // Root background box so the darker background is clearly visible
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (isLandscape) 8.dp else 10.dp,
                    vertical = if (isLandscape) 6.dp else 8.dp
                ),
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 4.dp else 6.dp)
        ) {
            // Screen title
            Text(
                text = "UVA Campus Maps",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = if (isLandscape) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.headlineSmall
                },
                color = UvaBlue
            )

            // Small helper text under title
            if (!isLandscape) {
                Text(
                    text = "Browse locations by tag, then tap a marker for details.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = UvaBlue.copy(alpha = 0.8f)
                )
            }

            // Compact filter card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LightBlueCard
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isLandscape) 8.dp else 10.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isLandscape) 4.dp else 6.dp)
                ) {
                    // Dropdown filter area
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, UvaBlue),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = UvaBlue
                            )
                        ) {
                            Text("Tag: ${uiState.selectedTag}")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.heightIn(max = 280.dp)
                        ) {
                            // One menu item per tag
                            uiState.availableTags.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag) },
                                    onClick = {
                                        onTagSelected(tag)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Quick action buttons for usability
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onTagSelected("core") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UvaOrange,
                                contentColor = Color.White
                            )
                        ) {
                            Text(if (isLandscape) "Core" else "Back to Core")
                        }

                        Button(
                            onClick = { selectedLocation = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ClearRed,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }

            // Map section label
            Text(
                text = "Map (${uiState.filteredLocations.size} locations)",
                style = MaterialTheme.typography.titleSmall,
                color = UvaBlue
            )

            // Card that holds the Google Map
            Card(
                modifier = Modifier
                    .weight(if (isLandscape) 1.35f else 1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = false
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            mapToolbarEnabled = true
                        ),
                        // Taps the empty map space to clear selected card
                        onMapClick = {
                            selectedLocation = null
                        }
                    ) {
                        // Makes one marker for each visible location
                        uiState.filteredLocations.forEach { location ->
                            Marker(
                                state = MarkerState(
                                    position = LatLng(location.latitude, location.longitude)
                                ),
                                title = location.name,
                                snippet = location.description,
                                onClick = {
                                    // Save clicked marker for bottom card
                                    selectedLocation = location

                                    // Returns false so Google Maps still shows default info window
                                    false
                                }
                            )
                        }
                    }

                    // Shows the loading spinner while data is loading
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = UvaOrange)
                    }

                    // Shows the message if no locations match selected tag
                    if (!uiState.isLoading && uiState.filteredLocations.isEmpty()) {
                        Text(
                            text = "No locations match this filter.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = UvaBlue
                        )
                    }
                }
            }

            // Details section label
            Text(
                text = "Selected Location Details",
                style = MaterialTheme.typography.titleSmall,
                color = UvaBlue
            )

            // Bottom details card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = if (isLandscape) 70.dp else 95.dp,
                        max = if (isLandscape) 105.dp else 155.dp
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LightOrangeCard
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isLandscape) 8.dp else 10.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (selectedLocation == null) {
                        // Default message before marker tap
                        Text(
                            text = "No location selected",
                            style = MaterialTheme.typography.titleSmall,
                            color = UvaBlue
                        )

                        Text(
                            text = "Tap any marker on the map to view its full description here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = UvaBlue.copy(alpha = 0.85f)
                        )
                    } else {
                        // Shows the selected building's name
                        Text(
                            text = selectedLocation!!.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = UvaOrange
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Shows the selected building's full description
                        Text(
                            text = selectedLocation!!.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = UvaBlue
                        )
                    }
                }
            }
        }
    }
}