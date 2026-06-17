package modelo;

import java.sql.Timestamp;

/**
 * Modelo que representa un usuario registrado o administrador.
 * Los pedidos de invitados NO tienen un objeto Usuario asociado.
 */
public class Usuario {

    private int       id;
    private String    nombre;
    private String    apellido;
    private String    telefono;
    private String    email;
    private String    password;           // Hash BCrypt — nunca texto plano
    private boolean   esAdmin;
    private boolean   primeraCompraUsada;
    private Timestamp fechaRegistro;

    // ─── Constructores ────────────────────────────────────

    public Usuario() {}

    /** Constructor para registro nuevo */
    public Usuario(String nombre, String apellido, String telefono,
                   String email, String password) {
        this.nombre   = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email    = email;
        this.password = password;
        this.esAdmin            = false;
        this.primeraCompraUsada = false;
    }

    // ─── Getters y Setters ────────────────────────────────

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public String getNombre()               { return nombre; }
    public void setNombre(String nombre)    { this.nombre = nombre; }

    public String getApellido()             { return apellido; }
    public void setApellido(String a)       { this.apellido = a; }

    public String getNombreCompleto()       { return nombre + " " + apellido; }

    public String getTelefono()             { return telefono; }
    public void setTelefono(String t)       { this.telefono = t; }

    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }

    public String getPassword()             { return password; }
    public void setPassword(String p)       { this.password = p; }

    public boolean isEsAdmin()              { return esAdmin; }
    public void setEsAdmin(boolean a)       { this.esAdmin = a; }

    public boolean isPrimeraCompraUsada()   { return primeraCompraUsada; }
    public void setPrimeraCompraUsada(boolean p) { this.primeraCompraUsada = p; }

    public Timestamp getFechaRegistro()     { return fechaRegistro; }
    public void setFechaRegistro(Timestamp f) { this.fechaRegistro = f; }

    /**
     * Devuelve true si el usuario puede usar el descuento del 5%.
     * Condiciones: no admin, no usada antes, y dentro de los 10 días.
     */
    public boolean puedeUsarDescuentoPrimeraCompra() {
        if (esAdmin || primeraCompraUsada || fechaRegistro == null) return false;
        long ahora   = System.currentTimeMillis();
        long registro = fechaRegistro.getTime();
        long diasTranscurridos = (ahora - registro) / (1000L * 60 * 60 * 24);
        return diasTranscurridos <= 10;
    }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", email=" + email + ", esAdmin=" + esAdmin + "}";
    }
}
