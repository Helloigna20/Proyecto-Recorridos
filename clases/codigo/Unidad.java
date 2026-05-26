package clases.codigo;

import java.util.Random;

/**
 * Clase que modela los vehículos/coches disponibles en el sistema.
 */
public class Unidad {
    // ATRIBUTOS
    private String idVehiculo; // Ej: "Taxi 0", "Taxi 1"
    private int idNodoActual; // Vértice del grafo donde se encuentra al azar o tras terminar un viaje
    private boolean disponible; // Estado de disponibilidad para aceptar solicitudes

    // NUEVO ATRIBUTO: Guarda el ETA calculado por Dijkstra para el despacho actual
    private double etaTemporal;
    public Unidad(String idVehiculo, int idNodoActual) {
        this.setIdVehiculo(idVehiculo);
        this.setIdNodoActual(idNodoActual);
        this.setDisponible(true);
        this.setEtaTemporal(0.0);
    }

    /**
     * Simula la aceptación del viaje mediante un factor aleatorio.
     * Si no acepta, la cola de prioridad ofrecerá la solicitud a la siguiente mejor unidad.
     */
    public boolean simularAceptacionViaje() {
       boolean estado;
       Random random=new Random();
       if (random.nextDouble()>=0.80) {
        estado=true;
       }else{
        estado=false;
       }
        return estado; 
    }

    // GETTERS Y SETTERS
    public String getIdVehiculo() { return idVehiculo; }
    public int getIdNodoActual() { return idNodoActual; }
    public void setIdNodoActual(int idNodoActual) { this.idNodoActual = idNodoActual; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public void setIdVehiculo(String idVehiculo){this.idVehiculo=idVehiculo;}
    public double getEtaTemporal() { return etaTemporal; }
    public void setEtaTemporal(double etaTemporal) { this.etaTemporal = etaTemporal; }
}