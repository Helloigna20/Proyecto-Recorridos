package clases.interfaz_grafica;

import javax.swing.*;

/**
 * Punto de entrada de la interfaz gráfica del simulador de despacho.
 *
 * Para ejecutar:
 *   1. Asegurate de que muestra1.geojson esté en la raíz del proyecto (o en archivos/).
 *   2. Compilá incluyendo libs/json-20240303.jar en el classpath.
 *   3. Ejecutá este main desde la raíz del proyecto.
 *
 * Ejemplo de compilación (desde la raíz del proyecto):
 *   javac -cp "libs/json-20240303.jar;." clases/interfaz_grafica/MainGUI.java
 *
 * Ejemplo de ejecución:
 *   java -cp "libs/json-20240303.jar;." clases.interfaz_grafica.MainGUI
 */
public class MainGUI {

    public static void main(String[] args) {
        // Usar el Look & Feel del sistema operativo para mejor integración
        try {
            // Intentar Nimbus primero (más moderno)
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());

                    // Personalizar Nimbus para que coincida con nuestra paleta
                    UIManager.put("control",           new java.awt.Color(0xF0F2F5));
                    UIManager.put("info",              new java.awt.Color(0xE3F2FD));
                    UIManager.put("nimbusBase",        new java.awt.Color(0x1976D2));
                    UIManager.put("nimbusLightBackground", java.awt.Color.WHITE);
                    UIManager.put("text",              new java.awt.Color(0x212529));
                    UIManager.put("ScrollBar.thumbHighlight", new java.awt.Color(0xBBDEFB));
                    break;
                }
            }
        } catch (Exception e) {
            // Si Nimbus no está disponible, usamos el LAF del sistema
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { /* usamos el default */ }
        }

        // Lanzar la ventana en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            System.out.println("=================================================");
            System.out.println("  SIMULADOR DE DESPACHO DE TAXIS - SALTA GUI");
            System.out.println("=================================================");
            new VentanaPrincipal();
        });
    }
}
