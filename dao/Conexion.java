package dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Única clase que sabe cómo conectarse a la base de datos.
 *
 * Los datos de conexión se leen del archivo "conexion.properties", que debe estar
 * en la carpeta del proyecto que se ejecuta (la que contiene el pom.xml).
 * Si el archivo no existe se usan los valores por defecto de abajo
 * (MariaDB local, usuario root, sin contraseña).
 *
 * Así cada integrante pone su propia contraseña sin tocar el código
 * y sin volver a generar el .jar del back end.
 */
public class Conexion {

    private static final String ARCHIVO = "conexion.properties";

    // Valores por defecto
    private static String url = "jdbc:mariadb://localhost:3306/biblioteca";
    private static String usuario = "root";
    private static String password = "";

    static {
        cargarConfiguracion();
    }

    private static void cargarConfiguracion() {
        File archivo = new File(ARCHIVO);
        if (!archivo.isFile()) {
            return;   // se quedan los valores por defecto
        }
        Properties p = new Properties();
        try (Reader lector = new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8)) {
            p.load(lector);
        } catch (IOException e) {
            System.err.println("No se pudo leer " + ARCHIVO + ": " + e.getMessage()
                    + ". Se usan los valores por defecto.");
            return;
        }
        url = p.getProperty("url", url).trim();
        usuario = p.getProperty("usuario", usuario).trim();
        password = p.getProperty("password", password);
    }

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, password);
    }
}
