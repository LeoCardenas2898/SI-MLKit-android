package com.untels.intelligent.systems.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.preference.PreferenceManager
import com.google.android.gms.common.images.Size
import com.google.common.base.Preconditions
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.objects.ObjectDetectorOptionsBase.DetectorMode
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.untels.intelligent.systems.R
import com.untels.intelligent.systems.util.graphic.CameraSource

class Preference {
    companion object {
        private fun getCustomObjectDetectorOptions(
            context: Context?,
            localModel: LocalModel?,
            @StringRes prefKeyForMultipleObjects: Int,
            @StringRes prefKeyForClassification: Int,
            @DetectorMode mode: Int
        ): CustomObjectDetectorOptions {
            val sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
            val enableMultipleObjects =
                sharedPreferences.getBoolean(context?.getString(prefKeyForMultipleObjects), false)
            val enableClassification = sharedPreferences.getBoolean(context?.getString(prefKeyForClassification), true)
            val builder = CustomObjectDetectorOptions.Builder(localModel).setDetectorMode(mode)
            if (enableMultipleObjects) {
                builder.enableMultipleObjects()
            }
            if (enableClassification) {
                builder.enableClassification().setMaxPerObjectLabelCount(1)
            }
            return builder.build()
        }

        fun getCustomObjectDetectorOptionsForLivePreview(
            context: Context?, localModel: LocalModel?
        ): CustomObjectDetectorOptions {
            return getCustomObjectDetectorOptions(
                context,
                localModel,
                R.string.pref_key_live_preview_object_detector_enable_multiple_objects,
                R.string.pref_key_live_preview_object_detector_enable_classification,
                CustomObjectDetectorOptions.STREAM_MODE
            )
        }

        fun isCameraLiveViewportEnabled(context: Context): Boolean {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val prefKey = context.getString(R.string.pref_key_camera_live_viewport)
            return sharedPreferences.getBoolean(prefKey, false)
        }


        fun getCameraPreviewSizePair(context: Context, cameraId: Int): CameraSource.SizePair? {
            Preconditions.checkArgument(
                cameraId == CameraSource.CAMERA_FACING_BACK
                        || cameraId == CameraSource.CAMERA_FACING_FRONT
            )
            val previewSizePrefKey: String
            val pictureSizePrefKey: String
            if (cameraId == CameraSource.CAMERA_FACING_BACK) {
                previewSizePrefKey = context.getString(R.string.pref_key_rear_camera_preview_size)
                pictureSizePrefKey = context.getString(R.string.pref_key_rear_camera_picture_size)
            } else {
                previewSizePrefKey = context.getString(R.string.pref_key_front_camera_preview_size)
                pictureSizePrefKey = context.getString(R.string.pref_key_front_camera_picture_size)
            }
            return try {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                CameraSource.SizePair(
                    Size.parseSize(sharedPreferences.getString(previewSizePrefKey, null)),
                    Size.parseSize(sharedPreferences.getString(pictureSizePrefKey, null))
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}