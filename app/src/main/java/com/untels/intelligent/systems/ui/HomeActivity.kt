package com.untels.intelligent.systems.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.mlkit.common.model.LocalModel
import com.untels.intelligent.systems.R
import com.untels.intelligent.systems.databinding.ActivityHomeBinding
import com.untels.intelligent.systems.util.Preference
import com.untels.intelligent.systems.util.graphic.CameraSource
import com.untels.intelligent.systems.util.isPermissionGranted
import com.untels.intelligent.systems.util.objectdetector.ObjectDetectorProcessor
import com.untels.intelligent.systems.util.showSnackbar
import java.io.IOException

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUESTS = 1
        const val OBJECT_DATA = "OBJECT_DATA"
    }

    private var data = ""
    private lateinit var binding: ActivityHomeBinding
    private var cameraSource: CameraSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()
    }

    private fun setupUI() {
        setSupportActionBar(binding.bottomAppBar)
        binding.bottomAppBar.setNavigationOnClickListener {
            startActivity(Intent(this, TranslateActivity::class.java).putExtra(OBJECT_DATA, data))
        }
        binding.addImageFloatingActionButton.setOnClickListener {
            binding.previewView.stop()
            if (allPermissionsGranted()) {
                createCameraSource()
                startCameraSource()
            } else {
                runtimePermissions
            }
        }
        if (allPermissionsGranted()) {
            createCameraSource()
        } else {
            runtimePermissions
        }
    }

    private fun createCameraSource() {
        if (cameraSource == null) {
            cameraSource = CameraSource(this, binding.graphicOverlay)
        }
        try {
            val localModel = LocalModel.Builder()
                .setAssetFilePath("custom_models/bird_classifier.tflite")
                .build()
            val customObjectDetectorOptions =
                Preference.getCustomObjectDetectorOptionsForLivePreview(this, localModel)
            cameraSource?.setMachineLearningFrameProcessor(
                ObjectDetectorProcessor(this, customObjectDetectorOptions)
            )
        } catch (e: Exception) {
            Log.e("HomeActivity", "Can not create image processor:", e)
            Toast.makeText(applicationContext, "Can not create image processor: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    private val requiredPermissions: Array<String?>
        get() = try {
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }

    private val runtimePermissions: Unit
        get() {
            val allNeededPermissions: MutableList<String?> = ArrayList()
            for (permission in requiredPermissions) {
                if (!isPermissionGranted(this, permission)) {
                    allNeededPermissions.add(permission)
                }
            }
            if (allNeededPermissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    allNeededPermissions.toTypedArray(),
                    PERMISSION_REQUESTS
                )
            }
        }

    private fun allPermissionsGranted(): Boolean {
        requiredPermissions.forEach {
            if (!isPermissionGranted(this, it)) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i("HomeActivity", "Permission granted!")
        if (allPermissionsGranted()) {
            createCameraSource()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        if (cameraSource != null) {
            try {
                binding.previewView.start(cameraSource, binding.graphicOverlay)
            } catch (e: IOException) {
                Log.e("HomeActivity", "Unable to start camera source.", e)
                cameraSource?.release()
                cameraSource = null
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.stopItem -> {
                binding.previewView.stop()
                binding.coordinatorLayout to binding.addImageFloatingActionButton showSnackbar "Se detuvo de escanear. Vuelve a darle Play para comenzar."
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.control_menu, menu)
        return true
    }

    public override fun onResume() {
        super.onResume()
        Log.d("HomeActivity", "onResume")
        createCameraSource()
        startCameraSource()
    }

    /** Stops the camera.  */
    override fun onPause() {
        super.onPause()
        binding.previewView.stop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (cameraSource != null) {
            cameraSource?.release()
        }
    }
}