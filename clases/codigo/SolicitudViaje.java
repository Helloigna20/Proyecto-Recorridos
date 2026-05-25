package clases.codigo;

import contenedores.ColaPrioridad;
import contenedores.ColaSLinkedList;
import contenedores.ElementoPrioridad;
import recursos.Nodo;

/**
 * Clase encargada de coordinar la petición de un usuario, evaluar las unidades cercanas,
 * ordenarlas por prioridad de arribo y efectuar el despacho final.
 */
public class SolicitudViaje{
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
     * * @param listaUnidadesSistema Lista con todas las unidades registradas.
     * @param motorGrafo Instancia activa de GestionGrafo para consultar Dijkstra.
     */
    public void procesarYDespachar(Object listaUnidadesSistema, GestionGrafo motorGrafo) {
       ColaPrioridad colaPrioridadVehiculos = new ColaPrioridad();
       Nodo actual= ((ColaSLinkedList)listaUnidadesSistema).getPrimero();
        while (actual!=null) {
            Unidad unidad=(Unidad)actual.getNodoInfo();
            if (unidad.isDisponible()) {
                CalculoETA resultadoETA = motorGrafo.calcularRutaOptima(unidad.getIdNodoActual(), this.pasajero.getIdNodoInterseccion());
                if (resultadoETA!=null) {
                    ElementoPrioridad elemento = new ElementoPrioridad(unidad, resultadoETA.getTiempoSegundos());
                    colaPrioridadVehiculos.meter(elemento);
                }
            }
            actual=actual.getNextNodo();
        }
        boolean viajeDespachado = false;
        /*System.out.println("\n=== INICIANDO PROCESO DE DESPACHO ===");
        System.out.println("Usuario: " + pasajero.getIdUsuario() + " en Nodo ID: " + pasajero.getIdNodoInterseccion());*/
        while (!colaPrioridadVehiculos.estaVacia() && !viajeDespachado) {
            //elemento con menor ETA
            ElementoPrioridad envuelto = (ElementoPrioridad) colaPrioridadVehiculos.sacar();
            Unidad unidadCercana = envuelto.getUnidad();
            boolean aceptaViaje = unidadCercana.simularAceptacionViaje();
            if (aceptaViaje) {
                this.setUnidadAsignada(unidadCercana);// se le asigna esa unidad si esta disponible
                this.getUnidadAsignada().setDisponible(false);//cambia estado
                this.setEtaFinal(motorGrafo.calcularRutaOptima(this.getUnidadAsignada().getIdNodoActual(), this.getPasajero().getIdNodoInterseccion()));
                viajeDespachado = true;
               /*  System.out.println("Solicitud ACEPTADA por: " + this.getUnidadAsignada().getIdVehiculo());*/
            }else{
                /*System.out.println("Denegado... El vehículo " + unidadCercana.getIdVehiculo() + " rechazó la alerta. Buscando al siguiente más cercano...");*/
            }
        }
        if (!viajeDespachado) { // Verificación y cierre del estado del despacho
            /*System.out.println("\nError... No se pudo asignar vehículo. Todas las unidades cercanas rechazaron o no hay taxis disponibles.");*/
            this.setUnidadAsignada(null);
            this.setEtaFinal(null);
        }else{
            this.mostrarDetalleDespacho();//cambiar con la interfaz grafica luego
        }
    }

    /**
     * Muestra en el Dashboard la información consolidada del despacho.
     */
    public void mostrarDetalleDespacho() {
        // Imprime el resultado en consola simulando la interfaz:
        System.out.println("Usuario:"+ this.getPasajero().getIdUsuario() +"|"+ "->"+
         "Destino:" +this.getIdNodoDestino()+"|"+ "Asignado a:"+
         this.getUnidadAsignada().getIdVehiculo()+ "|"+ "ETA:" +this.getEtaFinal());
    }

    public Usuario getPasajero() {
        return pasajero;
    }

    public void setPasajero(Usuario pasajero) {
        this.pasajero = pasajero;
    }

    public int getIdNodoDestino() {
        return idNodoDestino;
    }

    public void setIdNodoDestino(int idNodoDestino) {
        this.idNodoDestino = idNodoDestino;
    }

    public Unidad getUnidadAsignada() {
        return unidadAsignada;
    }

    public void setUnidadAsignada(Unidad unidadAsignada) {
        this.unidadAsignada = unidadAsignada;
    }

    public CalculoETA getEtaFinal() {
        return etaFinal;
    }

    public void setEtaFinal(CalculoETA etaFinal) {
        this.etaFinal = etaFinal;
    }
    
}