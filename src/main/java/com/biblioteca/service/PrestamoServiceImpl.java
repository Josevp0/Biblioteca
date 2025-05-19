package com.biblioteca.service;

import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PrestamoServiceImpl implements PrestamoService {
    
    @Autowired
    private PrestamoRepository prestamoRepository;
    
    @Autowired
    private LibroService libroService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Override
    public List<Prestamo> obtenerTodos() {
        return prestamoRepository.findAll();
    }
    
    @Override
    public Optional<Prestamo> obtenerPorId(String id) {
        return prestamoRepository.findById(id);
    }
    
    @Override
    public List<Prestamo> obtenerPorUsuario(String usuario) {
        return prestamoRepository.findByUsuario(usuario);
    }
    
    @Override
    public List<Prestamo> obtenerPorLibro(String libro) {
        return prestamoRepository.findByLibro(libro);
    }
    
    @Override
    public List<Prestamo> obtenerPorEstado(String estado) {
        return prestamoRepository.findByEstado(estado);
    }
    
    @Override
    public int contarPrestamosPorLibroYEstado(String libro, String estado) {
        List<Prestamo> prestamos = prestamoRepository.findByLibro(libro);
        return (int) prestamos.stream()
                .filter(p -> estado.equals(p.getEstado()))
                .count();
    }
    
    @Override
    public int contarPrestamosPorUsuarioYEstado(String usuario, String estado) {
        List<Prestamo> prestamos = prestamoRepository.findByUsuario(usuario);
        return (int) prestamos.stream()
                .filter(p -> estado.equals(p.getEstado()))
                .count();
    }
    
    @Override
    public Prestamo guardar(Prestamo prestamo) {
        return prestamoRepository.save(prestamo);
    }
    
    @Override
    public void eliminar(String id) {
        prestamoRepository.deleteById(id);
    }
    
    @Override
    public boolean existePorId(String id) {
        return prestamoRepository.existsById(id);
    }
    
    @Override
    public Prestamo aprobarPrestamo(String id) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(id);
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            
            // Solo se puede aprobar si está en estado Pendiente
            if (!"Pendiente".equals(prestamo.getEstado())) {
                return prestamo;
            }
            
            // Cambiar estado del préstamo
            prestamo.setEstado("Aprobado");
            
            // Actualizar el libro (reducir stock y cambiar estado)
            Optional<Libro> libroOpt = libroService.obtenerPorTitulo(prestamo.getLibro());
            if (libroOpt.isPresent()) {
                Libro libro = libroOpt.get();
                
                if ("fisico".equals(libro.getTipo()) && libro.getStockFisico() > 0) {
                    libro.setStockFisico(libro.getStockFisico() - 1);
                } else if ("virtual".equals(libro.getTipo()) && libro.getStockVirtual() > 0) {
                    libro.setStockVirtual(libro.getStockVirtual() - 1);
                }
                
                libro.setEstadoDisponibilidad("Prestado");
                libroService.guardar(libro);
            }
            
            // Actualizar el usuario (cambiar a inactivo)
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorNombre(prestamo.getUsuario());
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                usuario.setEstado("inactivo");
                usuarioService.guardar(usuario);
            }
            
            return prestamoRepository.save(prestamo);
        }
        return null;
    }
    
    @Override
    public boolean rechazarPrestamo(String id) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(id);
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            
            // Solo se puede rechazar si está en estado Pendiente
            if (!"Pendiente".equals(prestamo.getEstado())) {
                return false;
            }
            
            // En lugar de cambiar el estado, eliminamos directamente el préstamo
            prestamoRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean marcarComoDevuelto(String id) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(id);
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            
            // Solo se puede marcar como devuelto si está en estado Aprobado
            if (!"Aprobado".equals(prestamo.getEstado())) {
                return false;
            }
            
            // Actualizar el libro (aumentar stock y cambiar estado)
            Optional<Libro> libroOpt = libroService.obtenerPorTitulo(prestamo.getLibro());
            if (libroOpt.isPresent()) {
                Libro libro = libroOpt.get();
                
                if ("fisico".equals(libro.getTipo())) {
                    libro.setStockFisico(libro.getStockFisico() + 1);
                } else if ("virtual".equals(libro.getTipo())) {
                    libro.setStockVirtual(libro.getStockVirtual() + 1);
                }
                
                // Verificar si hay más préstamos activos para este libro
                if (contarPrestamosPorLibroYEstado(libro.getTitulo(), "Aprobado") <= 1) {
                    libro.setEstadoDisponibilidad("Disponible");
                }
                
                libroService.guardar(libro);
            }
            
            // Actualizar el usuario (cambiar a activo)
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorNombre(prestamo.getUsuario());
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                usuario.setEstado("activo");
                usuarioService.guardar(usuario);
            }
            
            // En lugar de guardar el préstamo con estado "Devuelto", lo eliminamos
            prestamoRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    @Override
    public Prestamo agregarComentario(String id, String comentario) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(id);
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            
            // Solo se puede comentar si está en estado Pendiente
            if (!"Pendiente".equals(prestamo.getEstado())) {
                return prestamo;
            }
            
            prestamo.setComentarios(comentario);
            return prestamoRepository.save(prestamo);
        }
        return null;
    }
    
    @Override
    public boolean tienePrestamoAprobado(String usuario) {
        return contarPrestamosPorUsuarioYEstado(usuario, "Aprobado") > 0;
    }
}
