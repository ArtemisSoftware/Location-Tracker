package com.artemissoftware.locationtracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.artemissoftware.locationtracker.R
import com.artemissoftware.locationtracker.models.Pin

class PinViewHolder (inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_location, parent, false)) {

    private var txt_data: TextView? = null
    private var txt_latitude: TextView? = null
    private var txt_longitute: TextView? = null


    init {
        txt_data = itemView.findViewById(R.id.txt_data)
        txt_latitude = itemView.findViewById(R.id.txt_latitude)
        txt_longitute = itemView.findViewById(R.id.txt_longitute)
    }

    fun bind(pin: Pin) {

        txt_latitude?.text = pin.latitude
        txt_longitute?.text = pin.longitude
        txt_data?.text = pin.date.toString()
    }

}