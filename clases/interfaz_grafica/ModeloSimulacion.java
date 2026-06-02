package clases.interfaz_grafica;

import clases.codigo.*;
import contenedores.ColaSLinkedList;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.Timer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Modelo central de la simulación para la GUI.
 */
public class ModeloSimulacion {

    public interface Listener {
        void onMapaCargado(int totalNodos, int totalAristas);
        void onSolicitudProcesada(SolicitudViaje solicitud, List<Integer> rutaNodos, double etaDestinoSegundos);
        void onError(String mensaje);
        void onTick();
        void onViajesActualizados();
    }

    private GestionGrafo motorGrafo;
    private final Map<Integer, double[]> coordenadasPorId;
    private final List<int[]> aristas;

    private ColaSLinkedList flota;
    private final List<Unidad>       listaUnidades;
    private final List<SolicitudViaje> historialSolicitudes;
    private final List<List<Integer>>  historialRutas;
    private final List<Listener>       listeners;

    public static class ViajeActivo {
        public Unidad taxi;
        public List<Integer> rutaCompleta;
        public int indiceActual;
        public SolicitudViaje solicitud;
        public int nodoPickup;       // nodo donde el taxi recoge al pasajero
        public int pausaTicks = 0;   // ticks restantes de pausa en el pickup
    }
    private final List<ViajeActivo> viajesActivos;
    private Timer timer;
    private Timer timerAutoSim;
    private boolean autoSimActiva = false;
    private final Random rndAutoSim = new Random();

    private int    totalNodos  = 0;
    private double minLng, maxLng, minLat, maxLat;
    private boolean mapaListo = false;

    public ModeloSimulacion() {
        this.motorGrafo           = new GestionGrafo();
        this.coordenadasPorId     = new LinkedHashMap<>();
        this.aristas              = new ArrayList<>();
        this.flota                = new ColaSLinkedList();
        this.listaUnidades        = new ArrayList<>();
        this.historialSolicitudes = new CopyOnWriteArrayList<>();//crea una copia subyacente del array original
        this.historialRutas       = new CopyOnWriteArrayList<>();
        this.listeners            = new CopyOnWriteArrayList<>();
        this.viajesActivos        = new CopyOnWriteArrayList<>();

        this.minLng = Double.MAX_VALUE;  this.maxLng = -Double.MAX_VALUE;
        this.minLat = Double.MAX_VALUE;  this.maxLat = -Double.MAX_VALUE;

        iniciarTimer();
    }

    private void iniciarTimer() {
        timer = new Timer(300, e -> {
            if (viajesActivos.isEmpty()) return;
            boolean algunCompletado = false;

            for (int i = viajesActivos.size() - 1; i >= 0; i--) {
                ViajeActivo v = viajesActivos.get(i);

                // Si está pausado en el nodo de pickup, descontar tick
                if (v.pausaTicks > 0) {
                    v.pausaTicks--;
                    continue;
                }

                if (v.indiceActual < v.rutaCompleta.size() - 1) {
                    v.indiceActual++;
                    int nodoActual = v.rutaCompleta.get(v.indiceActual);
                    v.taxi.setIdNodoActual(nodoActual);

                    // Si llegó al nodo de pickup, pausar y marcar recogida
                    if (nodoActual == v.nodoPickup && v.indiceActual < v.rutaCompleta.size() - 1) {
                        v.pausaTicks = 5; // ~1.5 segundos de pausa
                        v.taxi.setRecogiendo(true);
                        for (Listener l : listeners) l.onTick();
                        // Quitar el amarillo después de la pausa en el siguiente tick
                        Timer quitarAmarillo = new Timer(1600, ev -> {
                            v.taxi.setRecogiendo(false);
                            for (Listener l : listeners) l.onTick();
                        });
                        quitarAmarillo.setRepeats(false);
                        quitarAmarillo.start();
                    }
                } else {
                    v.solicitud.completarViaje();
                    viajesActivos.remove(i);
                    algunCompletado = true;
                }
            }

            for (Listener l : listeners) l.onTick();
            if (algunCompletado) {
                for (Listener l : listeners) l.onViajesActualizados();
            }
        });
        timer.start();
    }

