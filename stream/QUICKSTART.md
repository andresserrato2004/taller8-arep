# Stream Service - Guía de Inicio Rápido

## Descripción

Microservicio para gestionar posts de hasta 140 caracteres y streams (hilos), con autenticación mediante AWS Cognito.

## Prerrequisitos

✅ Antes de comenzar, asegúrate de tener:

- Java 21 instalado
- Maven 3.6+ instalado
- User Service configurado y ejecutándose (puerto 8081)
- AWS Cognito User Pool configurado (ver user-service)

## Configuración Rápida

### 1. Configurar Variables de Entorno

Crea un archivo `.env` o exporta las variables:

```bash
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
export AWS_REGION=us-east-1
```

### 2. Compilar el Proyecto

```bash
cd stream
mvn clean package
```

### 3. Ejecutar Localmente

```bash
mvn spring-boot:run
```

El servicio estará disponible en: `http://localhost:8082`

## Pruebas Rápidas

### Opción 1: Script Automático

```bash
./test-api.sh
```

### Opción 2: Postman

1. Importa `postman_collection.json` en Postman
2. Ejecuta "Login (User Service)" para obtener el token
3. Prueba los demás endpoints

### Opción 3: curl Manual

#### 1. Login (via user-service)

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tu-email@example.com",
    "password": "TuPassword123!"
  }'
```

Guarda el `accessToken` de la respuesta.

#### 2. Crear un Post

```bash
curl -X POST http://localhost:8082/api/posts \
  -H "Authorization: Bearer TU_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Mi primer post! 🚀"
  }'
```

#### 3. Ver Todos los Posts

```bash
curl http://localhost:8082/api/posts/public
```

## Endpoints Principales

### Posts

| Método | Endpoint | Autenticación | Descripción |
|--------|----------|---------------|-------------|
| POST | `/api/posts` | ✅ Requerida | Crear post |
| GET | `/api/posts` | ✅ Requerida | Obtener todos los posts |
| GET | `/api/posts/public` | ❌ Pública | Obtener todos los posts |
| GET | `/api/posts/my-posts` | ✅ Requerida | Obtener mis posts |
| GET | `/api/posts/{id}` | ✅ Requerida | Obtener post por ID |
| DELETE | `/api/posts/{id}` | ✅ Requerida | Eliminar post (solo autor) |

### Streams

| Método | Endpoint | Autenticación | Descripción |
|--------|----------|---------------|-------------|
| POST | `/api/streams` | ✅ Requerida | Crear stream |
| GET | `/api/streams` | ✅ Requerida | Obtener todos los streams |
| GET | `/api/streams/public` | ❌ Pública | Obtener todos los streams |
| GET | `/api/streams/{name}` | ✅ Requerida | Obtener stream por nombre |

### Health

| Método | Endpoint | Autenticación | Descripción |
|--------|----------|---------------|-------------|
| GET | `/health` | ❌ Pública | Estado del servicio |

## Reglas de Negocio

### Posts

- ✅ Máximo 140 caracteres
- ✅ No pueden estar vacíos
- ✅ Solo el autor puede eliminar sus posts
- ✅ Se registra el userId y username del autor

### Streams

- ✅ Nombre único
- ✅ Contienen colección de posts ordenados por fecha

## Despliegue en AWS Lambda

### Preparar Despliegue

1. Configura las credenciales de AWS:

```bash
aws configure
```

2. Crea un rol IAM para Lambda con permisos necesarios

3. Configura las variables de entorno:

```bash
export LAMBDA_ROLE_ARN=arn:aws:iam::YOUR_ACCOUNT:role/lambda-execution-role
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
export AWS_REGION=us-east-1
```

### Ejecutar Despliegue

```bash
./deploy-lambda.sh
```

El script:
- ✅ Compila el proyecto
- ✅ Crea o actualiza la función Lambda
- ✅ Configura las variables de entorno
- ✅ Muestra el ARN de la función

### Configurar API Gateway

Después del despliegue, configura API Gateway:

1. Ve a AWS Console → API Gateway
2. Crea una nueva API REST
3. Crea recursos y métodos
4. Integra con la función Lambda
5. Despliega la API

## Arquitectura

```
Cliente
   ↓
