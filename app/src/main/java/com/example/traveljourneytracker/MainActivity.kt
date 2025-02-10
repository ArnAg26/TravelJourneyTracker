package com.example.traveljourneytracker

import StopAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.OnScrollListener

class MainActivity : AppCompatActivity() {

    private lateinit var distanceText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var unitToggleButton: Button
    private lateinit var nextStopButton: Button
    private lateinit var stopAdapter: StopAdapter

    private var isKilometers = true
    private val routeData = mutableListOf<RouteStop>()
    private var currentStopIndex = 0
    private var fileLineIndex = 0
    private var totalDistance = 0  // Total distance extracted from the file
    private lateinit var fileData: List<String>

    private lateinit var stopListView: RecyclerView
    private val handler = Handler(Looper.getMainLooper())

    private fun setupPagination() {
        stopListView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItemPosition == layoutManager.itemCount - 1) {
                    loadMoreData()
                }
            }
        })
    }

    private fun loadMoreData() {
        val newStops = mutableListOf<RouteStop>()

        for (i in 0 until 3) {
            if (fileLineIndex >= fileData.size) break

            val line = fileData[fileLineIndex]
            val parts = line.split("-")

            if (parts.size == 3) {
                val stopName = parts[0].split(":")[1].trim()
                val distance = parts[1].trim().toIntOrNull() ?: 0
                val visa = parts[2].trim()
                newStops.add(RouteStop(stopName, distance, visa, 0))
            }

            fileLineIndex++
        }

        routeData.addAll(newStops)
        stopAdapter.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        distanceText = findViewById(R.id.distanceText)
        progressBar = findViewById(R.id.mainProgressBar)
        unitToggleButton = findViewById(R.id.unitToggleButton)
        nextStopButton = findViewById(R.id.nextStopButton)
        stopListView = findViewById(R.id.stopListView)

        stopAdapter = StopAdapter(routeData, isKilometers)
        stopListView.layoutManager = LinearLayoutManager(this)
        stopListView.adapter = stopAdapter
        stopListView.setHasFixedSize(true)

        setupPagination()
        loadRouteData()
        updateDistanceDisplay()
        startDistanceCounter()

        unitToggleButton.setOnClickListener {
            isKilometers = !isKilometers
            updateDistanceDisplay()
            stopAdapter.updateUnits(isKilometers)
        }

        nextStopButton.setOnClickListener {
            markNextStop()
        }
    }

    private fun loadRouteData() {
        val inputStream = resources.openRawResource(R.raw.seoul_to_sau_paulo)
        fileData = inputStream.bufferedReader().readLines()

        // Parse total distance from the first line
        val totalDistanceLine = fileData[0]
        val totalDistanceParts = totalDistanceLine.split(":")
        if (totalDistanceParts.size == 2) {
            totalDistance = totalDistanceParts[1].trim().removeSuffix(" km").toIntOrNull() ?: 0
        }
        fileLineIndex++  // Move to the second line for stops

        loadMoreData()
    }

    private fun updateDistanceDisplay() {
        if (routeData.isEmpty()) {
            distanceText.text = "No stops available"
            progressBar.progress = 0
            return
        }

        // Calculate distance covered
        val distanceCovered = routeData.sumOf { it.distanceCovered }
        val remainingDistance = totalDistance - distanceCovered

        // Display the remaining distance in appropriate units
        val distanceTextValue = if (isKilometers) {
            "$remainingDistance km"
        } else {
            val miles = remainingDistance * 0.621371
            String.format("%.2f miles", miles)
        }

        // Update main progress bar (overall trip progress)
        val progressPercentage = (distanceCovered.toFloat() / totalDistance) * 100
        progressBar.progress = progressPercentage.toInt()

        // Update button text and main distance display
        unitToggleButton.text = if (isKilometers) "Switch to Miles" else "Switch to Kilometers"
        distanceText.text = "Total Distance Left: $distanceTextValue"
    }

    private fun startDistanceCounter() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (currentStopIndex < routeData.size) {
                    val currentStop = routeData[currentStopIndex]

                    if (currentStop.distanceCovered < currentStop.distance) {
                        currentStop.distanceCovered+=10
                    } else {
                        markNextStop()
                    }

                    stopAdapter.notifyDataSetChanged()
                    updateDistanceDisplay()

                    handler.postDelayed(this, 1000)
                }
            }
        }, 1000)
    }

    private fun markNextStop() {
        if (currentStopIndex < routeData.size) {
            val currentStop = routeData[currentStopIndex]

            // Ensure the current stop's distanceCovered equals the distance (stop reached)
            currentStop.distanceCovered = currentStop.distance

            // Update the RecyclerView to reflect changes to the progress bar
            stopAdapter.notifyDataSetChanged()

            // Move to the next stop
            currentStopIndex++

            // Update total progress display
            updateDistanceDisplay()
        } else {
            // If all stops are reached, stop the counter and display completion message
            handler.removeCallbacksAndMessages(null)
            distanceText.text = "Congrats, Trip is Complete!"
        }
    }
}
