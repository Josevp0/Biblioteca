package com.biblioteca.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
public class Usuario {
    
    @Id
    private String id;
    private String nombre;
    private String rol;
    private String contrasena;
    private String estado;  // tendrá los estados de (activo/inactivo)
    
    public Usuario() {
        this.estado = "activo"; // Por defecto, los usuarios se crean como activos
    }
    
    public Usuario(String id, String nombre, String rol, String contrasena) {
        this.id = id;
        this.nombre = nombre;
        this.rol = rol;
        this.contrasena = contrasena;
        this.estado = "activo"; // Por defecto, los usuarios se crean como activos
    }
    
    public Usuario(String id, String nombre, String rol, String contrasena, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.rol = rol;
        this.contrasena = contrasena;
        this.estado = estado;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getRol() {
        return rol;
    }
    
    public void setRol(String rol) {
        this.rol = rol;
    }
    
    public String getContrasena() {
        return contrasena;
    }
    
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", rol='" + rol + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
