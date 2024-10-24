package com.smart.album.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.smart.album.R
import com.smart.album.models.CarouselItem
import com.smart.album.views.PanningImageView


class CarouselAdapter(private val items: List<CarouselItem>) :
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {
        private var holder: CarouselViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carousel, parent, false)
        holder = CarouselViewHolder(view)
        return holder as CarouselViewHolder
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val item = items[position % items.size]
        holder.bind(item)
    }

    override fun getItemCount(): Int = Int.MAX_VALUE


    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: PanningImageView = itemView.findViewById(R.id.carouselImage)
        val textView: TextView = itemView.findViewById(R.id.carouselText)


        fun bind(item: CarouselItem) {
            Glide.with(itemView.context)
                .load(item.imageUrl)
                .into(imageView)
//            imageView.startAnimat()
            textView.text = item.description
        }
    }
}
