package clases.codigo;

/**
 * Clase que modela al cliente/pasajero que solicita una unidad de transporte.
 */
public class Usuario {
    // ATRIBUTOS
    private String idUsuario; // Ej: "Usuario-0", "Usuario-1"
    private int idNodoInterseccion; // ID del vértice del grafo donde está parado físicamente

    public Usuario(String idUsuario, int idNodoInterseccion) {
        //completar
    }

    /**
     * Genera una solicitud formal de viaje apuntando a su ubicación actual.
     */
    public SolicitudViaje crearSolicitud(int idNodoDestino) {
        // TODO: Instanciar y retornar una SolicitudViaje vinculando este usuario, su origen y el destino elegido.
        return null;
    }

    // GETTERS Y SETTERS
    public String getIdUsuario() { return idUsuario; }
    public int getIdNodoInterseccion() { return idNodoInterseccion; }
}