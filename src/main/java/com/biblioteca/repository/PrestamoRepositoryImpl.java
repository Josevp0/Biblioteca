package com.biblioteca.repository;

import com.biblioteca.model.Prestamo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PrestamoRepositoryImpl implements PrestamoRepository {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public List<Prestamo> findAll() {
        return mongoTemplate.findAll(Prestamo.class);
    }
    
    @Override
    public Optional<Prestamo> findById(String id) {
        Prestamo prestamo = mongoTemplate.findById(id, Prestamo.class);
        return Optional.ofNullable(prestamo);
    }
    
    @Override
    public List<Prestamo> findByUsuario(String usuario) {
        Query query = new Query(Criteria.where("usuario").is(usuario));
        return mongoTemplate.find(query, Prestamo.class);
    }
    
    @Override
    public List<Prestamo> findByLibro(String libro) {
        Query query = new Query(Criteria.where("libro").is(libro));
        return mongoTemplate.find(query, Prestamo.class);
    }
    
    @Override
    public List<Prestamo> findByEstado(String estado) {
        Query query = new Query(Criteria.where("estado").is(estado));
        return mongoTemplate.find(query, Prestamo.class);
    }
    
    @Override
    public Prestamo save(Prestamo prestamo) {
        if (prestamo.getId() == null || prestamo.getId().isEmpty()) {
            prestamo.setId(UUID.randomUUID().toString());
        }
        return mongoTemplate.save(prestamo);
    }
    
    @Override
    public void deleteById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, Prestamo.class);
    }
    
    @Override
    public boolean existsById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        return mongoTemplate.exists(query, Prestamo.class);
    }
}
