package clases.interfaz_grafica;

import clases.Entidades.Unidad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PanelMapa extends JPanel {

    private static final Color COLOR_ARISTA     = new Color(60, 80, 100, 160);
    private static final Color COLOR_NODO       = new Color(0x455A64);
    private static final Color COLOR_NODO_HOVER = new Color(0x29B6F6);
    private static final Color COLOR_ORIGEN     = new Color(0x1E88E5);
    private static final Color COLOR_DESTINO    = new Color(0xEF5350);
    private static final Color COLOR_RUTA       = new Color(0x29B6F6);
    private static final Color COLOR_TAXI_DISP  = new Color(0x2E7D32);
    private static final Color COLOR_TAXI_OCUP  = new Color(0x546E7A);
    private static final Color COLOR_SOMBRA     = new Color(0, 0, 0, 80);

    // ── Zoom / Pan ────────────────────────────────────────────────────────────
    private double zoomVisual = 1.0;
    private static final double ZOOM_MIN  = 0.5;
    private static final double ZOOM_MAX  = 10.0;
    private static final double ZOOM_STEP = 1.15;

    private double panX = 0, panY = 0;
    private Point  dragStart = null;
    private double panXDrag,  panYDrag;

    // ── Modelo / Mapa ─────────────────────────────────────────────────────────
    private final ModeloSimulacion modelo;
    private final ProveedorMapaOSM proveedor;

    private int nodoOrigen = -1, nodoDestino = -1, nodoHover = -1;

    /**
     * Coordenadas de cada nodo en píxeles globales OSM (espacio zoomBase).
     * Misma unidad que usa ProveedorMapaOSM → nunca hay desfase.
     */
    private double[] gx, gy;
    private double   centerGX, centerGY; // centro del bounding-box en ese espacio

    // ── Listener ──────────────────────────────────────────────────────────────
    public interface SeleccionListener {
        void onOrigenSeleccionado(int nodoId);
        void onDestinoSeleccionado(int nodoId);
        void onHover(int nodoId);
    }
    private final List<SeleccionListener> selListeners = new ArrayList<>();
    public void addSeleccionListener(SeleccionListener l) { selListeners.add(l); }

    // ── Constructor ───────────────────────────────────────────────────────────
    public PanelMapa(ModeloSimulacion modelo) {
        this.modelo    = modelo;
        this.proveedor = new ProveedorMapaOSM();
        this.proveedor.setRepaintCallback(this::repaint);

        setBackground(new Color(0x0F0F1A));
        setPreferredSize(new Dimension(620, 540));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x1A1A2E), 1),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)));

        configurarMouse();
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                recalcularCoordenadas(); repaint();
            }
        });
    }

    // ── Transformación global → pantalla ──────────────────────────────────────
    // Misma fórmula que ProveedorMapaOSM usa internamente para los tiles:
    //   screenX = (globalX - centerGX) * zoomVisual + panelW/2 + panX

    private int screenX(int id) {
        return (int)((gx[id] - centerGX) * zoomVisual + getWidth()  / 2.0 + panX);
    }
    private int screenY(int id) {
        return (int)((gy[id] - centerGY) * zoomVisual + getHeight() / 2.0 + panY);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────
    private void configurarMouse() {
        addMouseWheelListener(e -> {
            if (!modelo.isMapaListo()) return;
            double mx = e.getX(), my = e.getY();
            double antes = zoomVisual;
            zoomVisual = e.getWheelRotation() < 0
                ? Math.min(ZOOM_MAX, zoomVisual * ZOOM_STEP)
                : Math.max(ZOOM_MIN, zoomVisual / ZOOM_STEP);
            // Mantener el punto bajo el cursor fijo
            double s = zoomVisual / antes;
            panX = mx - s * (mx - panX);
            panY = my - s * (my - panY);
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint(); panXDrag = panX; panYDrag = panY;
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (dragStart != null) {
                    double dx = e.getX() - dragStart.x, dy = e.getY() - dragStart.y;
                    if (Math.sqrt(dx*dx + dy*dy) < 5) manejarClic(e.getX(), e.getY());
                }
                dragStart = null;
                setCursor(Cursor.getDefaultCursor());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (dragStart != null) {
                    panX = panXDrag + (e.getX() - dragStart.x);
                    panY = panYDrag + (e.getY() - dragStart.y);
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    repaint();
                }
            }
            @Override public void mouseMoved(MouseEvent e) {
                if (!modelo.isMapaListo() || gx == null) return;
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

    private void manejarClic(int px, int py) {
        if (!modelo.isMapaListo()) return;
        int nodo = nodoEnPunto(px, py);
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

    // ── Coordenadas ───────────────────────────────────────────────────────────
    public void recalcularCoordenadas() {
        if (!modelo.isMapaListo()) return;
        int n = modelo.getTotalNodos();
        int w = getWidth() > 0 ? getWidth() : 620;
        int h = getHeight() > 0 ? getHeight() : 540;

        gx = new double[n];
        gy = new double[n];

        for (Map.Entry<Integer, double[]> e : modelo.getCoordenadasPorId().entrySet()) {
            int id     = e.getKey();
            double[] c = e.getValue(); // [lng, lat]
            gx[id] = proveedor.globalX(c[0], modelo.getMinLng(), modelo.getMaxLng(),
                                        modelo.getMinLat(), modelo.getMaxLat(), w, h);
            gy[id] = proveedor.globalY(c[1], modelo.getMinLng(), modelo.getMaxLng(),
                                        modelo.getMinLat(), modelo.getMaxLat(), w, h);
        }

        centerGX = proveedor.centerGX(modelo.getMinLng(), modelo.getMaxLng(),
                                       modelo.getMinLat(), modelo.getMaxLat(), w, h);
        centerGY = proveedor.centerGY(modelo.getMinLng(), modelo.getMaxLng(),
                                       modelo.getMinLat(), modelo.getMaxLat(), w, h);
    }

    private int nodoEnPunto(int px, int py) {
        if (gx == null) return -1;
        double umbral = Math.max(8, 16 / zoomVisual);
        double umbral2 = umbral * umbral, mejorD = umbral2;
        int mejorId = -1;
        for (int id = 0; id < gx.length; id++) {
            double dx = px - screenX(id), dy = py - screenY(id);
            double d = dx*dx + dy*dy;
            if (d < mejorD) { mejorD = d; mejorId = id; }
        }
        return mejorId;
    }

    public void limpiarSeleccion() { nodoOrigen = -1; nodoDestino = -1; repaint(); }
    public int  getNodoOrigen()    { return nodoOrigen; }
    public int  getNodoDestino()   { return nodoDestino; }
    public void onMapaCargado()    { recalcularCoordenadas(); repaint(); }

    // ── Paint ─────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (!modelo.isMapaListo() || gx == null) {
            dibujarCargando(g2); g2.dispose(); return;
        }

        int w = getWidth(), h = getHeight();

        // 1. Mapa OSM — recibe exactamente zoomVisual y pan para alinear sus tiles
        proveedor.dibujarMapaBase(g2,
            modelo.getMinLng(), modelo.getMaxLng(),
            modelo.getMinLat(), modelo.getMaxLat(),
            w, h, zoomVisual, panX, panY);

        // 2. Grafo — usa screenX/screenY (misma fórmula) con grosores fijos
        dibujarAristas(g2);
        dibujarViajesActivos(g2);
        dibujarNodos(g2);
        dibujarUnidades(g2);

        g2.dispose();
    }

    private void dibujarCargando(Graphics2D g2) {
        g2.setColor(new Color(0x0F0F1A));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(new Color(0x546E7A));
        String msg = "Cargando mapa de Salta...";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (getWidth()-fm.stringWidth(msg))/2,
                           (getHeight()-fm.getHeight())/2 + fm.getAscent());
    }

    private void dibujarAristas(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(COLOR_ARISTA);
        java.util.Set<Long> vistas = new java.util.HashSet<>();
        for (int[] a : modelo.getAristas()) {
            int u = a[0], v = a[1];
            long k = (long) Math.min(u,v) * 100000 + Math.max(u,v);
            if (!vistas.add(k)) continue;
            if (u < gx.length && v < gx.length)
                g2.drawLine(screenX(u), screenY(u), screenX(v), screenY(v));
        }
    }

    private void dibujarViajesActivos(Graphics2D g2) {
        Stroke sRuta   = new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        Stroke sSombra = new BasicStroke(5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        for (ModeloSimulacion.ViajeActivo viaje : modelo.getViajesActivos()) {
            List<Integer> ruta = viaje.rutaCompleta;
            int idx = viaje.indiceActual;
            if (ruta == null || ruta.size() < 2) continue;
            g2.setStroke(sSombra); g2.setColor(COLOR_SOMBRA);
            for (int i = idx; i < ruta.size()-1; i++) {
                int u = ruta.get(i), v = ruta.get(i+1);
                if (u < gx.length && v < gx.length)
                    g2.drawLine(screenX(u), screenY(u), screenX(v), screenY(v));
            }
            g2.setStroke(sRuta); g2.setColor(COLOR_RUTA);
            for (int i = idx; i < ruta.size()-1; i++) {
                int u = ruta.get(i), v = ruta.get(i+1);
                if (u < gx.length && v < gx.length)
                    g2.drawLine(screenX(u), screenY(u), screenX(v), screenY(v));
            }
            int df = ruta.get(ruta.size()-1);
            g2.setColor(COLOR_DESTINO.darker());
            g2.fillOval(screenX(df)-5, screenY(df)-5, 10, 10);
        }
    }

    private void dibujarNodos(Graphics2D g2) {
        int n = Math.min(gx.length, modelo.getTotalNodos());
        for (int id = 0; id < n; id++) {
            int sx = screenX(id), sy = screenY(id);
            boolean esO = id == nodoOrigen, esD = id == nodoDestino, esH = id == nodoHover;
            int r = 3; Color fill, stroke;
            if      (esO) { fill=COLOR_ORIGEN;     stroke=COLOR_ORIGEN.darker();     r=7; }
            else if (esD) { fill=COLOR_DESTINO;    stroke=COLOR_DESTINO.darker();    r=7; }
            else if (esH) { fill=COLOR_NODO_HOVER; stroke=COLOR_NODO_HOVER.darker(); r=6; }
            else          { fill=COLOR_NODO;        stroke=new Color(0x78909C); }
            g2.setColor(fill);
            g2.fillOval(sx-r, sy-r, 2*r, 2*r);
            if (r > 3) {
                g2.setColor(stroke);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(sx-r, sy-r, 2*r, 2*r);
            }
            if (esO || esD) {
                g2.setFont(new Font("SansSerif", Font.BOLD, 9));
                String lbl = esO ? "O" : "D";
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Color.WHITE);
                g2.drawString(lbl, sx-fm.stringWidth(lbl)/2, sy+fm.getAscent()/2-1);
            }
        }
    }

    private void dibujarUnidades(Graphics2D g2) {
        int n = Math.min(gx.length, modelo.getTotalNodos());
        for (Unidad u : modelo.getListaUnidades()) {
            int nodo = u.getIdNodoActual();
            if (nodo < 0 || nodo >= n) continue;
            int sx = screenX(nodo), sy = screenY(nodo);
            Color colorTaxi = u.isRecogiendo()
                ? new Color(0xFFD600)           // amarillo brillante al recoger
                : u.isDisponible() ? COLOR_TAXI_DISP : COLOR_TAXI_OCUP;
            dibujarIconoTaxi(g2, sx, sy, colorTaxi);
            String lbl = u.getIdVehiculo().replaceAll("[^0-9]", "");
            if (!lbl.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.BOLD, 8));
                g2.setColor(u.isRecogiendo() ? Color.BLACK : Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lbl, sx-fm.stringWidth(lbl)/2, sy+fm.getAscent()/2-1);
            }
        }
    }

    private void dibujarIconoTaxi(Graphics2D g2, int cx, int cy, Color color) {
        int w=16, h=12;
        g2.setColor(COLOR_SOMBRA);     g2.fillRoundRect(cx-w/2+2, cy-h/2+3, w, h, 4, 4);
        g2.setColor(color);            g2.fillRoundRect(cx-w/2,   cy-h/2,   w, h, 4, 4);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(cx-w/2, cy-h/2, w, h, 4, 4);
        g2.fillRoundRect(cx-w/2+3, cy-h/2-2, w-6, 4, 2, 2);
    }
}