package servicio;

import dao.PrestamoDAO;
import modelo.Prestamo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AlertaService {

    /** Se avisa de los préstamos que vencen dentro de este número de días (o menos). */
    public static final int DIAS_AVISO = 2;

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    /** Alertas de todos los usuarios (vista del bibliotecario). */
    public List<String> obtenerAlertas() throws BibliotecaException {
        return generar(0);
    }

    /** Alertas de un solo usuario. */
    public List<String> obtenerAlertas(int idUsuario) throws BibliotecaException {
        return generar(idUsuario);
    }

    private List<String> generar(int idUsuario) throws BibliotecaException {
        List<String> alertas = new ArrayList<>();
        try {
            LocalDate hoy = LocalDate.now();
            for (Prestamo p : prestamoDAO.listarAlertas(DIAS_AVISO, idUsuario)) {
                long dias = ChronoUnit.DAYS.between(hoy, p.getFechaLimite());
                String quien = p.getNombreUsuario();
                String libro = "\"" + p.getTituloLibro() + "\"";
                if (dias < 0) {
                    long retraso = -dias;
                    double multaActual = retraso * MultaService.TARIFA_POR_DIA;
                    alertas.add(String.format(Locale.US,
                            "VENCIDO: %s (%s) lleva %d día(s) de retraso. Multa acumulada: $%.2f",
                            libro, quien, retraso, multaActual));
                } else if (dias == 0) {
                    alertas.add("VENCE HOY: " + libro + " (" + quien + ")");
                } else {
                    alertas.add("Por vencer: " + libro + " (" + quien + ") vence en "
                            + dias + " día(s)");
                }
            }
            return alertas;
        } catch (SQLException e) {
            throw new BibliotecaException("No se pudieron consultar las alertas: " + e.getMessage(), e);
        }
    }
}
