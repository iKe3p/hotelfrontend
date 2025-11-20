# Configuración CORS - Backend Spring Boot

## ✅ Configuración Completada

Se ha configurado CORS globalmente en el backend Spring Boot para permitir la comunicación con el frontend Angular.

### 📋 Cambios Realizados

#### 1. **SecurityConfig.java**
- ✅ Configuración mejorada de `CorsConfigurationSource`
- ✅ Orígenes permitidos: `http://localhost:4200` y `http://127.0.0.1:4200`
- ✅ Métodos HTTP permitidos: GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD
- ✅ Headers permitidos: Authorization, Content-Type, etc.
- ✅ Credenciales habilitadas (necesario para JWT)
- ✅ Aplicado a todas las rutas `/api/**` y `/**`

#### 2. **WebSecurityConfig.java**
- ✅ Configuración adicional de CORS a nivel de WebMvc
- ✅ Mapeo para `/api/**` y `/**`
- ✅ Misma configuración de orígenes y métodos

#### 3. **REST Controllers**
- ✅ `AuthController` - Agregado `@CrossOrigin`
- ✅ `ReservaRestController` - Agregado `@CrossOrigin`
- ✅ `UsuarioRestController` - Agregado `@CrossOrigin`
- ✅ Otros controllers ya tenían `@CrossOrigin` configurado

#### 4. **Frontend - URLs Actualizadas**
- ✅ Todos los servicios actualizados para usar puerto `8084`
- ✅ `AuthService`: `http://localhost:8084/api/auth`
- ✅ `ClienteService`: `http://localhost:8084/api/clientes`
- ✅ `HabitacionService`: `http://localhost:8084/api/habitaciones`
- ✅ `ReservaService`: `http://localhost:8084/api/reservas`
- ✅ `ServicioService`: `http://localhost:8084/api/servicios`

### 🔧 Configuración de Orígenes Permitidos

```java
allowedOriginPatterns:
  - http://localhost:4200  // Angular dev server por defecto
  - http://127.0.0.1:4200
  - http://localhost:*      // Cualquier puerto localhost para flexibilidad
```

### 📝 Headers Permitidos

- `Authorization` - Para JWT tokens
- `Content-Type` - Para JSON requests
- `X-Requested-With`
- `Accept`
- `Origin`
- `Access-Control-Request-Method`
- `Access-Control-Request-Headers`

### 🔐 Credenciales

- `allowCredentials: true` - Necesario para enviar cookies y headers de autorización

### 🚀 Próximos Pasos

1. **Reiniciar el Backend Spring Boot**
   ```bash
   mvn spring-boot:run
   ```

2. **Verificar que el backend esté corriendo en puerto 8084**
   - Verificar en `application.properties`: `server.port=8084`

3. **Iniciar el Frontend Angular**
   ```bash
   cd frontend
   ng serve
   ```

4. **Probar la conexión**
   - Abrir navegador en `http://localhost:4200`
   - Intentar hacer login
   - Verificar en DevTools (F12) → Network que las peticiones se realicen correctamente

### ⚠️ Solución de Problemas

#### Error: "CORS policy: No 'Access-Control-Allow-Origin' header"
- Verificar que el backend esté corriendo en puerto 8084
- Verificar que el frontend esté corriendo en puerto 4200
- Revisar la consola del navegador para más detalles

#### Error: "Preflight request doesn't pass"
- Verificar que OPTIONS esté en los métodos permitidos
- Verificar que los headers estén correctamente configurados

#### Error: "Credentials flag is true, but Access-Control-Allow-Origin is not a specific origin"
- Asegurarse de usar orígenes específicos (no `*`) cuando `allowCredentials` es `true`

### 📚 Referencias

- [Spring CORS Documentation](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html)
- [Angular HTTP Client](https://angular.io/guide/http)

