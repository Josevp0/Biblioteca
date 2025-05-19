package com.biblioteca.controller.web;

import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;
import com.biblioteca.service.LibroService;
import com.biblioteca.service.PrestamoService;
import com.biblioteca.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/profesor")
public class ProfesorController {

@Autowired
private LibroService libroService;

@Autowired
private PrestamoService prestamoService;

@Autowired
private UsuarioService usuarioService;

// Mapas para almacenar las preferencias de los usuarios (simulación)
private Map<String, List<String>> librosFavoritos = new HashMap<>();
private Map<String, List<String>> librosPendientes = new HashMap<>();
private Map<String, List<String>> librosLeidos = new HashMap<>();

@GetMapping("/inicio")
public String inicio(Model model, HttpSession session) {
    // Obtener todos los libros para mostrar en la página de inicio
    List<Libro> librosDestacados = libroService.obtenerTodos();
    model.addAttribute("libros", librosDestacados);
    
    return "profesor/inicio";
}

@GetMapping("/catalogo")
public String catalogo(
        @RequestParam(required = false) String categoria,
        @RequestParam(required = false) String estado,
        @RequestParam(required = false) String busqueda,
        @RequestParam(required = false, defaultValue = "titulo") String tipoBusqueda,
        Model model) {
    
    List<Libro> libros;
    
    // Obtener todos los libros primero
    libros = libroService.obtenerTodos();
    
    // Aplicar filtro de búsqueda si se proporciona
    if (busqueda != null && !busqueda.trim().isEmpty()) {
        String busquedaLower = busqueda.toLowerCase();
        
        if ("titulo".equals(tipoBusqueda)) {
            // Buscar solo por título
            libros = libros.stream()
                .filter(libro -> libro.getTitulo().toLowerCase().contains(busquedaLower))
                .collect(Collectors.toList());
        } else if ("autor".equals(tipoBusqueda)) {
            // Buscar solo por autor
            libros = libros.stream()
                .filter(libro -> libro.getAutor().toLowerCase().contains(busquedaLower))
                .collect(Collectors.toList());
        } else {
            // Buscar por ambos (título o autor)
            libros = libros.stream()
                .filter(libro -> 
                    libro.getTitulo().toLowerCase().contains(busquedaLower) || 
                    libro.getAutor().toLowerCase().contains(busquedaLower))
                .collect(Collectors.toList());
        }
    }
    
    // Aplicar filtro de categoría si se proporciona
    if (categoria != null && !categoria.isEmpty()) {
        libros = libros.stream()
            .filter(libro -> categoria.equals(libro.getCategoria()))
            .collect(Collectors.toList());
    }
    
    // Aplicar filtro de estado si se proporciona
    if (estado != null && !estado.isEmpty()) {
        libros = libros.stream()
            .filter(libro -> estado.equals(libro.getEstadoDisponibilidad()))
            .collect(Collectors.toList());
    }
    
    model.addAttribute("libros", libros);
    return "profesor/catalogo";
}

@GetMapping("/detalle-libro/{id}")
public String detalleLibro(@PathVariable String id, Model model, HttpSession session) {
    Optional<Libro> libroOpt = libroService.obtenerPorId(id);
    
    if (libroOpt.isPresent()) {
        Libro libro = libroOpt.get();
        model.addAttribute("libro", libro);
        
        // Obtener el nombre de usuario de la sesión
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        
        // Verificar si el libro está en alguna de las listas del usuario
        boolean esFavorito = estaEnLista(nombreUsuario, id, librosFavoritos);
        boolean esPendiente = estaEnLista(nombreUsuario, id, librosPendientes);
        boolean esLeido = estaEnLista(nombreUsuario, id, librosLeidos);
        
        model.addAttribute("esFavorito", esFavorito);
        model.addAttribute("esPendiente", esPendiente);
        model.addAttribute("esLeido", esLeido);
        
        return "profesor/detalle-libro";
    } else {
        return "redirect:/profesor/catalogo";
    }
}

@GetMapping("/prestamos")
public String mostrarFormularioPrestamo(Model model, HttpSession session) {
    // Crear un nuevo préstamo
    Prestamo prestamo = new Prestamo();
    
    // Establecer el usuario actual como solicitante
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    prestamo.setUsuario(nombreUsuario);
    
    // Obtener libros disponibles
    List<Libro> librosDisponibles = libroService.obtenerPorEstadoDisponibilidad("Disponible");
    
    model.addAttribute("prestamo", prestamo);
    model.addAttribute("libros", librosDisponibles);
    
    return "profesor/prestamos/crear";
}

@PostMapping("/prestamos")
public String crearPrestamo(@ModelAttribute Prestamo prestamo, 
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
    
    // Establecer fechas si no se proporcionan
    if (prestamo.getFechaPrestamo() == null) {
        prestamo.setFechaPrestamo(LocalDate.now());
    }
    
    // Establecer estado inicial como Pendiente
    prestamo.setEstado("Pendiente");
    
    // Verificar que el usuario exista
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    Optional<Usuario> usuarioOpt = usuarioService.obtenerPorNombre(nombreUsuario);
    
    if (usuarioOpt.isPresent()) {
        Usuario usuario = usuarioOpt.get();
        
        // Verificar que el usuario esté activo
        if (!"activo".equals(usuario.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Su cuenta no está activa. No puede solicitar préstamos.");
            return "redirect:/profesor/prestamos";
        }
        
        // Verificar si el usuario ya tiene un préstamo aprobado
        if (prestamoService.tienePrestamoAprobado(usuario.getNombre())) {
            redirectAttributes.addFlashAttribute("error", "Ya tiene un préstamo aprobado. Debe devolverlo antes de solicitar otro.");
            return "redirect:/profesor/prestamos";
        }
    } else {
        redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
        return "redirect:/profesor/prestamos";
    }
    
    // Verificar que el libro exista
    Optional<Libro> libroOpt = libroService.obtenerPorTitulo(prestamo.getLibro());
    if (!libroOpt.isPresent()) {
        redirectAttributes.addFlashAttribute("error", "El libro seleccionado no existe");
        return "redirect:/profesor/prestamos";
    }
    
    // Guardar el préstamo (en estado Pendiente)
    prestamoService.guardar(prestamo);
    redirectAttributes.addFlashAttribute("mensaje", "Solicitud de préstamo enviada con éxito. Estado: Pendiente de aprobación");
    return "redirect:/profesor/historial-prestamos";
}

@GetMapping("/historial-prestamos")
public String historialPrestamos(Model model, HttpSession session) {
    // Obtener el nombre del usuario de la sesión
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    
    // Obtener los préstamos del usuario
    List<Prestamo> prestamos = prestamoService.obtenerPorUsuario(nombreUsuario);
    
    model.addAttribute("prestamos", prestamos);
    return "profesor/prestamos/historial";
}

@GetMapping("/prestamos/detalle/{id}")
public String detallePrestamo(@PathVariable String id, Model model, HttpSession session) {
    // Obtener el nombre del usuario de la sesión
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    
    // Obtener el préstamo
    Optional<Prestamo> prestamoOpt = prestamoService.obtenerPorId(id);
    
    if (prestamoOpt.isPresent()) {
        Prestamo prestamo = prestamoOpt.get();
        
        // Verificar que el préstamo pertenezca al usuario
        if (!prestamo.getUsuario().equals(nombreUsuario)) {
            return "redirect:/profesor/historial-prestamos";
        }
        
        model.addAttribute("prestamo", prestamo);
        return "profesor/prestamos/detalle";
    } else {
        return "redirect:/profesor/historial-prestamos";
    }
}

// Método para servir imágenes de libros
@GetMapping("/imagen-libro/{id}")
public void mostrarImagenLibro(@PathVariable String id, HttpServletResponse response) throws IOException {
    Optional<Libro> libroOpt = libroService.obtenerPorId(id);
    
    if (libroOpt.isPresent() && libroOpt.get().getImagen() != null && libroOpt.get().getImagen().length > 0) {
        Libro libro = libroOpt.get();
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        response.getOutputStream().write(libro.getImagen());
        response.getOutputStream().flush();
    }
}

// Nuevos métodos para gestionar favoritos, pendientes y leídos

@PostMapping("/libro/{id}/favorito")
public String marcarComoFavorito(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    agregarALista(nombreUsuario, id, librosFavoritos);
    redirectAttributes.addFlashAttribute("mensaje", "Libro añadido a favoritos");
    return "redirect:/profesor/detalle-libro/" + id;
}

@PostMapping("/libro/{id}/pendiente")
public String marcarComoPendiente(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    agregarALista(nombreUsuario, id, librosPendientes);
    redirectAttributes.addFlashAttribute("mensaje", "Libro añadido a pendientes");
    return "redirect:/profesor/detalle-libro/" + id;
}

@PostMapping("/libro/{id}/leido")
public String marcarComoLeido(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    agregarALista(nombreUsuario, id, librosLeidos);
    redirectAttributes.addFlashAttribute("mensaje", "Libro añadido a leídos");
    return "redirect:/profesor/detalle-libro/" + id;
}

@PostMapping("/libro/{id}/quitar-favorito")
public String quitarDeFavoritos(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    quitarDeLista(nombreUsuario, id, librosFavoritos);
    redirectAttributes.addFlashAttribute("mensaje", "Libro eliminado de favoritos");
    return "redirect:/profesor/detalle-libro/" + id;
}

@PostMapping("/libro/{id}/quitar-pendiente")
public String quitarDePendientes(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    quitarDeLista(nombreUsuario, id, librosPendientes);
    redirectAttributes.addFlashAttribute("mensaje", "Libro eliminado de pendientes");
    return "redirect:/profesor/detalle-libro/" + id;
}

@PostMapping("/libro/{id}/quitar-leido")
public String quitarDeLeidos(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    quitarDeLista(nombreUsuario, id, librosLeidos);
    redirectAttributes.addFlashAttribute("mensaje", "Libro eliminado de leídos");
    return "redirect:/profesor/detalle-libro/" + id;
}

// Vistas para mostrar libros favoritos, pendientes y leídos

@GetMapping("/libros-favoritos")
public String librosFavoritos(Model model, HttpSession session) {
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    List<Libro> libros = obtenerLibrosDeLista(nombreUsuario, librosFavoritos);
    model.addAttribute("libros", libros);
    model.addAttribute("titulo", "Mis Libros Favoritos");
    model.addAttribute("tipoLista", "favoritos");
    return "profesor/libros-lista";
}

@GetMapping("/libros-pendientes")
public String librosPendientes(Model model, HttpSession session) {
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    List<Libro> libros = obtenerLibrosDeLista(nombreUsuario, librosPendientes);
    model.addAttribute("libros", libros);
    model.addAttribute("titulo", "Mis Libros Pendientes");
    model.addAttribute("tipoLista", "pendientes");
    return "profesor/libros-lista";
}

@GetMapping("/libros-leidos")
public String librosLeidos(Model model, HttpSession session) {
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    List<Libro> libros = obtenerLibrosDeLista(nombreUsuario, librosLeidos);
    model.addAttribute("libros", libros);
    model.addAttribute("titulo", "Mis Libros Leídos");
    model.addAttribute("tipoLista", "leidos");
    return "profesor/libros-lista";
}

// Métodos para quitar libros de las listas desde las vistas de listas

@PostMapping("/libros-favoritos/{id}/quitar")
public String quitarDeFavoritosLista(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    quitarDeLista(nombreUsuario, id, librosFavoritos);
    redirectAttributes.addFlashAttribute("mensaje", "Libro eliminado de favoritos");
    return "redirect:/profesor/libros-favoritos";
}

@PostMapping("/libros-pendientes/{id}/quitar")
public String quitarDePendientesLista(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    quitarDeLista(nombreUsuario, id, librosPendientes);
    redirectAttributes.addFlashAttribute("mensaje", "Libro eliminado de pendientes");
    return "redirect:/profesor/libros-pendientes";
}

@PostMapping("/libros-leidos/{id}/quitar")
public String quitarDeLeidosLista(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
    String nombreUsuario = (String) session.getAttribute("usuarioNombre");
    quitarDeLista(nombreUsuario, id, librosLeidos);
    redirectAttributes.addFlashAttribute("mensaje", "Libro eliminado de leídos");
    return "redirect:/profesor/libros-leidos";
}

// Métodos auxiliares para manejar las listas

private boolean estaEnLista(String usuario, String libroId, Map<String, List<String>> mapa) {
    if (mapa.containsKey(usuario)) {
        return mapa.get(usuario).contains(libroId);
    }
    return false;
}

private void agregarALista(String usuario, String libroId, Map<String, List<String>> mapa) {
    if (!mapa.containsKey(usuario)) {
        mapa.put(usuario, new ArrayList<>());
    }
    if (!mapa.get(usuario).contains(libroId)) {
        mapa.get(usuario).add(libroId);
    }
}

private void quitarDeLista(String usuario, String libroId, Map<String, List<String>> mapa) {
    if (mapa.containsKey(usuario)) {
        mapa.get(usuario).remove(libroId);
    }
}

private List<Libro> obtenerLibrosDeLista(String usuario, Map<String, List<String>> mapa) {
    List<Libro> libros = new ArrayList<>();
    if (mapa.containsKey(usuario)) {
        for (String libroId : mapa.get(usuario)) {
            Optional<Libro> libroOpt = libroService.obtenerPorId(libroId);
            libroOpt.ifPresent(libros::add);
        }
    }
    return libros;
}
}
