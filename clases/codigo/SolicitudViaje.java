package clases.codigo;

import contenedores.ColaPrioridad;
import contenedores.ColaPrioridadETA;
import contenedores.ColaSLinkedList;
import recursos.Nodo;

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
        this.setPasajero(pasajero);
        this.setIdNodoDestino(idNodoDestino);
    }

    /**
     * Ejecuta el Matching Engine: evalúa qué unidades disponibles están más cerca en tiempo
     * utilizando el grafo, las encola por prioridad y les asigna el viaje.
     * * @param listaUnidadesSistema Lista con todas las unidades registradas (ColaSLinkedList).
     * @param motorGrafo Instancia activa de GestionGrafo para consultar Dijkstra.
     */
    public void procesarYDespachar(Object listaUnidadesSistema, GestionGrafo motorGrafo) {
        // 1. Instanciar la cola de prioridad concreta que maneja los ETAs
        ColaPrioridad colaPrioridadVehiculos = new ColaPrioridadETA();
        
        // 2. Obtener el primer nodo de la cola general de unidades del sistema
        Nodo actual = ((ColaSLinkedList) listaUnidadesSistema).getPrimero();
        
        // 3. Filtrar unidades e insertarlas ordenadas por prioridad
        while (actual != null) {
            Unidad unidad = (Unidad) actual.getNodoInfo();
            
            // Evaluamos solo si el vehículo no está ocupado en otro viaje
            if (unidad.isDisponible()) {
                // Calculamos el camino mínimo con Dijkstra a través del controlador del grafo
                CalculoETA resultadoETA = motorGrafo.calcularRutaOptima(unidad.getIdNodoActual(), this.getPasajero().getIdNodoInterseccion());
                
                if (resultadoETA != null) {
                    // Seteamos el ETA en el atributo temporal de la unidad para que ColaPrioridadETA pueda compararlo
                    unidad.setEtaTemporal(resultadoETA.getTiempoSegundos());
                    
                    // Al meterlo, tu estructura abstracta llamará a esMenor() y lo acomodará en su lugar correspondiente
                    colaPrioridadVehiculos.meter(unidad);
                }
            }
            // Avanzamos al siguiente nodo de la estructura enlazada
            actual = actual.getNextNodo();
        }
        
        // 4. El Bucle de Despacho (Matching Core)
        boolean viajeDespachado = false;
        
        System.out.println("\n=== INICIANDO PROCESO DE DESPACHO ===");
        System.out.println("Pasajero: " + this.getPasajero().getIdUsuario() + " | Ubicación Nodo: " + this.getPasajero().getIdNodoInterseccion());
        
        // Consumimos la cola de prioridad ofreciendo el viaje al taxi más cercano primero
        while (!colaPrioridadVehiculos.estaVacia() && !viajeDespachado) {
            
            // Extraemos la unidad con menor ETA (la que quedó al frente de la cola)
            Unidad unidadCercana = (Unidad) colaPrioridadVehiculos.sacar();
            
            // Simulación probabilística de si el taxista acepta o rechaza el viaje en su app
            boolean aceptaViaje = unidadCercana.simularAceptacionViaje();
            
            if (aceptaViaje) {
                // El viaje fue aceptado con éxito
                this.setUnidadAsignada(unidadCercana);
                unidadCercana.setDisponible(false); // Cambia el estado de la unidad a ocupada
                
                // Guardamos el cálculo del ETA final y definitivo para la solicitud
                this.setEtaFinal(motorGrafo.calcularRutaOptima(unidadCercana.getIdNodoActual(), this.getPasajero().getIdNodoInterseccion()));
                
                viajeDespachado = true;
                System.out.println("[OK] -> Solicitud ACEPTADA por: " + unidadCercana.getIdVehiculo());
            } else {
                // El taxista la rechazó (probabilidad < 80%), el bucle continúa con la siguiente unidad de la cola
                System.out.println("[RECHAZADO] -> El vehículo " + unidadCercana.getIdVehiculo() + " rechazó la alerta. Buscando al siguiente más cercano...");
            }
        }
        
        // 5. Cierre y verificación del estado del despacho
        if (!viajeDespachado) {
            System.out.println("\n[ERROR]: No se pudo asignar vehículo. Todas las unidades cercanas rechazaron o no hay taxis disponibles.");
            this.setUnidadAsignada(null);
            this.setEtaFinal(null);
        } else {
            // Si se concretó, se muestran los datos consolidados del despacho
            this.mostrarDetalleDespacho();
        }
    }

    /**
     * Muestra en consola la información consolidada del despacho exitoso.
     */
    public void mostrarDetalleDespacho() {
        System.out.println("\n========================================");
        System.out.println("   VIAJE DESPACHADO CON ÉXITO");
        System.out.println("========================================");
        System.out.println("Usuario           : " + this.getPasajero().getIdUsuario());
        System.out.println("Nodo Destino      : " + this.getIdNodoDestino());
        System.out.println("Vehículo Asignado : " + this.getUnidadAsignada().getIdVehiculo());
        System.out.println("ETA de Arribo     : " + this.getEtaFinal().obtenerTiempoFormateado());
        System.out.println("========================================\n");
    }
    public void completarViaje() {
    if (this.unidadAsignada != null && this.etaFinal != null) {
        //  Actualiza posición del taxi al destino del pasajero
        this.unidadAsignada.setIdNodoActual(this.idNodoDestino);
        this.unidadAsignada.setDisponible(true);
        this.unidadAsignada.setEtaTemporal(0.0);
        System.out.println("[COMPLETADO] -> " + this.unidadAsignada.getIdVehiculo() 
            + " llegó a nodo " + this.idNodoDestino + " y está disponible");
    }
}

    // GETTERS Y SETTERS
    public Usuario getPasajero() { return pasajero; }
    public void setPasajero(Usuario pasajero) { this.pasajero = pasajero; }

    public int getIdNodoDestino() { return idNodoDestino; }
    public void setIdNodoDestino(int idNodoDestino) { this.idNodoDestino = idNodoDestino; }

    public Unidad getUnidadAsignada() { return unidadAsignada; }
    public void setUnidadAsignada(Unidad unidadAsignada) { this.unidadAsignada = unidadAsignada; }

    public CalculoETA getEtaFinal() { return etaFinal; }
    public void setEtaFinal(CalculoETA etaFinal) { this.etaFinal = etaFinal; }
}