package com.biblioteca.repository;

import com.biblioteca.model.Libro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LibroRepositoryImpl implements LibroRepository {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public List<Libro> findAll() {
        return mongoTemplate.findAll(Libro.class);
    }
    
    @Override
    public Optional<Libro> findById(String id) {
        Libro libro = mongoTemplate.findById(id, Libro.class);
        return Optional.ofNullable(libro);
    }
    
    @Override
    public List<Libro> findByTipo(String tipo) {
        Query query = new Query(Criteria.where("tipo").is(tipo));
        return mongoTemplate.find(query, Libro.class);
    }
    
    @Override
    public List<Libro> findByCategoria(String categoria) {
        Query query = new Query(Criteria.where("categoria").is(categoria));
        return mongoTemplate.find(query, Libro.class);
    }
    
    @Override
    public List<Libro> findByEstadoDisponibilidad(String estadoDisponibilidad) {
        Query query = new Query(Criteria.where("estadoDisponibilidad").is(estadoDisponibilidad));
        return mongoTemplate.find(query, Libro.class);
    }
    
    @Override
    public List<Libro> findByTituloContaining(String titulo) {
        Query query = new Query(Criteria.where("titulo").regex(titulo, "i"));
        return mongoTemplate.find(query, Libro.class);
    }
    
    @Override
    public Optional<Libro> findByTitulo(String titulo) {
        Query query = new Query(Criteria.where("titulo").is(titulo));
        Libro libro = mongoTemplate.findOne(query, Libro.class);
        return Optional.ofNullable(libro);
    }
    
    @Override
    public List<Libro> findByAutorContaining(String autor) {
        Query query = new Query(Criteria.where("autor").regex(autor, "i"));
        return mongoTemplate.find(query, Libro.class);
    }
    
    @Override
    public Libro save(Libro libro) {
        if (libro.getId() == null || libro.getId().isEmpty()) {
            libro.setId(UUID.randomUUID().toString());
        }
        
        // Actualizar automáticamente el estado de disponibilidad solo si es necesario
        libro.actualizarEstadoDisponibilidad();
        
        return mongoTemplate.save(libro);
    }
    
    @Override
    public void deleteById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, Libro.class);
    }
    
    @Override
    public boolean existsById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        return mongoTemplate.exists(query, Libro.class);
    }
}
