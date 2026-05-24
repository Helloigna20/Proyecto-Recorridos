import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MatrizAdyacenciaSalta {

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
            int[][] matriz = new int[N][N];

            // 4. Llenar la matriz evaluando el sentido de la calle
            for (int i = 0; i < features.length(); i++) {
                JSONObject f = features.getJSONObject(i);
                JSONObject props = f.getJSONObject("properties");
                JSONArray coords = f.getJSONObject("geometry").getJSONArray("coordinates");

                // Lógica de DOBLE MANO:
                // Si 'oneway' es 'yes', es mano única. 
                // Si es 'no' o no existe el tag, es doble mano.
                boolean esManoUnica = props.optString("oneway", "no").equals("yes");
                String nombreCalle = props.optString("name", "S/N");

                for (int j = 0; j < coords.length() - 1; j++) {
                    int u = puntosUnicos.get(coords.getJSONArray(j).toString());
                    int v = puntosUnicos.get(coords.getJSONArray(j + 1).toString());

                    // Conexión de ida (siempre existe)
                    matriz[u][v] = 1;

                    // Conexión de vuelta (solo si NO es mano única)
                    if (!esManoUnica) {
                        matriz[v][u] = 1;
                    }
                }
            }

            // 5. Imprimir la matriz en formato tabla (primeros 20 nodos para visualizar)
            imprimirMatriz(matriz, 20);

        } catch (Exception e) {
            System.err.println("Error al procesar el mapa: " + e.getMessage());
        }
    }

    private static void imprimirMatriz(int[][] matriz, int limite) {
        System.out.println("\n--- MATRIZ DE ADYACENCIA (Mano única vs Doble mano) ---");
        int size = Math.min(matriz.length, limite);
        
        // Encabezado de columnas
        System.out.print("N\t");
        for (int j = 0; j < size; j++) System.out.print("["+j+"] ");
        System.out.println();

        for (int i = 0; i < size; i++) {
            System.out.print("["+i+"]\t"); // Encabezado de fila
            for (int j = 0; j < size; j++) {
                System.out.print(matriz[i][j] + "   ");
            }
            System.out.println();
        }
        System.out.println("\nNota: El valor '1' indica conexión permitida.");
    }
}