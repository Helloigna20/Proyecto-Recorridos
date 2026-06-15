package clases.Entidades;

/**
 * Clase que modela al cliente/pasajero que solicita una unidad de transporte.
 */
public class Usuario {
    // ATRIBUTOS
    private String idUsuario; // Ej: "Usuario-0", "Usuario-1"
    private int idNodoInterseccion; // ID del vértice del grafo donde está parado físicamente

    public Usuario(String idUsuario, int idNodoInterseccion) {
        //completar
        this.setIdUsuario(idUsuario);
        this.setIdNodoInterseccion(idNodoInterseccion);
    }

    /**
     * Genera una solicitud formal de viaje apuntando a su ubicación actual.
     */
    public SolicitudViaje crearSolicitud(int idNodoDestino) {
        /*System.out.println("[SISTEMA] -> " + this.getIdUsuario() + " ha creado una solicitud desde el Nodo " 
         + this.getNodoInterseccion() + " hacia el Nodo " + idNodoDestino);*/ 
        return (new SolicitudViaje(this, idNodoDestino));
    }

    // GETTERS Y SETTERS
    public String getIdUsuario() { return idUsuario; }
    public int getIdNodoInterseccion() { return idNodoInterseccion; }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
    public void setIdNodoInterseccion(int idNodoInterseccion) {
        this.idNodoInterseccion = idNodoInterseccion;
    }

    public String toString() {
        return "Usuario{" +
                "id='" + this.getIdUsuario() + '\'' +
                ", esquinaActual=" + this.getIdNodoInterseccion() +
                '}';
    }
    
}