package com.example.biblioteca.controller;

import com.example.biblioteca.model.Libro;
import com.example.biblioteca.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private LibroRepository libroRepository;

    @GetMapping("/libros")
    public String gestionLibros(Model model) {
        model.addAttribute("libros", libroRepository.findAll());
        return "admin/libros";
    }

    @GetMapping("/libros/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("libro", new Libro());
        return "admin/nuevo-libro";
    }

    @PostMapping("/libros/guardar")
    public String guardarLibro(@ModelAttribute Libro libro) {
        libroRepository.save(libro);
        return "redirect:/admin/libros";
    }

    @GetMapping("/libros/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Libro no encontrado con ID: " + id));
        model.addAttribute("libro", libro);
        return "admin/editar-libro";
    }

    @GetMapping("/libros/eliminar/{id}")
    public String eliminarLibro(@PathVariable Long id) {
        libroRepository.deleteById(id);
        return "redirect:/admin/libros";
    }

    @PostMapping("/libros/rellenar/{id}")
    public String rellenarStock(@PathVariable Long id, @RequestParam int cantidad) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        libro.setCantidadDisponible(libro.getCantidadDisponible() + cantidad);
        libroRepository.save(libro);

        return "redirect:/admin/libros";
    }
}