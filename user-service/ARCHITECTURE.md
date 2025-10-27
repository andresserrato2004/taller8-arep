# User Service - Arquitectura y Componentes

## 📐 Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                        Cliente (Web/Mobile)                      │
└───────────────┬─────────────────────────────────────────────────┘
                │
                │ HTTP/HTTPS
                │
┌───────────────▼─────────────────────────────────────────────────┐
│                       API Gateway (AWS)                          │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Routes:                                                  │  │
│  │  POST /api/auth/register                                 │  │
│  │  POST /api/auth/login                                    │  │
│  │  POST /api/auth/verify                                   │  │
│  │  GET  /api/users/me                                      │  │
│  │  GET  /api/users                                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└───────────────┬─────────────────────────────────────────────────┘
                │
                │ Proxy Request
                │
┌───────────────▼─────────────────────────────────────────────────┐
│                    AWS Lambda Function                           │
│                  (User Service - Java 21)                        │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           StreamLambdaHandler                            │  │
│  │  (Spring Boot Container Handler)                         │  │
│  └───────────────┬──────────────────────────────────────────┘  │
│                  │                                              │
│  ┌───────────────▼──────────────────────────────────────────┐  │
│  │              Spring Boot Application                      │  │
│  │                                                           │  │
│  │  ┌─────────────────────────────────────────────────────┐ │  │
│  │  │  Controllers (REST API)                             │ │  │
│  │  │  - AuthController                                   │ │  │
│  │  │  - UserController                                   │ │  │
│  │  │  - HealthController                                 │ │  │
│  │  └────────────┬────────────────────────────────────────┘ │  │
│  │               │                                           │  │
│  │  ┌────────────▼───────────────────────────────────────┐  │  │
│  │  │  Services (Business Logic)                         │  │  │
│  │  │  - UserService                                     │  │  │
│  │  │  - CognitoService                                  │  │  │
│  │  └────────────┬───────────────┬───────────────────────┘  │  │
│  │               │               │                           │  │
│  │  ┌────────────▼────────────┐  │                           │  │
│  │  │  Repositories (JPA)     │  │                           │  │
│  │  │  - UserRepository       │  │                           │  │
│  │  └────────────┬────────────┘  │                           │  │
│  └───────────────│────────────────│───────────────────────────┘  │
└─────────────────│────────────────│──────────────────────────────┘
                  │                │
     ┌────────────▼─────┐   ┌─────▼──────────────────┐
     │   Database       │   │   AWS Cognito          │
     │   (H2/RDS)       │   │   User Pool            │
     │                  │   │                        │
     │  ┌────────────┐  │   │  ┌──────────────────┐ │
     │  │   Users    │  │   │  │  Authentication  │ │
     │  │   Table    │  │   │  │  - JWT Tokens    │ │
     │  └────────────┘  │   │  │  - User Pool     │ │
     │                  │   │  │  - App Client    │ │
     └──────────────────┘   │  └──────────────────┘ │
                            └─────────────────────────┘
```

## 🔄 Flujo de Autenticación

### 1. Registro de Usuario
```
Cliente                 User Service           AWS Cognito          Database
  │                          │                      │                   │
  │  POST /auth/register     │                      │                   │
  ├─────────────────────────►│                      │                   │
  │                          │  SignUp(user)        │                   │
  │                          ├─────────────────────►│                   │
  │                          │  cognitoUserId       │                   │
  │                          │◄─────────────────────┤                   │
  │                          │  Save User           │                   │
  │                          ├──────────────────────────────────────────►│
  │  201 Created             │                      │   User Saved      │
  │◄─────────────────────────┤                      │                   │
  │                          │                      │                   │
  │  (Email de verificación enviado por Cognito)   │                   │
  │◄─────────────────────────────────────────────────┤                   │
```

### 2. Verificación de Email
```
Cliente                 User Service           AWS Cognito
  │                          │                      │
  │  POST /auth/verify       │                      │
  ├─────────────────────────►│                      │
  │  {code}                  │  ConfirmSignUp       │
  │                          ├─────────────────────►│
  │                          │  Verified            │
  │  200 OK                  │◄─────────────────────┤
  │◄─────────────────────────┤                      │
```

### 3. Login
```
Cliente                 User Service           AWS Cognito          Database
  │                          │                      │                   │
  │  POST /auth/login        │                      │                   │
  ├─────────────────────────►│                      │                   │
  │  {user, pass}            │  Check User Exists   │                   │
  │                          ├──────────────────────────────────────────►│
  │                          │  User Found          │                   │
  │                          │◄──────────────────────────────────────────┤
  │                          │  InitiateAuth        │                   │
  │                          ├─────────────────────►│                   │
  │                          │  JWT Tokens          │                   │
  │  200 OK + Tokens         │◄─────────────────────┤                   │
  │◄─────────────────────────┤                      │                   │
  │  {accessToken, idToken}  │                      │                   │
```

### 4. Acceso a Recurso Protegido
```
Cliente                 User Service           AWS Cognito
  │                          │                      │
  │  GET /users/me           │                      │
  │  Bearer: JWT             │                      │
  ├─────────────────────────►│                      │
  │                          │  Validate JWT        │
  │                          ├─────────────────────►│
  │                          │  Token Valid         │
  │                          │◄─────────────────────┤
  │                          │  Get User Info       │
  │  200 OK + User Data      │                      │
  │◄─────────────────────────┤                      │
