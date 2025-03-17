import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.culturabcn.MainActivity
import com.example.culturabcn.R
import com.example.culturabcn.login.LoginActivity

class OnboardingFragment2 : Fragment(R.layout.fragment_onboarding_2) {
    // Aquí puedes agregar la lógica específica para esta pantalla de onboarding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnContinuarF2 = view.findViewById<Button>(R.id.btnContinuarF2)
        val btnSaltarF2 = view.findViewById<Button>(R.id.btnSaltar2)

        btnContinuarF2.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 3
        }

        btnSaltarF2.setOnClickListener {
            // Redirigir al usuario a la MainActivity
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)

            requireActivity().finish()  // Termina la actividad de onboarding para que no pueda volver
        }
    }}