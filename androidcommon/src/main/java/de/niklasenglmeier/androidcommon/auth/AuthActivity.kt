package de.niklasenglmeier.androidcommon.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import de.niklasenglmeier.androidcommon.R
import de.niklasenglmeier.androidcommon.databinding.ActivityAuthBinding
import de.niklasenglmeier.androidcommon.databinding.ActivityLoginBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}