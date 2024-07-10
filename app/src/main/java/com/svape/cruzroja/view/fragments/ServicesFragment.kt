package com.svape.cruzroja.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.svape.cruzroja.R
import com.svape.cruzroja.view.adapters.ServiceAdapter
import com.svape.cruzroja.viewmodel.ServiceViewModel

class ServicesFragment : Fragment() {

    private lateinit var serviceViewModel: ServiceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_services, container, false)

        val recyclerView: RecyclerView = root.findViewById(R.id.recyclerView)
        val adapter = ServiceAdapter(requireContext())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        serviceViewModel = ViewModelProvider(requireActivity())[ServiceViewModel::class.java]
        serviceViewModel.services.observe(viewLifecycleOwner) { services ->
            services?.let { adapter.submitList(it) }
        }

        return root
    }
}