package com.biblioteca.controller.rest;

import com.biblioteca.model.Prestamo;
import com.biblioteca.service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoRestController {
    
    @Autowired
    private PrestamoService prestamoService;
    
    @GetMapping
    public ResponseEntity<List<Prestamo>> listarPrestamos() {
        List<Prestamo> prestamos = prestamoService.obtenerTodos();
        return new ResponseEntity<>(prestamos, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Prestamo> obtenerPrestamo(@PathVariable String id) {
        return prestamoService.obtenerPorId(id)
                .map(prestamo -> new ResponseEntity<>(prestamo, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @GetMapping("/usuario/{usuario}")
    public ResponseEntity<List<Prestamo>> listarPrestamosPorUsuario(@PathVariable String usuario) {
        List<Prestamo> prestamos = prestamoService.obtenerPorUsuario(usuario);
        return new ResponseEntity<>(prestamos, HttpStatus.OK);
    }
    
    @GetMapping("/libro/{libro}")
    public ResponseEntity<List<Prestamo>> listarPrestamosPorLibro(@PathVariable String libro) {
        List<Prestamo> prestamos = prestamoService.obtenerPorLibro(libro);
        return new ResponseEntity<>(prestamos, HttpStatus.OK);
    }
    
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Prestamo>> listarPrestamosPorEstado(@PathVariable String estado) {
        List<Prestamo> prestamos = prestamoService.obtenerPorEstado(estado);
        return new ResponseEntity<>(prestamos, HttpStatus.OK);
    }
    
    @PostMapping
    public ResponseEntity<Prestamo> crearPrestamo(@RequestBody Prestamo prestamo) {
        Prestamo nuevoPrestamo = prestamoService.guardar(prestamo);
        return new ResponseEntity<>(nuevoPrestamo, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Prestamo> actualizarPrestamo(@PathVariable String id, @RequestBody Prestamo prestamo) {
        return prestamoService.obtenerPorId(id)
                .map(prestamoExistente -> {
                    prestamo.setId(id);
                    Prestamo prestamoActualizado = prestamoService.guardar(prestamo);
                    return new ResponseEntity<>(prestamoActualizado, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPrestamo(@PathVariable String id) {
        if (prestamoService.existePorId(id)) {
            prestamoService.eliminar(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
