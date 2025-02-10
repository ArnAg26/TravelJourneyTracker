package com.example.version2

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelJourneyTrackerApp()
        }
    }
}

@Composable
fun TravelJourneyTrackerApp() {
    Scaffold(
        topBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Travel Journey Tracker", fontSize = 20.sp)

                // Add the Image composable for the map
                Image(
                    painter = painterResource(id = R.drawable.world_map),
                    contentDescription = "World Map",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = 16.dp)
                )
            }
        }
    ) { contentPadding ->
        MainContent(contentPadding)
    }
}

@Composable
fun MainContent(contentPadding: PaddingValues) {
    val context = LocalContext.current
    var isKilometers by remember { mutableStateOf(true) }
    val stops = remember { loadStopsFromFile(context) }
    var currentStopIndex by remember { mutableStateOf(0) }
    var totalDistanceLeft by remember { mutableStateOf(stops.sumOf { it.distance }) }

    // LazyListState to monitor scroll position
    val lazyListState = rememberLazyListState()

    // Function to handle moving to the next stop
    fun markNextStop() {
        if (currentStopIndex < stops.size) {
            val currentStop = stops[currentStopIndex]
            currentStop.distanceCovered = currentStop.distance  // Set distance covered to max
            totalDistanceLeft = stops.sumOf { it.distance - it.distanceCovered }  // Update total distance left
            currentStopIndex++  // Move to the next stop
        }
    }

    // Start distance counter effect
    LaunchedEffect(Unit) {
        while (currentStopIndex < stops.size) {
            val currentStop = stops[currentStopIndex]

            if (currentStop.distanceCovered < currentStop.distance) {
                delay(1000L)  // Wait for 1 second
                currentStop.distanceCovered += 10  // Increase distance covered
                totalDistanceLeft = stops.sumOf { it.distance - it.distanceCovered }  // Update total distance left
            } else {
                // Automatically move to the next stop if reached
                markNextStop()
            }
        }
    }

    // Monitor visible items to ensure only 3 items are displayed
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }.collect { index ->
            if (index + 3 < stops.size) {
                // Limit scrolling if needed or adjust here if you want to dynamically control behavior
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Distance Display Text
        Text(
            text = "Total Distance Left: ${if (isKilometers) totalDistanceLeft else (totalDistanceLeft * 0.621371).toInt()} ${if (isKilometers) "km" else "miles"}",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Progress Bar for the entire trip
        LinearProgressIndicator(
            progress = (stops.sumOf { it.distance } - totalDistanceLeft).toFloat() / stops.sumOf { it.distance },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Toggle Units Button
        Button(
            onClick = { isKilometers = !isKilometers },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(text = if (isKilometers) "Switch to Miles" else "Switch to Kilometers")
        }

        // Mark Next Stop Button
        Button(
            onClick = { markNextStop() },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(text = "Mark Next Stop as Reached")
        }

        // Lazy List to Display Stops (limited to 3 visible items at a time)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState
        ) {
            items(stops) { stop ->
                StopItem(stop = stop, isKilometers = isKilometers)
            }
        }
    }
}

@Composable
fun StopItem(stop: RouteStop, isKilometers: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Stop Name: ${stop.name}", fontSize = 16.sp)

            // Display "Reached" when stop is fully covered
            if (stop.distanceCovered >= stop.distance) {
                Text(
                    text = "Reached",
                    fontSize = 14.sp,
                    color = Color.Green
                )
            } else {
                Text(
                    text = "Distance Left: ${
                        if (isKilometers) stop.distance - stop.distanceCovered
                        else String.format("%.2f miles", (stop.distance - stop.distanceCovered) * 0.621371)
                    }",
                    fontSize = 14.sp
                )
            }

            Text(text = "Visa Requirement: ${stop.visaRequirement}", fontSize = 14.sp)

            LinearProgressIndicator(
                progress = stop.distanceCovered.toFloat() / stop.distance,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

data class RouteStop(
    val name: String,
    val distance: Int,
    val visaRequirement: String,
    var distanceCovered: Int
)

fun loadStopsFromFile(context: Context): List<RouteStop> {
    val stops = mutableListOf<RouteStop>()
    val inputStream = context.resources.openRawResource(R.raw.seoul_to_sau_paulo)
    val reader = BufferedReader(InputStreamReader(inputStream))

    reader.useLines { lines ->
        lines.forEach { line ->
            val parts = line.split("-").map { it.trim() }
            if (parts.size == 3) {
                val stopName = parts[0]
                val distance = parts[1].toIntOrNull() ?: 0
                val visaRequirement = parts[2]
                stops.add(RouteStop(stopName, distance, visaRequirement, 0))
            }
        }
    }
    return stops
}

@Preview(showBackground = true)
@Composable
fun PreviewTravelJourneyTrackerApp() {
    TravelJourneyTrackerApp()
}
