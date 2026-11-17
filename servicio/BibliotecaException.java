package servicio;

/**
 * Error "entendible" de la biblioteca. El mensaje (getMessage) está escrito para
 * mostrárselo directamente al usuario, por ejemplo en un JOptionPane:
 *
 *   catch (BibliotecaException e) {
 *       JOptionPane.showMessageDialog(this, e.getMessage());
 *   }
 */
public class BibliotecaException extends Exception {

    public BibliotecaException(String mensaje) {
        super(mensaje);
    }

    public BibliotecaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
