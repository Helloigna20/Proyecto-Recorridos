package contenedores;
import clases.codigo.Unidad;
public class ElementoPrioridad {
    private Unidad unidad;
    private double pesoETA; // El tiempo en segundos que tardará en llegar

    public ElementoPrioridad(Unidad unidad, double pesoETA) {
        this.unidad = unidad;
        this.pesoETA = pesoETA;
    }

    public Unidad getUnidad() { return unidad; }
    public double getPesoETA() { return pesoETA; }
}