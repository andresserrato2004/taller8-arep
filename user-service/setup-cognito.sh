#!/bin/bash

# Script para configurar AWS Cognito User Pool para el User Service

echo "🚀 Configurando AWS Cognito User Pool..."

# Variables (edita estas según tus necesidades)
POOL_NAME="twitter-lite-user-pool"
CLIENT_NAME="twitter-lite-client"
REGION="us-east-1"

# Crear User Pool
echo "📦 Creando User Pool..."
USER_POOL_ID=$(aws cognito-idp create-user-pool \
  --pool-name "$POOL_NAME" \
  --policies "PasswordPolicy={MinimumLength=8,RequireUppercase=true,RequireLowercase=true,RequireNumbers=true,RequireSymbols=true}" \
  --username-attributes email \
  --auto-verified-attributes email \
  --schema Name=email,AttributeDataType=String,Required=true,Mutable=true \
          Name=preferred_username,AttributeDataType=String,Required=false,Mutable=true \
  --region "$REGION" \
  --query 'UserPool.Id' \
  --output text)

echo "✅ User Pool creado: $USER_POOL_ID"

# Crear App Client
echo "📱 Creando App Client..."
CLIENT_OUTPUT=$(aws cognito-idp create-user-pool-client \
  --user-pool-id "$USER_POOL_ID" \
  --client-name "$CLIENT_NAME" \
  --generate-secret \
  --explicit-auth-flows ALLOW_USER_PASSWORD_AUTH ALLOW_REFRESH_TOKEN_AUTH \
  --region "$REGION" \
  --query 'UserPoolClient.[ClientId,ClientSecret]' \
  --output text)

CLIENT_ID=$(echo $CLIENT_OUTPUT | awk '{print $1}')
CLIENT_SECRET=$(echo $CLIENT_OUTPUT | awk '{print $2}')

echo "✅ App Client creado"
echo ""
echo "📋 Configuración de Cognito:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "User Pool ID:     $USER_POOL_ID"
echo "Client ID:        $CLIENT_ID"
echo "Client Secret:    $CLIENT_SECRET"
echo "Region:           $REGION"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "🔧 Agrega estas variables a tu .env:"
echo ""
echo "COGNITO_USER_POOL_ID=$USER_POOL_ID"
echo "COGNITO_CLIENT_ID=$CLIENT_ID"
echo "COGNITO_CLIENT_SECRET=$CLIENT_SECRET"
echo "AWS_REGION=$REGION"
echo ""
echo "✅ ¡Configuración completada!"
