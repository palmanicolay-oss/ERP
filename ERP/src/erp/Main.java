package erp;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    
    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            // Creamos un contenedor JFrame para alojar nuestro JPanel
            JFrame ventanaContenedora = new JFrame("ERP - Susurros de la Madera");
            ventanaContenedora.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ventanaContenedora.setSize(750, 450);
            ventanaContenedora.setLocationRelativeTo(null); // Centrar en pantalla

            // Instanciamos el JPanel de la ventana principal y lo agregamos a la ventana
            VentanaPrincipal panel = new VentanaPrincipal();
            ventanaContenedora.add(panel);

            ventanaContenedora.setVisible(true);
        });
    }
}