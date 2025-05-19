package com.biblioteca.controller.web;

import com.biblioteca.model.Usuario;
import com.biblioteca.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @GetMapping("/inicio")
    public String inicio(Model model, HttpSession session) {
        // Obtener información del usuario de la sesión
        String usuarioId = (String) session.getAttribute("usuarioId");
        
        // Buscar el usuario en la base de datos
        Usuario admin = usuarioService.obtenerPorId(usuarioId).orElse(null);
        model.addAttribute("administrador", admin);
        
        return "admin/inicio";
    }
}
