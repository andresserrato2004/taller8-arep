# Post Service

Microservicio dedicado exclusivamente a la gestión de posts de hasta 140 caracteres.

## Características

- **CRUD de Posts**: Crear, leer, actualizar y eliminar posts
- **Validación**: Posts de máximo 140 caracteres
- **Likes**: Sistema de likes para posts
- **Contador de Posts**: Estadísticas por usuario
- **Autenticación**: Integración con AWS Cognito
- **JWT**: Validación de tokens JWT de Cognito
- **AWS Lambda**: Preparado para despliegue serverless

## Arquitectura

### Entidad Post

- **id**: Long
- **content**: String (máx. 140 caracteres)
- **userId**: String (del token JWT)
- **username**: String
- **createdAt**: LocalDateTime
- **likeCount**: Integer
- **commentCount**: Integer

### Seguridad

El servicio valida tokens JWT emitidos por AWS Cognito. El user-service maneja:
- Registro de usuarios
- Login
- Emisión de tokens JWT

Este servicio (post-service):
- Valida tokens JWT
- Extrae información del usuario del token
- Protege endpoints que requieren autenticación

## API Endpoints

### Posts

#### Crear Post (Autenticado)
```bash
POST /api/posts
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "content": "Este es mi post de máximo 140 caracteres"
}
```

#### Obtener Todos los Posts
```bash
# Autenticado
GET /api/posts
Authorization: Bearer <JWT_TOKEN>

# Público
GET /api/posts/public
```

#### Obtener Posts por Usuario
```bash
GET /api/posts/user/{userId}
Authorization: Bearer <JWT_TOKEN>
```

#### Obtener Mis Posts
```bash
GET /api/posts/my-posts
Authorization: Bearer <JWT_TOKEN>
```

#### Obtener Post por ID
```bash
GET /api/posts/{id}
Authorization: Bearer <JWT_TOKEN>
```

#### Actualizar Post (Solo el autor)
```bash
PUT /api/posts/{id}
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "content": "Contenido actualizado"
}
```

#### Eliminar Post (Solo el autor)
```bash
DELETE /api/posts/{id}
Authorization: Bearer <JWT_TOKEN>
```

#### Dar Like a un Post (Público)
```bash
POST /api/posts/{id}/like
```

#### Contar Posts de Usuario
```bash
GET /api/posts/count/user/{userId}
```

### Health Check
```bash
GET /health
```

## Configuración

### Variables de Entorno

```properties
# AWS Cognito
COGNITO_USER_POOL_ID=your-user-pool-id
AWS_REGION=us-east-1

# Server
SERVER_PORT=8083
```

### application.properties

```properties
spring.application.name=post-service
server.port=8083

# AWS Cognito Configuration
aws.cognito.userPoolId=${COGNITO_USER_POOL_ID:your-user-pool-id}
aws.cognito.region=${AWS_REGION:us-east-1}

# Database Configuration (H2 for development)
spring.datasource.url=jdbc:h2:mem:postdb
```

## Ejecución Local

### Prerrequisitos
- Java 21
- Maven 3.6+
- Cognito User Pool configurado

### Compilar
```bash
cd posts
mvn clean package
```

### Ejecutar
```bash
# Configurar variables de entorno
export COGNITO_USER_POOL_ID=your-pool-id
export AWS_REGION=us-east-1

# Ejecutar aplicación
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8083`

## Despliegue en AWS Lambda

### 1. Compilar JAR
```bash
mvn clean package
```

### 2. Desplegar
```bash
export LAMBDA_ROLE_ARN=arn:aws:iam::ACCOUNT:role/lambda-role
export COGNITO_USER_POOL_ID=your-pool-id

./deploy-lambda.sh
```

## Testing

### Probar con curl

#### 1. Login (via user-service)
```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"Password123!"}' \
  | jq -r '.accessToken')
```

#### 2. Crear Post
```bash
curl -X POST http://localhost:8083/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Mi primer post en post-service!"}'
```

#### 3. Ver Posts
```bash
# Público
curl http://localhost:8083/api/posts/public

# Mis posts
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8083/api/posts/my-posts
```

#### 4. Dar Like
```bash
curl -X POST http://localhost:8083/api/posts/1/like
```

#### 5. Actualizar Post
```bash
curl -X PUT http://localhost:8083/api/posts/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Post actualizado!"}'
```

## Integración con Otros Servicios

### User Service (8081)
- Maneja autenticación
- Emite tokens JWT

### Post Service (8083)
- Gestiona posts
- Valida tokens JWT

### Stream Service (8082)
- Gestiona hilos/streams
- Puede consumir datos del Post Service

## Tecnologías

- **Spring Boot 3.5.7**: Framework principal
- **Spring Security**: Seguridad y OAuth2
- **Spring Data JPA**: Persistencia
- **H2 Database**: Base de datos en memoria
- **AWS Cognito**: Autenticación
- **AWS Lambda**: Despliegue serverless
- **Lombok**: Reducción de boilerplate
- **Maven**: Gestión de dependencias

## Base de Datos

### Desarrollo (H2)
Console disponible en: `http://localhost:8083/h2-console`
- JDBC URL: `jdbc:h2:mem:postdb`
- Username: `sa`
- Password: (vacío)

### Producción (RDS)
Configurar en `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://endpoint:5432/postdb
spring.datasource.username=dbuser
spring.datasource.password=dbpassword
```

## Estructura del Proyecto

```
posts/
├── src/main/java/com/example/posts/
│   ├── PostsApplication.java
│   ├── PostsLambdaHandler.java
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── PostController.java
│   │   └── HealthController.java
│   ├── dto/
│   │   ├── PostRequest.java
│   │   └── PostResponse.java
│   ├── model/
│   │   └── Post.java
│   ├── repository/
│   │   └── PostRepository.java
│   └── service/
│       └── PostService.java
├── src/main/resources/
│   └── application.properties
├── pom.xml
├── README.md
├── QUICKSTART.md
└── deploy-lambda.sh
```

## Diferencias con Stream Service

| Característica | Post Service | Stream Service |
|---------------|--------------|----------------|
| Puerto | 8083 | 8082 |
| Funcionalidad | Solo posts | Posts + Streams |
| Entidades | Post | Post, Stream |
| Endpoints | 10 | 13 |
| Features | CRUD, Likes, Count | CRUD, Agrupación |

## Próximos Pasos

1. ✅ Post Service implementado
2. ⏳ Desplegar en AWS Lambda
3. ⏳ Configurar API Gateway
4. ⏳ Integrar con frontend
5. ⏳ Migrar a RDS
6. ⏳ Implementar comentarios
7. ⏳ Agregar búsqueda de posts
8. ⏳ Sistema de hashtags

---

**Post Service - Microservicio Independiente de Posts** 🚀
