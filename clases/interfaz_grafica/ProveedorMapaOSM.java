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
 * Proveedor de mapas de OpenStreetMap que descarga y cachea teselas (tiles)
 * utilizando la proyección Web Mercator.
 */
public class ProveedorMapaOSM {

    private static final int TILE_SIZE = 256;
    private final Map<String, BufferedImage> tileCache;
    private final ExecutorService executor;
    private Runnable repaintCallback;

    public ProveedorMapaOSM() {
        this.tileCache = new ConcurrentHashMap<>();
        // Un pool pequeño para no saturar los servidores de OSM
        this.executor = Executors.newFixedThreadPool(4);
    }

    public void setRepaintCallback(Runnable callback) {
        this.repaintCallback = callback;
    }

    /**
     * Devuelve el píxel X global para una longitud y nivel de zoom dados.
     */
    public static double lonToPixelX(double lon, int zoom) {
        return ((lon + 180.0) / 360.0) * TILE_SIZE * (1 << zoom);
    }

    /**
     * Devuelve el píxel Y global para una latitud y nivel de zoom dados.
     */
    public static double latToPixelY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        double a = Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad));
        return (1.0 - a / Math.PI) / 2.0 * TILE_SIZE * (1 << zoom);
    }

    /**
     * Calcula el nivel de zoom óptimo para que la bounding box encaje en el panel.
     */
    public static int calcularZoomOptimo(double minLon, double maxLon, double minLat, double maxLat, int panelWidth, int panelHeight) {
        for (int zoom = 18; zoom >= 0; zoom--) {
            double pxMin = lonToPixelX(minLon, zoom);
            double pxMax = lonToPixelX(maxLon, zoom);
            double pyMin = latToPixelY(maxLat, zoom); // Latitudes invertidas en Y
            double pyMax = latToPixelY(minLat, zoom);

            if ((pxMax - pxMin) <= (panelWidth - 60) && (pyMax - pyMin) <= (panelHeight - 60)) {
                return zoom;
            }
        }
        return 12; // Valor por defecto seguro
    }

    /**
     * Dibuja las teselas de fondo en el Graphics2D dado, de acuerdo al encuadre.
     */
    public void dibujarMapaBase(Graphics2D g2, double minLon, double maxLon, double minLat, double maxLat, int panelWidth, int panelHeight) {
        int zoom = calcularZoomOptimo(minLon, maxLon, minLat, maxLat, panelWidth, panelHeight);
        
        // Calcular centro global en píxeles del bounding box
        double pxMin = lonToPixelX(minLon, zoom);
        double pxMax = lonToPixelX(maxLon, zoom);
        double pyMin = latToPixelY(maxLat, zoom); 
        double pyMax = latToPixelY(minLat, zoom);
        
        double centerGlobalX = (pxMin + pxMax) / 2.0;
        double centerGlobalY = (pyMin + pyMax) / 2.0;
        
        // Offset para centrar la imagen global en el panel local
        double offsetX = (panelWidth / 2.0) - centerGlobalX;
        double offsetY = (panelHeight / 2.0) - centerGlobalY;
        
        // Determinar qué teselas necesitamos (calculando las coordenadas extremas del panel)
        int tileXMin = (int) Math.floor((centerGlobalX - panelWidth / 2.0) / TILE_SIZE);
        int tileXMax = (int) Math.floor((centerGlobalX + panelWidth / 2.0) / TILE_SIZE);
        int tileYMin = (int) Math.floor((centerGlobalY - panelHeight / 2.0) / TILE_SIZE);
        int tileYMax = (int) Math.floor((centerGlobalY + panelHeight / 2.0) / TILE_SIZE);

        for (int tx = tileXMin; tx <= tileXMax; tx++) {
            for (int ty = tileYMin; ty <= tileYMax; ty++) {
                String tileKey = zoom + "/" + tx + "/" + ty;
                BufferedImage img = tileCache.get(tileKey);
                
                int drawX = (int) (tx * TILE_SIZE + offsetX);
                int drawY = (int) (ty * TILE_SIZE + offsetY);
                
                if (img != null) {
                    g2.drawImage(img, drawX, drawY, null);
                } else {
                    // Dibuja un cuadro gris mientras carga
                    g2.setColor(new Color(230, 230, 230));
                    g2.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
                    
                    // Enviar a descargar
                    descargarTile(zoom, tx, ty, tileKey);
                }
            }
        }
        
        // Dibujamos un filtro semitransparente sobre el mapa para que resalten las calles y nodos
        g2.setColor(new Color(255, 255, 255, 170));
        g2.fillRect(0, 0, panelWidth, panelHeight);
    }

    private void descargarTile(int z, int x, int y, String key) {
        // Marcamos con un valor nulo temporal para no encolar varias veces
        if (tileCache.containsKey(key)) return; 
        tileCache.put(key, new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)); // Placeholder

        executor.submit(() -> {
            try {
                String urlStr = String.format("https://tile.openstreetmap.org/%d/%d/%d.png", z, x, y);
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "SimuladorRecorridosSalta/1.0");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                try (InputStream in = conn.getInputStream()) {
                    BufferedImage img = ImageIO.read(in);
                    if (img != null) {
                        tileCache.put(key, img);
                        if (repaintCallback != null) {
                            repaintCallback.run();
                        }
                    }
                }
            } catch (Exception e) {
                // Falló, quitamos el placeholder para que intente después
                tileCache.remove(key);
            }
        });
    }

    /**
     * Calcula la posición X local en el panel dado longitud.
     */
    public int getLocalX(double lon, double minLon, double maxLon, double minLat, double maxLat, int panelWidth, int panelHeight) {
        int zoom = calcularZoomOptimo(minLon, maxLon, minLat, maxLat, panelWidth, panelHeight);
        double pxMin = lonToPixelX(minLon, zoom);
        double pxMax = lonToPixelX(maxLon, zoom);
        double centerGlobalX = (pxMin + pxMax) / 2.0;
        double offsetX = (panelWidth / 2.0) - centerGlobalX;
        
        return (int) (lonToPixelX(lon, zoom) + offsetX);
    }

    /**
     * Calcula la posición Y local en el panel dada latitud.
     */
    public int getLocalY(double lat, double minLon, double maxLon, double minLat, double maxLat, int panelWidth, int panelHeight) {
        int zoom = calcularZoomOptimo(minLon, maxLon, minLat, maxLat, panelWidth, panelHeight);
        double pyMin = latToPixelY(maxLat, zoom);
        double pyMax = latToPixelY(minLat, zoom);
        double centerGlobalY = (pyMin + pyMax) / 2.0;
        double offsetY = (panelHeight / 2.0) - centerGlobalY;
        
        return (int) (latToPixelY(lat, zoom) + offsetY);
    }
}
