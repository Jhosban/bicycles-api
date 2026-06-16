# API de Alquiler de Bicicletas

API REST para gestionar el alquiler de bicicletas en una empresa de turismo urbano. Permite registrar bicicletas, controlar su disponibilidad, iniciar y finalizar alquileres, y calcular el costo total aplicando la tarifa según el tipo de bicicleta, con multa automática por devolución tardía.

## Tabla de contenidos

- [Tecnologías](#tecnolog%C3%ADas)
- [Primeros pasos](#primeros-pasos)
- [Endpoints de la API](#endpoints-de-la-api)
- [Ejemplos de uso](#ejemplos-de-uso)
- [Reglas de negocio](#reglas-de-negocio)
- [Pruebas](#pruebas)
- [Arquitectura](#arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Licencia](#licencia)
- [Créditos](#cr%C3%A9ditos)

---

## Tecnologías

- **Java 21** + **Spring Boot 4.0.7**
- **Spring Data JPA** + **Hibernate** para persistencia
- **PostgreSQL** (Supabase) como base de datos
- **Flyway** para migraciones
- **Spring Security** + **JJWT** para autenticación con tokens de Supabase
- **SpringDoc OpenAPI** para documentación Swagger automática
- **Maven** para build (vía Maven Wrapper)
- **Lombok** para reducir código repetitivo
- **JUnit 5** + **Mockito** + **AssertJ** para pruebas

---

## Primeros pasos

### Requisitos previos

- **JDK 21** (Temurin, Oracle o cualquier distribución compatible)
- Un proyecto en [Supabase](https://supabase.com) (tiene plan gratuito)

### 1. Crear el proyecto en Supabase

1. Creá una cuenta en [supabase.com](https://supabase.com) y hacé **New Project**
2. Guardá la contraseña de la base de datos, la vas a necesitar
3. Esperá un par de minutos a que se inicialice

### 2. Obtener las credenciales

En el panel de Supabase:

- **URL de la base de datos (directa):** Settings → Database → Connection string → **Direct** → URI
  ```
  jdbc:postgresql://postgres.[REFERENCIA]:[CONTRASEÑA]@db.[REFERENCIA].supabase.co:5432/postgres
  ```
- **Clave anónima (anon key):** Settings → API → Project API keys → `anon` `public`
  (la que empieza con `eyJhbGc...`)

### 3. Configurar las variables de entorno

| Variable | Ejemplo |
|---|---|
| `SUPABASE_DB_URL` | `jdbc:postgresql://postgres.xxxxx:tu_contraseña@db.xxxxx.supabase.co:5432/postgres` |
| `SUPABASE_DB_USER` | `postgres` |
| `SUPABASE_DB_PASSWORD` | (la contraseña que pusiste al crear el proyecto) |
| `SUPABASE_JWKS_URI` | (opcional, tiene un valor por defecto) |
| `SUPABASE_JWT_ISSUER` | (opcional, tiene un valor por defecto) |

#### En IntelliJ IDEA

Arriba a la derecha, en el menú de Run/Debug → **Edit Configurations** → **Environment** → **Environment variables** → agregá cada una.

#### En terminal (PowerShell)

```powershell
$env:SUPABASE_DB_URL = "jdbc:postgresql://postgres.xxxxx:tu_contraseña@db.xxxxx.supabase.co:5432/postgres"
$env:SUPABASE_DB_USER = "postgres"
$env:SUPABASE_DB_PASSWORD = "tu_contraseña"
$env:SPRING_PROFILES_ACTIVE = "dev"

.\mvnw.cmd spring-boot:run
```

### 4. Ejecutar la aplicación

**En IntelliJ:** hacé click en ▶️

**En terminal:**
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

Al arrancar deberías ver en la consola:
```
The following 1 profile is active: "dev"
Loaded 1 public key(s) from JWKS
Flyway: Successfully applied 4 migrations to schema "public"
Tomcat started on port 8080
=================================================================
Application 'bicycles' is running on port 8080
Perfiles activos: dev
Swagger UI:        http://localhost:8080/swagger-ui.html
Documentación:     http://localhost:8080/v3/api-docs
=================================================================
```

El mensaje `Loaded 1 public key(s) from JWKS` confirma que la conexión con Supabase para validar JWT funciona.

Interfaz de Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Al arrancar, Flyway crea las tablas e inserta 5 bicicletas de ejemplo:

| Código | Tipo | Estado |
|---|---|---|
| BIC-001 | URBANA | DISPONIBLE |
| BIC-002 | MONTAÑA | DISPONIBLE |
| BIC-003 | ELECTRICA | DISPONIBLE |
| BIC-004 | MONTAÑA | EN_MANTENIMIENTO |
| BIC-005 | URBANA | DISPONIBLE |

---

## Endpoints de la API

Todos los endpoints bajo `/api/**` requieren un token JWT válido. Swagger y actuator son públicos.

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/bicycles` | Crear bicicleta |
| `GET` | `/api/bicycles/available` | Listar disponibles (filtro opcional `?type=URBANA`) |
| `POST` | `/api/rentals` | Iniciar alquiler |
| `POST` | `/api/rentals/{id}/finish` | Finalizar alquiler (cuerpo opcional) |
| `GET` | `/api/bicycles/{code}/history` | Historial de una bicicleta |

### Códigos de respuesta

| Código | Significado |
|---|---|
| 200 / 201 | OK |
| 400 | Validación fallida |
| 401 | Token inválido o ausente |
| 404 | Recurso no existe |
| 409 | Conflicto (por ejemplo, bicicleta no disponible) |
| 500 | Error inesperado |

---

## Ejemplos de uso

### Obtener un token de Supabase

```bash
export ANON_KEY="eyJhbGciOiJIUzI1NiIs...tu-clave-anonima-aqui..."

export TOKEN=$(curl -s -X POST "https://[REFERENCIA].supabase.co/auth/v1/token?grant_type=password" \
     -H "apikey: $ANON_KEY" \
     -H "Content-Type: application/json" \
     -d '{"email":"test@test.com","password":"secreto123"}' | jq -r '.access_token')
```

> Los tokens de Supabase duran 1 hora. Si ves un error 401, regenerá el token.

### Listar bicicletas disponibles

```bash
# Sin filtro
curl -X GET http://localhost:8080/api/bicycles/available \
     -H "Authorization: Bearer $TOKEN"

# Filtrando por tipo
curl -X GET "http://localhost:8080/api/bicycles/available?type=URBANA" \
     -H "Authorization: Bearer $TOKEN"
```

**Respuesta:**
```json
[
  {"id":1,"code":"BIC-001","type":"URBANA","status":"DISPONIBLE"},
  {"id":2,"code":"BIC-002","type":"MONTAÑA","status":"DISPONIBLE"},
  {"id":3,"code":"BIC-003","type":"ELECTRICA","status":"DISPONIBLE"},
  {"id":5,"code":"BIC-005","type":"URBANA","status":"DISPONIBLE"}
]
```

### Crear bicicleta

```bash
curl -X POST http://localhost:8080/api/bicycles \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "code": "BIC-099",
       "type": "URBANA",
       "status": "DISPONIBLE"
     }'
```

**Respuesta (201):**
```json
{"id":6,"code":"BIC-099","type":"URBANA","status":"DISPONIBLE"}
```

### Iniciar alquiler

```bash
curl -X POST http://localhost:8080/api/rentals \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "bicycleCode": "BIC-001",
       "customerName": "Juan Pérez",
       "estimatedDurationHours": 2
     }'
```

**Respuesta (201):**
```json
{
  "id": 1,
  "bicycleCode": "BIC-001",
  "customerName": "Juan Pérez",
  "startTime": "2026-06-14T18:30:00",
  "endTime": null,
  "estimatedDurationHours": 2,
  "realDurationMinutes": null,
  "baseCost": null,
  "lateFee": null,
  "totalCost": null,
  "hadPenalty": false,
  "finished": false
}
```

### Finalizar alquiler

```bash
# Sin cuerpo (usa la hora actual)
curl -X POST http://localhost:8080/api/rentals/1/finish \
     -H "Authorization: Bearer $TOKEN"

# Con hora de devolución explícita
curl -X POST http://localhost:8080/api/rentals/1/finish \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"endTime":"2026-06-14T21:00:00"}'
```

**Respuesta (200):**
```json
{
  "id": 1,
  "bicycleCode": "BIC-002",
  "customerName": "Juan Pérez",
  "startTime": "2026-06-14T17:40:00",
  "endTime": "2026-06-14T21:00:00",
  "estimatedDurationHours": 2,
  "realDurationMinutes": 200,
  "baseCost": 20000,
  "lateFee": 5000,
  "totalCost": 25000,
  "hadPenalty": true,
  "finished": true
}
```

### Ver historial de una bicicleta

```bash
curl -X GET http://localhost:8080/api/bicycles/BIC-001/history \
     -H "Authorization: Bearer $TOKEN"
```

**Respuesta:** lista de alquileres ordenados del más reciente al más viejo.

### Verificar que el JWT funciona (sin token debe dar 401)

```bash
curl -X GET http://localhost:8080/api/bicycles/available
```

**Respuesta (401):**
```json
{
  "timestamp": "2026-06-14T18:30:00.123456",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required to access this resource",
  "path": "/api/bicycles/available"
}
```

---

## Reglas de negocio

| Concepto | Cálculo |
|---|---|
| Tarifa URBANA | $3.500 por hora |
| Tarifa MONTAÑA | $5.000 por hora |
| Tarifa ELÉCTRICA | $7.500 por hora |
| Costo base | Tiempo real redondeado al alza a la hora completa, multiplicado por la tarifa |
| Multa | 50% de la tarifa por cada hora de retraso (redondeado al alza, mínimo 1 hora) |

**Ejemplo:** bicicleta MONTAÑA, estimada 2 horas, devuelta a las 3 horas 20 minutos:
- Tiempo real: 200 minutos → 4 horas (redondeo al alza)
- Costo base: 4h × $5.000 = $20.000
- Retraso: 200 - 120 = 80 minutos → 2 horas (redondeo al alza)
- Multa: 2h × $2.500 = $5.000
- **Total: $25.000**

---

## Pruebas

```powershell
# Todas las pruebas
.\mvnw.cmd test

# Prueba específica
.\mvnw.cmd test -Dtest=BicycleServiceTest
```

**50 pruebas, 0 fallas:**

| Capa | Qué cubre |
|---|---|
| Calculadoras (TariffCalculator, PenaltyCalculator) | Cálculo de tarifa y multa con casos límite (sin tiempo, con redondeo, ejemplo del enunciado) |
| Servicios (BicycleService, RentalService) | Crear, buscar, iniciar y finalizar alquiler con todos los caminos (caso exitoso, 404, 409) |
| Controladores (BicycleController, RentalController) | Validación HTTP, códigos de estado, JSON de request y response |

Las calculadoras se prueban como clases puras con `new`, sin Spring. Los servicios usan Mockito para simular los repositorios. Los controladores usan `@WebMvcTest` con `MockMvc` para verificar la capa HTTP completa.

---

## Arquitectura

```
Petición HTTP
     ↓
Controlador (REST)         → valida con @Valid
     ↓
Servicio                   → orquesta y valida reglas de negocio
     ↓                      ↓
Repositorio JPA         Calculadoras (clases puras)
     ↓
PostgreSQL (Supabase)
```

- **MVC clásico** por el alcance acotado del proyecto
- Las **calculadoras de tarifa y multa** son clases puras en Java (sin Spring), instanciables con `new` y probables sin contexto
- Los **DTOs son entrada y salida a la vez**, marcados con `@JsonProperty(READ_ONLY)` en los campos generados por el servidor
- **JWT híbrido**: Supabase emite los tokens, esta API solo los valida contra el endpoint JWKS de Supabase

---

## Estructura del proyecto

```
bicycles/
├── pom.xml
├── mvnw, mvnw.cmd
├── .gitignore
├── LICENSE
├── README.md
└── src/
    ├── main/
    │   ├── java/com/ceiba/bicycles/
    │   │   ├── BicycleApiApplication.java
    │   │   ├── model/         # entidades JPA y enumeraciones
    │   │   ├── service/       # lógica de negocio
    │   │   ├── repository/    # Spring Data JPA
    │   │   ├── controller/    # endpoints REST
    │   │   ├── dto/           # objetos de transferencia
    │   │   ├── exception/     # excepciones y manejador global
    │   │   ├── security/      # validación de JWT
    │   │   └── config/        # configuración de seguridad
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       └── db/migration/  # migraciones de Flyway
    └── test/
        └── java/com/ceiba/bicycles/
            ├── service/
            └── controller/
```

---

## Licencia

Licencia MIT. Consultá el archivo [LICENSE](LICENSE) para ver el texto completo.

---

## Créditos

API construida por **Jhosban Barajas**.
