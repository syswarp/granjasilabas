package com.granjasilabas.app

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.granjasilabas.app.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bounce = AnimationUtils.loadAnimation(this, R.anim.bounce)
        binding.tvCapybara.startAnimation(bounce)

        binding.btnJugar.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
    }
}
