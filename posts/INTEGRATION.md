# Post Service - Documentación de Integración

## 📋 Resumen

El **Post Service** ha sido implementado exitosamente como un microservicio independiente con integración completa de AWS Cognito para autenticación. Este servicio se especializa exclusivamente en la gestión de posts de hasta 140 caracteres.

## 🏗️ Arquitectura de Microservicios

```
┌──────────────┐
│   Cliente    │
│  (Frontend)  │
└──────┬───────┘
       │
       ▼
┌─────────────────────────────────┐
│    User Service (8081)          │
│  - Registro de usuarios         │
│  - Login                        │
│  - Emisión de tokens JWT        │
└──────────────┬──────────────────┘
               │
               ▼
       ┌──────────────┐
       │ AWS Cognito  │
       │  User Pool   │
       └──────┬───────┘
              │ Valida JWT
              ├──────────────────┐
              │                  │
              ▼                  ▼
┌─────────────────────┐ ┌──────────────────────┐
│ Post Service (8083) │ │ Stream Service(8082) │
│  - CRUD Posts       │ │  - Gestionar Streams │
│  - Likes            │ │  - Posts en Streams  │
│  - Contador         │ │                      │
└──────────┬──────────┘ └──────────┬───────────┘
           │                       │
           ▼                       ▼
   ┌──────────────┐       ┌──────────────┐
   │ DB Posts H2  │       │ DB Stream H2 │
   └──────────────┘       └──────────────┘
```

## ✅ Componentes Implementados

### 1. Entidad Post
```java
@Entity
public class Post {
    private Long id;
    private String content;        // Máx 140 caracteres
    private String userId;
    private String username;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;
}
```

### 2. DTOs
- ✅ `PostRequest`: Para crear/actualizar posts
- ✅ `PostResponse`: Respuesta con datos del post

### 3. Repositorio
- ✅ `PostRepository`: Operaciones CRUD + consultas personalizadas

### 4. Servicio
- ✅ `PostService`: Lógica de negocio completa
  - Crear posts
  - Obtener posts (todos, por usuario, por ID)
  - Actualizar posts
  - Eliminar posts
  - Sistema de likes
  - Contador de posts

### 5. Controlador
- ✅ `PostController`: 10 endpoints REST
- ✅ `HealthController`: Health check

### 6. Seguridad
- ✅ `SecurityConfig`: Integración con AWS Cognito
- ✅ Validación JWT
- ✅ Endpoints públicos y protegidos

### 7. AWS Lambda
- ✅ `PostsLambdaHandler`: Handler para Lambda
- ✅ Dependencias configuradas

### 8. Documentación y Scripts
- ✅ README.md completo
- ✅ QUICKSTART.md
- ✅ deploy-lambda.sh
- ✅ test-api.sh
- ✅ postman_collection.json

## 🔐 Flujo de Autenticación

### 1. Obtener Token
```bash
# Usuario hace login en User Service
POST http://localhost:8081/api/auth/login
{
  "email": "user@example.com",
  "password": "Password123!"
}

# Respuesta con JWT
{
  "accessToken": "eyJraWQiOiI...",
  "idToken": "eyJraWQiOiI...",
  "expiresIn": 3600
}
```

### 2. Usar Post Service
```bash
# Crear post con JWT
POST http://localhost:8083/api/posts
Authorization: Bearer eyJraWQiOiI...
{
  "content": "Mi primer post!"
}
```

### 3. Validación Automática
1. Post Service recibe JWT
2. SecurityConfig valida con Cognito
3. Extrae userId y username
4. Permite la operación

## 📊 Comparación de Microservicios

