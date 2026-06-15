package clases.Matematica;

public class Grafo{
    protected int cantNodos;
    protected MatrizAdyacencia matriz;

    public Grafo(int ordenGrafo){
        this.cantNodos= ordenGrafo;
        this.matriz= new MatrizAdyacencia(getOrden());
    }

    public int getOrden() {
        return  cantNodos;
    }

    public void agregarArista(int origen, int destino, double peso){
        this.matriz.registrarConexion(origen, destino, (int)peso);
    }

    public double obtenerPeso(int origen, int destino){
        return this.matriz.retornaConexion(origen, destino);
    }

    public void mostrarGrafo(int ordenGrafo) {
        this.matriz.imprimirMatriz(ordenGrafo);

    }
}
