package com.biblioteca.controller.web;

import com.biblioteca.model.Usuario;
import com.biblioteca.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioWebController {
    
    @Autowired
    private UsuarioService usuarioService;
    
    @GetMapping("/listar")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.obtenerTodos());
        return "admin/usuarios/listar";
    }
    
    @GetMapping("/crear")
    public String mostrarFormularioCrear(Model model) {
        Usuario usuario = new Usuario();
        usuario.setEstado("activo"); // Por defecto, los nuevos usuarios están activos
        model.addAttribute("usuario", usuario);
        return "admin/usuarios/crear";
    }
    
    @PostMapping("/crear")
    public String crearUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        // Validar que el nombre de usuario no exista
        if (usuarioService.obtenerPorNombre(usuario.getNombre()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya existe");
            return "redirect:/admin/usuarios/crear";
        }
        
        // Si no se especificó un estado, establecer como activo por defecto
        if (usuario.getEstado() == null || usuario.getEstado().isEmpty()) {
            usuario.setEstado("activo");
        }
        
        // Guardar el nuevo usuario
        usuarioService.guardar(usuario);
        redirectAttributes.addFlashAttribute("mensaje", "Usuario creado con éxito");
        return "redirect:/admin/usuarios/listar";
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable String id, Model model) {
        usuarioService.obtenerPorId(id).ifPresent(usuario -> model.addAttribute("usuario", usuario));
        return "admin/usuarios/editar";
    }
    
    @PostMapping("/editar/{id}")
    public String actualizarUsuario(@PathVariable String id, @ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        // Verificar si el usuario existe
        if (!usuarioService.existePorId(id)) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/admin/usuarios/listar";
        }
        
        // Verificar si se está cambiando el nombre y si el nuevo nombre ya existe
        Usuario usuarioExistente = usuarioService.obtenerPorId(id).get();
        if (!usuarioExistente.getNombre().equals(usuario.getNombre()) && 
            usuarioService.obtenerPorNombre(usuario.getNombre()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya existe");
            return "redirect:/admin/usuarios/editar/" + id;
        }
        
        // Actualizar el usuario
        usuario.setId(id);
        usuarioService.guardar(usuario);
        redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado con éxito");
        return "redirect:/admin/usuarios/listar";
    }
    
    @GetMapping("/cambiar-estado/{id}/{estado}")
    public String cambiarEstadoUsuario(@PathVariable String id, @PathVariable String estado, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.cambiarEstado(id, estado);
        if (usuario != null) {
            String mensaje = "activo".equals(estado) ? "Usuario activado con éxito" : "Usuario desactivado con éxito";
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo cambiar el estado del usuario");
        }
        return "redirect:/admin/usuarios/listar";
    }
    
    @GetMapping("/eliminar/{id}")
    public String mostrarConfirmacionEliminar(@PathVariable String id, Model model) {
        usuarioService.obtenerPorId(id).ifPresent(usuario -> model.addAttribute("usuario", usuario));
        return "admin/usuarios/eliminar";
    }
    
    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable String id, RedirectAttributes redirectAttributes) {
        // Verificar que no se elimine el último administrador
        Usuario usuario = usuarioService.obtenerPorId(id).orElse(null);
        if (usuario != null && "Administrador".equals(usuario.getRol())) {
            // Contar cuántos administradores hay
            long countAdmins = usuarioService.obtenerTodos().stream()
                    .filter(u -> "Administrador".equals(u.getRol()) && "activo".equals(u.getEstado()))
                    .count();
            
            if (countAdmins <= 1) {
                redirectAttributes.addFlashAttribute("error", "No se puede eliminar el último administrador");
                return "redirect:/admin/usuarios/listar";
            }
        }
        
        usuarioService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado con éxito");
        return "redirect:/admin/usuarios/listar";
    }
    
    @GetMapping("/detalle/{id}")
    public String verDetalleUsuario(@PathVariable String id, Model model) {
        usuarioService.obtenerPorId(id).ifPresent(usuario -> model.addAttribute("usuario", usuario));
        return "admin/usuarios/detalle";
    }
}
