package com.biblioteca.repository;

import com.biblioteca.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public List<Usuario> findAll() {
        return mongoTemplate.findAll(Usuario.class);
    }
    
    @Override
    public List<Usuario> findByEstado(String estado) {
        Query query = new Query(Criteria.where("estado").is(estado));
        return mongoTemplate.find(query, Usuario.class);
    }
    
    @Override
    public List<Usuario> findByRolesAndEstado(List<String> roles, String estado) {
        Query query = new Query(Criteria.where("rol").in(roles).and("estado").is(estado));
        return mongoTemplate.find(query, Usuario.class);
    }
    
    @Override
    public Optional<Usuario> findById(String id) {
        Usuario usuario = mongoTemplate.findById(id, Usuario.class);
        return Optional.ofNullable(usuario);
    }
    
    @Override
    public Optional<Usuario> findByNombre(String nombre) {
        Query query = new Query(Criteria.where("nombre").is(nombre));
        Usuario usuario = mongoTemplate.findOne(query, Usuario.class);
        return Optional.ofNullable(usuario);
    }
    
    @Override
    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null || usuario.getId().isEmpty()) {
            usuario.setId(UUID.randomUUID().toString());
        }
        
        // Si el estado es nulo, establecerlo como "activo" por defecto
        if (usuario.getEstado() == null || usuario.getEstado().isEmpty()) {
            usuario.setEstado("activo");
        }
        
        return mongoTemplate.save(usuario);
    }
    
    @Override
    public void deleteById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, Usuario.class);
    }
    
    @Override
    public boolean existsById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        return mongoTemplate.exists(query, Usuario.class);
    }
}
