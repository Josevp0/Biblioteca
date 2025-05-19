package com.biblioteca.controller.web;

import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;
import com.biblioteca.service.LibroService;
import com.biblioteca.service.PrestamoService;
import com.biblioteca.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/bibliotecario/prestamos")
public class BibliotecarioPrestamoController {
    
    @Autowired
    private PrestamoService prestamoService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private LibroService libroService;
    
    @GetMapping("/listar")
    public String listarPrestamos(Model model) {
        model.addAttribute("prestamos", prestamoService.obtenerTodos());
        return "bibliotecario/prestamos/listar";
    }
    
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("prestamo", new Prestamo());
        
        // Obtener solo usuarios con roles Profesor y Estudiante y estado activo
        List<Usuario> usuariosActivos = usuarioService.obtenerPorRolesYEstado(
            Arrays.asList("Profesor", "Estudiante"), "activo");
        
        model.addAttribute("usuarios", usuariosActivos);
        model.addAttribute("libros", libroService.obtenerTodos());
        return "bibliotecario/prestamos/crear";
    }
    
    @PostMapping("/crear")
    public String crearPrestamo(@ModelAttribute Prestamo prestamo, RedirectAttributes redirectAttributes) {
        // Establecer fechas si no se proporcionan
        if (prestamo.getFechaPrestamo() == null) {
            prestamo.setFechaPrestamo(LocalDate.now());
        }
        
        // Establecer estado inicial como Pendiente
        prestamo.setEstado("Pendiente");
        
        // Verificar que el usuario exista, tenga rol adecuado y esté activo
        Optional<Usuario> usuarioOpt = usuarioService.obtenerPorNombre(prestamo.getUsuario());
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            
            // Verificar que el usuario tenga rol Profesor o Estudiante
            if (!("Profesor".equals(usuario.getRol()) || "Estudiante".equals(usuario.getRol()))) {
                redirectAttributes.addFlashAttribute("error", "Solo usuarios con rol Profesor o Estudiante pueden realizar préstamos");
                return "redirect:/bibliotecario/prestamos/crear";
            }
            
            // Verificar que el usuario esté activo
            if (!"activo".equals(usuario.getEstado())) {
                redirectAttributes.addFlashAttribute("error", "Solo usuarios con estado activo pueden realizar préstamos");
                return "redirect:/bibliotecario/prestamos/crear";
            }
            
            // Verificar si el usuario ya tiene un préstamo aprobado
            if (prestamoService.tienePrestamoAprobado(usuario.getNombre())) {
                redirectAttributes.addFlashAttribute("error", "El usuario ya tiene un préstamo aprobado y no puede solicitar otro hasta devolverlo");
                return "redirect:/bibliotecario/prestamos/crear";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "El usuario seleccionado no existe");
            return "redirect:/bibliotecario/prestamos/crear";
        }
        
        // Verificar que el libro exista
        Optional<Libro> libroOpt = libroService.obtenerPorTitulo(prestamo.getLibro());
        if (!libroOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "El libro seleccionado no existe");
            return "redirect:/bibliotecario/prestamos/crear";
        }
        
        // Guardar el préstamo (en estado Pendiente)
        prestamoService.guardar(prestamo);
        redirectAttributes.addFlashAttribute("mensaje", "Préstamo creado con éxito en estado Pendiente");
        return "redirect:/bibliotecario/prestamos/listar";
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable String id, Model model) {
        prestamoService.obtenerPorId(id).ifPresent(prestamo -> model.addAttribute("prestamo", prestamo));
        
        // Obtener solo usuarios con roles Profesor y Estudiante y estado activo
        List<Usuario> usuariosActivos = usuarioService.obtenerPorRolesYEstado(
            Arrays.asList("Profesor", "Estudiante"), "activo");
        
        model.addAttribute("usuarios", usuariosActivos);
        model.addAttribute("libros", libroService.obtenerTodos());
        return "bibliotecario/prestamos/editar";
    }
    
    @PostMapping("/editar/{id}")
    public String actualizarPrestamo(
            @PathVariable String id,
            @ModelAttribute Prestamo prestamo,
            RedirectAttributes redirectAttributes) {
        
        if (prestamoService.existePorId(id)) {
            // Obtener el préstamo original
            Prestamo prestamoOriginal = prestamoService.obtenerPorId(id).get();
            
            // Mantener el estado original (no se puede cambiar directamente desde el formulario de edición)
            prestamo.setEstado(prestamoOriginal.getEstado());
            
            // Verificar que el usuario exista, tenga rol adecuado y esté activo
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorNombre(prestamo.getUsuario());
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                
                // Verificar que el usuario tenga rol Profesor o Estudiante
                if (!("Profesor".equals(usuario.getRol()) || "Estudiante".equals(usuario.getRol()))) {
                    redirectAttributes.addFlashAttribute("error", "Solo usuarios con rol Profesor o Estudiante pueden realizar préstamos");
                    return "redirect:/bibliotecario/prestamos/editar/" + id;
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "El usuario seleccionado no existe");
                return "redirect:/bibliotecario/prestamos/editar/" + id;
            }
            
            prestamo.setId(id);
            prestamoService.guardar(prestamo);
            redirectAttributes.addFlashAttribute("mensaje", "Préstamo actualizado con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "Préstamo no encontrado");
        }
        
        return "redirect:/bibliotecario/prestamos/listar";
    }
    
    @GetMapping("/detalle/{id}")
    public String verDetallePrestamo(@PathVariable String id, Model model) {
        prestamoService.obtenerPorId(id).ifPresent(prestamo -> model.addAttribute("prestamo", prestamo));
        return "bibliotecario/prestamos/detalle";
    }
    
    @GetMapping("/eliminar/{id}")
    public String mostrarConfirmacionEliminar(@PathVariable String id, Model model) {
        prestamoService.obtenerPorId(id).ifPresent(prestamo -> model.addAttribute("prestamo", prestamo));
        return "bibliotecario/prestamos/eliminar";
    }
    
    @PostMapping("/eliminar/{id}")
    public String eliminarPrestamo(@PathVariable String id, RedirectAttributes redirectAttributes) {
        if (prestamoService.existePorId(id)) {
            prestamoService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Préstamo eliminado con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "Préstamo no encontrado");
        }
        return "redirect:/bibliotecario/prestamos/listar";
    }
    
    @PostMapping("/aprobar/{id}")
    public String aprobarPrestamo(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Prestamo prestamo = prestamoService.aprobarPrestamo(id);
        if (prestamo != null && "Aprobado".equals(prestamo.getEstado())) {
            redirectAttributes.addFlashAttribute("mensaje", "Préstamo aprobado con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo aprobar el préstamo. Verifique que esté en estado Pendiente.");
        }
        return "redirect:/bibliotecario/prestamos/listar";
    }
    
    @PostMapping("/rechazar/{id}")
    public String rechazarPrestamo(@PathVariable String id, RedirectAttributes redirectAttributes) {
        boolean eliminado = prestamoService.rechazarPrestamo(id);
        if (eliminado) {
            redirectAttributes.addFlashAttribute("mensaje", "Préstamo rechazado y eliminado con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo rechazar el préstamo. Verifique que esté en estado Pendiente.");
        }
        return "redirect:/bibliotecario/prestamos/listar";
    }
    
    @PostMapping("/devolver/{id}")
    public String devolverPrestamo(@PathVariable String id, RedirectAttributes redirectAttributes) {
        boolean devuelto = prestamoService.marcarComoDevuelto(id);
        if (devuelto) {
            redirectAttributes.addFlashAttribute("mensaje", "Préstamo devuelto y eliminado con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo devolver el préstamo. Verifique que esté en estado Aprobado.");
        }
        return "redirect:/bibliotecario/prestamos/listar";
    }
}
