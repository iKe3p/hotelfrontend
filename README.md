## Sistema Hotelero Oasis Digital — Guía Técnica Completa

**Sistema completo de gestión hotelera** desarrollado con Spring Boot, implementando TDD, autenticación JWT, y una interfaz moderna con efectos visuales avanzados.

### 🚀 Estado Actual: SISTEMA COMPLETO Y FUNCIONAL
- ✅ Backend completo con Spring Boot 3.x + Java 17
- ✅ Autenticación JWT multi-rol (Admin/Recepcionista/Cliente)
- ✅ Base de datos MySQL con JPA/Hibernate
- ✅ Frontend Thymeleaf con diseño moderno y efectos hover
- ✅ Dashboard interactivo con métricas en tiempo real
- ✅ Gestión completa: Clientes, Reservas, Habitaciones, Empleados
- ✅ Tests unitarios e integración (TDD completo)
- ✅ API REST para futura migración a Angular

### 🛠️ Stack Tecnológico
- **Backend**: Java 17, Spring Boot 3.x, Spring Security, JWT
- **Base de Datos**: MySQL (producción), H2 (tests)
- **Frontend**: Thymeleaf, CSS3 con efectos avanzados, JavaScript
- **Testing**: JUnit 5, Mockito, TestContainers
- **Build**: Maven, Spring Boot DevTools

---

## 1) TDD (Test-Driven Development)

- Dependencias: `spring-boot-starter-test`, `mockito`, `assertj` (y H2 para integración).
- Config test: `src/test/resources/application-test.properties` (H2 en memoria).
- Ejecución: `mvn test`.

Dónde está en el código
- Tests unitarios (servicios):
  - `src/test/java/com/hotel/gestion/sistema_hotelero/service/ClienteServiceTest.java`
  - `src/test/java/com/hotel/gestion/sistema_hotelero/service/HabitacionServiceTest.java`
  - `src/test/java/com/hotel/gestion/sistema_hotelero/service/ReservaServiceTest.java`
- Tests de integración (arrancan contexto y BD H2):
  - `src/test/java/com/hotel/gestion/sistema_hotelero/BaseIntegrationTest.java`
  - `src/test/java/com/hotel/gestion/sistema_hotelero/service/ReservaServiceIntegrationTest.java`
- Tests de controlador (MockMvc):
  - `src/test/java/com/hotel/gestion/sistema_hotelero/controller/ReservaControllerTest.java`

Cómo se aplica
1. RED: agregar test para una regla (p. ej., crear/cancelar reserva en `ReservaServiceTest`).
2. GREEN: implementar mínima lógica en `src/main/java/.../service` hasta que pase.
3. REFACTOR: limpiar el código manteniendo los tests verdes.

---

## 2) MySQL

- Dependencia: `mysql-connector-j` en `pom.xml`.
- Configuración: `src/main/resources/application.properties` (en runtime) apuntando a MySQL; para tests se usa H2.
- Entidades y tablas se mapean con JPA/Hibernate (ver sección siguiente).

Inicio rápido (dev)
- Arranca MySQL y crea una BD (p. ej. `hotel_db`).
- Configura credenciales y URL en `application.properties`.
- Ejecuta: `mvn spring-boot:run`.

---

## 3) JPA, JPQL y Hibernate

Dónde se usa
- Entidades JPA: `src/main/java/com/hotel/gestion/sistema_hotelero/model/*` (p. ej., `Cliente`, `Habitacion`, `Reserva`, `Usuario`).
- Repositorios Spring Data JPA: `src/main/java/com/hotel/gestion/sistema_hotelero/repository/*` con queries derivadas por nombre.
- Servicios de dominio: `src/main/java/com/hotel/gestion/sistema_hotelero/service/*` (orquestan lógica con los repositorios).

Consultas y ejemplos
- Consultas derivadas (JPA/Hibernate genera SQL):
  - `ClienteRepository.findByDni(String dni)`
  - `HabitacionRepository.findByEstado(String estado)`
- Contadores con filtros:
  - `ReservaRepository.countByEstadoReservaIgnoreCase(String estado)`
  - `ReservaRepository.countByFechaInicio(LocalDate fecha)` / `countByFechaFin(...)`
- JPQL: puede añadirse con `@Query` según sea necesario (no intrusivo; hoy se aprovechan derivadas por nombre).

Mapping/Hibernate
- Anotaciones `@Entity`, `@Table`, `@Id`, etc. en `model/*`.
- Hibernate actúa como proveedor JPA por defecto con Spring Boot 3.

---

## 4) Spring Security y JWT

Arquitectura
- Filtro JWT: `src/main/java/com/hotel/gestion/sistema_hotelero/security/JwtAuthenticationFilter.java`
- Servicio JWT (emitir/validar): `src/main/java/com/hotel/gestion/sistema_hotelero/security/JwtService.java`
- Configuración Security: `src/main/java/com/hotel/gestion/sistema_hotelero/config/SecurityConfig.java`
- Usuarios (UserDetails): `model/Usuario.java` y `service/UserDetailsServiceImpl.java`
- Propiedades JWT: `config/JwtProperties.java` (habilitado con `@EnableConfigurationProperties` en la `Application`).

Rutas y autorizaciones (extracto)
- Públicas: `/`, `/login`, recursos estáticos y `/api/auth/**`.
- Solo ADMIN: `/empleados/**`, `/admin/**`, `/auditoria/logs`.
- ADMIN o RECEPCIONISTA: `/clientes/**`, `/reservas/crear`, `/habitaciones/**`.
- CLIENTE: `/cliente/**`, `/cliente/reservas/**`.
- `/dashboard` requiere autenticación; la vista mostrada depende del rol.

