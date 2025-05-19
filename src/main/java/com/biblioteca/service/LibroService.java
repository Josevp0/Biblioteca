package com.biblioteca.service;

import com.biblioteca.model.Libro;
import java.util.List;
import java.util.Optional;

public interface LibroService {
    
    List<Libro> obtenerTodos();
    
    Optional<Libro> obtenerPorId(String id);
    
    List<Libro> obtenerPorTipo(String tipo);
    
    List<Libro> obtenerPorCategoria(String categoria);
    
    List<Libro> obtenerPorEstadoDisponibilidad(String estadoDisponibilidad);
    
    List<Libro> buscarPorTitulo(String titulo);
    
    Optional<Libro> obtenerPorTitulo(String titulo);
    
    List<Libro> buscarPorAutor(String autor);
    
    Libro guardar(Libro libro);
    
    void eliminar(String id);
    
    boolean existePorId(String id);
    
    // Método para cambiar el estado de disponibilidad de un libro
    boolean cambiarEstadoDisponibilidad(String id, String nuevoEstado);
    
    // Método para obtener todos los estados de disponibilidad posibles
    List<String> obtenerEstadosDisponibilidad();
}
