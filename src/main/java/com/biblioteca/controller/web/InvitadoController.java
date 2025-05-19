package com.biblioteca.controller.web;

import com.biblioteca.model.Libro;
import com.biblioteca.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/invitado")
public class InvitadoController {
    
    @Autowired
    private LibroService libroService;
    
    @GetMapping("/inicio")
    public String inicio() {
        return "invitado/inicio";
    }
    
    @GetMapping("/catalogo")
    public String mostrarCatalogo(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String estado,
            Model model) {
        
        List<Libro> libros;
        
        if (categoria != null && !categoria.isEmpty()) {
            libros = libroService.obtenerPorCategoria(categoria);
        } else if (estado != null && !estado.isEmpty()) {
            libros = libroService.obtenerPorEstadoDisponibilidad(estado);
        } else {
            libros = libroService.obtenerTodos();
        }
        
        model.addAttribute("libros", libros);
        model.addAttribute("estados", libroService.obtenerEstadosDisponibilidad());
        return "invitado/catalogo";
    }
    
    @GetMapping("/detalle-libro/{id}")
    public String mostrarDetalleLibro(@PathVariable String id, Model model) {
        // Obtener el libro por su ID
        libroService.obtenerPorId(id).ifPresent(libro -> {
            model.addAttribute("libro", libro);
        });
        return "invitado/detalle-libro";
    }
    
    /**
     * Endpoint para servir imágenes de libros directamente desde la base de datos
     */
    @GetMapping("/imagen-libro/{id}")
    public void mostrarImagenLibro(@PathVariable String id, HttpServletResponse response) throws IOException {
        Optional<Libro> libroOpt = libroService.obtenerPorId(id);
        
        if (libroOpt.isPresent() && libroOpt.get().getImagen() != null && libroOpt.get().getImagen().length > 0) {
            Libro libro = libroOpt.get();
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            response.getOutputStream().write(libro.getImagen());
            response.getOutputStream().flush();
        } else {
            // Si no hay imagen, podríamos redirigir a una imagen por defecto
            // o simplemente no hacer nada y dejar que el cliente maneje el error
        }
    }
}
