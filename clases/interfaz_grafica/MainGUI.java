package clases.interfaz_grafica;

import javax.swing.*;

/**
 * Punto de entrada de la interfaz gráfica del simulador de despacho.
 *
 * Para ejecutar:
 *   1. Asegurate de que CentroyMacroSALTA.geojson esté en la raíz del proyecto (o en archivos/).
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
        // Usar el Look & Feel Nimbus con tema oscuro
        try {
            // Intentar Nimbus primero (más moderno)
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());

                    // Personalizar Nimbus — paleta oscura
                    UIManager.put("control",               new java.awt.Color(0x1A1A2E));
                    UIManager.put("info",                  new java.awt.Color(0x16213E));
                    UIManager.put("nimbusBase",            new java.awt.Color(0x1565C0));
                    UIManager.put("nimbusLightBackground", new java.awt.Color(0x12121E));
                    UIManager.put("nimbusDisabledText",    new java.awt.Color(0x546E7A));
                    UIManager.put("nimbusFocus",           new java.awt.Color(0x1E88E5));
                    UIManager.put("nimbusSelectedText",    new java.awt.Color(0xE0E0E0));
                    UIManager.put("nimbusSelectionBackground", new java.awt.Color(0x1565C0));
                    UIManager.put("text",                  new java.awt.Color(0xE0E0E0));
                    UIManager.put("ScrollBar.thumbHighlight", new java.awt.Color(0x263238));
                    UIManager.put("OptionPane.background", new java.awt.Color(0x1A1A2E));
                    UIManager.put("Panel.background",      new java.awt.Color(0x12121E));
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