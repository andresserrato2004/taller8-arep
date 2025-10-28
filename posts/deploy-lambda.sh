#!/bin/bash

# Script para desplegar Post Service en AWS Lambda

set -e

echo "========================================="
echo "Post Service - AWS Lambda Deployment"
echo "========================================="

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Variables
FUNCTION_NAME="post-service"
REGION="${AWS_REGION:-us-east-1}"
HANDLER="com.example.posts.PostsLambdaHandler::handleRequest"
RUNTIME="java21"
MEMORY=512
TIMEOUT=30

# Verificar AWS CLI
if ! command -v aws &> /dev/null; then
    echo -e "${RED}Error: AWS CLI no está instalado${NC}"
    echo "Instala AWS CLI: https://aws.amazon.com/cli/"
    exit 1
fi

# Verificar Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven no está instalado${NC}"
    exit 1
fi

# Verificar variables de entorno requeridas
if [ -z "$COGNITO_USER_POOL_ID" ]; then
    echo -e "${YELLOW}Warning: COGNITO_USER_POOL_ID no está configurado${NC}"
    read -p "Ingresa el User Pool ID de Cognito: " COGNITO_USER_POOL_ID
fi

if [ -z "$LAMBDA_ROLE_ARN" ]; then
    echo -e "${YELLOW}Warning: LAMBDA_ROLE_ARN no está configurado${NC}"
    read -p "Ingresa el ARN del rol de Lambda: " LAMBDA_ROLE_ARN
fi

echo ""
echo "Configuración:"
echo "  Function Name: $FUNCTION_NAME"
echo "  Region: $REGION"
echo "  Runtime: $RUNTIME"
echo "  Memory: ${MEMORY}MB"
echo "  Timeout: ${TIMEOUT}s"
echo "  Cognito Pool: $COGNITO_USER_POOL_ID"
echo ""

# Compilar el proyecto
echo -e "${GREEN}[1/5] Compilando proyecto...${NC}"
mvn clean package -DskipTests

if [ ! -f "target/posts-0.0.1-SNAPSHOT.jar" ]; then
    echo -e "${RED}Error: JAR no encontrado${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Compilación exitosa${NC}"
echo ""

# Verificar si la función Lambda existe
echo -e "${GREEN}[2/5] Verificando función Lambda...${NC}"
FUNCTION_EXISTS=$(aws lambda get-function --function-name $FUNCTION_NAME --region $REGION 2>&1 || true)

if echo "$FUNCTION_EXISTS" | grep -q "ResourceNotFoundException"; then
    echo "La función no existe. Creando..."
    
    # Crear función Lambda
    echo -e "${GREEN}[3/5] Creando función Lambda...${NC}"
    aws lambda create-function \
        --function-name $FUNCTION_NAME \
        --runtime $RUNTIME \
        --handler $HANDLER \
        --role $LAMBDA_ROLE_ARN \
        --zip-file fileb://target/posts-0.0.1-SNAPSHOT.jar \
        --memory-size $MEMORY \
        --timeout $TIMEOUT \
        --region $REGION \
        --environment Variables="{COGNITO_USER_POOL_ID=$COGNITO_USER_POOL_ID,AWS_REGION=$REGION}"
    
    echo -e "${GREEN}✓ Función Lambda creada${NC}"
else
    echo "La función ya existe. Actualizando..."
    
    # Actualizar código de la función
    echo -e "${GREEN}[3/5] Actualizando código Lambda...${NC}"
    aws lambda update-function-code \
        --function-name $FUNCTION_NAME \
        --zip-file fileb://target/posts-0.0.1-SNAPSHOT.jar \
        --region $REGION
    
    echo "Esperando a que la actualización se complete..."
    aws lambda wait function-updated \
        --function-name $FUNCTION_NAME \
        --region $REGION
    
    # Actualizar configuración
    echo -e "${GREEN}[4/5] Actualizando configuración Lambda...${NC}"
    aws lambda update-function-configuration \
        --function-name $FUNCTION_NAME \
        --handler $HANDLER \
        --runtime $RUNTIME \
        --memory-size $MEMORY \
        --timeout $TIMEOUT \
        --region $REGION \
        --environment Variables="{COGNITO_USER_POOL_ID=$COGNITO_USER_POOL_ID,AWS_REGION=$REGION}"
    
    echo -e "${GREEN}✓ Función Lambda actualizada${NC}"
fi

echo ""

# Obtener información de la función
echo -e "${GREEN}[5/5] Obteniendo información de la función...${NC}"
FUNCTION_ARN=$(aws lambda get-function --function-name $FUNCTION_NAME --region $REGION --query 'Configuration.FunctionArn' --output text)

echo ""
echo "========================================="
echo -e "${GREEN}✓ Despliegue completado exitosamente${NC}"
echo "========================================="
echo ""
echo "Información de la función:"
echo "  ARN: $FUNCTION_ARN"
echo ""
echo "Próximos pasos:"
echo "  1. Configura API Gateway para exponer la función"
echo "  2. Prueba la función con el AWS Console o CLI"
echo "  3. Configura el dominio personalizado (opcional)"
echo ""
echo "Para probar la función:"
echo "  aws lambda invoke --function-name $FUNCTION_NAME --region $REGION response.json"
echo ""
echo "Para ver logs:"
echo "  aws logs tail /aws/lambda/$FUNCTION_NAME --follow --region $REGION"
echo ""
