package dao;

import modelo.Multa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MultaDAO {

    /** Multas sin pagar. idUsuario = 0 significa "de todos los usuarios". */
    public List<Multa> listarPendientes(int idUsuario) throws SQLException {
        String sql = "SELECT m.id_multa, m.id_prestamo, m.monto, m.pagada, l.titulo, u.nombre "
                + "FROM Multa m "
                + "JOIN Prestamo p ON m.id_prestamo = p.id_prestamo "
                + "JOIN Libro l ON p.id_libro = l.id_libro "
                + "JOIN Usuario u ON p.id_usuario = u.id_usuario "
                + "WHERE m.pagada = FALSE AND (? = 0 OR p.id_usuario = ?) "
                + "ORDER BY m.id_multa";
        List<Multa> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Multa m = new Multa();
                    m.setIdMulta(rs.getInt("id_multa"));
                    m.setIdPrestamo(rs.getInt("id_prestamo"));
                    m.setMonto(rs.getDouble("monto"));
                    m.setPagada(rs.getBoolean("pagada"));
                    m.setTituloLibro(rs.getString("titulo"));
                    m.setNombreUsuario(rs.getString("nombre"));
                    lista.add(m);
                }
            }
        }
        return lista;
    }

    /** Suma de las multas sin pagar de un usuario. */
    public double totalPendiente(int idUsuario) throws SQLException {
        String sql = "SELECT COALESCE(SUM(m.monto), 0) FROM Multa m "
                + "JOIN Prestamo p ON m.id_prestamo = p.id_prestamo "
                + "WHERE p.id_usuario = ? AND m.pagada = FALSE";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    /** Devuelve true si la multa existía y estaba pendiente. */
    public boolean marcarPagada(int idMulta) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE Multa SET pagada = TRUE WHERE id_multa = ? AND pagada = FALSE")) {
            ps.setInt(1, idMulta);
            return ps.executeUpdate() > 0;
        }
    }
}
