package com.example.culturabcn.login

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class LoginAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 4 // Número de pantallas del login
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PreLoginFragment() // Primer fragmento
            1 -> LoginFragment() // Segundo fragmento
            2 -> RegistroFragment()
            else -> RecuperacionFragment() // Tercer fragmento
        }
    }
}
