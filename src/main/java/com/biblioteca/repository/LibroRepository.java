package com.biblioteca.repository;

import com.biblioteca.model.Libro;
import java.util.List;
import java.util.Optional;

public interface LibroRepository {
    
    List<Libro> findAll();
    
    Optional<Libro> findById(String id);
    
    List<Libro> findByTipo(String tipo);
    
    List<Libro> findByCategoria(String categoria);
    
    List<Libro> findByEstadoDisponibilidad(String estadoDisponibilidad);
    
    List<Libro> findByTituloContaining(String titulo);
    
    Optional<Libro> findByTitulo(String titulo);
    
    List<Libro> findByAutorContaining(String autor);
    
    Libro save(Libro libro);
    
    void deleteById(String id);
    
    boolean existsById(String id);
}
