package com.example.producto

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.producto3.R
import com.example.producto3.databinding.ActivityLoginBinding
import com.example.producto.model.Player
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.database
import java.util.Locale

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var jugadorActual: Player? = null
    private var musicReceiver: MusicReceiver? = null
    var selectedLanguage = ""
    // TODO Borrar ? var database = FirebaseDatabase.getInstance().getReference()

    private val RC_SIGN_IN = 1001

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO ??? database = FirebaseDatabase.getInstance().getReference()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedLanguage = getSelectedLanguage(this)
        setAppLocale(this, selectedLanguage)

        val btnSelectLanguage: ImageButton = findViewById(R.id.buttonSelectLanguage)
        btnSelectLanguage.setOnClickListener { view ->
            showLanguagePopup(view)
        }

        val musicIntent = Intent(this, MusicService::class.java)
        startService(musicIntent)

        findViewById<ImageButton>(R.id.buttonToggleMusic).setOnClickListener {
            toggleMusic()
        }

        findViewById<ImageButton>(R.id.buttonSelectMusic).setOnClickListener {
            selectMusicLauncher.launch(arrayOf("audio/*"))
        }

        musicReceiver = MusicReceiver { isPlaying ->
            updateMusicButtonIcon(isPlaying)
        }
        val filter = IntentFilter("com.example.producto3.MUSIC_STATE")
        registerReceiver(musicReceiver, filter, RECEIVER_EXPORTED)

        checkMusicState()

        findViewById<ImageButton>(R.id.buttonHelp).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        auth = FirebaseAuth.getInstance()
        auth.signOut()
        findViewById<Button>(R.id.signInButton).setOnClickListener {
            signIn()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            Log.d("LoginActivity", "Se ha cerrado la sesión de Google")
        }

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("LoginActivity", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w("LoginActivity", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let { firebaseUser ->
                        val playerEmail = firebaseUser.email ?: return@let
                        val playerName = firebaseUser.displayName ?: "Unknown Player"
                        val encodedEmail = playerEmail.replace(".", ",")

                        val playerRef = Firebase.database.reference.child("Player").child(encodedEmail)

                        playerRef.get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                val jugador = snapshot.getValue(Player::class.java)
                                if (jugador != null) {
                                    jugadorActual = jugador
                                }
                                navegarPantallaJuego()
                            } else {
                                val newPlayer = Player(id = playerEmail, name = playerName, coins = 100)
                                playerRef.setValue(newPlayer).addOnCompleteListener { saveTask ->
                                    if (saveTask.isSuccessful) {
                                        navegarPantallaJuego()
                                    } else {
                                        Log.e("LoginActivity", "Error al guardar el jugador: ${saveTask.exception}")
                                    }
                                }
                            }
                        }.addOnFailureListener { exception ->
                            Log.e("LoginActivity", "Error al verificar el jugador: $exception")
                        }
                    }
                } else {
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                }
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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(musicReceiver)
    }

    private fun toggleMusic() {
        val musicIntent = Intent(this, MusicService::class.java)
        musicIntent.action = "com.example.producto3.TOGGLE_MUSIC"
        startService(musicIntent)
        musicReceiver = MusicReceiver { isPlaying ->
            updateMusicButtonIcon(isPlaying)
        }
    }

    private fun updateMusicButtonIcon(isPlaying: Boolean) {
        val musicButton: ImageButton = findViewById(R.id.buttonToggleMusic)

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
}
