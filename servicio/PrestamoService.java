package servicio;

import dao.LibroDAO;
import dao.MultaDAO;
import dao.PrestamoDAO;
import dao.UsuarioDAO;
import modelo.Libro;
import modelo.Prestamo;
import modelo.Usuario;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class PrestamoService {

    /** Días que dura un préstamo. */
    public static final int DIAS_PRESTAMO = 7;
    /** Máximo de libros que un usuario puede tener prestados a la vez. */
    public static final int MAX_PRESTAMOS = 3;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final LibroDAO libroDAO = new LibroDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final MultaDAO multaDAO = new MultaDAO();
    private final MultaService multaService = new MultaService();

    /**
     * Presta un libro a un usuario, validando antes todas las reglas.
     *
     * @return mensaje de éxito para mostrar en pantalla
     * @throws BibliotecaException con el motivo si no se puede prestar
     */
    public String prestar(int idUsuario, int idLibro) throws BibliotecaException {
        try {
            Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
            if (usuario == null || !usuario.isActivo()) {
                throw new BibliotecaException("El usuario no existe o está inactivo.");
            }

            Libro libro = libroDAO.buscarPorId(idLibro);
            if (libro == null) {
                throw new BibliotecaException("El libro no existe.");
            }
            if (libro.getEjemplaresDisponibles() <= 0) {
                throw new BibliotecaException("No hay ejemplares disponibles de \""
                        + libro.getTitulo() + "\".");
            }

            double multasPendientes = multaDAO.totalPendiente(idUsuario);
            if (multasPendientes > 0) {
                throw new BibliotecaException(String.format(Locale.US,
                        "%s tiene multas pendientes por $%.2f. Debe pagarlas antes de pedir otro libro.",
                        usuario.getNombre(), multasPendientes));
            }
            if (prestamoDAO.contarVencidos(idUsuario) > 0) {
                throw new BibliotecaException(usuario.getNombre()
                        + " tiene libros vencidos sin devolver.");
            }
            if (prestamoDAO.contarActivos(idUsuario) >= MAX_PRESTAMOS) {
                throw new BibliotecaException(usuario.getNombre() + " ya tiene "
                        + MAX_PRESTAMOS + " libros prestados (el máximo permitido).");
            }
            if (prestamoDAO.tieneActivo(idUsuario, idLibro)) {
                throw new BibliotecaException(usuario.getNombre()
                        + " ya tiene prestado este libro.");
            }

            Prestamo p = new Prestamo();
            p.setIdUsuario(idUsuario);
            p.setIdLibro(idLibro);
            p.setFechaPrestamo(LocalDate.now());
            p.setFechaLimite(LocalDate.now().plusDays(DIAS_PRESTAMO));
            prestamoDAO.registrar(p);

            return "Préstamo registrado: \"" + libro.getTitulo() + "\" para "
                    + usuario.getNombre() + ". Fecha límite de devolución: " + p.getFechaLimite() + ".";
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudo registrar el préstamo: " + e.getMessage(), e);
        }
    }

    /**
     * Registra la devolución de un préstamo. Si hay retraso, genera la multa.
     *
     * @return mensaje para mostrar en pantalla
     */
    public String devolver(int idPrestamo) throws BibliotecaException {
        try {
            Prestamo p = prestamoDAO.buscarPorId(idPrestamo);
            if (p == null) {
                throw new BibliotecaException("El préstamo no existe.");
            }
            if (!"activo".equals(p.getEstado())) {
                throw new BibliotecaException("Este préstamo ya fue devuelto.");
            }

            LocalDate hoy = LocalDate.now();
            double multa = multaService.calcularMulta(p.getFechaLimite(), hoy);
            prestamoDAO.devolver(idPrestamo, hoy, multa);

            String mensaje = "Devolución registrada: \"" + p.getTituloLibro() + "\".";
            if (multa > 0) {
                mensaje += String.format(Locale.US,
                        " Se devolvió con retraso: multa de $%.2f.", multa);
            }
            return mensaje;
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudo registrar la devolución: " + e.getMessage(), e);
        }
    }

    /** Todos los préstamos activos (para la tabla de la pestaña Préstamos). */
    public List<Prestamo> listarActivos() throws BibliotecaException {
        try {
            return prestamoDAO.listarActivos();
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudieron consultar los préstamos: " + e.getMessage(), e);
        }
    }

    public List<Prestamo> listarActivosPorUsuario(int idUsuario) throws BibliotecaException {
        try {
            return prestamoDAO.listarActivosPorUsuario(idUsuario);
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudieron consultar los préstamos: " + e.getMessage(), e);
        }
    }
}
