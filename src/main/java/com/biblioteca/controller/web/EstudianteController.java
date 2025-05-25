package com.biblioteca.controller.web;

import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;
import com.biblioteca.service.LibroService;
import com.biblioteca.service.PrestamoService;
import com.biblioteca.service.UsuarioService;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/estudiante")
public class EstudianteController {

    @Autowired
    private LibroService libroService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PrestamoService prestamoService;

    // Mapas para almacenar las preferencias de los usuarios
    private Map<String, List<String>> librosFavoritos = new HashMap<>();
    private Map<String, List<String>> librosPendientes = new HashMap<>();
    private Map<String, List<String>> librosLeidos = new HashMap<>();

    @GetMapping("/inicio")
    public String inicio(Model model, HttpSession session) {
        // Obtener el nombre del usuario de la sesión
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        
        // Obtener libros recientes (últimos 5 añadidos)
        List<Libro> librosRecientes = libroService.obtenerTodos().stream()
                .limit(5)
                .collect(Collectors.toList());
        
        model.addAttribute("librosRecientes", librosRecientes);
        
        // Obtener préstamos activos del estudiante
        List<Prestamo> prestamosActivos = prestamoService.obtenerPorUsuario(nombreUsuario).stream()
                .filter(p -> !p.getEstado().equals("Finalizado") && !p.getEstado().equals("Cancelado"))
                .collect(Collectors.toList());
        
        model.addAttribute("prestamosActivos", prestamosActivos);
        
        return "estudiante/inicio";
    }

