package clases.codigo;

/**
 * Clase que modela los vehículos/coches disponibles en el sistema.
 */
public class Unidad {
    // ATRIBUTOS
    private String idVehiculo; // Ej: "Taxi 0", "Taxi 1"
    private int idNodoActual; // Vértice del grafo donde se encuentra al azar o tras terminar un viaje
    private boolean disponible; // Estado de disponibilidad para aceptar solicitudes

    public Unidad(String idVehiculo, int idNodoActual) {
        //completar
    }

    /**
     * Simula la aceptación del viaje mediante un factor aleatorio.
     * Si no acepta, la cola de prioridad ofrecerá la solicitud a la siguiente mejor unidad.
     */
    public boolean simularAceptacionViaje() {
        // TODO: Generar un valor random (ej. 80% de probabilidad de que acepte).
        // TODO: Cambiar el estado de 'disponible' a falso si el valor es favorable.
        return false; 
    }

    // GETTERS Y SETTERS
    public String getIdVehiculo() { return idVehiculo; }
    public int getIdNodoActual() { return idNodoActual; }
    public void setIdNodoActual(int idNodoActual) { this.idNodoActual = idNodoActual; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}