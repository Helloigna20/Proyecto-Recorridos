package clases.Matematica;

import contenedores.GraphPriorityQueue;
import contenedores.ListaDoubleLinkedL;
import recursos.Connection;

public class GrafoD extends Grafo {
    private ListaDoubleLinkedL listaDistancia, listaCamino, listaSolucion;

    public GrafoD(int ordenGrafo) {
        super(ordenGrafo);
    }

    public ListaDoubleLinkedL getListaDistancia() {
        return listaDistancia;
    }

    public ListaDoubleLinkedL getListaCamino() {
        return listaCamino;
    }

    public void Dijkstra(int verticeInicial) {
        // Inicializar las tres listas con valores por defecto
        this.listaDistancia = new ListaDoubleLinkedL();
        this.listaCamino    = new ListaDoubleLinkedL();
        this.listaSolucion  = new ListaDoubleLinkedL();
 
        for (int i = 0; i < getOrden(); i++) {
            this.listaSolucion.insertar(false, i);          // false = no visitado
            this.listaCamino.insertar(-1, i);               // predecesor desconocido
            this.listaDistancia.insertar(this.matriz.getInfinito(), i);
        }
 
        // El vértice inicial tiene distancia 0 y se marca como origen de sí mismo
        this.listaDistancia.reemplazar(0.0, verticeInicial);
        this.listaCamino.reemplazar(verticeInicial, verticeInicial);
 
        // Cola de prioridad: extrae siempre el nodo con menor costo acumulado
        GraphPriorityQueue cola = new GraphPriorityQueue();
        cola.meter(new Connection(verticeInicial, verticeInicial, 0.0));
 
        while (!cola.estaVacia()) {
            // Extraer el nodo no visitado con menor distancia acumulada
            Connection actual = (Connection) cola.sacar();
            int u = actual.getVertexJ();
 
            // Si ya fue procesado, ignorar (pueden quedar entradas viejas en la cola)
            if ((boolean) this.listaSolucion.devolver(u)) continue;
 
            // Marcar como visitado
            this.listaSolucion.reemplazar(true, u);
 
            double distU = (double) this.listaDistancia.devolver(u);
 
            // Relajar todos los vecinos de u
            for (int v = 0; v < getOrden(); v++) {
                double pesoArco = this.matriz.retornaConexion(u, v);
 
                // Solo si hay arista real (no infinito) y v no fue visitado
                if (pesoArco < this.matriz.getInfinito()
                        && !(boolean) this.listaSolucion.devolver(v)) {
 
                    double nuevaDist = distU + pesoArco;
                    double distActualV = (double) this.listaDistancia.devolver(v);
 
                    if (nuevaDist < distActualV) {
                        this.listaDistancia.reemplazar(nuevaDist, v);
                        this.listaCamino.reemplazar(u, v);
                        cola.meter(new Connection(u, v, nuevaDist));
                    }
                }
            }
        }
    }


}