User Service (8081) → AWS Cognito
   ↓ (JWT Token)
Stream Service (8082) → Valida JWT con Cognito
   ↓
Base de Datos (H2/RDS)
```

### Flujo de Autenticación

1. Usuario hace login en User Service
2. User Service valida con Cognito y devuelve JWT
3. Cliente usa JWT para llamar Stream Service
4. Stream Service valida JWT con Cognito
5. Stream Service permite acceso si JWT es válido

## Estructura del Proyecto

```
stream/
├── src/main/java/com/example/stream/
│   ├── StreamApplication.java          # Aplicación principal
│   ├── StreamLambdaHandler.java        # Handler para Lambda
│   ├── config/
│   │   └── SecurityConfig.java         # Configuración de seguridad
│   ├── controller/
│   │   ├── PostController.java         # Endpoints de posts
│   │   ├── StreamController.java       # Endpoints de streams
│   │   └── HealthController.java       # Health check
│   ├── dto/
│   │   ├── PostRequest.java
│   │   ├── PostResponse.java
│   │   └── StreamResponse.java
│   ├── model/
│   │   ├── Post.java                   # Entidad Post
│   │   └── Stream.java                 # Entidad Stream
│   ├── repository/
│   │   ├── PostRepository.java
│   │   └── StreamRepository.java
│   └── service/
│       ├── PostService.java            # Lógica de negocio posts
│       └── StreamService.java          # Lógica de negocio streams
├── src/main/resources/
│   └── application.properties          # Configuración
├── pom.xml                             # Dependencias Maven
├── deploy-lambda.sh                    # Script de despliegue
├── test-api.sh                         # Script de pruebas
├── postman_collection.json             # Colección Postman
├── README.md                           # Documentación completa
└── QUICKSTART.md                       # Esta guía
```

## Base de Datos

### Desarrollo (H2)

Por defecto usa H2 en memoria. La consola está disponible en:
`http://localhost:8082/h2-console`

Configuración:
- JDBC URL: `jdbc:h2:mem:streamdb`
- Username: `sa`
- Password: (vacío)

### Producción (RDS)

Para usar PostgreSQL en producción:

1. Agrega la dependencia en `pom.xml`:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. Configura en `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://your-rds-endpoint:5432/streamdb
spring.datasource.username=dbuser
spring.datasource.password=dbpassword
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## Solución de Problemas

### Error: "Unauthorized" al crear post

✅ **Solución**: Verifica que:
- El token JWT es válido
- El User Pool ID es correcto
- El token no ha expirado

### Error: "Post no puede exceder 140 caracteres"

✅ **Solución**: El post tiene más de 140 caracteres. Acórtalo.

### Error: "No se pudo conectar al servicio"

✅ **Solución**: Verifica que:
- El servicio está ejecutándose
- El puerto 8082 está disponible
- Las variables de entorno están configuradas

### Error de compilación

✅ **Solución**:
```bash
mvn clean install -U
```

## Monitoreo

### Logs Locales

```bash
tail -f logs/stream-service.log
```

### Logs en Lambda

```bash
aws logs tail /aws/lambda/stream-service --follow
```

## Próximos Pasos

1. ✅ Servicio implementado con Cognito
2. ⏳ Desplegar en AWS Lambda
3. ⏳ Configurar API Gateway
4. ⏳ Integrar con frontend en S3
5. ⏳ Migrar a RDS
6. ⏳ Implementar caché con Redis
7. ⏳ Agregar streaming en tiempo real

## Recursos Adicionales

- [Documentación completa](README.md)
- [User Service](../user-service/README.md)
- [AWS Lambda Docs](https://docs.aws.amazon.com/lambda/)
- [Spring Security OAuth2](https://spring.io/projects/spring-security-oauth)

## Soporte

Para problemas o preguntas, revisa:
1. Los logs del servicio
2. La documentación completa en README.md
3. La configuración de Cognito en user-service
