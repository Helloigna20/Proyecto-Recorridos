package clases.codigo;

import contenedores.ListaDoubleLinkedL;

public class GrafoD extends Grafo{
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

    public void Dijkstra(int verticeInicial){
        double minCost, currCost, arcCost; int minVertex, vertex;

        this.listaDistancia = new ListaDoubleLinkedL();
        this.listaCamino = new ListaDoubleLinkedL();
        this.listaSolucion = new ListaDoubleLinkedL();

        for (int i=0; i<getOrden();i++){
            this.listaSolucion.insertar(-1, i);
            this.listaCamino.insertar(-1, i);
            this.listaDistancia.insertar(this.matriz.getInfinito(), i);
        }
        this.listaSolucion.reemplazar(verticeInicial,verticeInicial); // el primer vertice del camino

        for (int i=0; i<getOrden();i++){
            if (i!=verticeInicial){
                this.listaDistancia.reemplazar(this.matriz.retornaConexion(verticeInicial, i), i);
                this.listaCamino.reemplazar(verticeInicial, i);
            }
        }

        for (int i=1; i<getOrden();i++){
            minCost= this.matriz.getInfinito() ;
            minVertex=-1;

            for (int w=0; w<getOrden();w++){
                if (w!=verticeInicial){
                    currCost=(double) this.listaDistancia.devolver(w);//
                    vertex=(int) this.listaSolucion.devolver(w);
                    if (currCost<minCost && vertex==-1){
                        minCost=currCost; minVertex=w;
                    }
                }
            }

            if(minVertex!=-1){
                //System.out.println("it " + i + " minVertex " + minVertex + " minCost " + minCost);
                this.listaSolucion.reemplazar(minVertex, minVertex);
                this.listaDistancia.reemplazar(minCost, minVertex);

                for (int v=0;v<getOrden();v++){
                    vertex=(int)this.listaSolucion.devolver(v);
                    if (vertex==-1){
                        arcCost=(double)this.matriz.retornaConexion(minVertex, v);
                        currCost=(double)this.listaDistancia.devolver(v);
                        if (minCost+arcCost<currCost){
                            this.listaDistancia.reemplazar(minCost+arcCost, v);
                            this.listaCamino.reemplazar(minVertex, v);

                        }
                    }
                }
            }
        }
    }


}