| Característica | User Service | Post Service | Stream Service |
|---------------|--------------|--------------|----------------|
| **Puerto** | 8081 | 8083 | 8082 |
| **Función Principal** | Autenticación | Gestión de Posts | Gestión de Streams |
| **Maneja Cognito** | ✅ Sí | ❌ No | ❌ No |
| **Valida JWT** | ✅ Sí | ✅ Sí | ✅ Sí |
| **Base de Datos** | userdb (H2) | postdb (H2) | streamdb (H2) |
| **Entidades** | User | Post | Post, Stream |
| **CRUD Posts** | ❌ | ✅ | ✅ |
| **CRUD Streams** | ❌ | ❌ | ✅ |
| **Actualizar Posts** | ❌ | ✅ | ❌ |
| **Sistema de Likes** | ❌ | ✅ | ❌ |
| **Contador Posts** | ❌ | ✅ | ❌ |

## 🚀 Endpoints Disponibles

### User Service (8081)
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/auth/register` | POST | Registrar usuario |
| `/api/auth/login` | POST | Login |
| `/api/auth/verify` | GET | Verificar token |
| `/health` | GET | Health check |

### Post Service (8083)
| Endpoint | Método | Auth | Descripción |
|----------|--------|------|-------------|
| `/api/posts` | POST | ✅ | Crear post |
| `/api/posts` | GET | ✅ | Todos los posts |
| `/api/posts/public` | GET | ❌ | Posts (público) |
| `/api/posts/my-posts` | GET | ✅ | Mis posts |
| `/api/posts/user/{userId}` | GET | ✅ | Posts por usuario |
| `/api/posts/{id}` | GET | ✅ | Post por ID |
| `/api/posts/{id}` | PUT | ✅ | Actualizar post |
| `/api/posts/{id}` | DELETE | ✅ | Eliminar post |
| `/api/posts/{id}/like` | POST | ❌ | Dar like |
| `/api/posts/count/user/{userId}` | GET | ❌ | Contar posts |
| `/health` | GET | ❌ | Health check |

### Stream Service (8082)
| Endpoint | Método | Auth | Descripción |
|----------|--------|------|-------------|
| `/api/posts` | POST | ✅ | Crear post |
| `/api/posts/public` | GET | ❌ | Ver posts |
| `/api/streams` | POST | ✅ | Crear stream |
| `/api/streams/public` | GET | ❌ | Ver streams |
| `/health` | GET | ❌ | Health check |

## 🎯 Casos de Uso

### Caso 1: Usuario Publica un Post

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Pass123!"}' \
  | jq -r '.accessToken')

# 2. Crear post en Post Service
curl -X POST http://localhost:8083/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"¡Hola mundo desde microservicios!"}'
```

### Caso 2: Usuarios Interactúan con Posts

```bash
# Usuario A crea un post
curl -X POST http://localhost:8083/api/posts \
  -H "Authorization: Bearer $TOKEN_A" \
  -d '{"content":"Post de Usuario A"}'

# Usuario B ve todos los posts (público)
curl http://localhost:8083/api/posts/public

# Usuario B da like al post
curl -X POST http://localhost:8083/api/posts/1/like

# Usuario A actualiza su post
curl -X PUT http://localhost:8083/api/posts/1 \
  -H "Authorization: Bearer $TOKEN_A" \
  -d '{"content":"Post actualizado por Usuario A"}'
```

### Caso 3: Estadísticas de Usuario

```bash
# Obtener cantidad de posts de un usuario
curl http://localhost:8083/api/posts/count/user/{userId}

# Ver todos los posts de un usuario
curl http://localhost:8083/api/posts/user/{userId}
```

## 🔧 Configuración Completa

### Variables de Entorno Necesarias

```bash
# User Service
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
export COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxx
export COGNITO_CLIENT_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
export AWS_REGION=us-east-1

# Post Service
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
export AWS_REGION=us-east-1

# Stream Service
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
export AWS_REGION=us-east-1
```

### Ejecución de los Tres Servicios

```bash
# Terminal 1: User Service
cd user-service
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
export COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxx
export COGNITO_CLIENT_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
mvn spring-boot:run

# Terminal 2: Post Service
cd posts
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
mvn spring-boot:run

# Terminal 3: Stream Service
cd stream
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
mvn spring-boot:run
```

## 🧪 Pruebas de Integración Completa

