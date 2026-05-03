package Girolook.com.GiroLook.infra;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {


    private final Bucket createBucket = Bucket.builder()
            .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
            .build();


    private final Bucket loginBucket = Bucket.builder()
            .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
            .build();

    private final Bucket storeBucket = Bucket.builder()
            .addLimit(Bandwidth.classic(2, Refill.intervally(2, Duration.ofHours(1))))
            .build();

    private final Bucket productBucket = Bucket.builder()
            .addLimit(Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1))))
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/users/create")) {
            applyRateLimit(createBucket, response, filterChain,request);
        } else if (path.startsWith("/users/login")) {
            applyRateLimit(loginBucket, response, filterChain,request);
        } else if (path.startsWith("/stores/create")) {
            applyRateLimit(storeBucket, response, filterChain,request); // Proteção de infraestrutura
        } else if (path.startsWith("/products/create")) {
            applyRateLimit(productBucket, response, filterChain,request); // Proteção contra spam
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private void applyRateLimit(Bucket bucket, HttpServletResponse response, FilterChain filterChain, HttpServletRequest request)
            throws IOException, ServletException {
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Estourou o limite de requisições. Tente novamente em 1 minuto.\"}");
        }
    }
}
