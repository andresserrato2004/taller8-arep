# 🚀 Guía Rápida - User Service con AWS Cognito

## Pasos para comenzar

### 1. Configurar AWS Cognito

#### Opción A: Usar el script automatizado
```bash
./setup-cognito.sh
```

#### Opción B: Configuración manual en AWS Console

1. Ve a AWS Console → Cognito → Create User Pool
2. Configura:
   - **Pool name**: `twitter-lite-user-pool`
   - **Sign-in options**: Username, Email
   - **Required attributes**: email
   - **Email verification**: Enabled
   
3. Crea un App Client:
   - **Name**: `twitter-lite-client`
   - **Generate client secret**: YES ✓
   - **Auth flows**: Enable `USER_PASSWORD_AUTH`
   
4. Copia la información:
   - User Pool ID: `us-east-1_XXXXXXXXX`
   - Client ID: `your-client-id`
   - Client Secret: `your-client-secret`

### 2. Configurar el proyecto

Edita `src/main/resources/application.properties`:

```properties
aws.cognito.userPoolId=us-east-1_XXXXXXXXX
aws.cognito.clientId=your-client-id
aws.cognito.clientSecret=your-client-secret
aws.cognito.region=us-east-1
```

O usa variables de entorno:
```bash
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
export COGNITO_CLIENT_ID=your-client-id
export COGNITO_CLIENT_SECRET=your-client-secret
export AWS_REGION=us-east-1
```

### 3. Ejecutar el servicio

```bash
# Compilar
mvn clean package

# Ejecutar
mvn spring-boot:run
```

El servicio estará en: http://localhost:8081

### 4. Probar el servicio

#### Opción A: Usar el script de pruebas
```bash
./test-api.sh
```

#### Opción B: Importar en Postman
Importa el archivo `postman_collection.json` en Postman

#### Opción C: Probar con curl

**1. Registrar usuario:**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPass123!"
  }'
```

**2. Verificar email:**
Revisa tu email y usa el código de verificación:
```bash
curl -X POST http://localhost:8081/api/auth/verify \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "confirmationCode": "123456"
  }'
```

**3. Login:**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "TestPass123!"
  }'
```

Guarda el `accessToken` de la respuesta.

**4. Obtener usuario actual:**
```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 📦 Estructura del Proyecto

```
user-service/
├── src/main/java/com/example/user_service/
│   ├── UserServiceApplication.java        # Aplicación principal
│   ├── StreamLambdaHandler.java          # Handler para Lambda
│   ├── config/
│   │   ├── CognitoConfig.java            # Configuración AWS SDK
│   │   └── SecurityConfig.java           # Configuración Spring Security + JWT
│   ├── controller/
│   │   ├── AuthController.java           # Endpoints de autenticación
│   │   ├── UserController.java           # Endpoints de usuarios
│   │   └── HealthController.java         # Health check
│   ├── dto/
│   │   ├── UserRegistrationRequest.java
│   │   ├── UserLoginRequest.java
│   │   ├── AuthenticationResponse.java
│   │   └── UserResponse.java
│   ├── model/
│   │   └── User.java                     # Entidad User
│   ├── repository/
│   │   └── UserRepository.java           # Repository JPA
│   └── service/
│       ├── CognitoService.java           # Servicio AWS Cognito
│       └── UserService.java              # Servicio de usuarios
└── src/main/resources/
    └── application.properties            # Configuración
```

## 🔐 Seguridad

- Los endpoints de `/api/auth/*` y `/health` son públicos
- Todos los demás endpoints requieren JWT válido
- Los tokens son emitidos por AWS Cognito
- Los usuarios solo pueden modificar su propio perfil

## 🚀 Desplegar en AWS Lambda

1. **Compilar:**
```bash
mvn clean package
```

2. **Crear función Lambda:**
```bash
aws lambda create-function \
  --function-name user-service \
  --runtime java21 \
  --handler com.example.user_service.StreamLambdaHandler::handleRequest \
  --role arn:aws:iam::YOUR_ACCOUNT:role/lambda-role \
  --zip-file fileb://target/user-service-0.0.1-SNAPSHOT.jar \
  --memory-size 512 \
  --timeout 30 \
  --environment Variables="{
    COGNITO_USER_POOL_ID=your-pool-id,
    COGNITO_CLIENT_ID=your-client-id,
    COGNITO_CLIENT_SECRET=your-secret,
    AWS_REGION=us-east-1
  }"
```

3. **Configurar API Gateway:**
   - Crear API REST
   - Añadir recurso proxy: `/{proxy+}`
   - Método: ANY
   - Integration: Lambda Function
   - Deploy

## 🗄️ Base de Datos

**Desarrollo:** H2 (en memoria)

**Producción:** PostgreSQL en RDS
```properties
spring.datasource.url=jdbc:postgresql://your-rds:5432/userdb
spring.datasource.username=postgres
spring.datasource.password=your-password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## 🐛 Troubleshooting

### Error: "User pool does not exist"
- Verifica el User Pool ID en application.properties
- Asegúrate de usar la región correcta

### Error: "Unable to verify signature"
- El JWT viene de un User Pool diferente
- Verifica que el User Pool ID sea correcto

### Error: "Incorrect username or password"
- Usuario no confirmado → usa `/api/auth/verify`
- Contraseña incorrecta
- Usuario no existe

## 📚 Recursos

- [AWS Cognito Docs](https://docs.aws.amazon.com/cognito/)
- [Spring Security](https://spring.io/projects/spring-security)
- [AWS Lambda Java](https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html)

## 🆘 Ayuda

Para más información, consulta el `README.md` completo.
