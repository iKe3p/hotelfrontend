package com.gestion.hotelera.controller;

import com.gestion.hotelera.dto.AuthResponse;
import com.gestion.hotelera.dto.LoginRequest;
import com.gestion.hotelera.dto.RegisterRequest;
import com.gestion.hotelera.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            if (request == null || !request.isValid()) {
                return ResponseEntity.badRequest().build();
            }
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body("{\"error\":\"Datos de registro requeridos\"}");
            }
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\":\"Error interno del servidor\"}");
        }
    }
}