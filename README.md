# QuickSales

QuickSales es una API REST desarrollada con **Ktor** y **Kotlin**, diseñada para facilitar la compra-venta de productos de forma rápida y sencilla, incorporando autenticación mediante JWT, gestión de usuarios, productos, favoritos y un sistema de chat integrado.

---

## Tabla de contenidos

- [QuickSales](#quicksales)
  - [Tabla de contenidos](#tabla-de-contenidos)
  - [Características](#características)
  - [Tecnologías y dependencias](#tecnologías-y-dependencias)
  - [Estructura del proyecto](#estructura-del-proyecto)
  - [Modelos de datos](#modelos-de-datos)
    - [Usuario](#usuario)
    - [Producto](#producto)
    - [Favorito](#favorito)
    - [Chat](#chat)
  - [Serializadores personalizados](#serializadores-personalizados)
  - [Repositorios](#repositorios)
  - [Rutas / Endpoints](#rutas--endpoints)
    - [Autenticación](#autenticación)
    - [Usuarios (requiere JWT)](#usuarios-requiere-jwt)
    - [Productos](#productos)
    - [Favoritos (requiere JWT)](#favoritos-requiere-jwt)
    - [Chat (requiere JWT)](#chat-requiere-jwt)
    - [Ping](#ping)
  - [Seguridad](#seguridad)
  - [Configuración de la base de datos](#configuración-de-la-base-de-datos)
  - [Construcción y ejecución](#construcción-y-ejecución)

---

## Características

* Registro y login de usuarios con **JWT**.
* Gestión completa de **usuarios** (crear, actualizar perfil, modificar contraseña, depósito y retiro de saldo).
* CRUD de **productos** y proceso de compra.
* Lista de **favoritos** por usuario.
* **Chat** en tiempo real entre comprador y vendedor.
* Serialización de tipos `BigDecimal` y `LocalDateTime`.
* Configuración de conexión a base de datos **MariaDB** con **HikariCP** y **Exposed**.

---

## Tecnologías y dependencias

* Kotlin
* Ktor
* kotlinx.serialization
* Exposed (DAO / SQL DSL)
* HikariCP
* MariaDB
* JWT (Auth0)

---

## Estructura del proyecto

```text
src/main/kotlin
├── com.data
│   ├── DatabaseFactory.kt
│   ├── models
│   │   ├── Usuarios.kt
│   │   ├── Productos.kt
│   │   ├── Favoritos.kt
│   │   ├── SesionesChat.kt
│   │   ├── MensajesChat.kt
│   │   ├── serializers
│   │   │   ├── BigDecimalSerializer.kt
│   │   │   └── LocalDateTimeSerializer.kt
│   └── repository
│       ├── UsuarioRepository.kt
│       ├── ProductoRepository.kt
│       ├── FavoritoRepository.kt
│       └── ChatRepository.kt
├── com.ktor
│   ├── Main.kt
│   ├── plugins
│   │   ├── Security.kt
│   │   ├── Serialization.kt
│   │   └── Routing.kt
│   └── routes
│       ├── AuthRoutes.kt
│       ├── UserRoutes.kt
│       ├── ProductoRoutes.kt
│       ├── FavoritoRoutes.kt
│       ├── ChatRoutes.kt    ← (implementar según ChatRepository)
│       └── PingRoute.kt
└── resources
    └── application.conf
```

---

## Modelos de datos

### Usuario

```kotlin
@Serializable
data class Usuario(
    val id: Int,
    val nombre: String,
    val nombreUsuario: String,
    val contrasena: String,
    val correo: String,
    val imagenBase64: String? = null,
    val rol: String? = null,
    val saldo: BigDecimal
)
```

### Producto

```kotlin
@Serializable
data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val imagenBase64: String? = null,
    val precio: BigDecimal,
    val estado: Estado,
    val idVendedor: Int,
    val idComprador: Int? = null
)
enum class Estado { en venta, reservado, comprado }
```

### Favorito

```kotlin
@Serializable
data class Favorito(
    val id: Int,
    val idUsuario: Int,
    val idProducto: Int,
    val fechaAgregado: LocalDateTime
)
```

### Chat

```kotlin
@Serializable
data class ChatSession(
    val idSesion: Int,
    val idProducto: Int,
    val idVendedor: Int,
    val idComprador: Int,
    val fechaCreacion: String
)

@Serializable
data class ChatMessage(
    val idMensaje: Int,
    val idSesion: Int,
    val idRemitente: Int,
    val texto: String,
    val fechaEnvio: String
)
```

---

## Serializadores personalizados

* **BigDecimalSerializer**: Convierte `BigDecimal` a cadena JSON.
* **LocalDateTimeSerializer**: Convierte `LocalDateTime` al formato ISO8601.

Estos serializers se configuran en `configureSerialization()`.

---

## Repositorios

La capa de repositorios encapsula la lógica de acceso a la base de datos usando Exposed:

* `UsuarioRepository`: CRUD de usuarios, depósito, retiro, cambio de contraseña.
* `ProductoRepository`: CRUD de productos y operación de compra.
* `FavoritoRepository`: Añadir, listar y eliminar favoritos.
* `ChatRepository`: Crear/obtener sesiones de chat y gestionar mensajes.

---

## Rutas / Endpoints

### Autenticación

| Método | Ruta             | Descripción                | Request Body      |
| ------ | ---------------- | -------------------------- | ----------------- |
| POST   | `/auth/register` | Registro de usuario        | `RegisterRequest` |
| POST   | `/auth/login`    | Login y obtención de token | `LoginRequest`    |

```kotlin
// Ejemplo con Ktor Client (Kotlin)
val client = HttpClient(CIO) { install(JsonFeature) }

// Registro
data class RegisterReq()
val regResponse: Usuario = client.post("http://localhost:8080/auth/register") {
    contentType(ContentType.Application.Json)
    body = RegisterReq("Ana", "ana123", "pass", "ana@mail.com")
}

// Login
data class LoginReq(); data class LoginRes(val token: String, val user: Usuario)
val loginRes: LoginRes = client.post("http://localhost:8080/auth/login") {
    contentType(ContentType.Application.Json)
    body = LoginReq("ana@mail.com", "pass")
}
```

### Usuarios (requiere JWT)

Autenticados con header `Authorization: Bearer <token>`.

| Método | Ruta                       | Descripción            | Request Body      |
| ------ | -------------------------- | ---------------------- | ----------------- |
| GET    | `/usuarios/{id}`           | Obtener usuario por ID | —                 |
| GET    | `/usuarios`                | Listar todos usuarios  | —                 |
| POST   | `/usuarios/{id}/depositar` | Depositar saldo        | `DepositoRequest` |
| POST   | `/usuarios/{id}/retirar`   | Retirar saldo          | `RetiroRequest`   |
| PUT    | `/usuarios/{id}/profile`   | Actualizar perfil      | `ProfileRequest`  |
| PUT    | `/usuarios/{id}/password`  | Cambiar contraseña     | `PasswordRequest` |
| DELETE | `/usuarios/{id}`           | Eliminar usuario       | —                 |

```bash
# Obtener mi perfil
echo "Authorization: Bearer $TOKEN" | http GET http://localhost:8080/usuarios/1
# Depositar 50€
echo '{"cantidad":50}' | http POST http://localhost:8080/usuarios/1/depositar Authorization:"Bearer $TOKEN"
```

### Productos

| Método | Ruta                  | Descripción             | Request Body        |
| ------ | --------------------- | ----------------------- | ------------------- |
| GET    | `/productos`          | Listar todos productos  | —                   |
| GET    | `/productos/{id}`     | Obtener producto por ID | —                   |
| POST   | `/productos`          | Crear producto          | `Producto` (sin id) |
| PUT    | `/productos/{id}`     | Actualizar producto     | `Producto` (sin id) |
| DELETE | `/productos/{id}`     | Eliminar producto       | —                   |
| POST   | `/productos/{id}/buy` | Comprar producto        | —                   |

```kotlin
// Crear un producto
val nuevo: Producto = client.post("http://localhost:8080/productos") {
    contentType(ContentType.Application.Json)
    body = mapOf(
        "nombre" to "Libro Kotlin",
        "descripcion" to "Manual práctico",
        "precio" to "29.90"
    )
    header("Authorization", "Bearer $TOKEN")
}
```

### Favoritos (requiere JWT)

| Método | Ruta                      | Descripción              | Request Body      |
| ------ | ------------------------- | ------------------------ | ----------------- |
| GET    | `/favoritos`              | Listar favoritos usuario | —                 |
| POST   | `/favoritos`              | Añadir a favoritos       | `FavoritoRequest` |
| DELETE | `/favoritos/{idProducto}` | Eliminar de favoritos    | —                 |

```bash
# Añadir favorito
echo '{"idProducto":5}' | http POST http://localhost:8080/favoritos Authorization:"Bearer $TOKEN"
```

### Chat (requiere JWT)

| Método | Ruta                                 | Descripción                    | Request Body                                          |
| ------ | ------------------------------------ | ------------------------------ | ----------------------------------------------------- |
| POST   | `/chat/sessions`                     | Crear u obtener sesión de chat | `{ "idProducto":1, "idVendedor":2, "idComprador":3 }` |
| GET    | `/chat/sessions`                     | Listar sesiones del usuario    | —                                                     |
| POST   | `/chat/sessions/{idSesion}/messages` | Enviar mensaje en sesión       | `{ "texto": "Hola!" }`                                |
| GET    | `/chat/sessions/{idSesion}/messages` | Obtener mensajes de la sesión  | —                                                     |

```kotlin
// Enviar mensaje
val msg: ChatMessage = client.post("http://localhost:8080/chat/sessions/1/messages") {
    contentType(ContentType.Application.Json)
    body = mapOf("texto" to "¿Está disponible?")
    header("Authorization", "Bearer $TOKEN")
}
```

### Ping

| Método | Ruta    | Descripción            |
| ------ | ------- | ---------------------- |
| GET    | `/ping` | Comprobación de estado |

```bash
curl http://localhost:8080/ping
# → { "ping": "pong" }
```

---

## Seguridad

* **JWT** para autenticación. Se configura en `configureSecurity()`.
* Secret: `mi_secreto` (configurable en `application.conf`).

---

## Configuración de la base de datos

Se utiliza **MariaDB** con **HikariCP**:

```kotlin
object DatabaseFactory {
    fun init() {
        // URL, usuario y contraseña en HikariConfig
        Database.connect(HikariDataSource(config))
        transaction {
            SchemaUtils.create(Usuarios, Productos, Favoritos, SesionesChat, MensajesChat)
        }
    }
}
```

Ajusta `jdbcUrl` y credenciales en `DatabaseFactory.kt` o en variables de entorno.

---

## Construcción y ejecución

```bash
# Ejecutar tests
gradlew test

# Compilar
gradlew build

# Crear JAR ejecutable
gradlew buildFatJar

# Ejecutar localmente
gradlew run
# o con Docker
gradlew runDocker
```

Si todo va bien, verás en consola:

```
2025-05-26 10:00:00.000 [main] INFO  Application - Started in Xs.
2025-05-26 10:00:00.100 [main] INFO  Application - Listening at http://0.0.0.0:8080
```

---


