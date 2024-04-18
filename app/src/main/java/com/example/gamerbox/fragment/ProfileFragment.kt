package com.example.gamerbox.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.gamerbox.R

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el diseño de este fragmento
        val view = inflater.inflate(R.layout.activity_profile, container, false)

        // Configurar la barra de herramientas
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit_profile -> {
                    // Manejar la acción de editar perfil
                    // Por ejemplo, navegar a la pantalla de edición de perfil
                    true
                }

                R.id.action_logout -> {
                    // Manejar la acción de cerrar sesión
                    // Por ejemplo, cerrar la sesión del usuario y volver a la pantalla de inicio de sesión
                    // Aquí debes implementar la lógica para cerrar la sesión del usuario
                    true
                }

                else -> false
            }
        }

        return view
    }
}