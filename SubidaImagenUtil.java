package util;

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Utilidad para manejar la subida de imágenes de productos.
 *
 * Las imágenes se guardan en:
 *   [ruta_absoluta_del_servidor]/uploads/productos/
 *
 * La ruta relativa guardada en BD es:
 *   /uploads/productos/uuid.jpg
 *
 * IMPORTANTE:
 *   Configura UPLOAD_DIR_RELATIVO según tu estructura de proyecto.
 *   La carpeta se crea automáticamente si no existe.
 */
public class SubidaImagenUtil {

    // Extensiones permitidas
    private static final List<String> EXTENSIONES_PERMITIDAS =
            Arrays.asList("jpg", "jpeg", "png", "webp");

    // Tamaño máximo: 5 MB
    private static final long TAMANIO_MAXIMO = 5 * 1024 * 1024;

    // Ruta relativa dentro del proyecto (webapp)
    private static final String UPLOAD_DIR_RELATIVO = "uploads/productos";

    /**
     * Sube una imagen al servidor y devuelve la ruta relativa para guardar en BD.
     *
     * @param part          El Part de la imagen del formulario multipart
     * @param rutaRealApp   Ruta absoluta del directorio de la app en el servidor
     *                      Obtener con: request.getServletContext().getRealPath("/")
     * @return Ruta relativa para guardar en BD (ej: /uploads/productos/uuid.jpg)
     *         o null si hubo un error
     * @throws IOException si hay error al escribir el archivo
     * @throws IllegalArgumentException si el archivo no es válido
     */
    public static String subirImagen(Part part, String rutaRealApp)
            throws IOException, IllegalArgumentException {

        // 1. Validar que el Part no está vacío
        if (part == null || part.getSize() == 0) {
            throw new IllegalArgumentException("No se recibió ninguna imagen.");
        }

        // 2. Validar tamaño
        if (part.getSize() > TAMANIO_MAXIMO) {
            throw new IllegalArgumentException(
                "La imagen supera el tamaño máximo permitido (5 MB).");
        }

        // 3. Obtener y validar extensión
        String nombreOriginal = obtenerNombreArchivo(part);
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            throw new IllegalArgumentException("Archivo sin extensión válida.");
        }

        String extension = nombreOriginal.substring(
                nombreOriginal.lastIndexOf(".") + 1).toLowerCase();

        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new IllegalArgumentException(
                "Solo se permiten imágenes JPG, PNG o WEBP.");
        }

        // 4. Crear directorio si no existe
        String uploadDirAbsoluto = rutaRealApp + File.separator + UPLOAD_DIR_RELATIVO;
        File uploadDir = new File(uploadDirAbsoluto);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 5. Generar nombre único con UUID para evitar colisiones
        String nombreUnico = UUID.randomUUID().toString() + "." + extension;
        String rutaAbsoluta = uploadDirAbsoluto + File.separator + nombreUnico;

        // 6. Guardar el archivo
        try (InputStream inputStream = part.getInputStream()) {
            Files.copy(inputStream,
                       Paths.get(rutaAbsoluta),
                       StandardCopyOption.REPLACE_EXISTING);
        }

        // 7. Devolver ruta relativa para BD
        return "/" + UPLOAD_DIR_RELATIVO + "/" + nombreUnico;
    }

    /**
     * Elimina una imagen del servidor dado su ruta relativa.
     *
     * @param rutaRelativa  Ruta relativa guardada en BD (ej: /uploads/productos/uuid.jpg)
     * @param rutaRealApp   Ruta absoluta del directorio de la app
     */
    public static void eliminarImagen(String rutaRelativa, String rutaRealApp) {
        if (rutaRelativa == null || rutaRelativa.isEmpty()) return;
        try {
            File archivo = new File(rutaRealApp + rutaRelativa.replace("/", File.separator));
            if (archivo.exists()) {
                archivo.delete();
            }
        } catch (Exception e) {
            System.err.println("Error al eliminar imagen: " + e.getMessage());
        }
    }

    /**
     * Extrae el nombre del archivo del header Content-Disposition del Part.
     */
    private static String obtenerNombreArchivo(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp == null) return null;
        for (String token : contentDisp.split(";")) {
            if (token.trim().startsWith("filename")) {
                String nombre = token.substring(token.indexOf("=") + 1)
                                     .trim()
                                     .replace("\"", "");
                // Quitar ruta del cliente si viene con ruta completa
                return Paths.get(nombre).getFileName().toString();
            }
        }
        return null;
    }
}
