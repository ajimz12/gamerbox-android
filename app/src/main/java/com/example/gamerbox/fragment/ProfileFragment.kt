package com.example.gamerbox.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import com.example.gamerbox.AuthActivity
import com.example.gamerbox.MainActivity
import com.example.gamerbox.R
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {


    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_profile, container, false)
        auth = FirebaseAuth.getInstance()

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_profile -> {
                    // TODO: Implementar la lógica para editar el perfil
                    true
                }

                R.id.action_logout -> {
                    showLogoutConfirmationDialog()
                    true
                }

                else -> false
            }
        }
        return view
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Cerrar sesión")
        builder.setMessage("¿Estás seguro de que quieres cerrar sesión?")
        builder.setPositiveButton("Aceptar") { _, _ ->
            // Cerrar la sesión del usuario
            auth.signOut()
            // Mostrar la pantalla de inicio de sesión
            showLogin()
        }
        builder.setNegativeButton("Cancelar", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun showLogin() {
        val loginIntent = Intent(requireContext(), AuthActivity::class.java)
        startActivity(loginIntent)
    }
}