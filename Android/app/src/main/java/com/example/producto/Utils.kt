package com.example.producto

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import java.util.*

object Utils {

    // Cambiar el idioma de la aplicación
    fun setAppLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    // Mostrar el popup de selección de idioma
    fun showLanguagePopup(context: Context, view: View, onLanguageSelected: (String) -> Unit) {
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(com.example.producto3.R.menu.language_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                com.example.producto3.R.id.language_english -> onLanguageSelected("en")
                com.example.producto3.R.id.language_spanish -> onLanguageSelected("es")
                com.example.producto3.R.id.language_catalan -> onLanguageSelected("ca")
            }
            true
        }
        popup.show()
    }

    // Guardar el idioma seleccionado en SharedPreferences
    fun saveSelectedLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        prefs.edit().putString("SelectedLanguage", languageCode).apply()
    }

    // Obtener el idioma seleccionado desde SharedPreferences
    fun getSelectedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return prefs.getString("SelectedLanguage", Locale.getDefault().language) ?: Locale.getDefault().language
    }

    // Cambiar el ícono del botón de música
    fun updateMusicButtonIcon(button: ImageButton, isPlaying: Boolean) {
        if (isPlaying) {
            button.setImageResource(com.example.producto3.R.drawable.ic_music_pause)
        } else {
            button.setImageResource(com.example.producto3.R.drawable.ic_music_play)
        }
    }

    // Reiniciar actividad con el nuevo idioma
    fun restartActivityWithLocale(context: Context, activityClass: Class<out AppCompatActivity>, languageCode: String) {
        saveSelectedLanguage(context, languageCode)
        val intent = Intent(context, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        (context as AppCompatActivity).finish()
        context.startActivity(intent)
    }
}
