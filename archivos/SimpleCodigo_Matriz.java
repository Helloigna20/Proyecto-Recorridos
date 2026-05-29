import clases.codigo.CalculoDistancia;
import clases.codigo.MatrizAdyacencia;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class SimpleCodigo_Matriz{
    public static void main(String[] args) {
        try {
            // 1. Cargar el archivo GeoJSON
            String content = new String(Files.readAllBytes(Paths.get("export (3).geojson")));
            JSONObject json = new JSONObject(content);
            JSONArray features = json.getJSONArray("features");

            // 2. Mapear cada coordenada única a un ID numérico (Índice de la matriz)
            Map<String, Integer> puntosUnicos = new HashMap<>();
            int contadorId = 0;

            for (int i = 0; i < features.length(); i++) {
                JSONObject geom = features.getJSONObject(i).getJSONObject("geometry");
                if (geom.getString("type").equals("LineString")) {
                    JSONArray coords = geom.getJSONArray("coordinates");

                    for (int j = 0; j < coords.length(); j++) {
                        String punto = coords.getJSONArray(j).toString();
                        if (!puntosUnicos.containsKey(punto)) {
                            puntosUnicos.put(punto, contadorId++);
                        }
                    }
                }
            }


            int N = contadorId;
            System.out.println("Total de nodos (esquinas/puntos): " + N);

            // 3. Crear la Matriz de Adyacencia N x N
            MatrizAdyacencia matriz = new MatrizAdyacencia(N);

            // 4. Llenar la matriz evaluando el sentido de la calle
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
                    double tiempoSegundos= distancia/velocidad;

                    // Conexión de ida (siempre existe)
                    matriz.registrarConexion(u,v, tiempoSegundos);

                    // Conexión de vuelta (solo si NO es mano única)
                    if (!esManoUnica) {
                        matriz.registrarConexion(u, v, tiempoSegundos);
                    }
                }
            }

            // 5. Imprimir la matriz en formato tabla (primeros 20 nodos para visualizar)
            matriz.imprimirMatriz(20);

        } catch (Exception e) {
            System.err.println("Error al procesar el mapa: " + e.getMessage());
        }
    }

}
