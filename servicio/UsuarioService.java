package servicio;

import dao.UsuarioDAO;
import modelo.Usuario;

import java.sql.SQLException;
import java.util.List;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Usuario> listarUsuarios() throws BibliotecaException {
        try {
            return usuarioDAO.listar();
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudieron consultar los usuarios: " + e.getMessage(), e);
        }
    }

    /** tipo debe ser "alumno", "docente" o "admin". */
    public void registrarUsuario(String nombre, String correo, String tipo) throws BibliotecaException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new BibliotecaException("El nombre es obligatorio.");
        }
        if (!"alumno".equals(tipo) && !"docente".equals(tipo) && !"admin".equals(tipo)) {
            throw new BibliotecaException("El tipo debe ser alumno, docente o admin.");
        }
        Usuario u = new Usuario();
        u.setNombre(nombre.trim());
        u.setCorreo(correo);
        u.setTipo(tipo);
        try {
            usuarioDAO.insertar(u);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // correo repetido
                throw new BibliotecaException("Ya existe un usuario con ese correo.", e);
            }
            throw new BibliotecaException("No se pudo registrar el usuario: " + e.getMessage(), e);
        }
    }
}
