package com.example.culturabcn.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.culturabcn.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_principal)

        supportActionBar?.hide()

        // Configura el ViewPager2
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.isUserInputEnabled = false // Esto inhabilita el deslizamiento lateral
        val adapter = LoginAdapter(this)
        viewPager.adapter = adapter
    }
}
