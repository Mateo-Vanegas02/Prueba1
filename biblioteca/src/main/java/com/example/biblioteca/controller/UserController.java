package com.example.biblioteca.controller;

import com.example.biblioteca.model.Libro;
import com.example.biblioteca.model.Prestamo;
import com.example.biblioteca.model.Usuario;
import com.example.biblioteca.repository.LibroRepository;
import com.example.biblioteca.repository.PrestamoRepository;
import com.example.biblioteca.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/catalogo")
    public String verCatalogo(Model model) {
        model.addAttribute("libros", libroRepository.findAll());
        return "user/catalogo";
    }

    @GetMapping("/prestamos")
    public String verPrestamos(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("prestamos", prestamoRepository.findByUsuarioId(usuario.getId()));
        return "user/prestamos";
    }

    @PostMapping("/prestamos/solicitar/{id}")
    public String solicitarPrestamo(@PathVariable Long id, @RequestParam int cantidad) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        if (libro.getCantidadDisponible() < cantidad) {
            throw new RuntimeException("No hay suficiente stock disponible para este libro");
        }

        libro.setCantidadDisponible(libro.getCantidadDisponible() - cantidad);
        libroRepository.save(libro);

        for (int i = 0; i < cantidad; i++) {
            Prestamo prestamo = new Prestamo();
            prestamo.setUsuario(usuario);
            prestamo.setLibro(libro);
            prestamo.setFechaPrestamo(LocalDate.now());
            prestamoRepository.save(prestamo);
        }

        return "redirect:/user/catalogo";
    }

    @PostMapping("/prestamos/devolver/{id}")
    public String devolverPrestamo(@PathVariable Long id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Préstamo no encontrado"));

        if (prestamo.getFechaDevolucion() != null) {
            throw new RuntimeException("Este préstamo ya ha sido devuelto");
        }

        prestamo.setFechaDevolucion(LocalDate.now());

        Libro libro = prestamo.getLibro();
        libro.setCantidadDisponible(libro.getCantidadDisponible() + 1);
        libroRepository.save(libro);

        prestamoRepository.save(prestamo);

        return "redirect:/user/prestamos";
    }
}