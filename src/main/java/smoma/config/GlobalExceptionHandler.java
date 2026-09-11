package smoma.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns every uncaught controller exception into a readable JSON body instead of the
 * default white-label "Internal Server Error" page, and logs the full stack trace so
 * the cause is visible in the server console while testing.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
        log.warn("Requête rejetée: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(body(ex, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("Corps de requête illisible: {}", ex.getMostSpecificCause().getMessage());
        Map<String, Object> b = body(ex, HttpStatus.BAD_REQUEST);
        b.put("error", "Données du formulaire invalides: " + ex.getMostSpecificCause().getMessage());
        return ResponseEntity.badRequest().body(b);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(ex, HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAny(Exception ex) {
        log.error("Erreur serveur non gérée", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body(ex, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private Map<String, Object> body(Throwable ex, HttpStatus status) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", status.value());
        String msg = ex.getMessage();
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        if ((msg == null || msg.isBlank()) && root.getMessage() != null) msg = root.getMessage();
        m.put("error", msg != null ? msg : ex.getClass().getSimpleName());
        m.put("type", ex.getClass().getSimpleName());
        if (root != ex && root.getMessage() != null) m.put("cause", root.getClass().getSimpleName() + ": " + root.getMessage());
        return m;
    }
}
