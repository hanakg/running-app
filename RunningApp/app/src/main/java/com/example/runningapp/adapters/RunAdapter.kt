package com.example.runningapp.adapters

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningapp.R
import com.example.runningapp.databinding.ActivityMainBinding
import com.example.runningapp.databinding.FragmentOnerunstatisticsBinding
import com.example.runningapp.db.Run
import com.example.runningapp.other.TrackingUtility
import kotlinx.android.synthetic.main.item_run.view.*
import com.example.runningapp.other.Constants
import com.example.runningapp.ui.viewmodels.SharedViewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RunAdapter():RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    private lateinit var mListener:onItemClickListener
    private lateinit var mLongListener:onItemLongClickListener

    interface onItemClickListener{

        fun onItemClick(position: Int)
    }
    interface onItemLongClickListener{

        fun onItemLongClick(position: Int)
    }


    //Két lista közötti különbség kiszámítása
    val diffCallback=object:DiffUtil.ItemCallback<Run>(){
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id==newItem.id
        }
         // Megnézni, hogy a két item hashCode-ja megegyezik e
        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode()==newItem.hashCode()
        }
    }

    val differ=AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>)=differ.submitList(list)
    private var lista: List<Run> =differ.currentList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_run,
                parent,
                false
            ), mListener, mLongListener
        )
    }

    // Adatok kiolvasása, és beállítása a visszajelzéshez
    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run=differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(run.img).into(ivRunImage)

            val calendar=Calendar.getInstance().apply {
                timeInMillis=run.timestamp
            }
            val dateFormat=SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            tvDate.text=dateFormat.format(calendar.time)

            val avgSpeed="${run.avgSpeed}km/h"
            tvAvgSpeed.text=avgSpeed

            val distanceInKm="${run.distance/1000f}km"
            tvDistance.text=distanceInKm

            tvTime.text=TrackingUtility.getFormattedStopWatchTime(run.timeMillisec)

            val caloriesBurned="${run.burnedCalories}kcal"
            tvCalories.text=caloriesBurned
        }

        return
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun setOnClickListener(listener:onItemClickListener){

        mListener=listener
    }

    fun setOnLongClickListener(listener:onItemLongClickListener){

        mLongListener=listener
    }

    inner class RunViewHolder(itemView:View, listener: onItemClickListener, longListener: onItemLongClickListener):RecyclerView.ViewHolder(itemView)
    {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
            itemView.setOnLongClickListener {
                longListener.onItemLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
        }
    }

}