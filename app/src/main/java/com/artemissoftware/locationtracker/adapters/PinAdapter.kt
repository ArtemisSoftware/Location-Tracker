package com.artemissoftware.locationtracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.artemissoftware.locationtracker.models.Pin

class PinAdapter() : RecyclerView.Adapter<PinViewHolder>() {

    private var list : MutableList<Pin>
    private var lastPin : Pin?

    init {

        list = mutableListOf<Pin>();
        lastPin = null
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

        if(lastPin == null){
            lastPin = pin
        }
        else{

            if(list.size + 1 > 5){
                list = list.dropLast(1) as MutableList<Pin>
            }

            list.add(0, lastPin!!);
            lastPin = pin
            notifyDataSetChanged();
        }
    }

    fun clear(){

        lastPin = null
        list.clear();
        notifyDataSetChanged();
    }


}