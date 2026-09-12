package smoma.controller.model.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

/**
 * Persists uploaded scans and reports (signed mandates/OMs, mission reports) on disk under a
 * configurable root directory ({@code smoma.uploads.dir}), and hands back the web-relative path
 * served back by {@link smoma.config.WebMvcConfig}'s "/uploads/**" resource handler.
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE_BYTES = 15L * 1024 * 1024;

    private final Path uploadRoot;

    public FileStorageService(@Value("${smoma.uploads.dir:uploads}") String uploadsDir) {
        this.uploadRoot = Paths.get(uploadsDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de créer le répertoire de téléversement: " + uploadRoot, e);
        }
    }

    /**
     * Saves {@code file} under {@code subDir} with a name derived from {@code baseName}, and
     * returns the public path ("/uploads/{subDir}/{filename}") to store on the owning entity.
     */
    public String store(MultipartFile file, String subDir, String baseName) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Aucun fichier n'a été fourni.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Le fichier dépasse la taille maximale autorisée (15 Mo).");
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        int dot = originalName.lastIndexOf('.');
        String ext = dot >= 0 ? originalName.substring(dot + 1).toLowerCase() : "";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Format de fichier non autorisé. Formats acceptés : PDF, JPG, JPEG, PNG.");
        }

        String safeSubDir = sanitize(subDir);
        String safeBaseName = sanitize(baseName);
        String filename = safeBaseName + "_" + System.currentTimeMillis() + "." + ext;

        try {
            Path targetDir = uploadRoot.resolve(safeSubDir).normalize();
            if (!targetDir.startsWith(uploadRoot)) {
                throw new IllegalArgumentException("Chemin de destination invalide.");
            }
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(filename).normalize();
            if (!targetFile.startsWith(targetDir)) {
                throw new IllegalArgumentException("Nom de fichier invalide.");
            }
            try (var in = file.getInputStream()) {
                Files.copy(in, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + safeSubDir + "/" + filename;
        } catch (IOException e) {
            throw new IllegalStateException("Échec de l'enregistrement du fichier: " + e.getMessage(), e);
        }
    }

    private static String sanitize(String s) {
        if (s == null || s.isBlank()) return "fichier";
        String cleaned = s.trim().replaceAll("[^A-Za-z0-9_-]", "_");
        return cleaned.isBlank() ? "fichier" : cleaned;
    }
}
