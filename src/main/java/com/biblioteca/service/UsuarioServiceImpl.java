package com.biblioteca.service;

import com.biblioteca.model.Usuario;
import com.biblioteca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }
    
    @Override
    public List<Usuario> obtenerPorEstado(String estado) {
        return usuarioRepository.findByEstado(estado);
    }
    
    @Override
    public List<Usuario> obtenerPorRolesYEstado(List<String> roles, String estado) {
        return usuarioRepository.findByRolesAndEstado(roles, estado);
    }
    
    @Override
    public Optional<Usuario> obtenerPorId(String id) {
        return usuarioRepository.findById(id);
    }
    
    @Override
    public Optional<Usuario> obtenerPorNombre(String nombre) {
        return usuarioRepository.findByNombre(nombre);
    }
    
    @Override
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
    
    @Override
    public void eliminar(String id) {
        usuarioRepository.deleteById(id);
    }
    
    @Override
    public boolean existePorId(String id) {
        return usuarioRepository.existsById(id);
    }
    
    @Override
    public Usuario obtenerAdministrador() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream()
                .filter(u -> "Administrador".equals(u.getRol()) && "activo".equals(u.getEstado()))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public Usuario cambiarEstado(String id, String nuevoEstado) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setEstado(nuevoEstado);
            return usuarioRepository.save(usuario);
        }
        return null;
    }
}
