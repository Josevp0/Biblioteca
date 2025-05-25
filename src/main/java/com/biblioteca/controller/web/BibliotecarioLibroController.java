package com.biblioteca.controller.web;

import com.biblioteca.model.Libro;
import com.biblioteca.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/bibliotecario/libros")
public class BibliotecarioLibroController {
    
    @Autowired
    private LibroService libroService;
    
    private final List<String> CATEGORIAS = Arrays.asList("Ficción", "No ficción", "Misterio", "Romántico", "Fantasía");
    
    @GetMapping("/listar")
    public String listarLibros(
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
        return "bibliotecario/libros/listar";
    }
    
    @GetMapping("/crear")
    public String mostrarFormularioSeleccionTipo(Model model) {
        model.addAttribute("categorias", CATEGORIAS);
        return "bibliotecario/libros/crear";
    }
    
    @GetMapping("/crear-fisico")
    public String mostrarFormularioCrearFisico(Model model) {
        model.addAttribute("libro", new Libro());
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("estados", libroService.obtenerEstadosDisponibilidad());
        return "bibliotecario/libros/crear-fisico";
    }
    
    @GetMapping("/crear-virtual")
    public String mostrarFormularioCrearVirtual(Model model) {
        model.addAttribute("libro", new Libro());
        model.addAttribute("categorias", CATEGORIAS);
        model.addAttribute("estados", libroService.obtenerEstadosDisponibilidad());
        return "bibliotecario/libros/crear-virtual";
    }
    
    @PostMapping("/crear-fisico")
    public String crearLibroFisico(
            @ModelAttribute Libro libro,
            @RequestParam("imagenFile") MultipartFile imagenFile,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Guardar imagen como byte array
            if (!imagenFile.isEmpty()) {
                libro.setImagen(imagenFile.getBytes());
            }
            
            libro.setTipo("fisico");
            // Para libros físicos, el stock virtual es 0
            libro.setStockVirtual(0);
            
            // Guardar el libro con el estado seleccionado
            libroService.guardar(libro);
            redirectAttributes.addFlashAttribute("mensaje", "Libro físico creado con éxito");
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la imagen: " + e.getMessage());
            return "redirect:/bibliotecario/libros/crear-fisico";
        }
        
        return "redirect:/bibliotecario/libros/listar";
    }
    
    @PostMapping("/crear-virtual")
    public String crearLibroVirtual(
            @ModelAttribute Libro libro,
            @RequestParam("imagenFile") MultipartFile imagenFile,
            @RequestParam("pdfFile") MultipartFile pdfFile,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Guardar imagen como byte array
            if (!imagenFile.isEmpty()) {
                libro.setImagen(imagenFile.getBytes());
            }
            
            // Guardar PDF como byte array
            if (!pdfFile.isEmpty()) {
                libro.setArchivoPdf(pdfFile.getBytes());
            }
            
            libro.setTipo("virtual");
            // Para libros virtuales, el stock físico es 0
            libro.setStockFisico(0);
            
            // Guardar el libro con el estado seleccionado
            libroService.guardar(libro);
            redirectAttributes.addFlashAttribute("mensaje", "Libro virtual creado con éxito");
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar los archivos: " + e.getMessage());
            return "redirect:/bibliotecario/libros/crear-virtual";
        }
        
        return "redirect:/bibliotecario/libros/listar";
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable String id, Model model) {
        libroService.obtenerPorId(id).ifPresent(libro -> {
            model.addAttribute("libro", libro);
            model.addAttribute("categorias", CATEGORIAS);
            model.addAttribute("estados", libroService.obtenerEstadosDisponibilidad());
        });
        return "bibliotecario/libros/editar";
    }
    
    @PostMapping("/editar/{id}")
    public String actualizarLibro(
            @PathVariable String id,
            @ModelAttribute Libro libro,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            @RequestParam(value = "pdfFile", required = false) MultipartFile pdfFile,
            RedirectAttributes redirectAttributes) {
        
        try {
            Libro libroExistente = libroService.obtenerPorId(id).orElse(null);
            if (libroExistente == null) {
                redirectAttributes.addFlashAttribute("error", "Libro no encontrado");
                return "redirect:/bibliotecario/libros/listar";
            }
            
            // Mantener valores existentes si no se proporcionan nuevos
            libro.setId(id);
            libro.setTipo(libroExistente.getTipo());
            
            // Actualizar imagen solo si se proporciona una nueva
            if (imagenFile != null && !imagenFile.isEmpty()) {
                libro.setImagen(imagenFile.getBytes());
            } else {
                libro.setImagen(libroExistente.getImagen());
            }
            
            // Actualizar PDF solo si se proporciona uno nuevo (solo para libros virtuales)
            if ("virtual".equals(libro.getTipo()) && pdfFile != null && !pdfFile.isEmpty()) {
                libro.setArchivoPdf(pdfFile.getBytes());
            } else {
                libro.setArchivoPdf(libroExistente.getArchivoPdf());
            }
            
            // Asegurar que los stocks sean coherentes con el tipo de libro
            if ("fisico".equals(libro.getTipo())) {
                libro.setStockVirtual(0);
            } else if ("virtual".equals(libro.getTipo())) {
                libro.setStockFisico(0);
            }
            
            // Guardar el libro con el estado seleccionado
            libroService.guardar(libro);
            redirectAttributes.addFlashAttribute("mensaje", "Libro actualizado con éxito");
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar los archivos: " + e.getMessage());
        }
        
        return "redirect:/bibliotecario/libros/listar";
    }
    
    @GetMapping("/detalle/{id}")
    public String verDetalleLibro(@PathVariable String id, Model model) {
        libroService.obtenerPorId(id).ifPresent(libro -> model.addAttribute("libro", libro));
        return "bibliotecario/libros/detalle";
    }
    
    @GetMapping("/eliminar/{id}")
    public String mostrarConfirmacionEliminar(@PathVariable String id, Model model) {
        libroService.obtenerPorId(id).ifPresent(libro -> model.addAttribute("libro", libro));
        return "bibliotecario/libros/eliminar";
    }
    
    @PostMapping("/eliminar/{id}")
    public String eliminarLibro(@PathVariable String id, RedirectAttributes redirectAttributes) {
        if (libroService.existePorId(id)) {
            libroService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Libro eliminado con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "Libro no encontrado");
        }
        return "redirect:/bibliotecario/libros/listar";
    }
    
    // Endpoint para servir imágenes directamente desde la base de datos
    @GetMapping("/imagen/{id}")
    public void mostrarImagenLibro(@PathVariable String id, HttpServletResponse response) throws IOException {
        Optional<Libro> libroOpt = libroService.obtenerPorId(id);
        
        if (libroOpt.isPresent() && libroOpt.get().getImagen() != null && libroOpt.get().getImagen().length > 0) {
            Libro libro = libroOpt.get();
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            response.getOutputStream().write(libro.getImagen());
            response.getOutputStream().flush();
        }
    }
    
    // Endpoint para servir PDFs directamente desde la base de datos
    @GetMapping("/pdf/{id}")
    public void mostrarPdfLibro(@PathVariable String id, HttpServletResponse response) throws IOException {
        Optional<Libro> libroOpt = libroService.obtenerPorId(id);
        
        if (libroOpt.isPresent() && libroOpt.get().getArchivoPdf() != null && libroOpt.get().getArchivoPdf().length > 0) {
            Libro libro = libroOpt.get();
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            response.getOutputStream().write(libro.getArchivoPdf());
            response.getOutputStream().flush();
        }
    }
}
