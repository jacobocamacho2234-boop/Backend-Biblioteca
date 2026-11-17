package dao;

import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private static final String SELECT =
            "SELECT id_usuario, nombre, correo, tipo, activo FROM Usuario ";

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setCorreo(rs.getString("correo"));
        u.setTipo(rs.getString("tipo"));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }

    /** Lista los usuarios activos. */
    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE activo = TRUE ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Usuario buscarPorId(int idUsuario) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public void insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO Usuario (nombre, correo, tipo, activo) VALUES (?,?,?,TRUE)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            // El correo es UNIQUE: si está vacío se guarda NULL
            if (u.getCorreo() == null || u.getCorreo().trim().isEmpty()) {
                ps.setNull(2, java.sql.Types.VARCHAR);
            } else {
                ps.setString(2, u.getCorreo().trim());
            }
            ps.setString(3, u.getTipo());
            ps.executeUpdate();
        }
    }
}
