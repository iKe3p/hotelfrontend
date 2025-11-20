package com.gestion.hotelera.config;

import com.gestion.hotelera.model.Usuario;
import com.gestion.hotelera.model.Servicio;
import com.gestion.hotelera.repository.UsuarioRepository;
import com.gestion.hotelera.repository.ServicioRepository;
import com.gestion.hotelera.service.HabitacionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ServicioRepository servicioRepository;
    private final HabitacionService habitacionService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, ServicioRepository servicioRepository, HabitacionService habitacionService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.servicioRepository = servicioRepository;
        this.habitacionService = habitacionService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Crear usuario admin si no existe
        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRol("ROLE_ADMIN");
            usuarioRepository.save(admin);
            System.out.println("Usuario admin creado: admin/admin123");
        }

        // Crear usuario recepcionista de prueba si no existe
        if (usuarioRepository.findByUsername("recep").isEmpty()) {
            Usuario recep = new Usuario();
            recep.setUsername("recep");
            recep.setPassword(passwordEncoder.encode("recep123"));
            recep.setRol("ROLE_RECEPCIONISTA");
            usuarioRepository.save(recep);
            System.out.println("Usuario recepcionista de prueba creado: recep/recep123");
        }

        // Crear usuario cliente si no existe
        if (usuarioRepository.findByUsername("cliente").isEmpty()) {
            Usuario cliente = new Usuario();
            cliente.setUsername("cliente");
            cliente.setPassword(passwordEncoder.encode("cliente123"));
            cliente.setRol("ROLE_CLIENTE");
            usuarioRepository.save(cliente);
            System.out.println("Usuario cliente creado: cliente/cliente123");
        }

        // Crear servicios si no existen
        if (servicioRepository.count() == 0) {
            crearServicios();
        }
        
        // Crear habitaciones si no existen
        habitacionService.inicializarHabitacionesSiNoExisten();
    }

    private void crearServicios() {
        Servicio spa = new Servicio("Spa & Wellness", "Relájate en nuestro exclusivo spa de clase mundial con tratamientos personalizados, masajes terapéuticos y terapias de relajación.", 120.0, true);
        spa.setOpciones(Arrays.asList("Masaje Relajante (60 min)", "Masaje Terapéutico (90 min)", "Facial Rejuvenecedor", "Tratamiento Corporal Completo"));
        
        Servicio restaurant = new Servicio("Restaurant Gourmet", "Cocina internacional de primer nivel preparada por chefs reconocidos. Menú degustación y platos a la carta.", 80.0, true);
        restaurant.setOpciones(Arrays.asList("Desayuno Buffet", "Almuerzo Ejecutivo", "Cena Romántica", "Menú Degustación"));
        
        Servicio bar = new Servicio("Bar Premium", "Cócteles exclusivos y bebidas premium en un ambiente sofisticado con vista panorámica de la ciudad.", 35.0, true);
        bar.setOpciones(Arrays.asList("Cócteles Clásicos", "Cócteles de Autor", "Vinos Premium", "Whisky de Colección"));
        
        Servicio transporte = new Servicio("Transporte VIP", "Servicio de traslado al aeropuerto y tours por la ciudad en vehículos de lujo con chofer profesional.", 50.0, true);
        transporte.setOpciones(Arrays.asList("Traslado Aeropuerto", "City Tour", "Tour Gastronómico", "Tour Nocturno"));
        
        Servicio lavanderia = new Servicio("Servicio de Lavandería", "Servicio de lavandería y tintorería express disponible 24/7 con entrega en habitación.", 25.0, true);
        lavanderia.setOpciones(Arrays.asList("Lavado Express (4 horas)", "Lavado Estándar (24 horas)", "Tintorería", "Planchado"));
        
        Servicio roomservice = new Servicio("Room Service 24/7", "Servicio a la habitación las 24 horas con menú completo de restaurant y bar disponible.", 15.0, true);
        roomservice.setOpciones(Arrays.asList("Desayuno en Cama", "Almuerzo Ejecutivo", "Cena Romántica", "Snacks y Bebidas"));

        List<Servicio> servicios = new ArrayList<>();
        servicios.add(spa);
        servicios.add(restaurant);
        servicios.add(bar);
        servicios.add(transporte);
        servicios.add(lavanderia);
        servicios.add(roomservice);
        servicioRepository.saveAll(servicios);
        System.out.println("Servicios del hotel creados exitosamente");
    }
}