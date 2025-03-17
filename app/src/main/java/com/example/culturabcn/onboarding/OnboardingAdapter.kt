package com.example.culturabcn.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import OnboardingFragment1
import OnboardingFragment2
import OnboardingFragment3

class OnboardingAdapter(activity: OnboardingActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3  // Número de fragmentos en el onboarding

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragment1()
            1 -> OnboardingFragment2()
            2 -> OnboardingFragment3()
            else -> throw IllegalStateException("Invalid position")
        }
    }
}

