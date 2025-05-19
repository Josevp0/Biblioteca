package com.biblioteca.controller.web;

import com.biblioteca.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @GetMapping("/")
    public String home() {
        // Verificar si hay usuarios en el sistema
        if (usuarioService.obtenerTodos().isEmpty()) {
            // Si no hay usuarios, redirigir a la configuración inicial
            return "redirect:/configuracion-inicial";
        }
        
        // Si hay usuarios, redirigir a la página de login
        return "redirect:/login";
    }
}
