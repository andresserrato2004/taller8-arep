# Integración Stream Service con User Service y AWS Cognito

## 📋 Resumen

El **Stream Service** ha sido implementado exitosamente con integración completa de AWS Cognito para autenticación y autorización. El servicio permite crear posts de hasta 140 caracteres y gestionar streams (hilos) de posts.

## 🏗️ Arquitectura

```
┌─────────────┐
│   Cliente   │
│  (Frontend) │
└──────┬──────┘
       │
       ▼
┌──────────────────────────────────────┐
│         User Service (8081)          │
│  - Registro de usuarios              │
│  - Login                             │
│  - Emisión de tokens JWT             │
└──────────────┬───────────────────────┘
               │
               ▼
       ┌──────────────┐
       │ AWS Cognito  │ ◄───────────┐
       │  User Pool   │             │
       └──────────────┘             │
               │                    │
               │ Valida JWT         │
               ▼                    │
┌──────────────────────────────────┐ │
│     Stream Service (8082)        │ │
│  - Crear posts                   │ │
│  - Gestionar streams             │ │
│  - Validar tokens JWT ───────────┘ │
└──────────────┬───────────────────┘
               │
               ▼
       ┌──────────────┐
       │ Base de Datos│
       │   H2/RDS     │
       └──────────────┘
```

## ✅ Componentes Implementados

### 1. Entidades
- ✅ **Post**: Posts de hasta 140 caracteres
  - id, content, userId, username, createdAt
- ✅ **Stream**: Hilos de posts
  - id, name, description, posts[], createdAt

### 2. DTOs
- ✅ PostRequest
- ✅ PostResponse
- ✅ StreamResponse

### 3. Repositorios
- ✅ PostRepository
- ✅ StreamRepository

### 4. Servicios
- ✅ PostService: Lógica de negocio para posts
- ✅ StreamService: Lógica de negocio para streams

### 5. Controladores
- ✅ PostController: Endpoints para posts
- ✅ StreamController: Endpoints para streams
- ✅ HealthController: Health check

### 6. Seguridad
- ✅ SecurityConfig: Integración con AWS Cognito
- ✅ Validación JWT
- ✅ Extracción de información del usuario

### 7. AWS Lambda
- ✅ StreamLambdaHandler: Handler para Lambda
- ✅ Dependencias para serverless

### 8. Documentación
- ✅ README.md completo
- ✅ QUICKSTART.md
- ✅ Scripts de despliegue
- ✅ Scripts de testing
- ✅ Colección de Postman

## 🔐 Flujo de Autenticación

### 1. Login
```bash
# Cliente hace login en User Service
POST http://localhost:8081/api/auth/login
{
  "email": "user@example.com",
  "password": "Password123!"
}

# Respuesta
{
  "accessToken": "eyJraWQiOiI...",
  "idToken": "eyJraWQiOiI...",
  "refreshToken": "eyJjdH...",
  "expiresIn": 3600
}
```

### 2. Usar Stream Service
```bash
# Cliente usa el accessToken para crear un post
POST http://localhost:8082/api/posts
Authorization: Bearer eyJraWQiOiI...
{
  "content": "Mi primer post!"
}
```

### 3. Validación
```
1. Stream Service recibe la petición con JWT
2. SecurityConfig valida el JWT con Cognito
3. Extrae userId y username del JWT
4. Permite la operación si el JWT es válido
```

## 🚀 Cómo Usar

### Paso 1: Configurar Variables de Entorno

```bash
# Desde el user-service, obtén:
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
export AWS_REGION=us-east-1
```

### Paso 2: Ejecutar User Service

```bash
cd user-service
mvn spring-boot:run
# Ejecutándose en http://localhost:8081
```

### Paso 3: Ejecutar Stream Service

```bash
cd stream
mvn spring-boot:run
# Ejecutándose en http://localhost:8082
```

### Paso 4: Probar la Integración

#### Opción A: Script Automático
```bash
cd stream
./test-api.sh
```

#### Opción B: Postman
1. Importa `stream/postman_collection.json`
2. Ejecuta "Login (User Service)"
3. El token se guarda automáticamente
4. Prueba los demás endpoints

#### Opción C: Manualmente

**1. Registrar usuario (si no existe)**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "name": "Test User"
  }'
```

**2. Login**
```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }' | jq -r '.accessToken')

echo $TOKEN
```

**3. Crear post**
```bash
curl -X POST http://localhost:8082/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Este es mi primer post! 🚀"
  }'
```

**4. Ver todos los posts**
```bash
# Público (sin autenticación)
curl http://localhost:8082/api/posts/public

# Autenticado
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8082/api/posts
```

**5. Ver mis posts**
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8082/api/posts/my-posts
```

**6. Crear stream**
```bash
curl -X POST http://localhost:8082/api/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "tech-news",
    "description": "Noticias de tecnología"
  }'
```

## 📊 Endpoints Disponibles

