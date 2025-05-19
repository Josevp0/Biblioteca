package com.biblioteca.controller.web;

import com.biblioteca.model.Prestamo;
import com.biblioteca.service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/prestamos")
public class AdminPrestamoController {
    
    @Autowired
    private PrestamoService prestamoService;
    
    @GetMapping("/listar")
    public String listarPrestamos(Model model) {
        model.addAttribute("prestamos", prestamoService.obtenerTodos());
        return "admin/prestamos/listar";
    }
    
    @GetMapping("/detalle/{id}")
    public String verDetallePrestamo(@PathVariable String id, Model model) {
        prestamoService.obtenerPorId(id).ifPresent(prestamo -> model.addAttribute("prestamo", prestamo));
        return "admin/prestamos/detalle";
    }
}
