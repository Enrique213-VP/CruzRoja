package com.svape.cruzroja.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.Manifest
import android.app.DatePickerDialog
import com.svape.cruzroja.R
import com.svape.cruzroja.model.Service
import com.svape.cruzroja.model.Volunteer
import com.svape.cruzroja.viewmodel.ServiceViewModel
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {


    private val serviceViewModel: ServiceViewModel by viewModels()
    private lateinit var imageView: ImageView
    private var photoUri: Uri? = null
    private var currentPhotoPath: String? = null
    private lateinit var dateInput: EditText
    private var selectedDate: Date? = null


    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
    }

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            photoUri = it
            imageView.setImageURI(it)
        }
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            val uri = photoUri
            if (uri != null) {
                imageView.setImageURI(uri)
                galleryAddPic()
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val adapter = ServiceAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        serviceViewModel.services.observe(this, Observer { services ->
            services?.let { adapter.submitList(it) }
        })

        val serviceNameInput: EditText = findViewById(R.id.serviceNameInput)
        dateInput = findViewById(R.id.dateInput)
        val volunteerNameInput: EditText = findViewById(R.id.volunteerNameInput)
        val hoursInput: EditText = findViewById(R.id.hoursInput)
        val addServiceButton: Button = findViewById(R.id.addServiceButton)
        val addVolunteerButton: Button = findViewById(R.id.addVolunteerButton)
        val selectImageButton: Button = findViewById(R.id.selectImageButton)
        val takePictureButton: Button = findViewById(R.id.takePictureButton)
        imageView = findViewById(R.id.imageView)

        dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        addServiceButton.setOnClickListener {
            val serviceName = serviceNameInput.text.toString()
            val dateString = dateInput.text.toString()
            val imageUri = photoUri?.toString() ?: ""

            val date = try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
            } catch (e: ParseException) {
                null
            }

            if (date != null) {
                val service = Service(serviceName, date, emptyList(), imageUri)
                serviceViewModel.addService(service)
            } else {
                // Manejar error de fecha inválida
                dateInput.error = "Fecha inválida"
            }
        }

        addVolunteerButton.setOnClickListener {
            val serviceName = serviceNameInput.text.toString()
            val volunteerName = volunteerNameInput.text.toString()
            val hours = hoursInput.text.toString().toInt()
            val volunteer = Volunteer(volunteerName, hours, photoUri?.toString() ?: "")
            serviceViewModel.addVolunteer(serviceName, volunteer)
        }

        dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        addServiceButton.setOnClickListener {
            val serviceName = serviceNameInput.text.toString()
            val imageUri = photoUri?.toString() ?: ""

            if (selectedDate != null) {
                val service = Service(serviceName, selectedDate!!, emptyList(), imageUri)
                serviceViewModel.addService(service)
            } else {
                // Manejar error de fecha no seleccionada
                dateInput.error = "Fecha no seleccionada"
            }
        }

        addVolunteerButton.setOnClickListener {
            val serviceName = serviceNameInput.text.toString()
            val volunteerName = volunteerNameInput.text.toString()
            val hours = hoursInput.text.toString().toInt()
            val volunteer = Volunteer(volunteerName, hours, photoUri?.toString() ?: "")
            serviceViewModel.addVolunteer(serviceName, volunteer)
        }

        selectImageButton.setOnClickListener {
            selectImage()
        }

        takePictureButton.setOnClickListener {
            if (checkPermissions()) {
                takePicture()
            } else {
                requestPermissions()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
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

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture()
            }
        }
    }

    private fun selectImage() {
        selectImageLauncher.launch("image/*")
    }

    private fun takePicture() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }

        photoFile?.also {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                it
            )
            photoUri = uri
            takePictureLauncher.launch(uri)
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = currentPhotoPath?.let { File(it) }
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
        }
    }
}