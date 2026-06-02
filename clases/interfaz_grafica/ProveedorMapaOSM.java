package clases.interfaz_grafica;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Proveedor de tiles OSM.
 *
 * Sistema de coordenadas único:
 *   - Todo se expresa en "píxeles globales OSM" al nivel zoomBase.
 *   - La transformación a pantalla es siempre:
 *       screenX = (globalX - centerGX) * zoomVisual + panelW/2 + panX
 *       screenY = (globalY - centerGY) * zoomVisual + panelH/2 + panY
 *   - El mapa y los nodos usan la misma fórmula → nunca se desfasan.
 */
public class ProveedorMapaOSM {

    private static final int TILE_SIZE = 256;
    private final Map<String, BufferedImage> tileCache = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private Runnable repaintCallback;

    public void setRepaintCallback(Runnable cb) { this.repaintCallback = cb; }

    public void limpiarCache() { tileCache.clear(); }

    // ── Conversiones geográficas → píxeles globales OSM ──────────────────────

    public static double lonToGlobalX(double lon, int zoom) {
        return ((lon + 180.0) / 360.0) * TILE_SIZE * (1 << zoom);
    }

    public static double latToGlobalY(double lat, int zoom) {
        double r = Math.toRadians(lat);
        return (1.0 - Math.log(Math.tan(r) + 1.0 / Math.cos(r)) / Math.PI)
               / 2.0 * TILE_SIZE * (1 << zoom);
    }

    /** Nivel OSM que hace encajar el bounding-box en el panel sin zoom del usuario. */
    public static int calcularZoomBase(double minLon, double maxLon,
                                       double minLat, double maxLat,
                                       int panelW, int panelH) {
        for (int z = 18; z >= 0; z--) {
            double pw = lonToGlobalX(maxLon, z) - lonToGlobalX(minLon, z);
            double ph = latToGlobalY(minLat, z) - latToGlobalY(maxLat, z);
            if (pw <= panelW - 60 && ph <= panelH - 60) return z;
        }
        return 12;
    }

    // ── API pública para PanelMapa ────────────────────────────────────────────

    /**
     * Coordenada global X de un nodo (espacio zoomBase, sin zoom de usuario).
     * PanelMapa la guarda en baseX[id].
     */
    public double globalX(double lon, double minLon, double maxLon,
                          double minLat, double maxLat, int panelW, int panelH) {
        int z = calcularZoomBase(minLon, maxLon, minLat, maxLat, panelW, panelH);
        return lonToGlobalX(lon, z);
    }

    public double globalY(double lat, double minLon, double maxLon,
                          double minLat, double maxLat, int panelW, int panelH) {
        int z = calcularZoomBase(minLon, maxLon, minLat, maxLat, panelW, panelH);
        return latToGlobalY(lat, z);
    }

    /** Centro del bounding-box en píxeles globales al zoomBase. */
    public double centerGX(double minLon, double maxLon,
                            double minLat, double maxLat, int panelW, int panelH) {
        int z = calcularZoomBase(minLon, maxLon, minLat, maxLat, panelW, panelH);
        return (lonToGlobalX(minLon, z) + lonToGlobalX(maxLon, z)) / 2.0;
    }

    public double centerGY(double minLon, double maxLon,
                            double minLat, double maxLat, int panelW, int panelH) {
        int z = calcularZoomBase(minLon, maxLon, minLat, maxLat, panelW, panelH);
        return (latToGlobalY(maxLat, z) + latToGlobalY(minLat, z)) / 2.0;
    }

    // ── Dibujo de tiles ───────────────────────────────────────────────────────

