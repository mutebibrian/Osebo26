package com.devbrian.osebo.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    /** Starts recording to a temp file in the app cache dir. Returns false if it couldn't start. */
    fun start(): Boolean {
        val file = File(context.cacheDir, "voice_input_${System.currentTimeMillis()}.m4a")

        @Suppress("DEPRECATION")
        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

        return try {
            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = mediaRecorder
            outputFile = file
            true
        } catch (e: IOException) {
            mediaRecorder.release()
            false
        } catch (e: IllegalStateException) {
            mediaRecorder.release()
            false
        }
    }

    /** Stops the current recording and returns the recorded file, or null if nothing was recording. */
    fun stop(): File? {
        val mediaRecorder = recorder ?: return null
        return try {
            mediaRecorder.stop()
            outputFile
        } catch (e: RuntimeException) {
            // stop() throws if start() never produced any data (e.g. released too quickly)
            null
        } finally {
            mediaRecorder.release()
            recorder = null
            outputFile = null
        }
    }

    fun cancel() {
        val mediaRecorder = recorder ?: return
        try {
            mediaRecorder.stop()
        } catch (e: RuntimeException) {
            // ignore — we're discarding the recording anyway
        }
        mediaRecorder.release()
        outputFile?.delete()
        recorder = null
        outputFile = null
    }

    val isRecording: Boolean get() = recorder != null
}
