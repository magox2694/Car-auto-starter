package com.example.carbeats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.carbeats.search.TrackSearchResult

class TrackResultsAdapter(
    private val onItemClick: (TrackSearchResult) -> Unit
) : RecyclerView.Adapter<TrackResultsAdapter.TrackViewHolder>() {

    private val items = mutableListOf<TrackSearchResult>()

    fun submitList(newItems: List<TrackSearchResult>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track_result, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artwork = itemView.findViewById<ImageView>(R.id.resultArtwork)
        private val title = itemView.findViewById<TextView>(R.id.resultTitle)
        private val subtitle = itemView.findViewById<TextView>(R.id.resultSubtitle)

        fun bind(item: TrackSearchResult) {
            title.text = item.title
            val sourceLabel = item.source.uppercase()
            val availabilityLabel = itemView.context.getString(
                if (item.playable) {
                    R.string.result_available_stream
                } else {
                    R.string.result_available_metadata
                }
            )
            subtitle.text = itemView.context.getString(
                R.string.result_subtitle_format,
                item.artist,
                item.album,
                sourceLabel,
                availabilityLabel
            )
            artwork.load(item.artworkUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
            }
            itemView.alpha = if (item.playable) 1f else 0.74f
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
