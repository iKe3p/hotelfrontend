package com.gestion.hotelera.restController;

import com.gestion.hotelera.model.Cliente;
import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.repository.ClienteRepository;
import com.gestion.hotelera.repository.UsuarioRepository;
import com.gestion.hotelera.security.JwtService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
public class UsuarioRestController {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final JwtService jwtService;

    public UsuarioRestController(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.jwtService = jwtService;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> obtenerPerfil(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Token de autorización inválido");
            }
            
            String token = authHeader.replace("Bearer ", "");
            if (token.trim().isEmpty()) {
                return ResponseEntity.status(401).body("Token vacío");
            }
            
            String username = jwtService.getUsernameFromToken(token);
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.status(401).body("Token inválido");
            }

            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();
            if (usuario.getCliente() == null) {
                return ResponseEntity.status(403).body("El usuario no tiene perfil de cliente");
            }

            return ResponseEntity.ok(usuario.getCliente());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno del servidor");
        }
    }

    @PostMapping("/registrarCliente")
    public ResponseEntity<?> registrarCliente(@RequestHeader("Authorization") String authHeader,
                                              @RequestBody Cliente nuevoCliente) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtService.getUsernameFromToken(token);
        Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow();

        if (!(usuario.getRol().equals("ROLE_ADMIN") || usuario.getRol().equals("ROLE_RECEPCIONISTA"))) {
            return ResponseEntity.status(403).body("No tienes permiso para registrar clientes.");
        }

        if (clienteRepository.findByDni(nuevoCliente.getDni()).isPresent()) {
            return ResponseEntity.badRequest().body("El cliente con ese DNI ya existe.");
        }

        Cliente guardado = clienteRepository.save(nuevoCliente);
        return ResponseEntity.ok(guardado);
    }

    @GetMapping("/clientes")
    public ResponseEntity<?> listarClientes(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtService.getUsernameFromToken(token);
        Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow();

        if (!(usuario.getRol().equals("ROLE_ADMIN") || usuario.getRol().equals("ROLE_RECEPCIONISTA"))) {
            return ResponseEntity.status(403).body("No tienes permiso para ver la lista de clientes.");
        }

        return ResponseEntity.ok(clienteRepository.findAll());
    }
}