package com.biblioteca.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/estudiante")
public class EstudianteController {
    
    @GetMapping("/inicio")
    public String inicio() {
        return "estudiante/inicio";
    }
}
