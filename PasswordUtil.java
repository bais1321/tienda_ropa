package util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para hashear y verificar contraseñas con BCrypt.
 *
 * LIBRERÍA NECESARIA:
 *   Descarga jbcrypt-0.4.jar desde:
 *   https://github.com/jeremyh/jBCrypt/releases
 *   o busca "jbcrypt" en https://mvnrepository.com/
 *   Colócalo en: WEB-INF/lib/
 */
public class PasswordUtil {

    // Costo de BCrypt: 12 es seguro para producción (más alto = más lento pero más seguro)
    private static final int BCRYPT_COST = 12;

    /**
     * Genera el hash BCrypt de una contraseña en texto plano.
     * Llama esto al REGISTRAR un usuario.
     *
     * @param passwordPlano Contraseña en texto plano
     * @return Hash BCrypt (60 caracteres)
     */
    public static String hashear(String passwordPlano) {
        return BCrypt.hashpw(passwordPlano, BCrypt.gensalt(BCRYPT_COST));
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash.
     * Llama esto al INICIAR SESIÓN.
     *
     * @param passwordPlano Contraseña ingresada por el usuario
     * @param hashGuardado  Hash guardado en la base de datos
     * @return true si la contraseña es correcta
     */
    public static boolean verificar(String passwordPlano, String hashGuardado) {
        if (passwordPlano == null || hashGuardado == null) return false;
        try {
            return BCrypt.checkpw(passwordPlano, hashGuardado);
        } catch (Exception e) {
            return false;
        }
    }
}
