package com.artemissoftware.locationtracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.artemissoftware.locationtracker.models.Pin

class PinAdapter() : RecyclerView.Adapter<PinViewHolder>() {

    private var list : MutableList<Pin>


    init {

        list = mutableListOf<Pin>();
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PinViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: PinViewHolder, position: Int) {

        val pin: Pin = list.get(position)
        holder.bind(pin)
    }

    override fun getItemCount(): Int = list.size


    fun addPin(pin : Pin){

        if(list.size + 1 > 5){
            list = list.dropLast(1) as MutableList<Pin>
        }

        list.add(0, pin);
        notifyDataSetChanged();
    }

}