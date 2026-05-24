package clases.codigo;

/**
 * Clase que representa el resultado de un cálculo de tiempo estimado de arribo (ETA)
 * y la trayectoria que se debe seguir en el grafo.
 */
public class CalculoETA {
    // ATRIBUTOS
    private double tiempoSegundos; // Peso acumulado por el algoritmo de Dijkstra
    // Tu estructura de Lista (ListaSimplementeEnlazada o ListaDoblementeEnlazada) con los IDs de los nodos que forman la ruta
    private Object rutaNodos; 

    public CalculoETA(double tiempoSegundos, Object rutaNodos) {
       
    }

    /**
     * Convierte los segundos calculados a un formato legible por el usuario.
     * Ejemplo: "5 min" o "3 min".
     */
    public String obtenerTiempoFormateado() {
        // TODO: Convertir 'tiempoSegundos' a minutos enteros o formato MM:SS.
        return "Completar";
    }

    // GETTERS
    public double getTiempoSegundos() { return tiempoSegundos; }
    public Object getRutaNodos() { return rutaNodos; }
}