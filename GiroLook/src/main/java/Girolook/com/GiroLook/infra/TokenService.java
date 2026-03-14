package Girolook.com.GiroLook.infra;

import Girolook.com.GiroLook.models.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    // ... (você já deve ter o método generateToken aqui)

    // ESTE É O MÉTODO QUE ESTÁ FALTANDO:
    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret); // Usa a mesma chave secreta
            return JWT.require(algorithm)
                    .withIssuer("girolook-api") // Verifica se fomos nós que emitimos
                    .build()
                    .verify(token)             // Descodifica e valida a assinatura
                    .getSubject();             // Se estiver OK, devolve o e-mail (Subject)
        } catch (JWTVerificationException exception) {
            // Se o token estiver expirado ou for falso, ele cai aqui
            return "";
        }
    }
}
