import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.culturabcn.MainActivity
import com.example.culturabcn.R

class OnboardingFragment1 : Fragment(R.layout.fragment_onboarding_1) {
    // Aquí puedes agregar la lógica específica para esta pantalla de onboarding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnContinuarF1 = view.findViewById<Button>(R.id.btnContinuarF1)
        val btnSaltarF1 = view.findViewById<Button>(R.id.btnSaltar1)

        btnContinuarF1.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 1
        }

        btnSaltarF1.setOnClickListener {
            // Redirigir al usuario a la MainActivity
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)

            requireActivity().finish()  // Termina la actividad de onboarding para que no pueda volver
        }
    }
}