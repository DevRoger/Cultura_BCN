package com.example.culturabcn.onboarding

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.culturabcn.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val viewPager2: ViewPager2 = findViewById(R.id.viewPager)  // El ID de tu ViewPager2

        // Establece el adaptador para ViewPager2
        val onboardingAdapter = OnboardingAdapter(this)
        viewPager2.adapter = onboardingAdapter
    }
}
