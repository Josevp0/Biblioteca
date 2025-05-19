package com.biblioteca.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "libros")
public class Libro {
    
    @Id
    private String id;
    private String titulo;
    private String autor;
    private String descripcion;
    private String tipo; // "fisico" o "virtual"
    private byte[] imagen; // Cambiado de String a byte[]
    private String archivoPdf; // Solo para libros virtuales
    private String categoria; // Ficción, No ficción, Misterio, Romántico, Fantasía
    private int stockFisico; // Número de copias para los libros físicos disponibles
    private int stockVirtual; // Número de copias para los libros virtuales disponibles
    private String estadoDisponibilidad; // Disponible, Prestado, Reservado, No disponible
    
    public Libro() {
        // Por defecto, un nuevo libro está disponible
        this.estadoDisponibilidad = "Disponible";
    }
    
    public Libro(String id, String titulo, String autor, String descripcion, String tipo, byte[] imagen, 
                String archivoPdf, String categoria, int stockFisico, int stockVirtual, 
                String estadoDisponibilidad) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.imagen = imagen;
        this.archivoPdf = archivoPdf;
        this.categoria = categoria;
        this.stockFisico = stockFisico;
        this.stockVirtual = stockVirtual;
        this.estadoDisponibilidad = estadoDisponibilidad;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getAutor() {
        return autor;
    }
    
    public void setAutor(String autor) {
        this.autor = autor;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public byte[] getImagen() {
        return imagen;
    }
    
    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }
    
    public String getArchivoPdf() {
        return archivoPdf;
    }
    
    public void setArchivoPdf(String archivoPdf) {
        this.archivoPdf = archivoPdf;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public int getStockFisico() {
        return stockFisico;
    }
    
    public void setStockFisico(int stockFisico) {
        this.stockFisico = stockFisico;
    }
    
    public int getStockVirtual() {
        return stockVirtual;
    }
    
    public void setStockVirtual(int stockVirtual) {
        this.stockVirtual = stockVirtual;
    }
    
    public String getEstadoDisponibilidad() {
        return estadoDisponibilidad;
    }
    
    public void setEstadoDisponibilidad(String estadoDisponibilidad) {
        this.estadoDisponibilidad = estadoDisponibilidad;
    }
    
    // Método para actualizar automáticamente el estado de disponibilidad basado en el stock
    public void actualizarEstadoDisponibilidad() {
        if (this.estadoDisponibilidad == null || 
            (("fisico".equals(this.tipo) && this.stockFisico == 0) || 
             ("virtual".equals(this.tipo) && this.stockVirtual == 0))) {
            
            if ("fisico".equals(this.tipo)) {
                this.estadoDisponibilidad = (this.stockFisico > 0) ? "Disponible" : "No disponible";
            } else if ("virtual".equals(this.tipo)) {
                this.estadoDisponibilidad = (this.stockVirtual > 0) ? "Disponible" : "No disponible";
            }
        }
    }
    
    @Override
    public String toString() {
        return "Libro{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", tipo='" + tipo + '\'' +
                ", categoria='" + categoria + '\'' +
                ", estadoDisponibilidad='" + estadoDisponibilidad + '\'' +
                '}';
    }
}
