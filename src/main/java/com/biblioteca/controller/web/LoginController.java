package com.biblioteca.controller.web;

import com.biblioteca.model.Usuario;
import com.biblioteca.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class LoginController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }
    
    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam String nombre, 
            @RequestParam String contrasena,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Optional<Usuario> usuarioOpt = usuarioService.obtenerPorNombre(nombre);
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Verificar contraseña (en texto plano según requisitos)
            if (usuario.getContrasena().equals(contrasena)) {
                // Guardar usuario en sesión
                session.setAttribute("usuarioId", usuario.getId());
                session.setAttribute("usuarioNombre", usuario.getNombre());
                session.setAttribute("usuarioRol", usuario.getRol());
                
                // Redirigir según el rol
                switch (usuario.getRol()) {
                    case "Administrador":
                        return "redirect:/admin/inicio";
                    case "Bibliotecario":
                        return "redirect:/bibliotecario/inicio";
                    case "Auxiliar":
                        return "redirect:/auxiliar/inicio";
                    case "Profesor":
                        return "redirect:/profesor/inicio";
                    case "Estudiante":
                        return "redirect:/estudiante/inicio";
                    default:
                        return "redirect:/invitado/inicio";
                }
            } else {
                // Contraseña incorrecta
                redirectAttributes.addFlashAttribute("error", "Contraseña incorrecta");
                return "redirect:/login";
            }
        } else {
            // Usuario no encontrado
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/login";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Invalidar sesión
        session.invalidate();
        return "redirect:/login";
    }
    
    @GetMapping("/acceso-denegado")
    public String accesoDenegado() {
        return "acceso-denegado";
    }
}
