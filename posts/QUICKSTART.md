# Post Service - Guía de Inicio Rápido

## Descripción

Microservicio dedicado exclusivamente a la gestión de posts de hasta 140 caracteres con autenticación mediante AWS Cognito.

## Prerrequisitos

✅ Antes de comenzar:

- Java 21 instalado
- Maven 3.6+ instalado
- User Service configurado y ejecutándose (puerto 8081)
- AWS Cognito User Pool configurado

## Configuración Rápida

### 1. Configurar Variables de Entorno

```bash
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
export AWS_REGION=us-east-1
```

### 2. Compilar el Proyecto

```bash
cd posts
mvn clean package
```

### 3. Ejecutar Localmente

```bash
mvn spring-boot:run
```

El servicio estará disponible en: `http://localhost:8083`

## Pruebas Rápidas

### Opción 1: Script Automático

```bash
./test-api.sh
```

### Opción 2: curl Manual

#### 1. Login

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
  -d '{"content":"Mi primer post! 🚀"}'
```

#### 3. Ver Posts

```bash
curl http://localhost:8083/api/posts/public
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

## Endpoints Principales

| Método | Endpoint | Auth | Descripción |
|--------|----------|------|-------------|
| POST | `/api/posts` | ✅ | Crear post |
| GET | `/api/posts` | ✅ | Todos los posts |
| GET | `/api/posts/public` | ❌ | Posts (público) |
| GET | `/api/posts/my-posts` | ✅ | Mis posts |
| GET | `/api/posts/{id}` | ✅ | Post por ID |
| PUT | `/api/posts/{id}` | ✅ | Actualizar post |
| DELETE | `/api/posts/{id}` | ✅ | Eliminar post |
| POST | `/api/posts/{id}/like` | ❌ | Dar like |
| GET | `/api/posts/count/user/{userId}` | ❌ | Contar posts |

## Características

- ✅ **CRUD Completo**: Crear, leer, actualizar y eliminar posts
- ✅ **Validación**: Máximo 140 caracteres
- ✅ **Sistema de Likes**: Los posts pueden recibir likes
- ✅ **Contador de Posts**: Estadísticas por usuario
- ✅ **Autenticación JWT**: Integración con AWS Cognito
- ✅ **Endpoints Públicos**: Algunos endpoints no requieren autenticación
- ✅ **Lambda Ready**: Listo para AWS Lambda

## Despliegue en AWS Lambda

```bash
export LAMBDA_ROLE_ARN=arn:aws:iam::ACCOUNT:role/lambda-role
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX

./deploy-lambda.sh
```

## Integración con Otros Servicios

### Arquitectura de Microservicios

```
User Service (8081) ──► AWS Cognito ──► JWT Token
                                          │
                                          ▼
Post Service (8083) ──────────────► Valida JWT
Stream Service (8082) ─────────────► Valida JWT
```

### Puertos

- **User Service**: 8081
- **Stream Service**: 8082
- **Post Service**: 8083

## Base de Datos

### Desarrollo (H2)
- Console: `http://localhost:8083/h2-console`
- JDBC URL: `jdbc:h2:mem:postdb`
- Username: `sa`
- Password: (vacío)

## Ejemplo Completo

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}' \
  | jq -r '.accessToken')

# 2. Crear posts
curl -X POST http://localhost:8083/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Primer post!"}'

curl -X POST http://localhost:8083/api/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Segundo post con emojis! 🎉"}'

# 3. Ver todos los posts
curl http://localhost:8083/api/posts/public | jq

# 4. Dar like al primer post
curl -X POST http://localhost:8083/api/posts/1/like

# 5. Actualizar un post
curl -X PUT http://localhost:8083/api/posts/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Post actualizado! ✨"}'

# 6. Ver mis posts
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8083/api/posts/my-posts | jq
```

## Diferencias con Stream Service

| Característica | Post Service | Stream Service |
|---------------|--------------|----------------|
| **Puerto** | 8083 | 8082 |
| **Funcionalidad** | Solo posts | Posts + Streams |
| **Actualizar** | ✅ | ❌ |
| **Likes** | ✅ | ❌ |
| **Contador** | ✅ | ❌ |

## Solución de Problemas

### Error: "Unauthorized"
- Verifica que el token JWT sea válido
- Verifica COGNITO_USER_POOL_ID

### Error: "Post no puede exceder 140 caracteres"
- El contenido es muy largo
- Acorta el texto a máximo 140 caracteres

### No se puede conectar
- Verifica que el servicio esté ejecutándose
- Verifica que el puerto 8083 esté libre
- Verifica las variables de entorno

## Tecnologías

- Spring Boot 3.5.7
- Spring Security + OAuth2
- Spring Data JPA
- H2 Database
- AWS Cognito
- AWS Lambda
- Lombok
- Maven

## Próximos Pasos

1. ✅ Post Service implementado
2. ⏳ Desplegar en AWS Lambda
3. ⏳ Configurar API Gateway
4. ⏳ Integrar con frontend en S3
5. ⏳ Implementar comentarios
6. ⏳ Sistema de hashtags
7. ⏳ Búsqueda de posts

---

**¡Post Service está listo! 🚀**
