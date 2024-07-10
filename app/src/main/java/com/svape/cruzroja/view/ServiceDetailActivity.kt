package com.svape.cruzroja.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.svape.cruzroja.R
import com.svape.cruzroja.databinding.ActivityServiceDetailBinding
import com.svape.cruzroja.model.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.log

class ServiceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiceDetailBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityServiceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val service = intent.getParcelableExtra<Service>("service")
        service?.let {
            binding.serviceNameTextView.text = it.serviceName
            binding.serviceDateTextView.text =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.date)

            it.volunteers.forEach { volunteer ->
                val volunteerTextView = TextView(this).apply {
                    text = "${volunteer.name} - ${volunteer.hours} horas"
                    textSize = 16f
                }
                binding.volunteerListLayout.addView(volunteerTextView)
            }

            Glide.with(this).load(it.imageUri).into(binding.serviceImage)
            Log.e("ImageCruzroja", it.imageUri)
        }

        binding.exportPdfButton.setOnClickListener {
            service?.let { service ->
                CoroutineScope(Dispatchers.Main).launch {
                    exportServiceToPdf(service)
                }
            }
        }

        binding.sharePdfButton.setOnClickListener {
            service?.let { service ->
                val pdfFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "service_${service.serviceName}.pdf")
                if (pdfFile.exists()) {
                    sharePdf(pdfFile)
                } else {
                    Toast.makeText(this, "Primero debes exportar el PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun exportServiceToPdf(service: Service) {
        withContext(Dispatchers.IO) {
            val pdfDocument = PdfDocument()

            // Crear página
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint()

            paint.color = Color.BLACK
            paint.textSize = 16f

            // Escribir contenido en el PDF
            var yPosition = 50f
            canvas.drawText("Nombre del Servicio: ${service.serviceName}", 10f, yPosition, paint)
            yPosition += 30f
            canvas.drawText(
                "Fecha: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(service.date)}",
                10f,
                yPosition,
                paint
            )
            yPosition += 30f
            canvas.drawText("Voluntarios:", 10f, yPosition, paint)
            yPosition += 30f
            service.volunteers.forEach { volunteer ->
                canvas.drawText("${volunteer.name} - ${volunteer.hours} horas", 20f, yPosition, paint)
                yPosition += 30f
            }

            // Dibujar imagen si está disponible
            if (!service.imageUri.isNullOrEmpty()) {
                try {
                    val bitmap: Bitmap = Glide.with(this@ServiceDetailActivity)
                        .asBitmap()
                        .load(service.imageUri)
                        .submit()
                        .get()

                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, 300, true)
                    canvas.drawBitmap(scaledBitmap, 10f, yPosition + 20f, paint)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Finalizar la página y escribir el PDF
            pdfDocument.finishPage(page)

            val filePath = File(
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "service_${service.serviceName}.pdf"
            )

            try {
                pdfDocument.writeTo(FileOutputStream(filePath))
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ServiceDetailActivity,
                        "PDF guardado en ${filePath.absolutePath}",
                        Toast.LENGTH_LONG
                    ).show()
                    openPdf(filePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ServiceDetailActivity,
                        "Error al guardar PDF: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                pdfDocument.close()
            }
        }
    }

    private fun openPdf(file: File) {
        val pdfUri: Uri = FileProvider.getUriForFile(
            this,
            "com.svape.cruzroja.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Abrir PDF")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        } else {
            Toast.makeText(this, "No se encontró una aplicación para abrir el PDF", Toast.LENGTH_LONG).show()
        }
    }

    private fun sharePdf(file: File) {
        val pdfUri = FileProvider.getUriForFile(
            this,
            "com.svape.cruzroja.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Compartir PDF")
        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(chooser)
        } else {
            Toast.makeText(this, "No se encontró una aplicación para compartir el PDF", Toast.LENGTH_LONG).show()
        }
    }
}