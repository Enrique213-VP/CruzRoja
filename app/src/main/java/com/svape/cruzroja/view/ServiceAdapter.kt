package com.svape.cruzroja.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.svape.cruzroja.R
import com.svape.cruzroja.model.Service
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ServiceAdapter : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    private var services = emptyList<Service>()

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceName: TextView = itemView.findViewById(R.id.serviceName)
        val serviceDate: TextView = itemView.findViewById(R.id.serviceDate)
        val volunteerList: TextView = itemView.findViewById(R.id.volunteerList)
        val serviceImage: ImageView = itemView.findViewById(R.id.serviceImage)

        fun bind(service: Service) {
            serviceName.text = service.serviceName
            serviceDate.text = formatDate(service.date)
            volunteerList.text = service.volunteers.joinToString("\n") { volunteer ->
                "${volunteer.name} - ${volunteer.hours} horas"
            }
            Glide.with(itemView.context).load(service.imageUri).into(serviceImage)
        }

        private fun formatDate(date: Date): String {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return format.format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.service_item, parent, false)
        return ServiceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val currentService = services[position]
        holder.bind(currentService)
    }

    override fun getItemCount() = services.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(serviceList: List<Service>) {
        services = serviceList
        notifyDataSetChanged()
    }
}