    public void addListener(Listener l)    { listeners.add(l); }
    public void removeListener(Listener l) { listeners.remove(l); }

    public void cargarMapa(String rutaArchivo) {
        try {
            String contenido = new String(Files.readAllBytes(Paths.get(rutaArchivo)));
            JSONObject json  = new JSONObject(contenido);
            JSONArray features = json.getJSONArray("features");

            Map<String, Integer> puntosUnicos = new HashMap<>();
            int contadorId = 0;

            for (int i = 0; i < features.length(); i++) {
                JSONObject geom = features.getJSONObject(i).getJSONObject("geometry");
                if (!"LineString".equals(geom.getString("type"))) continue;

                JSONArray coords = geom.getJSONArray("coordinates");
                for (int j = 0; j < coords.length(); j++) {
                    JSONArray coord = coords.getJSONArray(j);
                    String clave   = coord.toString();

                    if (!puntosUnicos.containsKey(clave)) {
                        int id  = contadorId++;
                        double lng = coord.getDouble(0);
                        double lat = coord.getDouble(1);

                        puntosUnicos.put(clave, id);
                        coordenadasPorId.put(id, new double[]{lng, lat});

                        if (lng < minLng) minLng = lng;
                        if (lng > maxLng) maxLng = lng;
                        if (lat < minLat) minLat = lat;
                        if (lat > maxLat) maxLat = lat;
                    }
                }
            }

            for (int i = 0; i < features.length(); i++) {
                JSONObject f    = features.getJSONObject(i);
                JSONObject geom = f.getJSONObject("geometry");
                if (!"LineString".equals(geom.getString("type"))) continue;

                JSONArray  coords       = geom.getJSONArray("coordinates");
                JSONObject props        = f.getJSONObject("properties");
                boolean    esManoUnica  = "yes".equals(props.optString("oneway", "no"));

                for (int j = 0; j < coords.length() - 1; j++) {
                    int u = puntosUnicos.get(coords.getJSONArray(j).toString());
                    int v = puntosUnicos.get(coords.getJSONArray(j + 1).toString());
                    aristas.add(new int[]{u, v});
                    if (!esManoUnica) {
                        aristas.add(new int[]{v, u});
                    }
                }
            }

            totalNodos = contadorId;
            motorGrafo.cargarMapaDesdeArchivo(rutaArchivo);
            crearFlotaInicial(8);
            mapaListo = true;

            for (Listener l : listeners)
                l.onMapaCargado(totalNodos, aristas.size());

        } catch (Exception e) {
            e.printStackTrace();
            for (Listener l : listeners)
                l.onError("Error al cargar el mapa: " + e.getMessage());
        }
    }

    private void crearFlotaInicial(int cantidad) {
        String[] tipos = {"Premium", "Estándar", "Eco", "Camioneta",
                          "Estándar", "Premium",  "Eco", "Estándar"};
        Random rnd = new Random(42); 
        for (int i = 0; i < cantidad; i++) {
            int nodo = rnd.nextInt(Math.min(totalNodos, 80));
            Unidad u = new Unidad("Taxi " + i + " (" + tipos[i % tipos.length] + ")", nodo);
            listaUnidades.add(u);
            flota.meter(u);
        }
    }

