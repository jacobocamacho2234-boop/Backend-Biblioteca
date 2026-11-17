package servicio;

import dao.CategoriaDAO;
import dao.LibroDAO;
import modelo.Categoria;
import modelo.Libro;

import java.sql.SQLException;
import java.util.List;

public class CatalogoService {

    private final LibroDAO libroDAO = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Libro> listarLibros() throws BibliotecaException {
        try {
            return libroDAO.listar();
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudo consultar el catálogo: " + e.getMessage(), e);
        }
    }

    /** Busca por título o autor. idCategoria = 0 significa "todas las categorías". */
    public List<Libro> buscar(String texto, int idCategoria) throws BibliotecaException {
        try {
            return libroDAO.buscar(texto, idCategoria);
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudo realizar la búsqueda: " + e.getMessage(), e);
        }
    }

    public Libro obtenerLibro(int idLibro) throws BibliotecaException {
        try {
            Libro l = libroDAO.buscarPorId(idLibro);
            if (l == null) {
                throw new BibliotecaException("El libro no existe.");
            }
            return l;
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudo consultar el libro: " + e.getMessage(), e);
        }
    }

    public List<Categoria> listarCategorias() throws BibliotecaException {
        try {
            return categoriaDAO.listar();
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudieron consultar las categorías: " + e.getMessage(), e);
        }
    }

    /**
     * Guarda un libro: si idLibro es 0 lo inserta, si no, lo actualiza.
     * Solo hay que llenar título, autor, isbn, idCategoria y ejemplaresTotales;
     * los ejemplares disponibles se calculan aquí.
     */
    public void guardarLibro(Libro libro) throws BibliotecaException {
        if (libro.getTitulo() == null || libro.getTitulo().trim().isEmpty()) {
            throw new BibliotecaException("El título es obligatorio.");
        }
        if (libro.getAutor() == null || libro.getAutor().trim().isEmpty()) {
            throw new BibliotecaException("El autor es obligatorio.");
        }
        if (libro.getEjemplaresTotales() < 1) {
            throw new BibliotecaException("Debe haber al menos 1 ejemplar.");
        }
        try {
            if (libro.getIdLibro() == 0) {
                libro.setEjemplaresDisponibles(libro.getEjemplaresTotales());
                libroDAO.insertar(libro);
            } else {
                // Si cambia el total, el disponible cambia en la misma cantidad
                Libro anterior = libroDAO.buscarPorId(libro.getIdLibro());
                if (anterior == null) {
                    throw new BibliotecaException("El libro no existe.");
                }
                int disponibles = anterior.getEjemplaresDisponibles()
                        + (libro.getEjemplaresTotales() - anterior.getEjemplaresTotales());
                if (disponibles < 0) {
                    throw new BibliotecaException("No se pueden reducir tanto los ejemplares: "
                            + "hay copias prestadas actualmente.");
                }
                libro.setEjemplaresDisponibles(disponibles);
                libroDAO.actualizar(libro);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // entrada duplicada (ISBN repetido)
                throw new BibliotecaException("Ya existe un libro con ese ISBN.", e);
            }
            throw new BibliotecaException("No se pudo guardar el libro: " + e.getMessage(), e);
        }
    }

    public void eliminarLibro(int idLibro) throws BibliotecaException {
        try {
            libroDAO.eliminar(idLibro);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) { // el libro tiene préstamos registrados
                throw new BibliotecaException("No se puede eliminar: el libro tiene préstamos registrados.", e);
            }
            throw new BibliotecaException("No se pudo eliminar el libro: " + e.getMessage(), e);
        }
    }
}
