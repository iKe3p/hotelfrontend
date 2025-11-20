package com.gestion.hotelera.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "empleados")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden exceder los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden exceder los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos")
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener exactamente 8 dígitos")
    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un formato de email válido")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Pattern(regexp = "^\\d{9}$", message = "El teléfono debe tener 9 dígitos")
    @Column(nullable = true, length = 20)
    private String telefono;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    @NotNull(message = "Los datos de usuario son obligatorios")
    @Valid
    private Usuario usuario;

    public Empleado(String nombres, String apellidos, String dni, String email, String telefono) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dni = dni;
        this.email = email;
        this.telefono = telefono;
        this.usuario = new Usuario();
    }

    public Empleado(String nombres, String apellidos, String dni, String email, String telefono, Usuario usuario) {
        this.nombres = nombres; this.apellidos = apellidos; this.dni = dni; this.email = email; this.telefono = telefono; this.usuario = usuario;
    }

    public Empleado() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}