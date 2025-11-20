package com.gestion.hotelera.repository;

import com.gestion.hotelera.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findByDni(String dni);
    Optional<Empleado> findByEmail(String email);
    Optional<Empleado> findByUsuarioUsername(String username);
}