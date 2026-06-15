package contenedores;

import clases.Entidades.Unidad;

/**
 * Clase concreta que le da la lógica de ordenamiento a tu ColaPrioridad abstracta.
 * Ordena de MENOR a MAYOR ETA (el que tarda menos tiempo llega primero al frente).
 */
public class ColaPrioridadETA extends ColaPrioridad {

    public ColaPrioridadETA() {
        super();
    }

    // Auxiliar para no repetir código al extraer el tiempo
    private double obtenerTiempo(Object obj) {
        // Asumiendo que guardamos un contenedor intermedio o una estructura con el ETA
        return ((Unidad) obj).getEtaTemporal();
    }

    @Override
    public boolean esMenor(Object objA, Object objB) {
        // En Dijkstra, "menor" significa menor tiempo (mayor prioridad de arribo)
        return obtenerTiempo(objA) < obtenerTiempo(objB);
    }

    @Override
    public boolean esMayor(Object objA, Object objB) {
        return obtenerTiempo(objA) > obtenerTiempo(objB);
    }

    @Override
    public boolean iguales(Object objA, Object objB) {
        return obtenerTiempo(objA) == obtenerTiempo(objB);
    }

    
}
