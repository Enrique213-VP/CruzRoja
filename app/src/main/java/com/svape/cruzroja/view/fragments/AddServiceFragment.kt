package com.svape.cruzroja.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
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
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.svape.cruzroja.R
import com.svape.cruzroja.data.database.AppDatabase
import com.svape.cruzroja.data.model.Service
import com.svape.cruzroja.data.model.Volunteer
import com.svape.cruzroja.repository.Repository
import com.svape.cruzroja.viewmodel.ServiceViewModel
import com.svape.cruzroja.viewmodel.ServiceViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.sql.DataSource

class AddServiceFragment : Fragment() {

    private lateinit var serviceViewModel: ServiceViewModel
    private lateinit var serviceNameInput: EditText
    private lateinit var serviceDescriptionInput: EditText
    private lateinit var volunteersLayout: LinearLayout
    private var existingService: Service? = null
    private lateinit var dateInput: EditText
    private var selectedDate: Date? = null
    private var photoUri: Uri? = null
    private lateinit var repository: Repository

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                photoUri = it
                // Usar Glide para cargar la imagen de manera más eficiente
                Glide.with(requireContext())
                    .load(it)
                    .into(view?.findViewById(R.id.imageView) ?: return@let)

                // También puedes guardar los permisos persistentes si los necesitas
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                requireContext().contentResolver.takePersistableUriPermission(it, flag)
            } catch (e: Exception) {
                Log.e("AddServiceFragment", "Error loading image", e)
                Toast.makeText(context, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recuperar el servicio si estamos en modo edición
        existingService = arguments?.getParcelable("service")
    }


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_add_service, container, false)

        // Inicializar vistas
        serviceNameInput = root.findViewById(R.id.serviceNameInput)
        serviceDescriptionInput = root.findViewById(R.id.serviceDescriptionInput)
        dateInput = root.findViewById(R.id.dateInput)
        volunteersLayout = root.findViewById(R.id.volunteersLayout)

        val database = AppDatabase.getDatabase(requireContext())
        val serviceDao = database.serviceDao()
        val volunteerDao = database.volunteerDao()
        repository = Repository(serviceDao, volunteerDao)
        val factory = ServiceViewModelFactory(repository)

        serviceViewModel = ViewModelProvider(requireActivity(), factory)[ServiceViewModel::class.java]


        val serviceNameInput: EditText = root.findViewById(R.id.serviceNameInput)
        dateInput = root.findViewById(R.id.dateInput)
        val addVolunteerButton: Button = root.findViewById(R.id.addVolunteerButton)
        val addServiceButton: Button = root.findViewById(R.id.addServiceButton)
        val selectImageButton: Button = root.findViewById(R.id.selectImageButton)
        val takePictureButton: Button = root.findViewById(R.id.takePictureButton)
        val volunteersLayout: LinearLayout = root.findViewById(R.id.volunteersLayout)
        val serviceDescriptionInput: EditText = root.findViewById(R.id.serviceDescriptionInput)

        // Si estamos en modo edición, llenar los campos
        existingService?.let { service ->
            serviceNameInput.setText(service.serviceName)
            serviceDescriptionInput.setText(service.description)
            selectedDate = service.date
            dateInput.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(service.date))


            if (service.imageUri.isNotEmpty()) {
                try {
                    val uri = Uri.parse(service.imageUri)
                    photoUri = uri

                    view?.findViewById<ImageView>(R.id.imageView)?.let { imageView ->
                        Glide.with(requireContext())
                            .load(uri)
                            .error(R.drawable.ic_launcher_background)
                            .into(imageView)
                    }
                } catch (e: Exception) {
                    Log.e("AddServiceFragment", "Error loading image: ${e.message}", e)
                    Toast.makeText(context, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
                }
            }

            // Cargar voluntarios existentes
            viewLifecycleOwner.lifecycleScope.launch {
                val volunteers = serviceViewModel.getVolunteersForService(service.id)
                volunteers.forEach { volunteer ->
                    addVolunteerField(volunteersLayout, volunteer)
                }
            }

            // Cambiar el texto del botón
            root.findViewById<Button>(R.id.addServiceButton).text = "Actualizar Servicio"
        }

        // Configurar botón de cancelar
        root.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Modificar el click listener del botón de guardar
        root.findViewById<Button>(R.id.addServiceButton).setOnClickListener {
            saveOrUpdateService()
        }

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
            val description = serviceDescriptionInput.text.toString()
            val imageUri = photoUri?.toString() ?: ""

            if (selectedDate != null) {
                if (serviceName.isEmpty()) {
                    serviceNameInput.error = "Nombre del servicio requerido"
                    return@setOnClickListener
                }

                if (description.isEmpty()) { // Nueva validación
                    serviceDescriptionInput.error = "Descripción requerida"
                    return@setOnClickListener
                }

                val volunteers = getVolunteersFromLayout(volunteersLayout)
                if (volunteers.isEmpty()) {
                    Toast.makeText(context, "Agregar al menos un voluntario", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val service = Service(
                    serviceName = serviceName,
                    description = description, // Nueva línea
                    date = selectedDate!!,
                    imageUri = imageUri
                )

                // Asignar los voluntarios al servicio
                service.volunteers = volunteers

                serviceViewModel.addService(service)

                // Mostrar mensaje de éxito y limpiar campos
                AlertDialog.Builder(requireContext())
                    .setTitle("Servicio Añadido")
                    .setMessage("El servicio ha sido añadido exitosamente.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        // Limpiar campos
                        serviceNameInput.text.clear()
                        dateInput.text.clear()
                        volunteersLayout.removeAllViews()
                        selectedDate = null
                        photoUri = null
                        view?.findViewById<ImageView>(R.id.imageView)?.setImageResource(R.drawable.ic_launcher_background)

                        // Opcional: volver al fragment anterior
                        // requireActivity().supportFragmentManager.popBackStack()
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
        selectImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
                volunteers.add(Volunteer(
                    serviceId = 0,  // Se asignará después cuando se inserte el servicio
                    name = name,
                    hours = hours
                ))
            }
        }
        return volunteers
    }

    private fun addVolunteerField(volunteersLayout: LinearLayout, existingVolunteer: Volunteer? = null) {
        val volunteerView = LayoutInflater.from(context)
            .inflate(R.layout.volunteer_input, volunteersLayout, false)

        // Si hay un voluntario existente, llenar los campos
        existingVolunteer?.let { volunteer ->
            volunteerView.findViewById<EditText>(R.id.volunteerNameInput).setText(volunteer.name)
            volunteerView.findViewById<EditText>(R.id.hoursInput).setText(volunteer.hours.toString())
        }

        volunteersLayout.addView(volunteerView)
    }

    private fun saveOrUpdateService() {
        val serviceName = serviceNameInput.text.toString()
        val description = serviceDescriptionInput.text.toString()
        val imageUri = photoUri?.toString() ?: ""

        if (selectedDate != null) {
            if (serviceName.isEmpty()) {
                serviceNameInput.error = "Nombre del servicio requerido"
                return
            }

            if (description.isEmpty()) {
                serviceDescriptionInput.error = "Descripción requerida"
                return
            }

            val volunteers = getVolunteersFromLayout(volunteersLayout)
            if (volunteers.isEmpty()) {
                Toast.makeText(context, "Agregar al menos un voluntario", Toast.LENGTH_SHORT).show()
                return
            }

            val service = Service(
                id = existingService?.id ?: 0,
                serviceName = serviceName,
                description = description,
                date = selectedDate!!,
                imageUri = imageUri
            ).apply {
                this.volunteers = volunteers
            }

            if (existingService != null) {
                serviceViewModel.updateService(service)
            } else {
                serviceViewModel.addService(service)
            }

            // Mostrar mensaje de éxito
            val message = if (existingService != null)
                "Servicio actualizado exitosamente"
            else
                "Servicio añadido exitosamente"

            AlertDialog.Builder(requireContext())
                .setTitle("Éxito")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    parentFragmentManager.popBackStack()
                }
                .show()
        } else {
            dateInput.error = "Fecha no seleccionada"
        }
    }
}