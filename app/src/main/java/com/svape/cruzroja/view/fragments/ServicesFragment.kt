package com.svape.cruzroja.view.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.svape.cruzroja.R
import com.svape.cruzroja.data.database.AppDatabase
import com.svape.cruzroja.repository.Repository
import com.svape.cruzroja.view.adapters.ServiceAdapter
import com.svape.cruzroja.viewmodel.ServiceViewModel
import com.svape.cruzroja.viewmodel.ServiceViewModelFactory

class ServicesFragment : Fragment() {
    private lateinit var serviceViewModel: ServiceViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServiceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_services, container, false)

        val database = AppDatabase.getDatabase(requireContext())
        val serviceDao = database.serviceDao()
        val volunteerDao = database.volunteerDao()
        val repository = Repository(serviceDao, volunteerDao)
        val factory = ServiceViewModelFactory(repository)

        recyclerView = root.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ServiceAdapter(requireContext())
        adapter.setOnDeleteClickListener { service ->
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar este servicio?")
                .setPositiveButton("Sí") { _, _ ->
                    serviceViewModel.deleteService(service)
                }
                .setNegativeButton("No", null)
                .show()
        }

        adapter.setOnEditClickListener { service ->
            val editFragment = AddServiceFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("service", service)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, editFragment)
                .addToBackStack(null)
                .commit()
        }

        recyclerView.adapter = adapter

        serviceViewModel = ViewModelProvider(requireActivity(), factory)[ServiceViewModel::class.java]

        serviceViewModel.services.observe(viewLifecycleOwner) { servicesWithVolunteers ->
            if (servicesWithVolunteers.isNullOrEmpty()) {
                recyclerView.visibility = View.GONE
                root.findViewById<LinearLayout>(R.id.emptyStateContainer).visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                root.findViewById<LinearLayout>(R.id.emptyStateContainer).visibility = View.GONE
            }
            adapter.submitList(servicesWithVolunteers)
        }

        serviceViewModel.loadServices()

        return root
    }
}