package com.biblioteca.service;

import com.biblioteca.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    
    List<Usuario> obtenerTodos();
    
    List<Usuario> obtenerPorEstado(String estado);
    
    List<Usuario> obtenerPorRolesYEstado(List<String> roles, String estado);
    
    Optional<Usuario> obtenerPorId(String id);
    
    Optional<Usuario> obtenerPorNombre(String nombre);
    
    Usuario guardar(Usuario usuario);
    
    void eliminar(String id);
    
    boolean existePorId(String id);
    
    Usuario obtenerAdministrador();
    
    // Método para cambiar el estado de un usuario
    Usuario cambiarEstado(String id, String nuevoEstado);
}
