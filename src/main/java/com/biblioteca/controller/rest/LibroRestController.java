package com.biblioteca.controller.rest;

import com.biblioteca.model.Libro;
import com.biblioteca.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/libros")
public class LibroRestController {
    
    @Autowired
    private LibroService libroService;
    
    @GetMapping
    public ResponseEntity<List<Libro>> listarLibros() {
        List<Libro> libros = libroService.obtenerTodos();
        return new ResponseEntity<>(libros, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerLibro(@PathVariable String id) {
        return libroService.obtenerPorId(id)
                .map(libro -> new ResponseEntity<>(libro, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Libro>> listarLibrosPorTipo(@PathVariable String tipo) {
        List<Libro> libros = libroService.obtenerPorTipo(tipo);
        return new ResponseEntity<>(libros, HttpStatus.OK);
    }
    
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Libro>> listarLibrosPorCategoria(@PathVariable String categoria) {
        List<Libro> libros = libroService.obtenerPorCategoria(categoria);
        return new ResponseEntity<>(libros, HttpStatus.OK);
    }
    
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Libro>> listarLibrosPorEstado(@PathVariable String estado) {
        List<Libro> libros = libroService.obtenerPorEstadoDisponibilidad(estado);
        return new ResponseEntity<>(libros, HttpStatus.OK);
    }
    
    @GetMapping("/buscar/titulo/{titulo}")
    public ResponseEntity<List<Libro>> buscarLibrosPorTitulo(@PathVariable String titulo) {
        List<Libro> libros = libroService.buscarPorTitulo(titulo);
        return new ResponseEntity<>(libros, HttpStatus.OK);
    }
    
    @GetMapping("/buscar/autor/{autor}")
    public ResponseEntity<List<Libro>> buscarLibrosPorAutor(@PathVariable String autor) {
        List<Libro> libros = libroService.buscarPorAutor(autor);
        return new ResponseEntity<>(libros, HttpStatus.OK);
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Libro> crearLibro(
            @RequestPart("libro") Libro libro,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            @RequestPart(value = "pdf", required = false) MultipartFile pdf) {
        
        try {
            // Procesar imagen si se proporciona
            if (imagen != null && !imagen.isEmpty()) {
                libro.setImagen(imagen.getBytes());
            }
            
            // Procesar PDF si se proporciona
            if (pdf != null && !pdf.isEmpty()) {
                libro.setArchivoPdf(pdf.getBytes());
            }
            
            Libro nuevoLibro = libroService.guardar(libro);
            return new ResponseEntity<>(nuevoLibro, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Libro> actualizarLibro(
            @PathVariable String id,
            @RequestPart("libro") Libro libro,
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,
            @RequestPart(value = "pdf", required = false) MultipartFile pdf) {
        
        return libroService.obtenerPorId(id)
                .map(libroExistente -> {
                    try {
                        libro.setId(id);
                        
                        // Actualizar imagen solo si se proporciona una nueva
                        if (imagen != null && !imagen.isEmpty()) {
                            libro.setImagen(imagen.getBytes());
                        } else {
                            libro.setImagen(libroExistente.getImagen());
                        }
                        
                        // Actualizar PDF solo si se proporciona uno nuevo
                        if (pdf != null && !pdf.isEmpty()) {
                            libro.setArchivoPdf(pdf.getBytes());
                        } else {
                            libro.setArchivoPdf(libroExistente.getArchivoPdf());
                        }
                        
                        Libro libroActualizado = libroService.guardar(libro);
                        return new ResponseEntity<>(libroActualizado, HttpStatus.OK);
                    } catch (IOException e) {
                        return new ResponseEntity<Libro>(HttpStatus.BAD_REQUEST);
                    }
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstadoDisponibilidad(
            @PathVariable String id, 
            @RequestBody Map<String, String> payload) {
        
        String nuevoEstado = payload.get("estado");
        if (nuevoEstado == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        boolean actualizado = libroService.cambiarEstadoDisponibilidad(id, nuevoEstado);
        if (actualizado) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLibro(@PathVariable String id) {
        if (libroService.existePorId(id)) {
            libroService.eliminar(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/estados-disponibilidad")
    public ResponseEntity<List<String>> obtenerEstadosDisponibilidad() {
        return new ResponseEntity<>(libroService.obtenerEstadosDisponibilidad(), HttpStatus.OK);
    }
    
    // Endpoint para obtener la imagen de un libro
    @GetMapping("/{id}/imagen")
    public ResponseEntity<byte[]> obtenerImagenLibro(@PathVariable String id) {
        return libroService.obtenerPorId(id)
                .filter(libro -> libro.getImagen() != null && libro.getImagen().length > 0)
                .map(libro -> ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(libro.getImagen()))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Endpoint para obtener el PDF de un libro
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> obtenerPdfLibro(@PathVariable String id) {
        return libroService.obtenerPorId(id)
                .filter(libro -> libro.getArchivoPdf() != null && libro.getArchivoPdf().length > 0)
                .map(libro -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(libro.getArchivoPdf()))
                .orElse(ResponseEntity.notFound().build());
    }
}
