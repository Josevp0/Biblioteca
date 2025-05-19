package com.biblioteca.service;

import com.biblioteca.model.Prestamo;
import java.util.List;
import java.util.Optional;

public interface PrestamoService {
    
    List<Prestamo> obtenerTodos();
    
    Optional<Prestamo> obtenerPorId(String id);
    
    List<Prestamo> obtenerPorUsuario(String usuario);
    
    List<Prestamo> obtenerPorLibro(String libro);
    
    List<Prestamo> obtenerPorEstado(String estado);
    
    int contarPrestamosPorLibroYEstado(String libro, String estado);
    
    int contarPrestamosPorUsuarioYEstado(String usuario, String estado);
    
    Prestamo guardar(Prestamo prestamo);
    
    void eliminar(String id);
    
    boolean existePorId(String id);
    
    // Nuevos métodos para la gestión de estados
    Prestamo aprobarPrestamo(String id);
    
    boolean rechazarPrestamo(String id);
    
    boolean marcarComoDevuelto(String id);
    
    Prestamo agregarComentario(String id, String comentario);
    
    boolean tienePrestamoAprobado(String usuario);
}