    /**
     * Dibuja el mapa OSM usando exactamente la misma transformación que PanelMapa
     * aplica a los nodos, garantizando alineación perfecta.
     *
     * Transformación: screenPos = (globalPos - centerG) * zoomVisual + panelCenter + pan
     */
    public void dibujarMapaBase(Graphics2D g2,
                                 double minLon, double maxLon,
                                 double minLat, double maxLat,
                                 int panelW, int panelH,
                                 double zoomVisual, double panX, double panY) {

        int zoomBase = calcularZoomBase(minLon, maxLon, minLat, maxLat, panelW, panelH);

        double cgx = centerGX(minLon, maxLon, minLat, maxLat, panelW, panelH);
        double cgy = centerGY(minLon, maxLon, minLat, maxLat, panelW, panelH);
        double panelCX = panelW / 2.0;
        double panelCY = panelH / 2.0;

        // Para dibujar tiles nítidos al hacer zoom, pedimos un nivel OSM mayor.
        // Cada nivel OSM duplica la resolución (igual que ×2 en zoomVisual).
        int nivelExtra = (int) Math.floor(Math.log(zoomVisual) / Math.log(2));
        int zoomOSM    = Math.min(18, zoomBase + nivelExtra);

        // Escala entre zoomOSM y zoomBase: los píxeles globales en zoomOSM
        // son (2^nivelExtra) veces más grandes que en zoomBase.
        double escalaOSM = Math.pow(2, zoomOSM - zoomBase);

        // El zoom visual "restante" que no fue absorbido por el cambio de nivel OSM.
        double escalaFrac = zoomVisual / escalaOSM;

        // Tamaño de un tile OSM en pantalla
        double tilePantalla = TILE_SIZE * escalaFrac;

        // Para un punto con globalX (en espacio zoomBase):
        //   su globalX en zoomOSM = globalX * escalaOSM
        //   su screenX = (globalXosm/escalaOSM - cgx) * zoomVisual + panelCX + panX
        //              = (globalXosm - cgx*escalaOSM) * escalaFrac + panelCX + panX
        //
        // Un tile tx ocupa globalXosm ∈ [tx*TILE_SIZE, (tx+1)*TILE_SIZE]
        // => screenX del borde izquierdo del tile tx:
        //      sx = (tx*TILE_SIZE - cgx*escalaOSM) * escalaFrac + panelCX + panX

        double offX = -cgx * escalaOSM * escalaFrac + panelCX + panX;
        double offY = -cgy * escalaOSM * escalaFrac + panelCY + panY;

        // Rango de tiles visibles en pantalla
        int txMin = (int) Math.floor(-offX / tilePantalla);
        int tyMin = (int) Math.floor(-offY / tilePantalla);
        int txMax = (int) Math.ceil((panelW - offX) / tilePantalla);
        int tyMax = (int) Math.ceil((panelH - offY) / tilePantalla);
        int maxTile = (1 << zoomOSM) - 1;

        for (int tx = txMin; tx <= txMax; tx++) {
            for (int ty = tyMin; ty <= tyMax; ty++) {
                if (tx < 0 || ty < 0 || tx > maxTile || ty > maxTile) continue;

                int drawX = (int) Math.round(tx * tilePantalla + offX);
                int drawY = (int) Math.round(ty * tilePantalla + offY);
                int drawS = (int) Math.round(tilePantalla) + 1; // +1 evita gap de 1px entre tiles

                String key = zoomOSM + "/" + tx + "/" + ty;
                BufferedImage img = tileCache.get(key);

                if (img != null && img.getWidth() > 1) {
                    g2.drawImage(img, drawX, drawY, drawS, drawS, null);
                } else {
                    g2.setColor(new Color(230, 230, 230));
                    g2.fillRect(drawX, drawY, drawS, drawS);
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawRect(drawX, drawY, drawS, drawS);
                    descargarTile(zoomOSM, tx, ty, key);
                }
            }
        }

        // Filtro blanco semitransparente para que resalten nodos/aristas
        g2.setColor(new Color(255, 255, 255, 170));
        g2.fillRect(0, 0, panelW, panelH);
    }

    private void descargarTile(int z, int x, int y, String key) {
        if (tileCache.containsKey(key)) return;
        tileCache.put(key, new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
        executor.submit(() -> {
            try {
                String url = String.format("https://tile.openstreetmap.org/%d/%d/%d.png", z, x, y);
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty("User-Agent", "SimuladorRecorridosSalta/1.0");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                try (InputStream in = conn.getInputStream()) {
                    BufferedImage img = ImageIO.read(in);
                    if (img != null) {
                        tileCache.put(key, img);
                        if (repaintCallback != null) repaintCallback.run();
                    }
                }
            } catch (Exception e) {
                tileCache.remove(key);
            }
        });
    }

    // Retrocompatibilidad (sin zoom)
    public void dibujarMapaBase(Graphics2D g2, double minLon, double maxLon,
                                 double minLat, double maxLat, int w, int h) {
        dibujarMapaBase(g2, minLon, maxLon, minLat, maxLat, w, h, 1.0, 0, 0);
    }

    // Métodos legacy usados por código externo (siguen funcionando)
    public int getLocalX(double lon, double minLon, double maxLon,
                          double minLat, double maxLat, int panelW, int panelH) {
        int z = calcularZoomBase(minLon, maxLon, minLat, maxLat, panelW, panelH);
        double cgx = (lonToGlobalX(minLon,z) + lonToGlobalX(maxLon,z)) / 2.0;
        return (int)(lonToGlobalX(lon, z) - cgx + panelW / 2.0);
    }

    public int getLocalY(double lat, double minLon, double maxLon,
                          double minLat, double maxLat, int panelW, int panelH) {
        int z = calcularZoomBase(minLon, maxLon, minLat, maxLat, panelW, panelH);
        double cgy = (latToGlobalY(maxLat,z) + latToGlobalY(minLat,z)) / 2.0;
        return (int)(latToGlobalY(lat, z) - cgy + panelH / 2.0);
    }
}