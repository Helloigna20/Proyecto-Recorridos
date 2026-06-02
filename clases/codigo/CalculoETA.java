package clases.codigo;
import contenedores.ListaDoubleLinkedL;

/**
 * Clase que representa el resultado de un cálculo de tiempo estimado de arribo (ETA)
 * y la trayectoria que se debe seguir en el grafo.
 */
public class CalculoETA {
    private double tiempoSegundos; // Peso acumulado por el algoritmo de Dijkstra
    private ListaDoubleLinkedL rutaNodos; //La estructura con los IDs de los nodos que forman la ruta

    public CalculoETA(double tiempoSegundos, ListaDoubleLinkedL rutaNodos) {
       this.tiempoSegundos= tiempoSegundos;
       this.rutaNodos= rutaNodos;
    }

    /**
     * Convierte los segundos calculados a un formato legible por el usuario.
     * Ejemplo: "5 min" o "3 min".
     */

    //Creo que este sabemos qué hace...

    public String obtenerTiempoFormateado() {
        int minutos  = (int) tiempoSegundos / 60;
        int segundos = (int) tiempoSegundos % 60;
        if (minutos == 0) return segundos + " seg";
        if (segundos == 0) return minutos + " min";
        return minutos + " min " + segundos + " seg";
    }

    // GETTERS
    public double getTiempoSegundos() {
        return tiempoSegundos;
    }
    public ListaDoubleLinkedL getRutaNodos() {
        return rutaNodos;
    }
}