package com.svape.cruzroja.view.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.svape.cruzroja.R
import com.svape.cruzroja.data.model.Service
import com.svape.cruzroja.data.model.ServiceWithVolunteers
import com.svape.cruzroja.view.ServiceDetailActivity
import java.text.SimpleDateFormat
import java.util.Locale

class ServiceAdapter(private val context: Context) :
    RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    private var servicesWithVolunteers = emptyList<ServiceWithVolunteers>()
    private var onDeleteClickListener: ((Service) -> Unit)? = null
    private var onEditClickListener: ((Service) -> Unit)? = null

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceName: TextView = itemView.findViewById(R.id.serviceName)
        val serviceDescription: TextView = itemView.findViewById(R.id.serviceDescription)
        val serviceDate: TextView = itemView.findViewById(R.id.serviceDate)
        val volunteerList: TextView = itemView.findViewById(R.id.volunteerList)
        val serviceImage: ImageView = itemView.findViewById(R.id.serviceImage)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        val editButton: Button = itemView.findViewById(R.id.editButton)

        fun bind(serviceWithVolunteers: ServiceWithVolunteers) {
            val service = serviceWithVolunteers.service
            serviceName.text = service.serviceName
            serviceDescription.text = service.description
            serviceDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(service.date)

            val volunteersText = serviceWithVolunteers.volunteers.joinToString("\n") {
                "${it.name} - ${it.hours} horas"
            }
            volunteerList.text = volunteersText

            if (!service.imageUri.isNullOrEmpty()) {
                Glide.with(context)
                    .load(service.imageUri)
                    .error(R.drawable.ic_launcher_background)
                    .into(serviceImage)
            }

            itemView.setOnClickListener {
                val intent = Intent(context, ServiceDetailActivity::class.java).apply {
                    putExtra("service", serviceWithVolunteers.service)
                    putExtra("volunteers", ArrayList(serviceWithVolunteers.volunteers))
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }

            deleteButton.setOnClickListener {
                onDeleteClickListener?.invoke(service)
            }

            editButton.setOnClickListener {
                onEditClickListener?.invoke(service)
            }
        }
    }

    fun setOnDeleteClickListener(listener: (Service) -> Unit) {
        onDeleteClickListener = listener
    }

    fun setOnEditClickListener(listener: (Service) -> Unit) {
        onEditClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.service_item, parent, false)
        return ServiceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val currentServiceWithVolunteers = servicesWithVolunteers[position]
        holder.bind(currentServiceWithVolunteers)
    }

    override fun getItemCount() = servicesWithVolunteers.size

    fun submitList(serviceList: List<ServiceWithVolunteers>) {
        servicesWithVolunteers = serviceList
        notifyDataSetChanged()
    }
}