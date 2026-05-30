package clases.codigo;

import contenedores.ListaDoubleLinkedL;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase controladora que gestiona el Grafo Dirigido de la ciudad de Salta.
 * Conecta los datos geográficos raw con la lógica matemática del algoritmo de camino mínimo.
 */
public class GestionGrafo {
    private GrafoD grafo;
    private Map<String, Integer> puntosUnicos;  // Un mapa o diccionario para asociar un String (coordenada "[lng, lat]") con su respectivo ID numérico.

    public GestionGrafo() {
        this.puntosUnicos = new HashMap<>();
    }
    /**
     * Carga y procesa el archivo GeoJSON/JSON con el mapa de Salta.
     * Calcula la distancia utilizando Haversine y los tiempos de velocidad por sector.
     */
    public void cargarMapaDesdeArchivo(String rutaArchivo) {
        try {
            // 1. Cargar el archivo GeoJSON
            String content = new String(Files.readAllBytes(Paths.get(rutaArchivo)));
            JSONObject json = new JSONObject(content);
            JSONArray features = json.getJSONArray("features");

            // 2. Mapear cada coordenada única a un ID numérico (Índice de la matriz)
            this. puntosUnicos = new HashMap<>();
            int contadorId = 0;

            for (int i = 0; i < features.length(); i++) {
                JSONObject geom = features.getJSONObject(i).getJSONObject("geometry");
                if (geom.getString("type").equals("LineString")) {
                    JSONArray coords = geom.getJSONArray("coordinates");

                    for (int j = 0; j < coords.length(); j++) {
                        String punto = coords.getJSONArray(j).toString();
                        if (!this.puntosUnicos.containsKey(punto)) {
                            this.puntosUnicos.put(punto, contadorId++);
                        }
                    }
                }
            }

            int N = contadorId;
            System.out.println("Total de nodos (esquinas/puntos): " + N);

            // 3. Inicilizamos nuestro Grafo con N vertices
            GrafoD grafo= new GrafoD(N);

            // 4. Llenar la matriz evaluando el sentido de la calle, calculamos costo de tiempo y conectamos las aristas
            for (int i = 0; i < features.length(); i++) {
                JSONObject f = features.getJSONObject(i);
                JSONObject props = f.getJSONObject("properties");
                JSONArray coords = f.getJSONObject("geometry").getJSONArray("coordinates");
                String tipoCalle = props.optString("highway", "residential");
                double velocidad = PesosETA.obtenerVelocidadMS(tipoCalle);

                // Lógica de DOBLE MANO:
                // Si 'oneway' es 'yes', es mano única.
                // Si es 'no' o no existe el tag, es doble mano.

                boolean esManoUnica = props.optString("oneway", "no").equals("yes");
                String nombreCalle = props.optString("name", "S/N");

                for (int j = 0; j < coords.length() - 1; j++) {
                    int u = puntosUnicos.get(coords.getJSONArray(j).toString());
                    int v = puntosUnicos.get(coords.getJSONArray(j + 1).toString());//es lo mismo que aca <-

                    JSONArray coordU = coords.getJSONArray(j); //esto de aca->
                    JSONArray coordV = coords.getJSONArray(j + 1);

                    double lon1 = coordU.getDouble(0);
                    double lat1 = coordU.getDouble(1);
                    double lon2 = coordV.getDouble(0);
                    double lat2 = coordV.getDouble(1);

                    double distancia= CalculoDistancia.haversine(lat1, lon1, lat2, lon2);
                    double tiempoSegundos= distancia/velocidad; //El peso verdadero (costo en tiempo)

                    // Conexión de ida (siempre existe)
                    grafo.agregarArista(u,v, tiempoSegundos);

                    // Conexión de vuelta (solo si NO es mano única)
                    if (!esManoUnica) {
                        grafo.agregarArista(v, u, tiempoSegundos);
                    }
                }
            }

            // 5. Imprimir la matriz en formato tabla (primeros 20 nodos para visualizar)
            this.grafo.mostrarGrafo(20);

        } catch (Exception e) {
            System.err.println("Error al procesar el mapa: " + e.getMessage());
        }
    }

    /**
     * Implementación del algoritmo de Dijkstra para encontrar el camino mínimo.
     * Debe calcular la ruta óptima desde la posición de una Unidad hasta el Pasajero.
     * * @return Objeto CalculoETA con el tiempo total acumulado y la ruta detallada.
     */

    //Este es el "caminoMinimoDijkstra(int origen, int destino)" que hicimos en el TP7
    //Solo que este me devuelve CalculoETA

    public CalculoETA calcularRutaOptima(int idOrigenUnidad, int idDestinoPasajero) {
        //Llamamos al metodo Dijkstra
        this.grafo.Dijkstra(idOrigenUnidad);

        //Pregunto si la distancia final (costo total) es infinito. Esto me va a asegurar si existe un camino desde idOrigen a idDestino
        if((double) grafo.getListaDistancia().devolver(idDestinoPasajero)== Double.MAX_VALUE){
            System.out.println("No existe un camino desde "+idOrigenUnidad+" a "+idDestinoPasajero);
            return null;
        }
        else {
            //Reconstruimos para obtener el camino o ruta óptima
            //Recordar que en Dijkstra el camino se reconstruye desde el final
            ListaDoubleLinkedL camino = new ListaDoubleLinkedL();
            int aux = idDestinoPasajero;

            while (aux != idOrigenUnidad) {
                camino.insertar(aux, 0);
                aux = (int) this.grafo.getListaCamino().devolver(aux);
            }
            camino.insertar(idOrigenUnidad, 0);

            //Retorno la distancia final (ETA total) y el camino óptimo
            //(double)grafo.getListaDistancia().devolver(idDestinoPasajero) es el costo total de ir desde idOrigen a idDestino
            return new CalculoETA((double) grafo.getListaDistancia().devolver(idDestinoPasajero), camino);

        }
    }

}