package servicio;

import dao.LibroDAO;
import modelo.Libro;

import java.sql.SQLException;
import java.util.List;

public class RecomendacionService {

    private final LibroDAO libroDAO = new LibroDAO();

    /**
     * Recomienda hasta 5 libros relacionados con el libro seleccionado:
     * primero los del mismo autor y después los de la misma categoría.
     */
    public List<Libro> recomendar(int idLibro) throws BibliotecaException {
        try {
            Libro actual = libroDAO.buscarPorId(idLibro);
            if (actual == null) {
                throw new BibliotecaException("El libro no existe.");
            }
            return libroDAO.relacionados(idLibro, actual.getIdCategoria(), actual.getAutor());
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudieron obtener recomendaciones: " + e.getMessage(), e);
        }
    }
}
