package pruebas;

import modelo.Libro;
import modelo.Prestamo;
import servicio.AlertaService;
import servicio.BibliotecaException;
import servicio.CatalogoService;
import servicio.PrestamoService;
import servicio.RecomendacionService;

/**
 * Programa de consola para probar el back end SIN necesidad del front end.
 * Ejecútenlo (clic derecho > Run File) después de crear la base de datos
 * y cargar los datos de prueba. Si imprime los libros, la conexión funciona.
 */
public class Prueba {

    public static void main(String[] args) {
        try {
            CatalogoService catalogo = new CatalogoService();
            PrestamoService prestamos = new PrestamoService();

            System.out.println("=== Catálogo ===");
            for (Libro l : catalogo.listarLibros()) {
                System.out.println(l.getIdLibro() + " | " + l.getTitulo() + " | " + l.getAutor()
                        + " | " + l.getNombreCategoria()
                        + " | disponibles: " + l.getEjemplaresDisponibles()
                        + "/" + l.getEjemplaresTotales());
            }

            System.out.println("\n=== Búsqueda: 'java' ===");
            for (Libro l : catalogo.buscar("java", 0)) {
                System.out.println(l);
            }

            System.out.println("\n=== Recomendaciones para el libro 1 ===");
            for (Libro l : new RecomendacionService().recomendar(1)) {
                System.out.println(l);
            }

            System.out.println("\n=== Alertas ===");
            for (String alerta : new AlertaService().obtenerAlertas()) {
                System.out.println(alerta);
            }

            System.out.println("\n=== Préstamos activos ===");
            for (Prestamo p : prestamos.listarActivos()) {
                System.out.println(p.getIdPrestamo() + " | " + p.getTituloLibro() + " | "
                        + p.getNombreUsuario() + " | límite: " + p.getFechaLimite());
            }

            // Prueba de préstamo: el usuario 4 pide el libro 4.
            // La primera vez funciona; si lo ejecutan otra vez, mostrará el error de
            // validación "ya tiene prestado este libro" (también es una buena prueba).
            System.out.println("\n=== Prestar libro 4 al usuario 4 ===");
            System.out.println(prestamos.prestar(4, 4));

        } catch (BibliotecaException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}
