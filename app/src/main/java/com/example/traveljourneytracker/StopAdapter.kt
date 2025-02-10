import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traveljourneytracker.R
import com.example.traveljourneytracker.RouteStop

class StopAdapter(private val stopList: List<RouteStop>, private var isKilometers: Boolean) :
    RecyclerView.Adapter<StopAdapter.StopViewHolder>() {

    class StopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stopNameText: TextView = itemView.findViewById(R.id.stopNameText)
        val distanceLeftText: TextView = itemView.findViewById(R.id.distanceLeftText)
        val distanceCoveredText: TextView = itemView.findViewById(R.id.distanceCoveredText)
        val visaRequirementText: TextView = itemView.findViewById(R.id.visaRequirementText)
        val stopProgressBar: ProgressBar = itemView.findViewById(R.id.stopProgressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stop, parent, false)
        return StopViewHolder(view)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        val stop = stopList[position]

        holder.stopNameText.text = stop.name

        // Convert distance based on the unit system
        val distanceLeft = stop.distance - stop.distanceCovered
        val distanceCovered = stop.distanceCovered

        if (isKilometers) {
            holder.distanceLeftText.text = "Distance Left: $distanceLeft km"
            holder.distanceCoveredText.text = "Distance Covered: $distanceCovered km"
        } else {
            val distanceLeftMiles = distanceLeft * 0.621371
            val distanceCoveredMiles = distanceCovered * 0.621371
            holder.distanceLeftText.text = "Distance Left: ${"%.2f".format(distanceLeftMiles)} miles"
            holder.distanceCoveredText.text = "Distance Covered: ${"%.2f".format(distanceCoveredMiles)} miles"
        }

        // Visa requirement display
        holder.visaRequirementText.text = "Visa Requirement: ${stop.visaRequirement}"

        // Update progress bar
        val progress = (stop.distanceCovered.toFloat() / stop.distance) * 100
        holder.stopProgressBar.progress = progress.toInt()
    }

    override fun getItemCount() = stopList.size

    // Add a method to update the unit toggle
    fun updateUnits(isKilometers: Boolean) {
        this.isKilometers = isKilometers
        notifyDataSetChanged()  // Refresh the entire list
    }
}
