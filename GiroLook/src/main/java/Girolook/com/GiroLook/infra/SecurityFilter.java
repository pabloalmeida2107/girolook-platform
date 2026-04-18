package Girolook.com.GiroLook.infra;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 🔓 ROTAS LIBERADAS (SEM TOKEN)
        if (path.contains("/users/create") ||
                path.contains("/v3/api-docs") ||
                path.contains("/swagger-ui")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 🔐 PEGAR TOKEN DO HEADER
        String token = recoverToken(request);

        if (token != null) {
            String login = tokenService.validateToken(token);

            if (login != null) {
                UserDetails user = userDetailsService.loadUserByUsername(login);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // continua o fluxo
        filterChain.doFilter(request, response);
    }

    // 📌 Método pra pegar o token do header Authorization
    private String recoverToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) return null;

        return authHeader.replace("Bearer ", "");
    }
}