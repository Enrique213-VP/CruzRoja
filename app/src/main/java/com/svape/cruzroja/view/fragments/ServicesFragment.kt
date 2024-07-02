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
import com.svape.cruzroja.view.ServiceAdapter
import com.svape.cruzroja.viewmodel.ServiceViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ServicesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ServicesFragment : Fragment() {

    private lateinit var serviceViewModel: ServiceViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_services, container, false)

        val recyclerView: RecyclerView = root.findViewById(R.id.recyclerView)
        val adapter = ServiceAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        serviceViewModel = ViewModelProvider(requireActivity()).get(ServiceViewModel::class.java)
        serviceViewModel.services.observe(viewLifecycleOwner, { services ->
            services?.let { adapter.submitList(it) }
        })

        return root
    }
}