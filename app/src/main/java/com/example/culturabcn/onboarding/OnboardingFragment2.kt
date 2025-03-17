import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.culturabcn.R

class OnboardingFragment2 : Fragment(R.layout.fragment_onboarding_2) {
    // Aquí puedes agregar la lógica específica para esta pantalla de onboarding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnContinuarF2 = view.findViewById<Button>(R.id.btnContinuarF2)

        btnContinuarF2.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 3
        }
    }}