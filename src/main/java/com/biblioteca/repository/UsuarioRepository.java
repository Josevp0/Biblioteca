package com.biblioteca.repository;

import com.biblioteca.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    
    List<Usuario> findAll();
    
    List<Usuario> findByEstado(String estado);
    
    List<Usuario> findByRolesAndEstado(List<String> roles, String estado);
    
    Optional<Usuario> findById(String id);
    
    Optional<Usuario> findByNombre(String nombre);
    
    Usuario save(Usuario usuario);
    
    void deleteById(String id);
    
    boolean existsById(String id);
}
