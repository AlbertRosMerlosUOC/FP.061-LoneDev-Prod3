package com.example.producto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MusicReceiver(private val onMusicStateChanged: (Boolean) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val isPlaying = intent?.getBooleanExtra("isPlaying", false) ?: false
        onMusicStateChanged(isPlaying)
    }
}
