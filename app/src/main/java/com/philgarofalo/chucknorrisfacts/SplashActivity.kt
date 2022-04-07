package com.philgarofalo.chucknorrisfacts

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.philgarofalo.chucknorrisfacts.MainActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}