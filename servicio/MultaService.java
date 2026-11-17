package servicio;

import dao.MultaDAO;
import modelo.Multa;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MultaService {

    /** Pesos que se cobran por cada día de retraso. */
    public static final double TARIFA_POR_DIA = 5.0;

    private final MultaDAO multaDAO = new MultaDAO();

    /** Días de retraso x tarifa. Si se devolvió a tiempo, la multa es 0. */
    public double calcularMulta(LocalDate fechaLimite, LocalDate fechaDevolucion) {
        long diasRetraso = ChronoUnit.DAYS.between(fechaLimite, fechaDevolucion);
        return diasRetraso > 0 ? diasRetraso * TARIFA_POR_DIA : 0;
    }

    /** Multas sin pagar. idUsuario = 0 para ver las de todos. */
    public List<Multa> listarPendientes(int idUsuario) throws BibliotecaException {
        try {
            return multaDAO.listarPendientes(idUsuario);
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudieron consultar las multas: " + e.getMessage(), e);
        }
    }

    public double totalPendiente(int idUsuario) throws BibliotecaException {
        try {
            return multaDAO.totalPendiente(idUsuario);
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudo consultar el total de multas: " + e.getMessage(), e);
        }
    }

    public void pagar(int idMulta) throws BibliotecaException {
        try {
            if (!multaDAO.marcarPagada(idMulta)) {
                throw new BibliotecaException("La multa no existe o ya estaba pagada.");
            }
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudo registrar el pago: " + e.getMessage(), e);
        }
    }
}
