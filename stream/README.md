# Stream Service

Microservicio para gestionar posts y streams (hilos) de hasta 140 caracteres, similar a Twitter.

## Características

- **Posts**: Crear, leer y eliminar posts de hasta 140 caracteres
- **Streams**: Visualizar hilos de posts organizados
- **Autenticación**: Integración con AWS Cognito (manejado por user-service)
- **JWT**: Validación de tokens JWT de Cognito
- **AWS Lambda**: Preparado para despliegue serverless

## Arquitectura

### Entidades

1. **Post**
   - id: Long
   - content: String (máx. 140 caracteres)
   - userId: String (del token JWT)
   - username: String
   - createdAt: LocalDateTime

2. **Stream**
   - id: Long
   - name: String
   - description: String
   - posts: List<Post>
   - createdAt: LocalDateTime

### Seguridad

El servicio valida tokens JWT emitidos por AWS Cognito. El user-service es el responsable de:
- Registro de usuarios
- Login
- Emisión de tokens JWT

Este servicio (stream-service) solo:
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

#### Eliminar Post (Solo el autor)
```bash
DELETE /api/posts/{id}
Authorization: Bearer <JWT_TOKEN>
```

### Streams

#### Crear Stream (Autenticado)
```bash
POST /api/streams
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "name": "tech-news",
  "description": "Noticias de tecnología"
}
```

#### Obtener Todos los Streams
```bash
# Autenticado
GET /api/streams
Authorization: Bearer <JWT_TOKEN>

# Público
GET /api/streams/public
```

#### Obtener Stream por Nombre
```bash
GET /api/streams/{name}
Authorization: Bearer <JWT_TOKEN>
```

### Health Check
```bash
GET /health
```

## Configuración

### Variables de Entorno

Crear un archivo `.env` o configurar las siguientes variables:

```properties
# AWS Cognito
COGNITO_USER_POOL_ID=your-user-pool-id
AWS_REGION=us-east-1

# Server
SERVER_PORT=8082
```

### application.properties

Las propiedades de configuración se encuentran en `src/main/resources/application.properties`:

```properties
spring.application.name=stream-service
server.port=8082

# AWS Cognito Configuration
aws.cognito.userPoolId=${COGNITO_USER_POOL_ID:your-user-pool-id}
aws.cognito.region=${AWS_REGION:us-east-1}

# Database Configuration (H2 for development)
spring.datasource.url=jdbc:h2:mem:streamdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
```

## Ejecución Local

### Prerrequisitos
- Java 21
- Maven 3.6+
- Cognito User Pool configurado (ver user-service)

### Compilar
```bash
cd stream
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

La aplicación estará disponible en `http://localhost:8082`

## Despliegue en AWS Lambda

### 1. Compilar JAR
```bash
mvn clean package
```

### 2. Crear función Lambda
```bash
aws lambda create-function \
  --function-name stream-service \
  --runtime java21 \
  --handler com.example.stream.StreamLambdaHandler::handleRequest \
  --role arn:aws:iam::YOUR_ACCOUNT:role/lambda-execution-role \
  --zip-file fileb://target/stream-0.0.1-SNAPSHOT.jar \
  --memory-size 512 \
  --timeout 30 \
  --environment Variables="{COGNITO_USER_POOL_ID=your-pool-id,AWS_REGION=us-east-1}"
```

### 3. Configurar API Gateway
```bash
# Crear API REST
aws apigateway create-rest-api \
  --name stream-service-api \
  --description "API for Stream Service"

# Configurar integración con Lambda
# ... (ver documentación de API Gateway)
```

## Integración con User Service

El stream-service depende del user-service para la autenticación:

1. **User Service** (puerto 8081):
   - Maneja registro y login
   - Emite tokens JWT de Cognito
   - Gestiona usuarios

2. **Stream Service** (puerto 8082):
   - Valida tokens JWT
   - Gestiona posts y streams
   - Usa información del usuario del token

### Flujo de Autenticación

```
Cliente → User Service (login) → Cognito → JWT Token
Cliente → Stream Service (con JWT) → Valida con Cognito → Acceso
```

## Testing

### Probar con curl

#### 1. Login (via user-service)
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password123!"
  }'
```

Respuesta:
```json
{
  "accessToken": "eyJraWQiOiI...",
  "idToken": "eyJraWQiOiI...",
  "refreshToken": "eyJjdH...",
  "expiresIn": 3600
}
```

#### 2. Crear Post
```bash
curl -X POST http://localhost:8082/api/posts \
  -H "Authorization: Bearer eyJraWQiOiI..." \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Mi primer post en el stream!"
  }'
```

#### 3. Obtener Posts
```bash
# Todos los posts (público)
curl http://localhost:8082/api/posts/public

# Mis posts (autenticado)
curl http://localhost:8082/api/posts/my-posts \
  -H "Authorization: Bearer eyJraWQiOiI..."
```

## Base de Datos

En desarrollo se usa H2 (en memoria). Para producción, se recomienda:
- Amazon RDS (PostgreSQL o MySQL)
- Amazon DynamoDB

### Migrar a RDS

1. Agregar dependencia en `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. Configurar en `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://your-rds-endpoint:5432/streamdb
spring.datasource.username=dbuser
spring.datasource.password=dbpassword
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## Tecnologías

- **Spring Boot 3.5.7**: Framework principal
- **Spring Security**: Seguridad y JWT
- **Spring Data JPA**: ORM
- **H2 Database**: Base de datos en memoria (desarrollo)
- **AWS Cognito**: Autenticación
- **AWS Lambda**: Despliegue serverless
- **Lombok**: Reducción de boilerplate
- **Maven**: Gestión de dependencias

## Estructura del Proyecto

```
stream/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/stream/
│   │   │       ├── StreamApplication.java
│   │   │       ├── StreamLambdaHandler.java
│   │   │       ├── config/
│   │   │       │   └── SecurityConfig.java
│   │   │       ├── controller/
│   │   │       │   ├── PostController.java
│   │   │       │   ├── StreamController.java
│   │   │       │   └── HealthController.java
│   │   │       ├── dto/
│   │   │       │   ├── PostRequest.java
│   │   │       │   ├── PostResponse.java
│   │   │       │   └── StreamResponse.java
│   │   │       ├── model/
│   │   │       │   ├── Post.java
│   │   │       │   └── Stream.java
│   │   │       ├── repository/
│   │   │       │   ├── PostRepository.java
│   │   │       │   └── StreamRepository.java
│   │   │       └── service/
│   │   │           ├── PostService.java
│   │   │           └── StreamService.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

## Próximos Pasos

1. ✅ Implementar servicio básico con Cognito
2. ⏳ Desplegar en AWS Lambda
3. ⏳ Configurar API Gateway
4. ⏳ Integrar con frontend en S3
5. ⏳ Implementar base de datos RDS
6. ⏳ Agregar caché con Redis
7. ⏳ Implementar streaming en tiempo real (WebSocket)

## Soporte

Para problemas o preguntas, contacta al equipo de desarrollo.
