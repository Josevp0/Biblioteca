package com.biblioteca.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Document(collection = "prestamos")
public class Prestamo {
    
    @Id
    private String id;
    private String usuario; // Referencia textual al usuario
    private String libro; // Referencia textual al libro
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private String estado; // "Pendiente", "Aprobado", "Rechazado", "Devuelto"
    private String comentarios; // Comentarios del auxiliar
    
    public Prestamo() {
    }
    
    public Prestamo(String id, String usuario, String libro, LocalDate fechaPrestamo, LocalDate fechaDevolucion, String estado, String comentarios) {
        this.id = id;
        this.usuario = usuario;
        this.libro = libro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = estado;
        this.comentarios = comentarios;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsuario() {
        return usuario;
    }
    
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
    
    public String getLibro() {
        return libro;
    }
    
    public void setLibro(String libro) {
        this.libro = libro;
    }
    
    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }
    
    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }
    
    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }
    
    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getComentarios() {
        return comentarios;
    }
    
    public void setComentarios(String comentarios) {
        this.comentarios = comentarios;
    }
    
    @Override
    public String toString() {
        return "Prestamo{" +
                "id='" + id + '\'' +
                ", usuario='" + usuario + '\'' +
                ", libro='" + libro + '\'' +
                ", fechaPrestamo=" + fechaPrestamo +
                ", fechaDevolucion=" + fechaDevolucion +
                ", estado='" + estado + '\'' +
                ", comentarios='" + comentarios + '\'' +
                '}';
    }
}
