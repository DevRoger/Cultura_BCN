import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.culturabcn.R
import com.example.culturabcn.login.LoginActivity
import com.example.culturabcn.login.LoginAdapter

class OnboardingFragment3 : Fragment(R.layout.fragment_onboarding_3) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Acceder al Button de la actividad desde el fragmento
        val btnContinuar = view.findViewById<Button>(R.id.btnFinalizar)

        btnContinuar.setOnClickListener {
            /*
            // Guardar que el onboarding ha sido completado en SharedPreferences
            val sharedPref = requireActivity().getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("onboardingCompleted", true)  // Marca que el onboarding ha sido completado
            editor.apply()*/

            // Redirigir al usuario a la MainActivity
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)

            requireActivity().finish()  // Termina la actividad de onboarding para que no pueda volver
        }
    }
}
