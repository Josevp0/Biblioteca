package com.biblioteca.controller.web;

import com.biblioteca.model.Prestamo;
import com.biblioteca.service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auxiliar/prestamos")
public class AuxiliarPrestamoController {
    
    @Autowired
    private PrestamoService prestamoService;
    
    @GetMapping("/listar")
    public String listarPrestamos(Model model) {
        model.addAttribute("prestamos", prestamoService.obtenerTodos());
        return "auxiliar/prestamos/listar";
    }
    
    @GetMapping("/detalle/{id}")
    public String verDetallePrestamo(@PathVariable String id, Model model) {
        prestamoService.obtenerPorId(id).ifPresent(prestamo -> model.addAttribute("prestamo", prestamo));
        return "auxiliar/prestamos/detalle";
    }
    
    @PostMapping("/comentar/{id}")
    public String agregarComentario(
            @PathVariable String id,
            @RequestParam("comentarios") String comentarios,
            RedirectAttributes redirectAttributes) {
        
        Prestamo prestamo = prestamoService.agregarComentario(id, comentarios);
        if (prestamo != null) {
            redirectAttributes.addFlashAttribute("mensaje", "Comentario agregado con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo agregar el comentario. Verifique que el préstamo esté en estado Pendiente.");
        }
        return "redirect:/auxiliar/prestamos/detalle/" + id;
    }
    
    @PostMapping("/devolver/{id}")
    public String devolverPrestamo(@PathVariable String id, RedirectAttributes redirectAttributes) {
        boolean devuelto = prestamoService.marcarComoDevuelto(id);
        if (devuelto) {
            redirectAttributes.addFlashAttribute("mensaje", "Préstamo devuelto y eliminado con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo devolver el préstamo. Verifique que esté en estado Aprobado.");
        }
        return "redirect:/auxiliar/prestamos/listar";
    }
}
