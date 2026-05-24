package clases.codigo;

/**
 * Clase encargada de coordinar la petición de un usuario, evaluar las unidades cercanas,
 * ordenarlas por prioridad de arribo y efectuar el despacho final.
 */
public class SolicitudViaje {
    // ATRIBUTOS
    private Usuario pasajero;
    private int idNodoDestino;
    private Unidad unidadAsignada;
    private CalculoETA etaFinal;

    public SolicitudViaje(Usuario pasajero, int idNodoDestino) {
        //completar
    }

    /**
     * Ejecuta el Matching Engine: evalúa qué unidades disponibles están más cerca en tiempo
     * utilizando el grafo, las encola por prioridad y les asigna el viaje.
     * * @param listaUnidadesSistema Lista con todas las unidades registradas.
     * @param motorGrafo Instancia activa de GestionGrafo para consultar Dijkstra.
     */
    public void procesarYDespachar(Object listaUnidadesSistema, GestionGrafo motorGrafo) {
        // TODO: Crear o instanciar una Cola de Prioridad (Priority Queue estructurada con tus nodos/listas).
        // TODO: Iterar sobre 'listaUnidadesSistema' filtrando solo las que estén 'disponibles'.
        
        // TODO: Por cada unidad, invocar motorGrafo.calcularRutaOptima(unidad.getIdNodoActual(), pasajero.getIdNodoInterseccion()).
        // TODO: Insertar la unidad en la Cola de Prioridad, donde la prioridad ('p1 > p2 > p3') está dictada por el MENOR ETA en segundos.
        
        // TODO: Realizar un bucle de desencolado (sacando la cabeza de la cola de prioridad):
        //          - Tomar la unidad con mejor ETA.
        //          - Invocar unidad.simularAceptacionViaje().
        //          - Si acepta: Asignar 'this.unidadAsignada = unidad', fijar su 'etaFinal' y romper el bucle.
        //          - Si rechaza: Pasar a la siguiente mejor unidad disponible en la cola.
    }

    /**
     * Muestra en el Dashboard la información consolidada del despacho.
     */
    public void mostrarDetalleDespacho() {
        // TODO: Imprimir el resultado en consola simulando la interfaz:
        // "Usuario: [id] | Origen: [idNodo] -> Destino: [idNodo] | Asignado a: [Taxi X] | ETA: [X min]".
    }
}