### User Service (8081)
| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/auth/register` | POST | Registrar usuario |
| `/api/auth/login` | POST | Login |
| `/api/auth/verify` | GET | Verificar token |
| `/health` | GET | Health check |

### Stream Service (8082)
| Endpoint | Método | Auth | Descripción |
|----------|--------|------|-------------|
| `/api/posts` | POST | ✅ | Crear post |
| `/api/posts` | GET | ✅ | Obtener todos los posts |
| `/api/posts/public` | GET | ❌ | Obtener posts (público) |
| `/api/posts/my-posts` | GET | ✅ | Mis posts |
| `/api/posts/{id}` | GET | ✅ | Post por ID |
| `/api/posts/{id}` | DELETE | ✅ | Eliminar post |
| `/api/streams` | POST | ✅ | Crear stream |
| `/api/streams` | GET | ✅ | Obtener streams |
| `/api/streams/public` | GET | ❌ | Obtener streams (público) |
| `/api/streams/{name}` | GET | ✅ | Stream por nombre |
| `/health` | GET | ❌ | Health check |

## 🎯 Validaciones Implementadas

### Posts
- ✅ Contenido no vacío
- ✅ Máximo 140 caracteres
- ✅ Solo el autor puede eliminar sus posts
- ✅ userId y username extraídos del JWT

### Streams
- ✅ Nombre único
- ✅ Posts ordenados por fecha descendente

### Seguridad
- ✅ Validación de JWT con Cognito
- ✅ Endpoints públicos y protegidos
- ✅ Extracción automática de información del usuario
- ✅ CORS configurado

## 🔧 Configuración

### application.properties
```properties
spring.application.name=stream-service
server.port=8082

# AWS Cognito
aws.cognito.userPoolId=${COGNITO_USER_POOL_ID}
aws.cognito.region=${AWS_REGION:us-east-1}

# Database (H2 para desarrollo)
spring.datasource.url=jdbc:h2:mem:streamdb
spring.jpa.hibernate.ddl-auto=update
```

### SecurityConfig
```java
@Bean
public JwtDecoder jwtDecoder() {
    String jwkSetUri = String.format(
        "https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json",
        region, userPoolId
    );
    return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
}
```

## 🚀 Despliegue en AWS Lambda

### 1. Compilar
```bash
cd stream
mvn clean package
```

### 2. Desplegar
```bash
export LAMBDA_ROLE_ARN=arn:aws:iam::ACCOUNT:role/lambda-role
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX

./deploy-lambda.sh
```

### 3. Configurar API Gateway
1. Crear API REST en AWS Console
2. Crear recursos para los endpoints
3. Integrar con Lambda function
4. Habilitar CORS
5. Desplegar la API

## 📝 Ejemplo Completo de Uso

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}' \
  | jq -r '.accessToken')

# 2. Crear varios posts
for i in {1..5}; do
  curl -X POST http://localhost:8082/api/posts \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"content\":\"Post número $i - $(date +%H:%M:%S)\"}"
  sleep 1
done

# 3. Ver todos los posts
curl http://localhost:8082/api/posts/public | jq

# 4. Ver mis posts
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8082/api/posts/my-posts | jq

# 5. Crear un stream
curl -X POST http://localhost:8082/api/streams \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"general","description":"Stream general"}'

# 6. Ver streams
curl http://localhost:8082/api/streams/public | jq
```

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
./test-api.sh
```

### Manual Testing
Usa Postman con la colección incluida: `postman_collection.json`

## 📚 Tecnologías Utilizadas

- **Spring Boot 3.5.7**: Framework principal
- **Spring Security**: Seguridad y OAuth2
- **Spring Data JPA**: Persistencia
- **H2 Database**: Base de datos en memoria (desarrollo)
- **AWS Cognito**: Autenticación
- **AWS Lambda**: Despliegue serverless
- **Lombok**: Reducción de boilerplate
- **Maven**: Gestión de dependencias

## 🔄 Próximos Pasos

1. ✅ Stream Service implementado
2. ⏳ Desplegar en AWS Lambda
3. ⏳ Configurar API Gateway
4. ⏳ Integrar frontend en S3
5. ⏳ Migrar a base de datos RDS
6. ⏳ Implementar caché con Redis
7. ⏳ Agregar WebSocket para tiempo real
8. ⏳ Implementar búsqueda de posts
9. ⏳ Agregar likes y comentarios
10. ⏳ Implementar notificaciones

## 🐛 Troubleshooting

### Error: "Unauthorized"
- Verifica que el token sea válido
- Verifica que COGNITO_USER_POOL_ID sea correcto
- Verifica que el token no haya expirado

### Error: "Post no puede exceder 140 caracteres"
- El contenido del post es muy largo
- Acorta el texto

### No se puede conectar al servicio
- Verifica que el servicio esté ejecutándose
- Verifica que el puerto 8082 esté libre
- Verifica las variables de entorno

### Error de compilación
```bash
mvn clean install -U
```

## 📞 Soporte

Para más información, consulta:
- [README.md](README.md) - Documentación completa
- [QUICKSTART.md](QUICKSTART.md) - Guía de inicio rápido
- [User Service](../user-service/README.md) - Documentación del User Service

---

**¡Stream Service está listo para usar! 🎉**
