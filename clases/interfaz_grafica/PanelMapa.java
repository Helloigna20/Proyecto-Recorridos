package clases.interfaz_grafica;

import clases.codigo.Unidad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Panel de visualización del grafo de calles de Salta con mapa base OSM.
 */
public class PanelMapa extends JPanel {

    // ============================================================
    //  PALETA DE COLORES
    // ============================================================
    private static final Color COLOR_ARISTA        = new Color(180, 180, 180, 150);
    private static final Color COLOR_NODO          = new Color(0x607D8B);
    private static final Color COLOR_NODO_HOVER    = new Color(0x42A5F5);
    private static final Color COLOR_ORIGEN        = new Color(0x1976D2);
    private static final Color COLOR_DESTINO       = new Color(0xE53935);
    private static final Color COLOR_RUTA          = new Color(0x1565C0);
    private static final Color COLOR_TAXI_DISP     = new Color(0x2E7D32);
    private static final Color COLOR_TAXI_OCUP     = new Color(0x9E9E9E);
    private static final Color COLOR_SOMBRA        = new Color(0, 0, 0, 50);

    private final ModeloSimulacion modelo;
    private final ProveedorMapaOSM proveedorMapa;

    private int nodoOrigen  = -1;
    private int nodoDestino = -1;
    private int nodoHover   = -1;

    private int[] screenX;
    private int[] screenY;

    public interface SeleccionListener {
        void onOrigenSeleccionado(int nodoId);
        void onDestinoSeleccionado(int nodoId);
        void onHover(int nodoId);
    }
    private final List<SeleccionListener> selListeners = new ArrayList<>();
    public void addSeleccionListener(SeleccionListener l) { selListeners.add(l); }