    @GetMapping("/catalogo")
    public String catalogo(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false, defaultValue = "ambos") String tipoBusqueda,
            Model model, 
            HttpSession session) {
        
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
        
        // Obtener categorías únicas para el filtro
        Set<String> categorias = libroService.obtenerTodos().stream()
                .map(Libro::getCategoria)
                .collect(Collectors.toSet());
        
        // Obtener estados de disponibilidad
        List<String> estados = libroService.obtenerEstadosDisponibilidad();
        
        model.addAttribute("libros", libros);
        model.addAttribute("categorias", categorias);
        model.addAttribute("estados", estados);
        model.addAttribute("categoriaSeleccionada", categoria);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("tipoBusqueda", tipoBusqueda);
        
        return "estudiante/catalogo";
    }

    @GetMapping("/libro/{id}")
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
            
            // Verificar si el usuario ya tiene un préstamo activo de este libro
            boolean tienePrestamoActivo = prestamoService.obtenerPorUsuario(nombreUsuario).stream()
                    .anyMatch(p -> p.getLibro().equals(libro.getTitulo()) && 
                             !p.getEstado().equals("Finalizado") && 
                             !p.getEstado().equals("Cancelado"));
            
            model.addAttribute("tienePrestamoActivo", tienePrestamoActivo);
            
            return "estudiante/detalle-libro";
        } else {
            return "redirect:/estudiante/catalogo";
        }
    }

    // Métodos para gestionar libros favoritos
    @PostMapping("/libro/{id}/favorito")
    public String marcarFavorito(@PathVariable String id, HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        agregarALista(nombreUsuario, id, librosFavoritos);
        return "redirect:/estudiante/libro/" + id;
    }

    @PostMapping("/libro/{id}/quitar-favorito")
    public String quitarFavorito(@PathVariable String id, HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        quitarDeLista(nombreUsuario, id, librosFavoritos);
        return "redirect:/estudiante/libro/" + id;
    }

    @GetMapping("/libros/favoritos")
    public String librosFavoritos(Model model, HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        List<Libro> libros = obtenerLibrosDeLista(nombreUsuario, librosFavoritos);
        
        model.addAttribute("libros", libros);
        model.addAttribute("tipoLista", "favoritos");
        model.addAttribute("titulo", "Mis Libros Favoritos");
        
        return "estudiante/libros-lista";
    }

    // Métodos para gestionar libros pendientes
    @PostMapping("/libro/{id}/pendiente")
    public String marcarPendiente(@PathVariable String id, HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        agregarALista(nombreUsuario, id, librosPendientes);
        return "redirect:/estudiante/libro/" + id;
    }

    @PostMapping("/libro/{id}/quitar-pendiente")
    public String quitarPendiente(@PathVariable String id, HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        quitarDeLista(nombreUsuario, id, librosPendientes);
        return "redirect:/estudiante/libro/" + id;
    }

    @GetMapping("/libros/pendientes")
    public String librosPendientes(Model model, HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        List<Libro> libros = obtenerLibrosDeLista(nombreUsuario, librosPendientes);
        
        model.addAttribute("libros", libros);
        model.addAttribute("tipoLista", "pendientes");
        model.addAttribute("titulo", "Mis Lecturas Pendientes");
        
        return "estudiante/libros-lista";
    }

    // Métodos para gestionar libros leídos
    @PostMapping("/libro/{id}/leido")
    public String marcarLeido(@PathVariable String id, HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        agregarALista(nombreUsuario, id, librosLeidos);
        return "redirect:/estudiante/libro/" + id;
    }

    @PostMapping("/libro/{id}/quitar-leido")
    public String quitarLeido(@PathVariable String id, HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        quitarDeLista(nombreUsuario, id, librosLeidos);
        return "redirect:/estudiante/libro/" + id;
    }

    @GetMapping("/libros/leidos")
    public String librosLeidos(Model model, HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        List<Libro> libros = obtenerLibrosDeLista(nombreUsuario, librosLeidos);
        
        model.addAttribute("libros", libros);
        model.addAttribute("tipoLista", "leidos");
        model.addAttribute("titulo", "Mis Libros Leídos");
        
        return "estudiante/libros-lista";
    }

    // Métodos auxiliares para gestionar las listas
    @PostMapping("/libros/{tipoLista}/{id}/quitar")
    public String quitarDeListaDesdeVista(
            @PathVariable String tipoLista, 
            @PathVariable String id, 
            HttpSession session) {
        
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        
        switch (tipoLista) {
            case "favoritos":
                quitarDeLista(nombreUsuario, id, librosFavoritos);
                return "redirect:/estudiante/libros/favoritos";
            case "pendientes":
                quitarDeLista(nombreUsuario, id, librosPendientes);
                return "redirect:/estudiante/libros/pendientes";
            case "leidos":
                quitarDeLista(nombreUsuario, id, librosLeidos);
                return "redirect:/estudiante/libros/leidos";
            default:
                return "redirect:/estudiante/catalogo";
        }
    }

    // FUNCIONALIDADES DE PRÉSTAMOS

    @GetMapping("/prestamos")
    public String listarPrestamos(Model model, HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        List<Prestamo> prestamos = prestamoService.obtenerPorUsuario(nombreUsuario);
        
        model.addAttribute("prestamos", prestamos);
        
        return "estudiante/prestamos/listar";
    }

    @GetMapping("/prestamos/{id}")
    public String detallePrestamo(@PathVariable String id, Model model, HttpSession session) {
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        Optional<Prestamo> prestamoOpt = prestamoService.obtenerPorId(id);
        
        // Verificar que el préstamo existe y pertenece al usuario
        if (prestamoOpt.isPresent() && prestamoOpt.get().getUsuario().equals(nombreUsuario)) {
            Prestamo prestamo = prestamoOpt.get();
            model.addAttribute("prestamo", prestamo);
            return "estudiante/prestamos/detalle";
        } else {
            return "redirect:/estudiante/prestamos";
        }
    }

    @GetMapping("/libro/{id}/solicitar")
    public String mostrarFormularioSolicitud(@PathVariable String id, Model model, HttpSession session) {
        Optional<Libro> libroOpt = libroService.obtenerPorId(id);
        
        if (libroOpt.isPresent()) {
            Libro libro = libroOpt.get();
            
            // Verificar que el libro esté disponible
            if (!"Disponible".equals(libro.getEstadoDisponibilidad())) {
                model.addAttribute("error", "El libro no está disponible para préstamo.");
                return "redirect:/estudiante/libro/" + id;
            }
            
            // Obtener el nombre de usuario de la sesión
            String nombreUsuario = (String) session.getAttribute("usuarioNombre");
            
            // Verificar que el usuario no tenga ya un préstamo activo de este libro
            boolean tienePrestamoActivo = prestamoService.obtenerPorUsuario(nombreUsuario).stream()
                    .anyMatch(p -> p.getLibro().equals(libro.getTitulo()) && 
                             !p.getEstado().equals("Finalizado") && 
                             !p.getEstado().equals("Cancelado"));
            
            if (tienePrestamoActivo) {
                model.addAttribute("error", "Ya tienes un préstamo activo para este libro.");
                return "redirect:/estudiante/libro/" + id;
            }
            
            model.addAttribute("libro", libro);
            model.addAttribute("fechaMinima", LocalDate.now().plusDays(1));
            model.addAttribute("fechaMaxima", LocalDate.now().plusDays(30));
            
            return "estudiante/prestamos/solicitar";
        } else {
            return "redirect:/estudiante/catalogo";
        }
    }

    @PostMapping("/libro/{id}/solicitar-prestamo")
    public String solicitarPrestamo(
            @PathVariable String id, 
            @RequestParam String motivo,
            @RequestParam LocalDate fechaDevolucionPropuesta,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Optional<Libro> libroOpt = libroService.obtenerPorId(id);
        
        if (!libroOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "El libro no existe.");
            return "redirect:/estudiante/catalogo";
        }
        
        Libro libro = libroOpt.get();
        
        // Verificar que el libro esté disponible
        if (!"Disponible".equals(libro.getEstadoDisponibilidad())) {
            redirectAttributes.addFlashAttribute("error", "El libro no está disponible para préstamo.");
            return "redirect:/estudiante/libro/" + id;
        }
        
        // Obtener el nombre de usuario de la sesión
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        
        // Verificar que el usuario no tenga ya un préstamo activo de este libro
        boolean tienePrestamoActivo = prestamoService.obtenerPorUsuario(nombreUsuario).stream()
                .anyMatch(p -> p.getLibro().equals(libro.getTitulo()) && 
                         !p.getEstado().equals("Finalizado") && 
                         !p.getEstado().equals("Cancelado"));
        
        if (tienePrestamoActivo) {
            redirectAttributes.addFlashAttribute("error", "Ya tienes un préstamo activo para este libro.");
            return "redirect:/estudiante/libro/" + id;
        }
        
        // Crear el préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setLibro(libro.getTitulo());
        prestamo.setUsuario(nombreUsuario);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucion(fechaDevolucionPropuesta);
        prestamo.setComentarios(motivo);
        prestamo.setEstado("Pendiente");
        
        prestamoService.guardar(prestamo);
        
        redirectAttributes.addFlashAttribute("mensaje", "Solicitud de préstamo enviada correctamente.");
        return "redirect:/estudiante/prestamos";
    }

    @PostMapping("/prestamos/{id}/cancelar")
    public String cancelarSolicitud(
            @PathVariable String id, 
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        String nombreUsuario = (String) session.getAttribute("usuarioNombre");
        Optional<Prestamo> prestamoOpt = prestamoService.obtenerPorId(id);
        
        // Verificar que el préstamo existe y pertenece al usuario
        if (!prestamoOpt.isPresent() || !prestamoOpt.get().getUsuario().equals(nombreUsuario)) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la solicitud de préstamo.");
            return "redirect:/estudiante/prestamos";
        }
        
        Prestamo prestamo = prestamoOpt.get();
        
        // Verificar que el préstamo esté en estado Pendiente
        if (!"Pendiente".equals(prestamo.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Solo se pueden cancelar solicitudes pendientes.");
            return "redirect:/estudiante/prestamos/" + id;
        }
        
        // Cancelar la solicitud
        prestamo.setEstado("Cancelado");
        prestamoService.guardar(prestamo);
        
        redirectAttributes.addFlashAttribute("mensaje", "Solicitud de préstamo cancelada correctamente.");
        return "redirect:/estudiante/prestamos";
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

    // Método para servir PDFs de libros
    @GetMapping("/pdf-libro/{id}")
    public void mostrarPdfLibro(@PathVariable String id, HttpServletResponse response) throws IOException {
        Optional<Libro> libroOpt = libroService.obtenerPorId(id);
        
        if (libroOpt.isPresent() && libroOpt.get().getArchivoPdf() != null && libroOpt.get().getArchivoPdf().length > 0) {
            Libro libro = libroOpt.get();
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            response.setHeader("Content-Disposition", "inline; filename=\"" + libro.getTitulo().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf\"");
            response.setContentLength(libro.getArchivoPdf().length);
            response.getOutputStream().write(libro.getArchivoPdf());
            response.getOutputStream().flush();
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "PDF no encontrado");
        }
    }

    // Métodos de utilidad
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
    
    @GetMapping("/imagen-libro-por-titulo")
    public void mostrarImagenLibroPorTitulo(
            @RequestParam String titulo, 
            HttpServletResponse response) throws IOException {
        
        // Buscar el primer libro con este título (asumiendo títulos únicos)
        Optional<Libro> libroOpt = libroService.obtenerTodos().stream()
            .filter(l -> l.getTitulo().equalsIgnoreCase(titulo))
            .findFirst();
        
        if (libroOpt.isPresent() && libroOpt.get().getImagen() != null) {
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            response.getOutputStream().write(libroOpt.get().getImagen());
            response.getOutputStream().flush();
        } else {
            // Servir imagen por defecto si no se encuentra
            InputStream in = getClass().getResourceAsStream("/static/placeholder.jpg");
            if (in != null) {
                response.setContentType(MediaType.IMAGE_JPEG_VALUE);
                IOUtils.copy(in, response.getOutputStream());
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
}
