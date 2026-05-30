package clases.interfaz_grafica;

import clases.codigo.SolicitudViaje;
import clases.codigo.Unidad;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Panel lateral derecho con:
 *  1. Sección de vehículos disponibles (chips con estado visual).
 *  2. Cola de solicitudes con tarjetas detalladas.
 */
public class PanelLateral extends JPanel {

    // ============================================================
    //  PALETA
    // ============================================================
    private static final Color COLOR_BG         = new Color(0xF8F9FA);
    private static final Color COLOR_CARD        = Color.WHITE;
    private static final Color COLOR_HEADER_BG   = new Color(0xFFFFFF);
    private static final Color COLOR_BORDER      = new Color(0xE9ECEF);
    private static final Color COLOR_TEXTO       = new Color(0x212529);
    private static final Color COLOR_TEXTO_SEC   = new Color(0x6C757D);
    private static final Color COLOR_DISP        = new Color(0x198754);
    private static final Color COLOR_OCUP        = new Color(0x6C757D);
    private static final Color COLOR_BADGE_OK    = new Color(0x198754);
    private static final Color COLOR_BADGE_FAIL  = new Color(0xDC3545);
    private static final Color COLOR_RUTA        = new Color(0x0D6EFD);
    private static final Color COLOR_ETA         = new Color(0x495057);

