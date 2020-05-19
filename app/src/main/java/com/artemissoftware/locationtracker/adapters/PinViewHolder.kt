package com.artemissoftware.locationtracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.artemissoftware.locationtracker.R
import com.artemissoftware.locationtracker.models.Pin
import kotlinx.android.synthetic.main.item_location.view.*

class PinViewHolder (inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.item_location, parent, false)) {

    fun bind(pin: Pin) {

        itemView.txt_latitude.text = pin.latitude
        itemView.txt_longitute.text = pin.longitude
        itemView.txt_data.text = pin.date.toString()
        itemView.txt_battery.text = pin.battery.toString() + "%"
        itemView.txt_address.text = pin.address
    }

}