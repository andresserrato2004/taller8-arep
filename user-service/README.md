# User Service - Twitter Lite

Microservicio de usuarios con autenticación mediante AWS Cognito y JWT.

## 🚀 Características

- Registro de usuarios con AWS Cognito
- Autenticación JWT
- Gestión de perfiles de usuario
- API REST
- Preparado para AWS Lambda

## 📋 Prerrequisitos

- Java 21
- Maven 3.6+
- Cuenta de AWS
- AWS Cognito User Pool configurado

## 🔧 Configuración de AWS Cognito

### 1. Crear User Pool en AWS Cognito

1. Ir a AWS Console → Cognito → Create user pool
2. Configurar:
   - Sign-in options: Username, Email
   - Password policy: Default
   - MFA: Optional
   - User account recovery: Email
3. Crear un App Client:
   - App client name: `twitter-lite-client`
   - Authentication flows: `USER_PASSWORD_AUTH` (habilitado)
   - Generate client secret: YES
4. Anotar:
   - User Pool ID
   - App Client ID
   - App Client Secret
   - Region

### 2. Configurar variables de entorno

Crear un archivo `.env` o configurar las variables:

```bash
export COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
export COGNITO_CLIENT_ID=your-client-id
export COGNITO_CLIENT_SECRET=your-client-secret
export AWS_REGION=us-east-1
```

O editar `src/main/resources/application.properties`:

```properties
aws.cognito.userPoolId=us-east-1_XXXXXXXXX
aws.cognito.clientId=your-client-id
aws.cognito.clientSecret=your-client-secret
aws.cognito.region=us-east-1
```

## 🏃 Ejecución Local

```bash
# Compilar
mvn clean package

# Ejecutar
mvn spring-boot:run

# O con java
java -jar target/user-service-0.0.1-SNAPSHOT.jar
```

El servicio estará disponible en: `http://localhost:8081`

## 📡 API Endpoints

### Autenticación (público)

#### Registro
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "Password123!"
}
```

#### Verificar Email
```bash
POST /api/auth/verify
Content-Type: application/json

{
  "username": "john_doe",
  "confirmationCode": "123456"
}
```

#### Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "Password123!"
}
```

Respuesta:
```json
{
  "accessToken": "eyJraWQiOiJ...",
  "idToken": "eyJraWQiOiJ...",
  "refreshToken": "eyJjdHkiOiJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "username": "john_doe"
}
```

#### Refresh Token
```bash
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJjdHkiOiJ...",
  "username": "john_doe"
}
```

### Usuarios (requiere autenticación)

#### Obtener usuario actual
```bash
GET /api/users/me
Authorization: Bearer <access-token>
```

#### Obtener todos los usuarios
```bash
GET /api/users
Authorization: Bearer <access-token>
```

#### Obtener usuario por ID
```bash
GET /api/users/{id}
Authorization: Bearer <access-token>
```

#### Obtener usuario por username
```bash
GET /api/users/username/{username}
Authorization: Bearer <access-token>
```

#### Actualizar perfil
```bash
PUT /api/users/{id}
Authorization: Bearer <access-token>
Content-Type: application/json

{
  "bio": "Software Developer",
  "profilePicture": "https://example.com/profile.jpg"
}
```

### Health Check
```bash
GET /health
```

## 🐳 Despliegue en AWS Lambda

### 1. Crear el handler para Lambda

El proyecto ya incluye el adaptador `aws-serverless-java-container-springboot3`.

Crear `StreamLambdaHandler.java`:

```java
package com.example.user_service;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamLambdaHandler implements RequestStreamHandler {
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
    
    static {
        try {
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(UserServiceApplication.class);
        } catch (ContainerInitializationException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }
    
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }
}
```

### 2. Empaquetar para Lambda

```bash
mvn clean package
```

### 3. Crear función Lambda en AWS

```bash
# Usando AWS CLI
aws lambda create-function \
  --function-name user-service \
  --runtime java21 \
  --handler com.example.user_service.StreamLambdaHandler::handleRequest \
  --role arn:aws:iam::ACCOUNT_ID:role/lambda-execution-role \
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

### 4. Configurar API Gateway

1. Crear API REST en API Gateway
2. Configurar proxy resource: `/{proxy+}`
3. Método: ANY
4. Integration: Lambda Function (user-service)
5. Deploy API

## 🔒 Seguridad

- Todos los endpoints (excepto `/api/auth/*` y `/health`) requieren token JWT
- Los tokens son validados contra AWS Cognito
- Los usuarios solo pueden modificar su propio perfil

## 🗄️ Base de Datos

Por defecto usa H2 (en memoria) para desarrollo. Para producción, configurar PostgreSQL en RDS:

```properties
spring.datasource.url=jdbc:postgresql://your-rds-endpoint:5432/userdb
spring.datasource.username=postgres
spring.datasource.password=your-password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

## 📝 Modelo de Datos

### User
```
- id: Long (PK)
- username: String (unique)
- email: String (unique)
- cognitoUserId: String
- bio: String
- profilePicture: String
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

## 🧪 Testing

```bash
# Ejecutar tests
mvn test

# Con coverage
mvn clean test jacoco:report
```

## 📦 Tecnologías

- Spring Boot 3.3.5
- Spring Security
- Spring Data JPA
- AWS SDK for Java 2.x
- AWS Cognito
- JWT (via Cognito)
- Lombok
- H2 / PostgreSQL

## 🤝 Contribuir

1. Fork el proyecto
2. Crear feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📄 Licencia

MIT License
