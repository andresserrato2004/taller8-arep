package com.example.user_service.service;

import com.example.user_service.dto.AuthenticationResponse;
import com.example.user_service.dto.UserLoginRequest;
import com.example.user_service.dto.UserRegistrationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class CognitoService {
    
    private final CognitoIdentityProviderClient cognitoClient;
    
    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;
    
    @Value("${aws.cognito.clientId}")
    private String clientId;
    
    @Value("${aws.cognito.clientSecret}")
    private String clientSecret;
    
    public CognitoService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }
    
    public String registerUser(UserRegistrationRequest request) {
        try {
            Map<String, String> userAttributes = new HashMap<>();
            userAttributes.put("email", request.getEmail());
            userAttributes.put("preferred_username", request.getUsername());
            
            SignUpRequest signUpRequest = SignUpRequest.builder()
                .clientId(clientId)
                .secretHash(calculateSecretHash(request.getUsername()))
                .username(request.getUsername())
                .password(request.getPassword())
                .userAttributes(
                    AttributeType.builder().name("email").value(request.getEmail()).build(),
                    AttributeType.builder().name("preferred_username").value(request.getUsername()).build()
                )
                .build();
            
            SignUpResponse response = cognitoClient.signUp(signUpRequest);
            return response.userSub();
            
        } catch (CognitoIdentityProviderException e) {
            throw new RuntimeException("Error registering user in Cognito: " + e.getMessage(), e);
        }
    }
    
    public AuthenticationResponse authenticateUser(UserLoginRequest request) {
        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", request.getUsername());
            authParams.put("PASSWORD", request.getPassword());
            authParams.put("SECRET_HASH", calculateSecretHash(request.getUsername()));
            
            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .clientId(clientId)
                .authParameters(authParams)
                .build();
            
            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
            AuthenticationResultType result = authResponse.authenticationResult();
            
            return new AuthenticationResponse(
                result.accessToken(),
                result.idToken(),
                result.refreshToken(),
                result.tokenType(),
                result.expiresIn(),
                request.getUsername()
            );
            
        } catch (CognitoIdentityProviderException e) {
            throw new RuntimeException("Error authenticating user: " + e.getMessage(), e);
        }
    }
    
    public void confirmUser(String username, String confirmationCode) {
        try {
            ConfirmSignUpRequest confirmRequest = ConfirmSignUpRequest.builder()
                .clientId(clientId)
                .secretHash(calculateSecretHash(username))
                .username(username)
                .confirmationCode(confirmationCode)
                .build();
            
            cognitoClient.confirmSignUp(confirmRequest);
            
        } catch (CognitoIdentityProviderException e) {
            throw new RuntimeException("Error confirming user: " + e.getMessage(), e);
        }
    }
    
    public AuthenticationResponse refreshToken(String refreshToken, String username) {
        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("REFRESH_TOKEN", refreshToken);
            authParams.put("SECRET_HASH", calculateSecretHash(username));
            
            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .clientId(clientId)
                .authParameters(authParams)
                .build();
            
            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
            AuthenticationResultType result = authResponse.authenticationResult();
            
            return new AuthenticationResponse(
                result.accessToken(),
                result.idToken(),
                refreshToken,
                result.tokenType(),
                result.expiresIn(),
                username
            );
            
        } catch (CognitoIdentityProviderException e) {
            throw new RuntimeException("Error refreshing token: " + e.getMessage(), e);
        }
    }
    
    private String calculateSecretHash(String username) {
        try {
            String message = username + clientId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                clientSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(keySpec);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating secret hash", e);
        }
    }
}