    public PanelMapa(ModeloSimulacion modelo) {
        this.modelo = modelo;
        this.proveedorMapa = new ProveedorMapaOSM();
        this.proveedorMapa.setRepaintCallback(this::repaint);
        
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(620, 540));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDEE2E6), 1),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        configurarMouse();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalcularCoordenadas();
                repaint();
            }
        });
    }

    private void configurarMouse() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!modelo.isMapaListo()) return;
                int nodo = nodoEnPunto(e.getX(), e.getY());
                if (nodo == -1) return;

                if (nodoOrigen == -1) {
                    nodoOrigen = nodo;
                    for (SeleccionListener l : selListeners) l.onOrigenSeleccionado(nodo);
                } else if (nodoDestino == -1 && nodo != nodoOrigen) {
                    nodoDestino = nodo;
                    for (SeleccionListener l : selListeners) l.onDestinoSeleccionado(nodo);
                }
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!modelo.isMapaListo()) return;
                int nuevo = nodoEnPunto(e.getX(), e.getY());
                if (nuevo != nodoHover) {
                    nodoHover = nuevo;
                    setCursor(nuevo != -1
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                    for (SeleccionListener l : selListeners) l.onHover(nuevo);
                    repaint();
                }
            }
        });
    }

    public void recalcularCoordenadas() {
        if (!modelo.isMapaListo()) return;
        int n = modelo.getTotalNodos();
        screenX = new int[n];
        screenY = new int[n];
        int w = getWidth()  > 0 ? getWidth()  : 620;
        int h = getHeight() > 0 ? getHeight() : 540;

        for (Map.Entry<Integer, double[]> e : modelo.getCoordenadasPorId().entrySet()) {
            int id     = e.getKey();
            double[] c = e.getValue();
            // Usamos la proyección de Mercator para que coincida con el mapa base
            screenX[id] = proveedorMapa.getLocalX(c[0], modelo.getMinLng(), modelo.getMaxLng(), modelo.getMinLat(), modelo.getMaxLat(), w, h);
            screenY[id] = proveedorMapa.getLocalY(c[1], modelo.getMinLng(), modelo.getMaxLng(), modelo.getMinLat(), modelo.getMaxLat(), w, h);
        }
    }

    private int nodoEnPunto(int px, int py) {
        if (screenX == null) return -1;
        int mejorId   = -1;
        int umbral2   = 16 * 16; 
        double mejorD = umbral2;

        for (int id = 0; id < screenX.length; id++) {
            double d = Math.pow(px - screenX[id], 2) + Math.pow(py - screenY[id], 2);
            if (d < mejorD) { mejorD = d; mejorId = id; }
        }
        return mejorId;
    }

    public void limpiarSeleccion() {
        nodoOrigen  = -1;
        nodoDestino = -1;
        repaint();
    }

    public int getNodoOrigen()  { return nodoOrigen; }
    public int getNodoDestino() { return nodoDestino; }

    public void onMapaCargado() {
        recalcularCoordenadas();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,        RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,   RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,      RenderingHints.VALUE_STROKE_PURE);

        if (!modelo.isMapaListo() || screenX == null) {
            dibujarCargando(g2);
            g2.dispose();
            return;
        }

        // 1. Dibuja mapa de OpenStreetMap en el fondo
        proveedorMapa.dibujarMapaBase(g2, modelo.getMinLng(), modelo.getMaxLng(), modelo.getMinLat(), modelo.getMaxLat(), getWidth(), getHeight());

        // 2. Dibuja las aristas y el grafo por encima
        dibujarAristas(g2);
        
        // 3. Dibuja las rutas activas de los taxis en movimiento
        dibujarViajesActivos(g2);
        
        // 4. Dibuja los nodos e interacciones
        dibujarNodos(g2);
        
        // 5. Dibuja los vehículos
        dibujarUnidades(g2);

        g2.dispose();
    }

    private void dibujarCargando(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(new Color(0x90A4AE));
        String msg = "Cargando mapa de Salta...";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg,
            (getWidth() - fm.stringWidth(msg)) / 2,
            (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
    }

    private void dibujarAristas(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(COLOR_ARISTA);

        java.util.Set<Long> dibujadas = new java.util.HashSet<>();

        for (int[] arista : modelo.getAristas()) {
            int u = arista[0], v = arista[1];
            long clave = (long) Math.min(u, v) * 100000 + Math.max(u, v);
            if (dibujadas.contains(clave)) continue;
            dibujadas.add(clave);

            if (u < screenX.length && v < screenX.length) {
                g2.drawLine(screenX[u], screenY[u], screenX[v], screenY[v]);
            }
        }
    }

    private void dibujarViajesActivos(Graphics2D g2) {
        Stroke strokeRuta   = new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        Stroke strokeSombra = new BasicStroke(5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        for (ModeloSimulacion.ViajeActivo viaje : modelo.getViajesActivos()) {
            List<Integer> ruta = viaje.rutaCompleta;
            int currentIdx = viaje.indiceActual;

            if (ruta == null || ruta.size() < 2) continue;

            // Dibujar sombra del camino restante
            g2.setStroke(strokeSombra);
            g2.setColor(COLOR_SOMBRA);
            for (int i = currentIdx; i < ruta.size() - 1; i++) {
                int u = ruta.get(i), v = ruta.get(i + 1);
                if (u < screenX.length && v < screenX.length)
                    g2.drawLine(screenX[u], screenY[u], screenX[v], screenY[v]);
            }

            // Dibujar línea principal azul
            g2.setStroke(strokeRuta);
            g2.setColor(COLOR_RUTA);
            for (int i = currentIdx; i < ruta.size() - 1; i++) {
                int u = ruta.get(i), v = ruta.get(i + 1);
                if (u < screenX.length && v < screenX.length)
                    g2.drawLine(screenX[u], screenY[u], screenX[v], screenY[v]);
            }
            
            // Destino del viaje actual (para no perderlo visualmente cuando hacemos múltiples request)
            int destFinal = ruta.get(ruta.size() - 1);
            g2.setColor(COLOR_DESTINO.darker());
            g2.fillOval(screenX[destFinal] - 5, screenY[destFinal] - 5, 10, 10);
        }
    }

    private void dibujarNodos(Graphics2D g2) {
        int n = Math.min(screenX.length, modelo.getTotalNodos());

        for (int id = 0; id < n; id++) {
            int sx = screenX[id], sy = screenY[id];
            boolean esOrigen  = (id == nodoOrigen);
            boolean esDestino = (id == nodoDestino);
            boolean esHover   = (id == nodoHover);

            int r = 3;
            Color fill, stroke;

            if (esOrigen) {
                fill   = COLOR_ORIGEN;
                stroke = COLOR_ORIGEN.darker();
                r = 7;
            } else if (esDestino) {
                fill   = COLOR_DESTINO;
                stroke = COLOR_DESTINO.darker();
                r = 7;
            } else if (esHover) {
                fill   = COLOR_NODO_HOVER;
                stroke = COLOR_NODO_HOVER.darker();
                r = 6;
            } else {
                fill   = COLOR_NODO;
                stroke = new Color(0x78909C);
            }
            
            // Solo dibujamos los normales más pequeños para no tapar el mapa,
            // y los interactuados o en hover un poco más grandes.
            g2.setColor(fill);
            g2.fillOval(sx - r, sy - r, 2 * r, 2 * r);
            if (r > 3) {
                g2.setColor(stroke);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(sx - r, sy - r, 2 * r, 2 * r);
            }

            if (esOrigen || esDestino) {
                g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                String label = esOrigen ? "O" : "D";
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Color.WHITE);
                g2.drawString(label, sx - fm.stringWidth(label) / 2, sy + fm.getAscent() / 2 - 1);
            }
        }
    }

    private void dibujarUnidades(Graphics2D g2) {
        List<Unidad> unidades = modelo.getListaUnidades();
        int n = Math.min(screenX.length, modelo.getTotalNodos());

        for (Unidad u : unidades) {
            int nodo = u.getIdNodoActual();
            if (nodo < 0 || nodo >= n) continue;

            int sx = screenX[nodo];
            int sy = screenY[nodo];
            Color color = u.isDisponible() ? COLOR_TAXI_DISP : COLOR_TAXI_OCUP;

            dibujarIconoTaxi(g2, sx, sy, color);

            String label = u.getIdVehiculo().replaceAll("[^0-9]", "");
            if (!label.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.BOLD, 8));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(label, sx - fm.stringWidth(label) / 2, sy + fm.getAscent() / 2 - 1);
            }
        }
    }

    private void dibujarIconoTaxi(Graphics2D g2, int cx, int cy, Color color) {
        int w = 16, h = 12;
        g2.setColor(COLOR_SOMBRA);
        g2.fillRoundRect(cx - w/2 + 2, cy - h/2 + 3, w, h, 4, 4);
        
        g2.setColor(color);
        g2.fillRoundRect(cx - w/2, cy - h/2, w, h, 4, 4);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(cx - w/2, cy - h/2, w, h, 4, 4);
        
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(cx - w/2 + 3, cy - h/2 - 2, w - 6, 4, 2, 2);
    }
}