    // ============================================================
    //  COMPONENTES
    // ============================================================
    private final ModeloSimulacion modelo;
    private final JPanel           panelFlota;
    private final JPanel           panelSolicitudes;
    private final JLabel           lblSinSolicitudes;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================
    public PanelLateral(ModeloSimulacion modelo) {
        this.modelo = modelo;
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(370, 600));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, COLOR_BORDER));
        setLayout(new BorderLayout(0, 0));

        JPanel panelNorte = new JPanel();
        panelNorte.setLayout(new BoxLayout(panelNorte, BoxLayout.Y_AXIS));
        panelNorte.setBackground(COLOR_BG);
        panelNorte.setBorder(new EmptyBorder(16, 16, 16, 16));

        // --- Sección vehículos ---
        JLabel lblVehiculos = crearTituloSeccion("🚕  Vehículos Disponibles");
        panelNorte.add(lblVehiculos);
        panelNorte.add(Box.createVerticalStrut(10));

        panelFlota = new JPanel(new GridLayout(0, 4, 6, 6));
        panelFlota.setBackground(COLOR_HEADER_BG);
        panelFlota.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        panelFlota.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelNorte.add(panelFlota);
        panelNorte.add(Box.createVerticalStrut(22));

        // --- Sección solicitudes ---
        JLabel lblSolicitudes = crearTituloSeccion("📋  Cola de Solicitudes");
        panelNorte.add(lblSolicitudes);
        panelNorte.add(Box.createVerticalStrut(10));

        add(panelNorte, BorderLayout.NORTH);

        panelSolicitudes = new JPanel();
        panelSolicitudes.setLayout(new BoxLayout(panelSolicitudes, BoxLayout.Y_AXIS));
        panelSolicitudes.setBackground(COLOR_BG);
        panelSolicitudes.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblSinSolicitudes = new JLabel("Aún no hay solicitudes procesadas.");
        lblSinSolicitudes.setFont(new Font("SansSerif", Font.ITALIC, 13));
        lblSinSolicitudes.setForeground(COLOR_TEXTO_SEC);
        lblSinSolicitudes.setBorder(new EmptyBorder(12, 4, 12, 4));
        panelSolicitudes.add(lblSinSolicitudes);

        JScrollPane scroll = new JScrollPane(panelSolicitudes);
        scroll.setBorder(new EmptyBorder(0, 16, 16, 16));
        scroll.setBackground(COLOR_BG);
        scroll.getViewport().setBackground(COLOR_BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        // Scrollbar moderna
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200, 200, 200);
                this.trackColor = COLOR_BG;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return crearBotonVacio();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return crearBotonVacio();
            }
            private JButton crearBotonVacio() {
                JButton jbutton = new JButton();
                jbutton.setPreferredSize(new Dimension(0, 0));
                jbutton.setMinimumSize(new Dimension(0, 0));
                jbutton.setMaximumSize(new Dimension(0, 0));
                return jbutton;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 6, 6);
                g2.dispose();
            }
        });
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        
        add(scroll, BorderLayout.CENTER);
    }

    // ============================================================
    //  ACTUALIZACIÓN DE FLOTA
    // ============================================================
    /** Reconstruye los chips de la flota. Llamar después de cargar el mapa. */
    public void actualizarFlota() {
        panelFlota.removeAll();
        for (Unidad u : modelo.getListaUnidades()) {
            panelFlota.add(crearChipTaxi(u));
        }
        panelFlota.revalidate();
        panelFlota.repaint();
    }

    /** Crea un chip visual para un taxi */
    private JLabel crearChipTaxi(Unidad u) {
        String num   = u.getIdVehiculo().replaceAll("[^0-9]", "");
        JLabel chip  = new JLabel("🚖 Taxi " + num) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = u.isDisponible() ? COLOR_DISP : COLOR_OCUP;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setForeground(Color.WHITE);
        chip.setFont(new Font("SansSerif", Font.BOLD, 11));
        chip.setBorder(new EmptyBorder(4, 10, 4, 10));
        chip.setOpaque(false);
        chip.setPreferredSize(new Dimension(chip.getPreferredSize().width + 4, 24));
        return chip;
    }

    // ============================================================
    //  AGREGAR TARJETA DE SOLICITUD
    // ============================================================
    /**
     * Agrega una nueva tarjeta al tope de la lista de solicitudes.
     * @param solicitud la solicitud procesada
     * @param rutaNodos lista de IDs de nodos de la ruta
     */
    public void agregarSolicitud(SolicitudViaje solicitud, List<Integer> rutaNodos) {
        lblSinSolicitudes.setVisible(false);

        JPanel tarjeta = crearTarjetaSolicitud(solicitud, rutaNodos);
        panelSolicitudes.add(tarjeta, 0);   // Insertar al tope (más reciente primero)
        panelSolicitudes.add(Box.createVerticalStrut(10), 1);

        // Actualizar chips de flota (estados pueden haber cambiado)
        actualizarFlota();

        panelSolicitudes.revalidate();
        panelSolicitudes.repaint();
    }

    /** Construye la tarjeta visual de una solicitud */
    private JPanel crearTarjetaSolicitud(SolicitudViaje solicitud, List<Integer> rutaNodos) {
        boolean exito = (solicitud.getUnidadAsignada() != null);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Sombra
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(2, 3, getWidth() - 2, getHeight() - 2, 12, 12);
                // Fondo blanco
                g2.setColor(COLOR_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 12, 12);
                // Borde izquierdo de color
                g2.setColor(exito ? COLOR_BADGE_OK : COLOR_BADGE_FAIL);
                g2.fillRoundRect(0, 0, 4, getHeight() - 2, 4, 4);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(8, 4));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // --- Fila superior: usuario + badge estado ---
        JPanel filaSuperior = new JPanel(new BorderLayout());
        filaSuperior.setOpaque(false);

        String nomUsuario = solicitud.getPasajero().getIdUsuario();
        JLabel lblUsuario = new JLabel(nomUsuario);
        lblUsuario.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblUsuario.setForeground(COLOR_TEXTO);

        JLabel badge = crearBadge(exito ? "✓ asignado" : "✗ sin unidad",
                                   exito ? COLOR_BADGE_OK : COLOR_BADGE_FAIL);

        filaSuperior.add(lblUsuario, BorderLayout.WEST);
        filaSuperior.add(badge,      BorderLayout.EAST);
        card.add(filaSuperior, BorderLayout.NORTH);

        // --- Cuerpo: ruta + ETA ---
        JPanel cuerpo = new JPanel();
        cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
        cuerpo.setOpaque(false);

        String txtRuta = "Nodo " + solicitud.getPasajero().getIdNodoInterseccion()
                       + " → Nodo " + solicitud.getIdNodoDestino();
        JLabel lblRuta = new JLabel("🗺 " + txtRuta);
        lblRuta.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblRuta.setForeground(COLOR_RUTA);
        lblRuta.setBorder(new EmptyBorder(4, 0, 2, 0));
        cuerpo.add(lblRuta);

        if (exito) {
            String eta    = solicitud.getEtaFinal().obtenerTiempoFormateado();
            String vehic  = solicitud.getUnidadAsignada().getIdVehiculo();

            JLabel lblVehiculo = new JLabel("🚕 " + vehic);
            lblVehiculo.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblVehiculo.setForeground(COLOR_TEXTO_SEC);
            cuerpo.add(lblVehiculo);

            JLabel lblETA = new JLabel("⏱ ETA: " + eta
                          + "   |   " + rutaNodos.size() + " nodos recorridos");
            lblETA.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblETA.setForeground(COLOR_ETA);
            lblETA.setBorder(new EmptyBorder(2, 0, 0, 0));
            cuerpo.add(lblETA);
        } else {
            JLabel lblFail = new JLabel("No se encontró unidad disponible.");
            lblFail.setFont(new Font("SansSerif", Font.ITALIC, 11));
            lblFail.setForeground(COLOR_BADGE_FAIL);
            cuerpo.add(lblFail);
        }

        card.add(cuerpo, BorderLayout.CENTER);
        return card;
    }

    /** Crea un badge redondeado con color de fondo */
    private JLabel crearBadge(String texto, Color color) {
        JLabel badge = new JLabel(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        badge.setOpaque(false);
        return badge;
    }

    // ============================================================
    //  HELPERS
    // ============================================================
    private JLabel crearTituloSeccion(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(COLOR_TEXTO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 0, 0));
        return lbl;
    }
}
