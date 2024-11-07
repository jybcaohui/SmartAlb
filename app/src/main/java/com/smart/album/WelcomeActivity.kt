package com.smart.album

import android.content.Intent
import android.os.Bundle
import com.smart.album.utils.PreferencesHelper

class WelcomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        val transitionEffect =  PreferencesHelper.getInstance(this@WelcomeActivity).getInt(
            PreferencesHelper.TRANSITION_EFFECT,0)
        when (transitionEffect) {
            1 -> {
                //Cross Fade
                startActivity(Intent(this@WelcomeActivity,CrossFadeActivity::class.java))
            }
            2 -> {
                //Memory
                startActivity(Intent(this@WelcomeActivity,SlideActivity::class.java))
            }
            else -> {
                // Fade
                startActivity(Intent(this@WelcomeActivity,FadeActivity::class.java))
            }
        }

    }

}
