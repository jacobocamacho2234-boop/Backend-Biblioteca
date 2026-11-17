package dao;

import modelo.Libro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {

    private static final String SELECT =
            "SELECT l.id_libro, l.titulo, l.autor, l.isbn, l.id_categoria, c.nombre AS categoria, "
            + "l.ejemplares_totales, l.ejemplares_disponibles "
            + "FROM Libro l LEFT JOIN Categoria c ON l.id_categoria = c.id_categoria ";

    private Libro mapear(ResultSet rs) throws SQLException {
        Libro l = new Libro();
        l.setIdLibro(rs.getInt("id_libro"));
        l.setTitulo(rs.getString("titulo"));
        l.setAutor(rs.getString("autor"));
        l.setIsbn(rs.getString("isbn"));
        l.setIdCategoria(rs.getInt("id_categoria"));
        l.setNombreCategoria(rs.getString("categoria"));
        l.setEjemplaresTotales(rs.getInt("ejemplares_totales"));
        l.setEjemplaresDisponibles(rs.getInt("ejemplares_disponibles"));
        return l;
    }

    // Ejecuta una consulta SELECT con parámetros y devuelve la lista de libros
    private List<Libro> consultar(String sql, Object... parametros) throws SQLException {
        List<Libro> lista = new ArrayList<>();
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

    public List<Libro> listar() throws SQLException {
        return consultar(SELECT + "ORDER BY l.titulo");
    }

    /** Busca por título o autor. idCategoria = 0 significa "todas las categorías". */
    public List<Libro> buscar(String texto, int idCategoria) throws SQLException {
        String patron = "%" + (texto == null ? "" : texto.trim()) + "%";
        return consultar(SELECT
                + "WHERE (l.titulo LIKE ? OR l.autor LIKE ?) AND (? = 0 OR l.id_categoria = ?) "
                + "ORDER BY l.titulo",
                patron, patron, idCategoria, idCategoria);
    }

    public Libro buscarPorId(int idLibro) throws SQLException {
        List<Libro> lista = consultar(SELECT + "WHERE l.id_libro = ?", idLibro);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /** Libros de la misma categoría o del mismo autor, sin incluir el libro actual. */
    public List<Libro> relacionados(int idLibro, int idCategoria, String autor) throws SQLException {
        return consultar(SELECT
                + "WHERE l.id_libro <> ? AND (l.id_categoria = ? OR l.autor = ?) "
                + "ORDER BY (l.autor = ?) DESC, l.titulo LIMIT 5",
                idLibro, idCategoria, autor, autor);
    }

    public void insertar(Libro l) throws SQLException {
        String sql = "INSERT INTO Libro (titulo, autor, isbn, id_categoria, "
                + "ejemplares_totales, ejemplares_disponibles) VALUES (?,?,?,?,?,?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            llenar(ps, l);
            ps.setInt(6, l.getEjemplaresDisponibles());
            ps.executeUpdate();
        }
    }

    public void actualizar(Libro l) throws SQLException {
        String sql = "UPDATE Libro SET titulo = ?, autor = ?, isbn = ?, id_categoria = ?, "
                + "ejemplares_totales = ?, ejemplares_disponibles = ? WHERE id_libro = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            llenar(ps, l);
            ps.setInt(6, l.getEjemplaresDisponibles());
            ps.setInt(7, l.getIdLibro());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idLibro) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM Libro WHERE id_libro = ?")) {
            ps.setInt(1, idLibro);
            ps.executeUpdate();
        }
    }

    // Llena los primeros 5 parámetros, que son iguales en insertar y actualizar
    private void llenar(PreparedStatement ps, Libro l) throws SQLException {
        ps.setString(1, l.getTitulo());
        ps.setString(2, l.getAutor());
        // El ISBN es UNIQUE: si está vacío se guarda NULL para no chocar con otros vacíos
        if (l.getIsbn() == null || l.getIsbn().trim().isEmpty()) {
            ps.setNull(3, Types.VARCHAR);
        } else {
            ps.setString(3, l.getIsbn().trim());
        }
        if (l.getIdCategoria() == 0) {
            ps.setNull(4, Types.INTEGER);
        } else {
            ps.setInt(4, l.getIdCategoria());
        }
        ps.setInt(5, l.getEjemplaresTotales());
    }
}