```bash
# 1. Verificar que todos los servicios estén activos
curl http://localhost:8081/health  # User Service
curl http://localhost:8082/health  # Stream Service
curl http://localhost:8083/health  # Post Service

# 2. Registrar usuario
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "name": "Test User"
  }'

# 3. Login
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}' \
  | jq -r '.accessToken')

# 4. Crear posts en Post Service
curl -X POST http://localhost:8083/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Post desde Post Service!"}'

# 5. Crear posts en Stream Service
curl -X POST http://localhost:8082/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Post desde Stream Service!"}'

# 6. Ver posts en Post Service
curl http://localhost:8083/api/posts/public | jq

# 7. Ver posts en Stream Service
curl http://localhost:8082/api/posts/public | jq

# 8. Dar like a un post
curl -X POST http://localhost:8083/api/posts/1/like

# 9. Crear un stream
curl -X POST http://localhost:8082/api/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"tech","description":"Tech posts"}'

# 10. Ver streams
curl http://localhost:8082/api/streams/public | jq
```

## 🚀 Despliegue en AWS Lambda

### Desplegar los Tres Servicios

```bash
# Configurar variables
export LAMBDA_ROLE_ARN=arn:aws:iam::ACCOUNT:role/lambda-execution-role
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
export AWS_REGION=us-east-1

# Desplegar User Service
cd user-service
./deploy-lambda.sh

# Desplegar Post Service
cd ../posts
./deploy-lambda.sh

# Desplegar Stream Service
cd ../stream
./deploy-lambda.sh
```

## 📝 Características Únicas del Post Service

### 1. Actualización de Posts
Solo el Post Service permite actualizar posts:
```bash
PUT /api/posts/{id}
```

### 2. Sistema de Likes
Solo el Post Service implementa likes:
```bash
POST /api/posts/{id}/like
```

### 3. Contador de Posts
Solo el Post Service cuenta posts por usuario:
```bash
GET /api/posts/count/user/{userId}
```

### 4. Base de Datos Independiente
- Post Service: `jdbc:h2:mem:postdb`
- Stream Service: `jdbc:h2:mem:streamdb`
- User Service: `jdbc:h2:mem:userdb`

## 🔄 Integración Frontend

```javascript
// Login
const loginResponse = await fetch('http://localhost:8081/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});
const { accessToken } = await loginResponse.json();

// Crear post en Post Service
const postResponse = await fetch('http://localhost:8083/api/posts', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ content: 'Mi post!' })
});

// Ver posts públicos
const posts = await fetch('http://localhost:8083/api/posts/public')
  .then(r => r.json());

// Dar like
await fetch(`http://localhost:8083/api/posts/${postId}/like`, {
  method: 'POST'
});
```

## 📚 Tecnologías Utilizadas

- **Spring Boot 3.5.7**: Framework principal
- **Spring Security**: Seguridad y OAuth2
- **Spring Data JPA**: Persistencia
- **H2 Database**: Base de datos en memoria
- **AWS Cognito**: Autenticación
- **AWS Lambda**: Despliegue serverless
- **Lombok**: Reducción de boilerplate
- **Maven**: Gestión de dependencias

## 🎉 Estado Actual

### ✅ Implementado
1. **User Service**: Autenticación completa con Cognito
2. **Post Service**: CRUD, Likes, Contador
3. **Stream Service**: Posts + Streams
4. Integración JWT en los 3 servicios
5. Documentación completa
6. Scripts de despliegue y testing
7. Colecciones de Postman

### ⏳ Próximos Pasos
1. Desplegar en AWS Lambda
2. Configurar API Gateway
3. Integrar frontend en S3
4. Migrar a RDS
5. Implementar comentarios
6. Sistema de hashtags
7. Búsqueda de posts
8. WebSocket para tiempo real

---

**¡Los tres microservicios están listos y funcionando! 🚀**

User Service (8081) + Post Service (8083) + Stream Service (8082) = Arquitectura de Microservicios Completa
