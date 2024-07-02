package com.svape.cruzroja.view.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.svape.cruzroja.R
import com.svape.cruzroja.model.Service
import com.svape.cruzroja.viewmodel.ServiceViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddServiceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddServiceFragment : Fragment() {

    private lateinit var serviceViewModel: ServiceViewModel
    private lateinit var dateInput: EditText
    private var selectedDate: Date? = null
    private var photoUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            photoUri = it
            view?.findViewById<ImageView>(R.id.imageView)?.setImageURI(it)
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri?.let {
                view?.findViewById<ImageView>(R.id.imageView)?.setImageURI(it)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            takePicture()
        } else {
            Toast.makeText(context, "Permitir permiso para tomar foto", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_add_service, container, false)

        serviceViewModel = ViewModelProvider(requireActivity()).get(ServiceViewModel::class.java)

        val serviceNameInput: EditText = root.findViewById(R.id.serviceNameInput)
        dateInput = root.findViewById(R.id.dateInput)
        val addServiceButton: Button = root.findViewById(R.id.addServiceButton)
        val selectImageButton: Button = root.findViewById(R.id.selectImageButton)
        val takePictureButton: Button = root.findViewById(R.id.takePictureButton)
        val imageView: ImageView = root.findViewById(R.id.imageView)

        dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        selectImageButton.setOnClickListener {
            selectImage()
        }

        takePictureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                takePicture()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        addServiceButton.setOnClickListener {
            val serviceName = serviceNameInput.text.toString()
            val imageUri = photoUri?.toString() ?: ""

            if (selectedDate != null) {
                val service = Service(serviceName, selectedDate!!, emptyList(), imageUri)
                serviceViewModel.addService(service)
            } else {
                dateInput.error = "Fecha no seleccionada"
            }
        }

        return root
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.time
                dateInput.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate!!))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun selectImage() {
        selectImageLauncher.launch("image/*")
    }

    private fun takePicture() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Log.e("AddServiceFragment", "Error creating image file", ex)
            null
        }
        photoFile?.also {
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "com.svape.cruzroja.fileprovider",
                it
            )
            takePictureLauncher.launch(photoUri)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }
}