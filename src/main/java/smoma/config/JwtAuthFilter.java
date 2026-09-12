package smoma.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import smoma.controller.model.Service.JwtService;

import java.io.IOException;

/**
 * Closes the identity-spoofing hole at the root of the app's authorization model: every
 * AccessPolicy check resolves "who is calling" from the client-supplied X-User-Email header,
 * with nothing verifying the caller actually authenticated as that person. This filter requires
 * that any mutating request (POST/PUT/PATCH/DELETE) claiming an identity via that header also
 * present a validly signed JWT — obtainable only from a real successful login (AuthController) —
 * whose subject matches the claimed identity. A request with no claimed identity is left alone;
 * the existing AccessPolicy null-checks already reject it downstream.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String method = request.getMethod();
        boolean isMutation = "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method);
        String claimedIdentity = request.getHeader("X-User-Email");

        if (isMutation && claimedIdentity != null && !claimedIdentity.isBlank()) {
            String authHeader = request.getHeader("Authorization");
            String token = (authHeader != null && authHeader.startsWith("Bearer "))
                    ? authHeader.substring(7).trim() : null;
            String verifiedSubject = jwtService.validateAndGetSubject(token);

            if (verifiedSubject == null || !verifiedSubject.equalsIgnoreCase(claimedIdentity.trim())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"error\":\"Session invalide ou expirée, veuillez vous reconnecter. / Invalid or expired session, please log in again.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
