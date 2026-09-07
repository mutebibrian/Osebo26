package com.devbrian.osebo.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import com.devbrian.osebo.R
import com.devbrian.osebo.data.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var ivLogo: ImageView
    private lateinit var tvAppName: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initViews()
        startAnimations()
    }

    private fun initViews() {
        ivLogo = findViewById(R.id.iv_logo)
        tvAppName = findViewById(R.id.tv_app_name)
        progressBar = findViewById(R.id.progress_bar)
        preferenceManager = PreferenceManager.getInstance(this)
    }

    private fun startAnimations() {
        
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1.0f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1.0f)
        val alpha = PropertyValuesHolder.ofFloat("alpha", 0.2f, 0.9f)

        val logoAnimator = ObjectAnimator.ofPropertyValuesHolder(ivLogo, scaleX, scaleY, alpha)
        logoAnimator.duration = 1200
        logoAnimator.interpolator = AnticipateOvershootInterpolator()
        logoAnimator.start()

        
        val rotateAnimator = ObjectAnimator.ofFloat(ivLogo, "rotation", 0f, 360f)
        rotateAnimator.duration = 1500
        rotateAnimator.interpolator = AccelerateDecelerateInterpolator()
        rotateAnimator.startDelay = 300
        rotateAnimator.start()

        
        Handler(Looper.getMainLooper()).postDelayed({
            tvAppName.alpha = 0f
            tvAppName.translationY = 50f
            tvAppName.visibility = TextView.VISIBLE

            val textAlpha = ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f)
            textAlpha.duration = 800

            val textTranslate = ObjectAnimator.ofFloat(tvAppName, "translationY", 50f, 0f)
            textTranslate.duration = 800
            textTranslate.interpolator = BounceInterpolator()

            textAlpha.start()
            textTranslate.start()
        }, 600)

        
        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.visibility = ProgressBar.VISIBLE
            val progressAlpha = ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 0.8f)
            progressAlpha.duration = 500
            progressAlpha.start()
        }, 1400)

        
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 2800)
    }

    private fun navigateToNextScreen() {
        
        val fadeOut = ObjectAnimator.ofFloat(ivLogo, "alpha", 0.9f, 0f)
        fadeOut.duration = 300
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                
                val intent = if (preferenceManager.isLoggedIn()) {
                    Intent(this@SplashActivity, MainActivity::class.java)
                } else {
                    Intent(this@SplashActivity, LoginActivity::class.java)
                }
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        })
        fadeOut.start()
    }
}


