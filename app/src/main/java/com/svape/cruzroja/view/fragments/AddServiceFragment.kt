package com.svape.cruzroja.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.svape.cruzroja.R
import com.svape.cruzroja.model.Service
import com.svape.cruzroja.model.Volunteer
import com.svape.cruzroja.viewmodel.ServiceViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddServiceFragment : Fragment() {

    private lateinit var serviceViewModel: ServiceViewModel
    private lateinit var dateInput: EditText
    private var selectedDate: Date? = null
    private var photoUri: Uri? = null

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                photoUri = it
                view?.findViewById<ImageView>(R.id.imageView)?.setImageURI(it)
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoUri?.let {
                    view?.findViewById<ImageView>(R.id.imageView)?.setImageURI(it)
                }
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                takePicture()
            } else {
                Toast.makeText(context, "Permitir permiso para tomar foto", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_add_service, container, false)

        serviceViewModel = ViewModelProvider(requireActivity()).get(ServiceViewModel::class.java)

        val serviceNameInput: EditText = root.findViewById(R.id.serviceNameInput)
        dateInput = root.findViewById(R.id.dateInput)
        val addVolunteerButton: Button = root.findViewById(R.id.addVolunteerButton)
        val addServiceButton: Button = root.findViewById(R.id.addServiceButton)
        val selectImageButton: Button = root.findViewById(R.id.selectImageButton)
        val takePictureButton: Button = root.findViewById(R.id.takePictureButton)
        val volunteersLayout: LinearLayout = root.findViewById(R.id.volunteersLayout)

        dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        selectImageButton.setOnClickListener {
            selectImage()
        }

        takePictureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                takePicture()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        addVolunteerButton.setOnClickListener {
            addVolunteerField(volunteersLayout)
        }

        addServiceButton.setOnClickListener {
            val serviceName = serviceNameInput.text.toString()
            val imageUri = photoUri?.toString() ?: ""

            if (selectedDate != null) {
                val volunteers = getVolunteersFromLayout(volunteersLayout)
                val service = Service(serviceName, selectedDate!!, volunteers, imageUri)
                serviceViewModel.addService(service)

                // Mostrar el AlertDialog después de añadir el servicio
                AlertDialog.Builder(requireContext())
                    .setTitle("Servicio Añadido")
                    .setMessage("El servicio ha sido añadido exitosamente.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
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
                dateInput.setText(
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                        selectedDate!!
                    )
                )
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
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File =
            requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun addVolunteerField(volunteersLayout: LinearLayout) {
        val volunteerView = LayoutInflater.from(context).inflate(R.layout.volunteer_input, volunteersLayout, false)
        volunteersLayout.addView(volunteerView)
    }

    private fun getVolunteersFromLayout(volunteersLayout: LinearLayout): List<Volunteer> {
        val volunteers = mutableListOf<Volunteer>()
        for (i in 0 until volunteersLayout.childCount) {
            val volunteerView = volunteersLayout.getChildAt(i)
            val volunteerNameInput: EditText = volunteerView.findViewById(R.id.volunteerNameInput)
            val hoursInput: EditText = volunteerView.findViewById(R.id.hoursInput)

            val name = volunteerNameInput.text.toString()
            val hours = hoursInput.text.toString().toIntOrNull() ?: 0

            if (name.isNotEmpty()) {
                volunteers.add(Volunteer(name, hours))
            }
        }
        return volunteers
    }
}