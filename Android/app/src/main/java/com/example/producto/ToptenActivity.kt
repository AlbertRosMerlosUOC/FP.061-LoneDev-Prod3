package com.example.producto

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.producto.dao.PlayerDao
import com.example.producto3.R
import com.example.producto.model.Player
import com.example.producto.model.GameResult
import com.example.producto3.databinding.ActivityToptenBinding
import kotlinx.coroutines.launch
import java.util.Locale

class ToptenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityToptenBinding
    private var musicReceiver: MusicReceiver? = null
    private var jugadorActual: Player? = null
    private val playerDao = PlayerDao()

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityToptenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btnSelectLanguage: ImageButton = findViewById(R.id.buttonSelectLanguage)
        btnSelectLanguage.setOnClickListener { view ->
            showLanguagePopup(view)
        }

        val jugadorId = intent.getStringExtra("jugadorId").toString()

        playerDao.findPlayerById(jugadorId) { player ->
            if (player != null) {
                jugadorActual = player
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val top10Results = repository.getTop10()
            println("->" + top10Results.size)
            val adapter = ToptenAdapter(top10Results)
            binding.recyclerView.adapter = adapter
        }

        binding.botonIniciarJuego.setOnClickListener {
            if (jugadorActual != null) {
                navegarPantallaJuego()
            } else {
                Toast.makeText(this, getString(R.string.select_player),
                    Toast.LENGTH_SHORT).show()
            }
        }

        binding.leaderboardButton.setOnClickListener {
            navegarPantallaLeaderboard()
        }

        binding.historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("jugadorId", jugadorActual?.id)
            startActivity(intent)
        }

        binding.changeUserButton.setOnClickListener {
            navegarPantallaInicio()
        }

        val musicIntent = Intent(this, MusicService::class.java)
        startService(musicIntent)

        binding.buttonToggleMusic.setOnClickListener {
            toggleMusic()
        }

        binding.buttonSelectMusic.setOnClickListener {
            selectMusicLauncher.launch(arrayOf("audio/*"))
        }

        musicReceiver = MusicReceiver { isPlaying ->
            updateMusicButtonIcon(isPlaying)
        }
        val filter = IntentFilter("com.example.producto3.MUSIC_STATE")
        registerReceiver(musicReceiver, filter, RECEIVER_EXPORTED)

        checkMusicState()

        binding.buttonHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
    }

    private fun showLanguagePopup(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.language_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.language_english -> restartActivityWithLocale("en")
                R.id.language_spanish -> restartActivityWithLocale("es")
                R.id.language_catalan -> restartActivityWithLocale("ca")
            }
            true
        }
        popup.show()
    }

    private fun setAppLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    private fun restartActivityWithLocale(languageCode: String) {
        saveSelectedLanguage(this, languageCode)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        finish()
        startActivity(intent)
    }

    override fun attachBaseContext(newBase: Context?) {
        val selectedLanguage = newBase?.let { getSelectedLanguage(it) } ?: Locale.getDefault().language
        val contextWithLocale = newBase?.let { setAppLocale(it, selectedLanguage) }
        super.attachBaseContext(contextWithLocale)
    }

    private fun saveSelectedLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        prefs.edit().putString("SelectedLanguage", languageCode).apply()
    }

    private fun getSelectedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return prefs.getString("SelectedLanguage", Locale.getDefault().language) ?: Locale.getDefault().language
    }

    private fun navegarPantallaJuego() {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("jugadorId", jugadorActual?.id)
        startActivity(intent)
    }

    private fun navegarPantallaLeaderboard() {
        val intent = Intent(this, LeaderboardActivity::class.java)
        intent.putExtra("jugadorId", jugadorActual?.id)
        startActivity(intent)
    }

    private fun navegarPantallaInicio() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(musicReceiver)
    }

    private fun toggleMusic() {
        val musicIntent = Intent(this, MusicService::class.java)
        musicIntent.action = "com.example.producto3.TOGGLE_MUSIC"
        startService(musicIntent)
    }

    private fun updateMusicButtonIcon(isPlaying: Boolean) {
        val musicButton: ImageButton = binding.buttonToggleMusic
        if (isPlaying) {
            musicButton.setImageResource(R.drawable.ic_music_pause)
        } else {
            musicButton.setImageResource(R.drawable.ic_music_play)
        }
    }

    private fun checkMusicState() {
        val musicIntent = Intent(this, MusicService::class.java)
        musicIntent.action = "com.example.producto3.GET_MUSIC_STATE"
        startService(musicIntent)
    }

    private val selectMusicLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val musicIntent = Intent(this, MusicService::class.java).apply {
                action = "com.example.producto3.CHANGE_MUSIC"
                data = uri
            }
            startService(musicIntent)
        }
    }

    private val repository by lazy {
        GameResultRepository(RetrofitInstance.retrofit.create(FirebaseApi::class.java))
    }

    private fun mostrarTop10EnRecyclerView(results: List<GameResult>) {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ToptenAdapter(results)
    }

}