package clases.interfaz_grafica;

import clases.codigo.SolicitudViaje;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Ventana principal del simulador de despacho de taxis - Salta.
 * Integra el PanelMapa (izquierda) y el PanelLateral (derecha)
 * con un header de controles y barra de estado inferior.
 */
public class VentanaPrincipal extends JFrame {

    // ============================================================
    //  PALETA
    // ============================================================
    private static final Color COLOR_HEADER_BG   = new Color(0x1976D2);
    private static final Color COLOR_HEADER_TEXT  = Color.WHITE;
    private static final Color COLOR_STATUS_BG    = new Color(0xE3F2FD);
    private static final Color COLOR_STATUS_TEXT  = new Color(0x0D47A1);
    private static final Color COLOR_FONDO        = new Color(0xF0F2F5);

    // ============================================================
    //  COMPONENTES
    // ============================================================
    private final ModeloSimulacion modelo;
    private final PanelMapa        panelMapa;
    private final PanelLateral     panelLateral;

    private JButton  btnNuevaSolicitud;
    private JButton  btnLimpiar;
    private JButton  btnResetTaxis;
    private JLabel   lblEstado;
    private JLabel   lblCargando;

    // ============================================================
    //  CONSTRUCTOR
    // ============================================================
    public VentanaPrincipal() {
        super("🚕  Dashboard de Gestión de Transporte - Salta");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 620));

        modelo       = new ModeloSimulacion();
        panelMapa    = new PanelMapa(modelo);
        panelLateral = new PanelLateral(modelo);

        construirUI();
        configurarListeners();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        iniciarCargaMapa();
    }

    // ============================================================
    //  CONSTRUCCIÓN DE UI
    // ============================================================
    private void construirUI() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(COLOR_FONDO);

        add(crearHeader(),     BorderLayout.NORTH);
        add(crearCentro(),     BorderLayout.CENTER);
        add(crearBarraEstado(),BorderLayout.SOUTH);
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                    0, 0, COLOR_HEADER_BG,
                    getWidth(), 0, new Color(0x1565C0)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel titulo = new JLabel("Dashboard de Gestión de Transporte");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(COLOR_HEADER_TEXT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        btnNuevaSolicitud = crearBoton("● NUEVA SOLICITUD", new Color(0x0D47A1), Color.WHITE);
        btnLimpiar        = crearBoton("LIMPIAR SELECCIÓN", new Color(0x1976D2), Color.WHITE);
        btnLimpiar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 1, true),
            new EmptyBorder(6, 14, 6, 14)
        ));
        
        btnResetTaxis = crearBoton("RESETEAR TAXIS", new Color(0x607D8B), Color.WHITE);

        btnNuevaSolicitud.setEnabled(false);

        btnPanel.add(btnNuevaSolicitud);
        btnPanel.add(btnLimpiar);
        btnPanel.add(btnResetTaxis);

        header.add(titulo,   BorderLayout.WEST);
        header.add(btnPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel crearCentro() {
        JPanel centro = new JPanel(new BorderLayout(0, 0));
        centro.setBackground(COLOR_FONDO);

        JPanel contenedorMapa = new JPanel(new BorderLayout(0, 0));
        contenedorMapa.setBackground(COLOR_FONDO);
        contenedorMapa.setBorder(new EmptyBorder(14, 14, 14, 8));

        JLabel lblMapa = new JLabel("Mapa de Intersecciones — Salta Capital");
        lblMapa.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblMapa.setForeground(new Color(0x495057));
        lblMapa.setBorder(new EmptyBorder(0, 0, 8, 0));

        lblCargando = new JLabel("  ⏳ Cargando mapa...");
        lblCargando.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblCargando.setForeground(new Color(0x6C757D));

        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setOpaque(false);
        filaTitulo.add(lblMapa,    BorderLayout.WEST);
        filaTitulo.add(lblCargando,BorderLayout.EAST);

        contenedorMapa.add(filaTitulo, BorderLayout.NORTH);
        contenedorMapa.add(panelMapa,  BorderLayout.CENTER);

        centro.add(contenedorMapa, BorderLayout.CENTER);
        centro.add(panelLateral,   BorderLayout.EAST);

        return centro;
    }

    private JPanel crearBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout(0, 0));
        barra.setBackground(COLOR_STATUS_BG);
        barra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xBBDEFB)),
            new EmptyBorder(6, 16, 6, 16)
        ));

        lblEstado = new JLabel("ℹ️  Cargando el grafo de calles de Salta...");
        lblEstado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEstado.setForeground(COLOR_STATUS_TEXT);

        barra.add(lblEstado, BorderLayout.WEST);
        return barra;
    }

    // ============================================================
    //  LISTENERS
    // ============================================================
    private void configurarListeners() {

        modelo.addListener(new ModeloSimulacion.Listener() {
            @Override
            public void onMapaCargado(int totalNodos, int totalAristas) {
                SwingUtilities.invokeLater(() -> {
                    lblCargando.setText("");
                    lblEstado.setText("✅  Mapa listo: " + totalNodos + " intersecciones | "
                                    + totalAristas + " conexiones. Hacé clic en dos nodos para simular un viaje.");
                    btnNuevaSolicitud.setEnabled(false);
                    panelMapa.onMapaCargado();
                    panelLateral.actualizarFlota();
                });
            }

            @Override
            public void onSolicitudProcesada(SolicitudViaje solicitud, List<Integer> rutaNodos) {
                SwingUtilities.invokeLater(() -> {
                    panelLateral.agregarSolicitud(solicitud, rutaNodos);
                    panelMapa.limpiarSeleccion();

                    boolean exito = solicitud.getUnidadAsignada() != null;
                    if (exito) {
                        String eta = solicitud.getEtaFinal().obtenerTiempoFormateado();
                        lblEstado.setText("🚕  Viaje despachado → "
                            + solicitud.getUnidadAsignada().getIdVehiculo()
                            + " | ETA: " + eta + " (Animación en progreso, podés elegir otra).");
                    } else {
                        lblEstado.setText("⚠️  No se pudo asignar vehículo. Todos rechazaron o no hay disponibles.");
                    }
                });
            }

            @Override
            public void onError(String mensaje) {
                SwingUtilities.invokeLater(() -> {
                    lblCargando.setText("");
                    lblEstado.setText("❌  " + mensaje);
                    JOptionPane.showMessageDialog(VentanaPrincipal.this, mensaje,
                        "Error al cargar mapa", JOptionPane.ERROR_MESSAGE);
                });
            }

            @Override
            public void onTick() {
                // Se repinta el mapa en cada tick del timer para actualizar las posiciones de los taxis
                SwingUtilities.invokeLater(panelMapa::repaint);
            }

            @Override
            public void onViajesActualizados() {
                // Se repinta el panel lateral (chips de la flota)
                SwingUtilities.invokeLater(panelLateral::actualizarFlota);
            }
        });

        panelMapa.addSeleccionListener(new PanelMapa.SeleccionListener() {
            @Override
            public void onOrigenSeleccionado(int nodoId) {
                lblEstado.setText("📍  Origen seleccionado: Nodo " + nodoId
                    + "  — Ahora hacé clic en el nodo destino.");
                btnNuevaSolicitud.setEnabled(false);
            }

            @Override
            public void onDestinoSeleccionado(int nodoId) {
                lblEstado.setText("📍  Destino seleccionado: Nodo " + nodoId
                    + "  — Presioná «NUEVA SOLICITUD» para despachar.");
                btnNuevaSolicitud.setEnabled(true);
            }

            @Override
            public void onHover(int nodoId) {
                if (nodoId >= 0) {
                    double[] coords = modelo.getCoordenadasPorId().get(nodoId);
                    if (coords != null) {
                        lblEstado.setText(String.format(
                            "🔍  Nodo %d  |  lng: %.5f  |  lat: %.5f",
                            nodoId, coords[0], coords[1]));
                    }
                }
            }
        });

        btnNuevaSolicitud.addActionListener(e -> {
            int origen  = panelMapa.getNodoOrigen();
            int destino = panelMapa.getNodoDestino();
            if (origen == -1 || destino == -1) {
                JOptionPane.showMessageDialog(this,
                    "Seleccioná primero un nodo de origen y uno de destino en el mapa.",
                    "Selección incompleta", JOptionPane.WARNING_MESSAGE);
                return;
            }

            btnNuevaSolicitud.setEnabled(false);
            lblEstado.setText("⚙️  Calculando ruta óptima con Dijkstra...");

            new SwingWorker<SolicitudViaje, Void>() {
                @Override
                protected SolicitudViaje doInBackground() {
                    return modelo.procesarSolicitud(origen, destino);
                }
                @Override
                protected void done() {
                }
            }.execute();
        });

        btnLimpiar.addActionListener(e -> {
            panelMapa.limpiarSeleccion();
            btnNuevaSolicitud.setEnabled(false);
            lblEstado.setText("🗺  Selección limpiada. Hacé clic en un nodo para comenzar.");
        });

        btnResetTaxis.addActionListener(e -> {
            modelo.reposicionarTaxisAlAzar();
            lblEstado.setText("🔄  Flota reposicionada aleatoriamente. Se han cancelado los viajes en curso.");
            panelMapa.limpiarSeleccion();
            btnNuevaSolicitud.setEnabled(false);
        });
    }

    private void iniciarCargaMapa() {
        String[] rutas = {
            "muestra1.geojson",
            "archivos/muestra1.geojson",
            "archivos\\muestra1.geojson"
        };

        String rutaEncontrada = null;
        for (String r : rutas) {
            if (new File(r).exists()) {
                rutaEncontrada = r;
                break;
            }
        }

        if (rutaEncontrada == null) {
            JFileChooser chooser = new JFileChooser(".");
            chooser.setDialogTitle("Seleccioná el archivo muestra1.geojson");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "GeoJSON (*.geojson, *.json)", "geojson", "json"));
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                rutaEncontrada = chooser.getSelectedFile().getAbsolutePath();
            } else {
                lblEstado.setText("❌  No se seleccionó ningún archivo. Cerrá y volvé a intentar.");
                lblCargando.setText("");
                return;
            }
        }

        final String rutaFinal = rutaEncontrada;
        lblEstado.setText("⏳  Cargando: " + rutaFinal + "  (puede tardar unos segundos)...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                modelo.cargarMapa(rutaFinal);
                return null;
            }
        }.execute();
    }

    private JButton crearBoton(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color = getModel().isPressed()   ? bg.darker()
                            : getModel().isRollover() ? bg.brighter()
                            : bg;
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(7, 16, 7, 16));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
