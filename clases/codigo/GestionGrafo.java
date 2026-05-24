package clases.codigo;

/**
 * Clase controladora que gestiona el Grafo Dirigido de la ciudad de Salta.
 * Conecta los datos geográficos raw con la lógica matemática del algoritmo de camino mínimo.
 */
public class GestionGrafo {
    // ATRIBUTOS
    // Instancia de tu clase 'GrafoDirigido' (que ya tienes implementada)
    // Un mapa o diccionario para asociar un String (coordenada "[lat,lng]") con su respectivo ID numérico.

    /**
     * Carga y procesa el archivo GeoJSON/JSON con el mapa de Salta.
     */
    public void cargarMapaDesdeArchivo(String rutaArchivo) {
        // TODO: Leer el archivo de texto y parsearlo como JSON.
        // TODO: Identificar los puntos únicos y asociarles un ID numérico correlativo.
        // TODO: Instanciar la MatrizAdyacencia y la estructura del GrafoDirigido.
        // TODO: Recorrer las 'ways' (calles), verificar si son "oneway" ("yes"/"no").
        // TODO: Calcular la distancia real entre nodos consecutivos usando la fórmula de Haversine.
        // TODO: Obtener la velocidad según el "highway" (primary, secondary, residential).
        // TODO: Calcular el tiempo de viaje en segundos (peso = distancia / velocidad).
        // TODO: Agregar las aristas con sus respectivos pesos calculados al GrafoDirigido.
    }

    /**
     * Implementación del algoritmo de Dijkstra para encontrar el camino mínimo.
     * Debe calcular la ruta óptima desde la posición de una Unidad hasta el Pasajero.
     * * @return Objeto CalculoETA con el tiempo total acumulado y la ruta detallada.
     */
    public CalculoETA calcularRutaOptima(int idOrigenUnidad, int idDestinoPasajero) {
        // TODO: Inicializar vectores de distancias/tiempos mínimos con infinito y el de visitados en falso.
        // TODO: Aplicar la lógica de Dijkstra usando tus clases de Nodos y Listas Enlazadas.
        // TODO: Reconstruir el camino desde el destino hacia el origen para determinar los nodos de la ruta.
        // TODO: Retornar una nueva instancia de CalculoETA con el coste total del tiempo estimado.
       
    }
}