```

## 🔐 Configuración de Seguridad

### Spring Security Filter Chain
```
Request
   │
   ▼
┌─────────────────────────┐
│  CORS Filter            │
│  (Allow all origins)    │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────┐
│  CSRF Disabled          │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────┐
│  Authorization Check    │
│  - Public: /auth/*      │
│  - Protected: Others    │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────┐
│  JWT Decoder            │
│  (Cognito JWKS)         │
└──────────┬──────────────┘
           │
           ▼
┌─────────────────────────┐
│  Resource Server        │
│  (OAuth2 JWT)           │
└──────────┬──────────────┘
           │
           ▼
      Controller
```

## 📦 Modelo de Datos

### User Entity
```
┌─────────────────────────────────────┐
│             users                    │
├─────────────────────────────────────┤
│ id               BIGINT (PK)        │
│ username         VARCHAR (UNIQUE)   │
│ email            VARCHAR (UNIQUE)   │
│ cognito_user_id  VARCHAR            │
│ bio              VARCHAR            │
│ profile_picture  VARCHAR            │
│ created_at       TIMESTAMP          │
│ updated_at       TIMESTAMP          │
└─────────────────────────────────────┘
```

### Relación con Cognito
```
┌─────────────────────────────────────┐
│  Local Database (users table)       │
│                                     │
│  id=1                               │
│  username="john_doe"                │
│  email="john@example.com"           │
│  cognito_user_id="abc-123..."       │
└──────────────┬──────────────────────┘
               │
               │ cognito_user_id
               │
┌──────────────▼──────────────────────┐
│  AWS Cognito User Pool              │
│                                     │
│  sub="abc-123..."                   │
│  username="john_doe"                │
│  email="john@example.com"           │
│  email_verified=true                │
└─────────────────────────────────────┘
```

## 🔑 JWT Token Structure

### Access Token (Cognito emitido)
```json
{
  "sub": "abc-123-xyz",
  "cognito:username": "john_doe",
  "iss": "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXX",
  "client_id": "your-client-id",
  "origin_jti": "...",
  "event_id": "...",
  "token_use": "access",
  "scope": "aws.cognito.signin.user.admin",
  "auth_time": 1234567890,
  "exp": 1234571490,
  "iat": 1234567890,
  "jti": "...",
  "username": "john_doe"
}
```

## 📊 API Endpoints Summary

| Método | Endpoint                  | Auth? | Descripción                  |
|--------|---------------------------|-------|------------------------------|
| POST   | /api/auth/register        | ❌    | Registrar nuevo usuario      |
| POST   | /api/auth/verify          | ❌    | Verificar email              |
| POST   | /api/auth/login           | ❌    | Iniciar sesión               |
| POST   | /api/auth/refresh         | ❌    | Renovar token                |
| GET    | /api/users/me             | ✅    | Obtener usuario actual       |
| GET    | /api/users                | ✅    | Listar todos los usuarios    |
| GET    | /api/users/{id}           | ✅    | Obtener usuario por ID       |
| GET    | /api/users/username/{u}   | ✅    | Obtener usuario por username |
| PUT    | /api/users/{id}           | ✅    | Actualizar perfil            |
| GET    | /health                   | ❌    | Health check                 |

## 🚀 Deployment Options

### Opción 1: AWS Lambda + API Gateway
```
Ventajas:
✅ Serverless (pago por uso)
✅ Auto-scaling
✅ Alta disponibilidad
✅ Integración nativa con Cognito

Desventajas:
❌ Cold start
❌ Timeout límite (15 min)
```

### Opción 2: ECS/Fargate
```
Ventajas:
✅ Sin cold start
✅ Control total del contenedor
✅ Mejor para cargas constantes

Desventajas:
❌ Más costoso
❌ Requiere más configuración
```

### Opción 3: EC2
```
Ventajas:
✅ Control total
✅ Personalizable

Desventajas:
❌ Gestión de servidor
❌ No auto-scaling automático
❌ Más caro
```

## 🔧 Variables de Entorno

```bash
# AWS Cognito
COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxx
COGNITO_CLIENT_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
AWS_REGION=us-east-1

# Database (Producción)
DB_HOST=your-rds-endpoint.rds.amazonaws.com
DB_PORT=5432
DB_NAME=userdb
DB_USERNAME=postgres
DB_PASSWORD=your-password

# Spring Boot
SERVER_PORT=8081
SPRING_PROFILES_ACTIVE=production
```

## 📈 Métricas y Monitoreo

Recomendado configurar:
- **CloudWatch Logs**: Para logs de Lambda
- **CloudWatch Metrics**: Para métricas de rendimiento
- **X-Ray**: Para tracing distribuido
- **CloudWatch Alarms**: Para alertas

---

**Nota**: Este diagrama es una representación simplificada. En producción, considera agregar:
- Load Balancer
- WAF (Web Application Firewall)
- VPC y Security Groups
- RDS en Multi-AZ
- Backup y Disaster Recovery
