package backend;

public class Usuario {

    private int idUsuario;
    private String nombre;
    private String correo;
    private String tipo;      // alumno, docente o admin
    private boolean activo;

    public Usuario() {
    }

    public Usuario(int idUsuario, String nombre, String correo, String tipo, boolean activo) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.tipo = tipo;
        this.activo = activo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Para que un JComboBox muestre el nombre directamente
    @Override
    public String toString() {
        return nombre;
    }
}
