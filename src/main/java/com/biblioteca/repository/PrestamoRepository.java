package com.biblioteca.repository;

import com.biblioteca.model.Prestamo;
import java.util.List;
import java.util.Optional;

public interface PrestamoRepository {
    
    List<Prestamo> findAll();
    
    Optional<Prestamo> findById(String id);
    
    List<Prestamo> findByUsuario(String usuario);
    
    List<Prestamo> findByLibro(String libro);
    
    List<Prestamo> findByEstado(String estado);
    
    Prestamo save(Prestamo prestamo);
    
    void deleteById(String id);
    
    boolean existsById(String id);
}
