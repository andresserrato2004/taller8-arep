# Taller 8 - AREP: Microservicios con AWS Lambda

Sistema de microservicios tipo Twitter desplegado en AWS Lambda con autenticación mediante AWS Cognito.



[![VIDEO_DEMOSTRATIVO_DE_LA_APLCACION](https://i9.ytimg.com/vi_webp/wFpiakrxCrk/mq2.webp?sqp=COCPhsgG-oaymwEmCMACELQB8quKqQMa8AEB-AH-CYAC0AWKAgwIABABGF4gXiheMA8=&rs=AOn4CLA8dtmcoSoHPO0lLFV6RKnEppfkXg)](https://youtu.be/wFpiakrxCrk)

**[Ver Video en YouTube](https://youtu.be/wFpiakrxCrkC55S0Bi9lj8)**



## 📋 Tabla de Contenidos

- [Arquitectura](#arquitectura)
- [Servicios](#servicios)
- [Tecnologías](#tecnologías)
- [Requisitos Previos](#requisitos-previos)
- [Configuración Local](#configuración-local)
- [Despliegue en AWS Lambda](#despliegue-en-aws-lambda)
- [Pruebas](#pruebas)
- [Variables de Entorno](#variables-de-entorno)

---

## 🏗️ Arquitectura

```
┌─────────────────┐
│   Frontend      │
│  (HTML/JS/CSS)  │
└────────┬────────┘
         │
         ├──────────────┬──────────────┬─────────────┐
         │              │              │             │
         ▼              ▼              ▼             ▼
┌────────────────┐ ┌─────────────┐   ┌──────────┐ ┌──────────┐
│  User Service  │ │Post Service │   │ Stream   │ │  Cognito │
│   (Port 8081)  │ │ (Port 8083) │   │(Port8082)│ │   Auth   │
└────────┬───────┘ └──────┬──────┘   └────┬─────┘ └──────────┘
         │                │              │
         └────────────────┴──────────────┘
             │             │             │
        ┌────▼─────┐  ┌────▼─────┐  ┌────▼─────┐
        │PostgreSQL│  │PostgreSQL│  │PostgreSQL│
        │  (EC2)   │  │  (EC2)   │  │  (EC2)   │
        │Port 5433 │  │Port 5433 │  │Port 5433 │
        └──────────┘  └──────────┘  └──────────┘               
    

---

## 🔧 Servicios

### 1. User Service (Puerto 8081)
- Registro y autenticación de usuarios
- Integración con AWS Cognito
- Gestión de perfiles de usuario
- **Handler Lambda:** `com.example.user_service.StreamLambdaHandler::handleRequest`

### 2. Post Service (Puerto 8083)
- Crear, leer, actualizar y eliminar posts
- Sistema de likes
- Gestión de contenido
- **Handler Lambda:** `com.example.posts.StreamLambdaHandler::handleRequest`

### 3. Stream Service (Puerto 8082)
- Feed personalizado de usuarios
- Timeline global
- Búsqueda de posts
- Filtrado por hashtags
- Posts trending
- **Handler Lambda:** `com.example.stream.StreamLambdaHandler::handleRequest`

### 4. Frontend
- Interfaz de usuario HTML/CSS/JavaScript
- Páginas: login, registro, verificación, home
- Comunicación con microservicios vía REST API
- Manejo de tokens JWT

---

## 🛠️ Tecnologías

- **Backend:** Spring Boot 3.3.5, Java 21
- **Base de Datos:** PostgreSQL 15 (Docker en EC2)
- **Autenticación:** AWS Cognito
- **Cloud:** AWS Lambda, API Gateway
- **ORM:** Hibernate/JPA
- **Seguridad:** Spring Security, JWT
- **Build:** Maven
- **Frontend:** HTML5, CSS3, JavaScript (Vanilla)

---

## 📦 Requisitos Previos

### Para desarrollo local:
- Java 21
- Maven 3.8+
- PostgreSQL (o acceso al servidor en EC2)
- AWS CLI (para despliegue)
- Git

### Para despliegue en AWS:
- Cuenta de AWS
- AWS CLI configurado
- Permisos para Lambda, IAM, API Gateway

---

## 🚀 Configuración Local

### 1. Clonar el repositorio

```bash
git clone https://github.com/andresserrato2004/taller8-arep.git
cd taller8-arep
```

### 2. Configurar PostgreSQL

El proyecto usa una base de datos PostgreSQL en Docker en EC2:

```
Host: 54.152.221.17
Port: 5433
Database: db3
Username: admin1
Password: pass1
```

**O configurar PostgreSQL localmente:**

```bash
# Iniciar PostgreSQL con Docker
docker run --name postgres-taller8 \
  -e POSTGRES_USER=admin1 \
  -e POSTGRES_PASSWORD=pass1 \
  -e POSTGRES_DB=db3 \
  -p 5433:5432 \
  -d postgres:15

# Verificar que esté corriendo
docker ps | grep postgres-taller8
```

### 3. Configurar variables de entorno

Crea un archivo `.env` en cada servicio o configura las variables:

```bash
# AWS Cognito
COGNITO_USER_POOL_ID=us-********-**********rL
COGNITO_CLIENT_ID=2h*************f
COGNITO_CLIENT_SECRET=13------------uj
COGNITO_REGION=us-east-1

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://54.152.221.17:5433/db3
SPRING_DATASOURCE_USERNAME=admin1
SPRING_DATASOURCE_PASSWORD=pass1
```

### 4. Compilar los servicios

```bash
# User Service
cd user-service
mvn clean package -DskipTests
cd ..

# Post Service
cd posts
mvn clean package -DskipTests
cd ..

# Stream Service
cd stream
mvn clean package -DskipTests
cd ..
```

### 5. Ejecutar los servicios

**Terminal 1 - User Service:**
```bash
cd user-service
mvn spring-boot:run
# Corre en http://localhost:8081
```

**Terminal 2 - Post Service:**
```bash
cd posts
mvn spring-boot:run
# Corre en http://localhost:8083
```

**Terminal 3 - Stream Service:**
```bash
cd stream
mvn spring-boot:run
# Corre en http://localhost:8082
```

**Terminal 4 - Frontend:**
```bash
cd front
python3 -m http.server 3000
# Abre http://localhost:3000/login.html
```

---

## ☁️ Despliegue en AWS Lambda

### Preparación del JAR

Cada servicio necesita ser empaquetado para Lambda:

```bash
# Ejemplo para User Service
cd user-service
mvn clean package
```

El JAR estará en: `target/user-service-0.0.1-SNAPSHOT.jar`

### Opción 1: Despliegue Manual (Interfaz AWS)

1. **Subir el JAR:**
   - Ve a AWS Lambda Console
   - Crea una nueva función (Runtime: Java 21)
   - Sube el JAR desde `target/`

2. **Configurar Handler:**
   - User Service: `com.example.user_service.StreamLambdaHandler::handleRequest`
   - Post Service: `com.example.posts.StreamLambdaHandler::handleRequest`
   - Stream Service: `com.example.stream.StreamLambdaHandler::handleRequest`

3. **Configurar Variables de Entorno:** (ver sección Variables de Entorno)

4. **Configurar Memoria y Timeout:**
   - Memory: 1024 MB
   - Timeout: 30 segundos

### Opción 2: Despliegue con Lambda Layers (JAR > 50MB)

Si tu JAR supera los 50MB, usa la estrategia de Layers:

```bash
cd user-service
./create-layer.sh
```

Esto genera:
- `lambda-layer.zip` (dependencias ~60MB)
- `user-service-code.zip` (código ~32KB)

**Subir manualmente:**
1. Publica la Layer con `lambda-layer.zip`
2. Sube el código con `user-service-code.zip`
3. Asocia la Layer a la función

### Crear API Gateway (Opcional)

Para exponer la Lambda como API HTTP:

1. Ve a API Gateway Console
2. Crea REST API
3. Crea recurso `{proxy+}`
4. Crea método ANY
5. Integra con tu función Lambda
6. Despliega en stage (ej: `prod`)

---

## 🧪 Pruebas

### Pruebas Locales

#### User Service
```bash
cd user-service
./test-api.sh
```

#### Post Service
```bash
cd posts
./test-api.sh
```

#### Stream Service
```bash
cd stream
./test-api.sh
```

### Pruebas en Lambda

#### Health Check
```json
{
  "httpMethod": "GET",
  "path": "/health",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": null
}
```

#### Registro de Usuario
```json
{
  "httpMethod": "POST",
  "path": "/auth/register",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"Test123!\"}"
}
```

#### Login
```json
{
  "httpMethod": "POST",
  "path": "/auth/login",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": "{\"username\":\"testuser\",\"password\":\"Test123!\"}"
}
```

#### Crear Post
```json
{
  "httpMethod": "POST",
  "path": "/posts",
  "headers": {
    "Content-Type": "application/json",
    "Authorization": "Bearer YOUR_TOKEN_HERE"
  },
  "body": "{\"content\":\"Mi primer post\",\"username\":\"testuser\"}"
}
```

#### Obtener Stream
```json
{
  "httpMethod": "GET",
  "path": "/stream",
  "headers": {
    "Content-Type": "application/json"
  },
  "queryStringParameters": {
    "page": "0",
    "size": "20"
  },
  "body": null
}
```

---

## 🔐 Variables de Entorno

### User Service
```
COGNITO_USER_POOL_ID=us-east-1_LZN3XN9rL
COGNITO_CLIENT_ID=2hsmgtii4k4qq30ekd4nk3pabf
COGNITO_CLIENT_SECRET=13r2cklcsh0k1k7aeol4ukh0rac468a7jtj54i13kuahh90p8uj
COGNITO_REGION=us-east-1
SPRING_DATASOURCE_URL=jdbc:postgresql://54.152.221.17:5433/db3
SPRING_DATASOURCE_USERNAME=admin1
SPRING_DATASOURCE_PASSWORD=pass1
```

### Post Service
```
SPRING_DATASOURCE_URL=jdbc:postgresql://54.152.221.17:5433/db3
SPRING_DATASOURCE_USERNAME=admin1
SPRING_DATASOURCE_PASSWORD=pass1
```

### Stream Service
```
SPRING_DATASOURCE_URL=jdbc:postgresql://54.152.221.17:5433/db3
SPRING_DATASOURCE_USERNAME=admin1
SPRING_DATASOURCE_PASSWORD=pass1
```

---

## 📁 Estructura del Proyecto

```
taller8-arep/
├── user-service/          # Servicio de usuarios y autenticación
│   ├── src/
│   ├── pom.xml
│   ├── create-layer.sh    # Script para crear Lambda Layer
│   ├── deploy-lambda.sh   # Script de despliegue
│   └── test-api.sh        # Script de pruebas
├── posts/                 # Servicio de posts
│   ├── src/
│   ├── pom.xml
│   └── test-api.sh
├── stream/                # Servicio de timeline/feed
│   ├── src/
│   ├── pom.xml
│   └── test-api.sh
├── front/                 # Frontend web
│   ├── login.html
│   ├── home.html
│   ├── register.html
│   ├── verify.html
│   ├── css/
│   └── js/
└── README.md
```

---

## 🔗 Endpoints

### User Service (8081)
- `POST /auth/register` - Registrar usuario
- `POST /auth/login` - Iniciar sesión
- `POST /auth/verify` - Verificar código (2FA)
- `GET /users/{username}` - Obtener perfil
- `PUT /users/{username}` - Actualizar perfil
- `GET /health` - Health check

### Post Service (8083)
- `POST /posts` - Crear post
- `GET /posts` - Listar posts
- `GET /posts/{id}` - Obtener post
- `GET /posts/user/{username}` - Posts de usuario
- `DELETE /posts/{id}` - Eliminar post
- `POST /posts/{id}/like` - Dar like
- `GET /health` - Health check

### Stream Service (8082)
- `GET /stream` - Feed global
- `GET /stream/{username}` - Feed de usuario
- `GET /stream/hashtag/{tag}` - Posts por hashtag
- `GET /stream/search?q=` - Buscar posts
- `GET /stream/trending` - Posts trending
- `GET /health` - Health check

---

## 👤 Autores

**Juan Andres Rodriguez Peñuela**
**Andrés Serrato Camero**
**Juan David Parroquiano Roldan**

- Repositorio: [taller8-arep](https://github.com/andresserrato2004/taller8-arep)

---

## 📄 Licencia

Este proyecto es parte del curso de Arquitecturas Empresariales (AREP).

---