    public SolicitudViaje procesarSolicitud(int nodoOrigen, int nodoDestino) {
        int idSolicitud = historialSolicitudes.size();
        Usuario pasajero   = new Usuario("Usuario-" + idSolicitud, nodoOrigen);
        SolicitudViaje sol = new SolicitudViaje(pasajero, nodoDestino);

        CalculoETA etaDestino = motorGrafo.calcularRutaOptima(nodoOrigen, nodoDestino);
        
        List<Integer> rutaNodos = new ArrayList<>();
        if (etaDestino == null) {
            // Si el destino es inalcanzable, abortamos
            historialSolicitudes.add(sol);
            historialRutas.add(rutaNodos);
            for (Listener l : listeners) {
                l.onError("No hay ruta posible hacia ese destino (calle sin salida o contramano).");
                l.onSolicitudProcesada(sol, rutaNodos, -1);
                l.onViajesActualizados();
            }
            return sol;
        }

        sol.procesarYDespachar(flota, motorGrafo);

        if (sol.getUnidadAsignada() != null && sol.getEtaFinal() != null) {
            List<Integer> rutaPickup = extraerRutaList(sol.getEtaFinal().getRutaNodos());
            rutaNodos.addAll(rutaPickup);
            
            List<Integer> rutaDrop = extraerRutaList(etaDestino.getRutaNodos());
            if (!rutaDrop.isEmpty() && !rutaNodos.isEmpty()) {
                rutaDrop.remove(0);
            }
            rutaNodos.addAll(rutaDrop);

            ViajeActivo va = new ViajeActivo();
            va.taxi = sol.getUnidadAsignada();
            va.solicitud = sol;
            va.rutaCompleta = rutaNodos;
            va.indiceActual = 0;
            // El nodo de pickup es el último de la ruta de arribo (= origen del pasajero)
            va.nodoPickup = sol.getPasajero().getIdNodoInterseccion();
            viajesActivos.add(va);
        }

        historialSolicitudes.add(sol);
        historialRutas.add(rutaNodos);

        double segsDestino = etaDestino.getTiempoSegundos();
        for (Listener l : listeners) {
            l.onSolicitudProcesada(sol, rutaNodos, segsDestino);
            l.onViajesActualizados();
        }

        return sol;
    }

    private List<Integer> extraerRutaList(contenedores.ListaDoubleLinkedL ruta) {
        List<Integer> rutaList = new ArrayList<>();
        if (ruta != null) {
            int tam = ruta.tamanio();
            for (int i = 0; i < tam; i++) {
                Object val = ruta.devolver(i);
                if (val instanceof Integer) rutaList.add((Integer) val);
                else if (val instanceof Double) rutaList.add(((Double) val).intValue());
            }
        }
        return rutaList;
    }

    public void reposicionarTaxisAlAzar() {
        Random rnd = new Random();
        for (Unidad u : listaUnidades) {
            u.setDisponible(true);
            u.setRecogiendo(false);
            u.setIdNodoActual(rnd.nextInt(totalNodos));
        }
        
        viajesActivos.clear();

        for (Listener l : listeners) {
            l.onTick();
            l.onViajesActualizados();
        }
    }

    // ── Simulación automática ─────────────────────────────────────────────────

    public void iniciarSimulacionAutomatica() {
        if (autoSimActiva) return;
        autoSimActiva = true;

        // Cada 3 segundos intenta generar un viaje aleatorio
        timerAutoSim = new Timer(3000, e -> {
            if (!mapaListo) return;

            // Buscar cuántos taxis libres hay
            long libres = listaUnidades.stream().filter(Unidad::isDisponible).count();
            if (libres == 0) return;

            // Generar origen y destino aleatorios distintos
            int origen  = rndAutoSim.nextInt(totalNodos);
            int destino = rndAutoSim.nextInt(totalNodos);
            if (origen == destino) return;

            // Procesar en hilo aparte para no bloquear el EDT
            new Thread(() -> procesarSolicitud(origen, destino)).start();
        });
        timerAutoSim.start();
    }

    public void detenerSimulacionAutomatica() {
        autoSimActiva = false;
        if (timerAutoSim != null) {
            timerAutoSim.stop();
            timerAutoSim = null;
        }
    }

    public boolean isAutoSimActiva() { return autoSimActiva; }

    // Getters para el mapa base Mercator
    public double getMinLng() { return minLng; }
    public double getMaxLng() { return maxLng; }
    public double getMinLat() { return minLat; }
    public double getMaxLat() { return maxLat; }
    
    public List<ViajeActivo>      getViajesActivos()         { return viajesActivos; }
    public Map<Integer, double[]> getCoordenadasPorId()      { return coordenadasPorId; }
    public List<int[]>            getAristas()               { return aristas; }
    public List<Unidad>           getListaUnidades()         { return listaUnidades; }
    public List<SolicitudViaje>   getHistorialSolicitudes()  { return historialSolicitudes; }
    public List<List<Integer>>    getHistorialRutas()        { return historialRutas; }
    public GestionGrafo           getMotorGrafo()            { return motorGrafo; }
    public int                    getTotalNodos()             { return totalNodos; }
    public boolean                isMapaListo()               { return mapaListo; }
}