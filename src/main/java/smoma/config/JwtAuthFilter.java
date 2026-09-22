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
 * that any request claiming an identity via that header — GET included, not just mutations —
 * also present a validly signed JWT (obtainable only from a real successful login via
 * AuthController) whose subject matches the claimed identity. A request with no claimed identity
 * is left alone; the existing AccessPolicy null-checks already reject it downstream.
 *
 * GET requests used to be exempt entirely, which meant an endpoint like
 * GET /api/ordres-mission/{id} (or its /pdf variant) could not tell who was actually asking even
 * once it started checking — any caller could claim any identity via the header with nothing to
 * verify it. Covering GET here is what makes that kind of per-record authorization check (see
 * AccessPolicy#canViewOrdreDeMission) actually mean something instead of trusting an unverified
 * header.
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
        String claimedIdentity = request.getHeader("X-User-Email");

        if (claimedIdentity != null && !claimedIdentity.isBlank()) {
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