Flujo JWT
1. Login obtiene token (ver `AuthController` / `AuthLoginController`).
2. El filtro `JwtAuthenticationFilter` valida el token en cada request y establece el `Authentication`.
3. Security aplica reglas de autorización en `SecurityConfig`.

Uso en vistas
- Thymeleaf + Spring Security dialect: se usa `sec:authorize` para mostrar/ocultar elementos por rol en plantillas (`templates/dashboard.html`, `templates/index.html`, etc.).

---

## 5) Capas y puntos de entrada

- Controladores MVC (vistas): `src/main/java/.../controller/*` (p. ej., `DashboardController`, `ReservaController`, `ClienteController`).
- Controladores REST: `src/main/java/.../restController/*` (p. ej., `ReservaRestController`, `HabitacionRestController`).
- Plantillas UI: `src/main/resources/templates/*` (Thymeleaf).
- Recursos estáticos: `src/main/resources/static/*` (CSS/JS/Imágenes).

---

## 6) Comandos útiles

- Ejecutar tests: `mvn test`
- Ejecutar app: `mvn spring-boot:run`
- Empaquetar: `mvn clean package`

---

## 7) Funcionalidades Implementadas

### 🎯 Características Principales
- **Dashboard Inteligente**: Métricas por rol con visualización en tiempo real
- **Gestión de Clientes**: CRUD completo con búsqueda por DNI
- **Sistema de Reservas**: Creación, modificación, check-in/check-out
- **Gestión de Habitaciones**: Control de estados y disponibilidad
- **Administración de Empleados**: Solo para usuarios ADMIN
- **Autenticación Robusta**: JWT con roles y permisos granulares

### 🎨 Interfaz y UX
- **Diseño Moderno**: Paleta de colores azules profesional
- **Efectos Interactivos**: Hover effects en navbar y elementos
- **Responsive Design**: Adaptable a móviles y tablets
- **Navegación Intuitiva**: Menús contextuales por rol
- **Feedback Visual**: Animaciones y transiciones suaves

### 🔐 Seguridad
- **Autenticación JWT**: Tokens seguros con expiración
- **Autorización por Roles**: ADMIN, RECEPCIONISTA, CLIENTE
- **Protección CSRF**: Implementada en formularios
- **Validación de Datos**: Backend y frontend
- **Sesiones Seguras**: Manejo de logout y timeouts

---

## 8) Roadmap y Extensiones

### 🔄 Migración a Angular (Planificada)
- **Frontend Moderno**: Angular 17+ con Material Design
- **SPA Experience**: Navegación fluida sin recargas
- **Estado Reactivo**: RxJS y Signals para manejo de estado
- **Componentes Reutilizables**: Arquitectura modular
- **Ver**: `MIGRACION_ANGULAR.md` para guía completa

### 📊 Funcionalidades Futuras
- **Reportes Avanzados**: PDF/Excel con gráficos
- **Sistema de Pagos**: Integración con pasarelas de pago
- **Notificaciones**: Email/SMS para confirmaciones
- **API Pública**: Para integraciones externas
- **Mobile App**: React Native o Flutter

### 🔧 Mejoras Técnicas
- **Microservicios**: Separación por dominios
- **Cache Redis**: Para mejor performance
- **Monitoring**: Prometheus + Grafana
- **CI/CD**: Pipeline automatizado
- **Docker**: Containerización completa

---

## 9) Guía de Instalación y Uso

### 📋 Prerrequisitos
- **JDK 17** (obligatorio - no funciona con Java 24)
- **MySQL 8.0+** para base de datos
- **Maven 3.8+** para build
- **IDE** recomendado: IntelliJ IDEA o VS Code

### 🚀 Instalación Rápida
```bash
# 1. Clonar repositorio
git clone <repository-url>
cd sistema-hotelero

# 2. Configurar base de datos
# Crear BD 'hotel_db' en MySQL
# Actualizar credenciales en application.properties

# 3. Ejecutar aplicación
mvn spring-boot:run

# 4. Acceder al sistema
# http://localhost:8080
```

### 👥 Usuarios de Prueba
- **Admin**: `admin` / `admin123`
- **Recepcionista**: `recepcionista` / `recep123`
- **Cliente**: `cliente` / `cliente123`

### 🧪 Ejecutar Tests
```bash
# Tests unitarios
mvn test

# Tests de integración
mvn test -Dtest=*IntegrationTest

# Cobertura de código
mvn jacoco:report
```

### 🔧 Troubleshooting
- **Java Version**: Verificar `java -version` = 17
- **MySQL**: Verificar conexión y credenciales
- **Puerto**: Cambiar puerto en `application.properties` si 8080 está ocupado
- **Tests**: H2 configurado automáticamente para tests

---

## 📚 Documentación Adicional

- **[Colección Postman](POSTMAN_COLLECTION.md)**: Tests de API REST
- **[Guía TDD](TDD_GUIDE.md)**: Metodología de desarrollo
- **[Migración Angular](MIGRACION_ANGULAR.md)**: Roadmap de modernización

## 🤝 Contribución

1. Fork del proyecto
2. Crear rama feature (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

---

**Sistema Hotelero Oasis Digital** - Desarrollado con ❤️ usando Spring Boot y las mejores prácticas de desarrollo.


