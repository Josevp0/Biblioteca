package com.biblioteca.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auxiliar")
public class AuxiliarController {
    
    @GetMapping("/inicio")
    public String inicio() {
        return "auxiliar/inicio";
    }
}
