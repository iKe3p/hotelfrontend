# API REST – Guía Postman (JWT)

Esta guía resume los endpoints disponibles (GET/POST/PUT/DELETE) y cómo probarlos con Postman usando autenticación JWT. Incluye las URLs completas por operación, ejemplos de cuerpos JSON y manejo de tokens por rol.

Base URL (por defecto)
- `http://localhost:8084`

Autenticación (obtener token)
1) Login (AuthController):
   - Método: POST
   - URL: `http://localhost:8084/api/auth/login`
   - Body (JSON): `{ "username": "admin", "password": "admin123" }`
   - Respuesta: `{ "token": "<JWT>" }`
2) En Postman, usa Authorization: `Bearer {{token}}` o guarda el token como variable de colección `token`.

Encabezados comunes
- `Authorization: Bearer {{token}}`
- `Content-Type: application/json`

Notas de seguridad y formato
- Las respuestas JSON no incluyen contraseñas ni referencias cíclicas.
- Si ves HTML en una API, probablemente faltó el token correcto.

---

## Clientes – `/api/clientes`

- POST crear [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/clientes`
  - Body:
  ```json
  {
    "nombres": "Juan",
    "apellidos": "Pérez",
    "dni": "12345678",
    "nacionalidad": "Peruana",
    "email": "juan@test.com",
    "telefono": "987654321"
  }
  ```

- GET listar [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/clientes`

- GET por DNI [ADMIN/RECEPCIONISTA/CLIENTE]
  - `http://localhost:8084/api/clientes/{dni}`

- PUT actualizar [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/clientes/{id}`
  - Body: mismos campos que POST

- DELETE eliminar [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/clientes/{id}`
  - Si el cliente mantiene reservas en estado `ACTIVA` o `PENDIENTE`, la API responde **409 Conflict** con el detalle de reservas que bloquean la eliminación para que puedas cancelarlas o finalizarlas.

---

## Empleados – `/api/empleados`

- POST crear recepcionista [ADMIN]
  - `http://localhost:8084/api/empleados`
  - Body:
  ```json
  {
    "nombres": "María",
    "apellidos": "Lopez Diaz",
    "dni": "87654321",
    "email": "maria.lopez@example.com",
    "telefono": "912345678",
    "usuario": { "username": "recep1", "password": "Recep!123" }
  }
  ```

- GET listar [ADMIN]
  - `http://localhost:8084/api/empleados`

- GET por id [ADMIN]
  - `http://localhost:8084/api/empleados/{id}`

- PUT actualizar [ADMIN]
  - `http://localhost:8084/api/empleados/{id}`
  - Body (puede incluir `usuario.password` si deseas cambiarla):
  ```json
  {
    "nombres": "María Fernanda",
    "apellidos": "Lopez Diaz",
    "dni": "87654321",
    "email": "maria.lopez@example.com",
    "telefono": "912345678",
    "usuario": { "username": "recep1", "password": "NuevaClave!456" }
  }
  ```

- DELETE eliminar [ADMIN]
  - `http://localhost:8084/api/empleados/{id}`

---

## Habitaciones – `/api/habitaciones`

- POST crear [ADMIN]
  - `http://localhost:8084/api/habitaciones`
  - Body:
  ```json
  { "numero": "101", "tipo": "Suite", "precioPorNoche": 150.0, "estado": "DISPONIBLE" }
  ```

- GET listar
  - `http://localhost:8084/api/habitaciones`

- GET por id
  - `http://localhost:8084/api/habitaciones/{id}`

- GET por estado (DISPONIBLE|OCUPADA|MANTENIMIENTO)
  - `http://localhost:8084/api/habitaciones/estado/{estado}`

- PUT actualizar [ADMIN]
  - `http://localhost:8084/api/habitaciones/{id}`
  - Body: mismos campos que POST

- PATCH cambiar estado [ADMIN]
  - `http://localhost:8084/api/habitaciones/{id}/estado?estado=OCUPADA`

- DELETE eliminar [ADMIN]
  - `http://localhost:8084/api/habitaciones/{id}`

---

## Reservas – `/api/reservas`

- POST crear [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/reservas`
  - Body:
  ```json
  {
    "cliente": { "id": 1 },
    "habitacion": { "id": 2 },
    "fechaInicio": "2025-10-20",
    "fechaFin": "2025-10-22",
    "horaEntrada": "14:00:00",
    "horaSalida": "12:00:00",
    "diasEstadia": 2,
    "totalPagar": 300.0,
    "estadoReserva": "PENDIENTE"
  }
  ```

- GET listar [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/reservas`

- GET por id [ADMIN/RECEPCIONISTA/CLIENTE]
  - `http://localhost:8084/api/reservas/{id}`

- PUT actualizar [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/reservas/{id}`
  - Body: mismos campos que POST

- PUT finalizar [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/reservas/{id}/finalizar`

- DELETE cancelar [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/reservas/{id}`

---

## Servicios – `/api/servicios`

- GET activos [ADMIN/RECEPCIONISTA/CLIENTE]
  - `http://localhost:8084/api/servicios`

- GET todos (incluye inactivos) [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/servicios/todos`

- POST crear [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/servicios`
  - Body:
  ```json
  {
    "nombre": "Traslado aeropuerto",
    "descripcion": "Recepción en aeropuerto y traslado al hotel",
    "precio": 80.0,
    "activo": true
  }
  ```

- PUT actualizar [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/servicios/{id}`

- DELETE eliminar [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/servicios/{id}`

---

## Pagos – `/api/pagos`

- POST procesar pago [ADMIN/RECEPCIONISTA/CLIENTE]
  - `http://localhost:8084/api/pagos`
  - Body (tarjeta):
  ```json
  {
    "reservaId": 10,
    "metodo": "TARJETA",
    "tarjeta": {
      "numero": "4111111111111111",
      "cvv": "123",
      "fechaExp": "12/28",
      "titular": "Juan Perez"
    }
  }
  ```
  - Body (Yape/Plin):
  ```json
  {
    "reservaId": 10,
    "metodo": "YAPE",
    "wallet": {
      "telefono": "987654321",
      "titular": "Juan Perez"
    }
  }
  ```

- GET por reserva [ADMIN/RECEPCIONISTA/CLIENTE]
  - `http://localhost:8084/api/pagos/reserva/{reservaId}`

---

## Usuarios – `/api/usuarios`

- GET listar usuarios [ADMIN]
  - `http://localhost:8084/api/usuarios`

- GET perfil (usuario autenticado)
  - `http://localhost:8084/api/usuarios/perfil`

- POST registrar cliente (con usuario ADMIN/RECEPCIONISTA)
  - `http://localhost:8084/api/usuarios/registrarCliente`
  - Body (igual a crear cliente):
  ```json
  {
    "nombres": "Luis",
    "apellidos": "Ramirez Soto",
    "dni": "11223344",
    "nacionalidad": "Peruana",
    "email": "luis.ramirez@example.com",
    "telefono": "911222333"
  }
  ```

- GET listar clientes (vía usuarios) [ADMIN/RECEPCIONISTA]
  - `http://localhost:8084/api/usuarios/clientes`

---

## Tokens por rol en Postman (opcional)

- Variables de colección: `base = http://localhost:8084`, `activeRole = admin|recepcionista|cliente`, credenciales por rol (`adminUser`, `adminPass`, etc.) y `token`.
- Pre-request de colección: realiza login según `activeRole` y guarda `{{token}}` automáticamente.
- En todos los requests usa `Authorization: Bearer {{token}}`. Cambia `activeRole` para alternar de rol.

## Cómo probar en Postman – Paso a paso
1) POST `http://localhost:8084/api/auth/login` → copia `token` → setea `{{token}}`.
2) Clientes: Crear → Listar → Buscar DNI → Actualizar → Eliminar (201/200/204/404).
3) Habitaciones: Crear → Listar → PATCH estado → Eliminar.
4) Reservas: Crear con IDs válidos → Finalizar → Cancelar.
5) Seguridad: quita `Authorization` y verifica 401/403 cuando corresponda.

## 📝 Usuarios de Prueba
- **Admin**: `admin` / `admin123`
- **Recepcionista**: `recepcionista` / `recep123`
- **Cliente**: `cliente` / `cliente123`

Sugerencias
- Usa una Collection con carpetas por recurso y variables `base` y `token`.
- Añade tests automáticos en Postman para validar status y campos clave.
- Todos los endpoints están en el puerto **8084**.


