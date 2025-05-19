package com.biblioteca.service;

import com.biblioteca.model.Libro;
import com.biblioteca.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class LibroServiceImpl implements LibroService {
    
    @Autowired
    private LibroRepository libroRepository;
    
    private final List<String> ESTADOS_DISPONIBILIDAD = Arrays.asList(
        "Disponible", "Prestado", "Reservado", "No disponible"
    );
    
    @Override
    public List<Libro> obtenerTodos() {
        return libroRepository.findAll();
    }
    
    @Override
    public Optional<Libro> obtenerPorId(String id) {
        return libroRepository.findById(id);
    }
    
    @Override
    public List<Libro> obtenerPorTipo(String tipo) {
        return libroRepository.findByTipo(tipo);
    }
    
    @Override
    public List<Libro> obtenerPorCategoria(String categoria) {
        return libroRepository.findByCategoria(categoria);
    }
    
    @Override
    public List<Libro> obtenerPorEstadoDisponibilidad(String estadoDisponibilidad) {
        return libroRepository.findByEstadoDisponibilidad(estadoDisponibilidad);
    }
    
    @Override
    public List<Libro> buscarPorTitulo(String titulo) {
        return libroRepository.findByTituloContaining(titulo);
    }
    
    @Override
    public Optional<Libro> obtenerPorTitulo(String titulo) {
        return libroRepository.findByTitulo(titulo);
    }
    
    @Override
    public List<Libro> buscarPorAutor(String autor) {
        return libroRepository.findByAutorContaining(autor);
    }
    
    @Override
    public Libro guardar(Libro libro) {
        return libroRepository.save(libro);
    }
    
    @Override
    public void eliminar(String id) {
        libroRepository.deleteById(id);
    }
    
    @Override
    public boolean existePorId(String id) {
        return libroRepository.existsById(id);
    }
    
    @Override
    public boolean cambiarEstadoDisponibilidad(String id, String nuevoEstado) {
        if (!ESTADOS_DISPONIBILIDAD.contains(nuevoEstado)) {
            return false;
        }
        
        Optional<Libro> libroOpt = libroRepository.findById(id);
        if (libroOpt.isPresent()) {
            Libro libro = libroOpt.get();
            libro.setEstadoDisponibilidad(nuevoEstado);
            libroRepository.save(libro);
            return true;
        }
        return false;
    }
    
    @Override
    public List<String> obtenerEstadosDisponibilidad() {
        return ESTADOS_DISPONIBILIDAD;
    }
}
