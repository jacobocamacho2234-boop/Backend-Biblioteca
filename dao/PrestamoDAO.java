package dao;

import modelo.Prestamo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    private static final String SELECT =
            "SELECT p.id_prestamo, p.id_usuario, p.id_libro, p.fecha_prestamo, p.fecha_limite, "
            + "p.fecha_devolucion, p.estado, l.titulo, u.nombre "
            + "FROM Prestamo p "
            + "JOIN Libro l ON p.id_libro = l.id_libro "
            + "JOIN Usuario u ON p.id_usuario = u.id_usuario ";

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setIdPrestamo(rs.getInt("id_prestamo"));
        p.setIdUsuario(rs.getInt("id_usuario"));
        p.setIdLibro(rs.getInt("id_libro"));
        p.setFechaPrestamo(rs.getDate("fecha_prestamo").toLocalDate());
        p.setFechaLimite(rs.getDate("fecha_limite").toLocalDate());
        Date devolucion = rs.getDate("fecha_devolucion");
        p.setFechaDevolucion(devolucion == null ? null : devolucion.toLocalDate());
        p.setEstado(rs.getString("estado"));
        p.setTituloLibro(rs.getString("titulo"));
        p.setNombreUsuario(rs.getString("nombre"));
        return p;
    }

    private List<Prestamo> consultar(String sql, Object... parametros) throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private int contar(String sql, Object... parametros) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public Prestamo buscarPorId(int idPrestamo) throws SQLException {
        List<Prestamo> lista = consultar(SELECT + "WHERE p.id_prestamo = ?", idPrestamo);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** Todos los préstamos que siguen activos, los más urgentes primero. */
    public List<Prestamo> listarActivos() throws SQLException {
        return consultar(SELECT + "WHERE p.estado = 'activo' ORDER BY p.fecha_limite");
    }

    public List<Prestamo> listarActivosPorUsuario(int idUsuario) throws SQLException {
        return consultar(SELECT + "WHERE p.estado = 'activo' AND p.id_usuario = ? ORDER BY p.fecha_limite",
                idUsuario);
    }

    /**
     * Préstamos activos que ya vencieron o que vencen en los próximos "dias" días.
     * idUsuario = 0 significa "de todos los usuarios".
     */
    public List<Prestamo> listarAlertas(int dias, int idUsuario) throws SQLException {
        Date limite = Date.valueOf(LocalDate.now().plusDays(dias));
        return consultar(SELECT
                + "WHERE p.estado = 'activo' AND p.fecha_limite <= ? AND (? = 0 OR p.id_usuario = ?) "
                + "ORDER BY p.fecha_limite",
                limite, idUsuario, idUsuario);
    }

    public int contarActivos(int idUsuario) throws SQLException {
        return contar("SELECT COUNT(*) FROM Prestamo WHERE id_usuario = ? AND estado = 'activo'",
                idUsuario);
    }

    public int contarVencidos(int idUsuario) throws SQLException {
        return contar("SELECT COUNT(*) FROM Prestamo WHERE id_usuario = ? AND estado = 'activo' "
                + "AND fecha_limite < ?",
                idUsuario, Date.valueOf(LocalDate.now()));
    }

    public boolean tieneActivo(int idUsuario, int idLibro) throws SQLException {
        return contar("SELECT COUNT(*) FROM Prestamo WHERE id_usuario = ? AND id_libro = ? "
                + "AND estado = 'activo'", idUsuario, idLibro) > 0;
    }

    /**
     * Registra el préstamo y descuenta un ejemplar. Las dos cosas se hacen en una
     * transacción: o se hacen las dos o no se hace ninguna.
     */
    public void registrar(Prestamo p) throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE Libro SET ejemplares_disponibles = ejemplares_disponibles - 1 "
                        + "WHERE id_libro = ? AND ejemplares_disponibles > 0")) {
                    ps.setInt(1, p.getIdLibro());
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("No hay ejemplares disponibles de este libro.");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO Prestamo (id_usuario, id_libro, fecha_prestamo, fecha_limite, estado) "
                        + "VALUES (?,?,?,?,'activo')")) {
                    ps.setInt(1, p.getIdUsuario());
                    ps.setInt(2, p.getIdLibro());
                    ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
                    ps.setDate(4, Date.valueOf(p.getFechaLimite()));
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Marca el préstamo como devuelto, regresa el ejemplar al inventario y,
     * si hubo retraso (montoMulta > 0), guarda la multa. Todo en una transacción.
     */
    public void devolver(int idPrestamo, LocalDate fechaDevolucion, double montoMulta) throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE Prestamo SET fecha_devolucion = ?, estado = 'devuelto' "
                        + "WHERE id_prestamo = ? AND estado = 'activo'")) {
                    ps.setDate(1, Date.valueOf(fechaDevolucion));
                    ps.setInt(2, idPrestamo);
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("El préstamo no existe o ya fue devuelto.");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE Libro SET ejemplares_disponibles = ejemplares_disponibles + 1 "
                        + "WHERE id_libro = (SELECT id_libro FROM Prestamo WHERE id_prestamo = ?)")) {
                    ps.setInt(1, idPrestamo);
                    ps.executeUpdate();
                }
                if (montoMulta > 0) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO Multa (id_prestamo, monto, pagada) VALUES (?,?,FALSE)")) {
                        ps.setInt(1, idPrestamo);
                        ps.setDouble(2, montoMulta);
                        ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
