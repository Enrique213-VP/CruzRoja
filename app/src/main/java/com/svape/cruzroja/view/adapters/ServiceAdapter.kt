package com.svape.cruzroja.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.svape.cruzroja.R
import com.svape.cruzroja.model.Service
import com.svape.cruzroja.view.ServiceDetailActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ServiceAdapter(private val context: Context) :
    RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    private var services = emptyList<Service>()

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serviceName: TextView = itemView.findViewById(R.id.serviceName)
        private val serviceDate: TextView = itemView.findViewById(R.id.serviceDate)
        private val volunteerList: TextView = itemView.findViewById(R.id.volunteerList)
        private val serviceImage: ImageView = itemView.findViewById(R.id.serviceImage)

        fun bind(service: Service) {
            serviceName.text = service.serviceName
            serviceDate.text = formatDate(service.date)
            volunteerList.text = service.volunteers.joinToString("\n") { volunteer ->
                "${volunteer.name} - ${volunteer.hours} horas"
            }
            Glide.with(itemView.context).load(service.imageUri).into(serviceImage)

            itemView.setOnClickListener {
                val intent = Intent(context, ServiceDetailActivity::class.java).apply {
                    putExtra("service", service)
                }
                context.startActivity(intent)
            }
        }

        private fun formatDate(date: Date): String {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return format.format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.service_item, parent, false)
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