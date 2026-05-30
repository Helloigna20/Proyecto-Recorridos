package clases.codigo;
/**
 * Clase que gestiona la Matriz de Adyacencia para representar las conexiones
 * físicas entre las esquinas/intersecciones de la ciudad de Salta.
 */
public class MatrizAdyacencia {
    // ATRIBUTOS
    private final double[][] matriz;
    private final int cantNodos;
    private static final double infinito= Double.MAX_VALUE;


    /**
     * Constructor que inicializa la dimensión N x N de la matriz.
     */
    public MatrizAdyacencia(int cantidadNodos){
        this.cantNodos= cantidadNodos;
        this.matriz= new double[cantidadNodos][cantidadNodos];
        inicializarMatriz();
    }

    public void inicializarMatriz() {
        for (int i = 0; i < getCantNodos(); i++) {
            for (int j = 0; j < getCantNodos(); j++) {
                if(i==j){
                    this.matriz[j][j]=0;
                }
                else{
                    this.matriz[i][j]= getInfinito();
                }
            }
        }
    }


    private boolean esValido(int indice){
        return indice>=0 && indice<getCantNodos();
    }

    /**
     * Registra una conexión dirigida o doble mano en la matriz.
     * Si no es mano única, debe marcar la conexión en ambos sentidos.
     **/


    public void registrarConexion(int origenId, int destinoId, double valor){
        if (esValido(origenId) && esValido(destinoId)) {
            this.matriz[origenId][destinoId]= valor;
        }
        else{
            System.out.println("Error, el indice no es valido...");
        }
    }

    public double retornaConexion(int origenId, int destinoId){
        double valor;

        if(esValido(origenId) && esValido(destinoId)){
            valor= this.matriz[origenId][destinoId];
        }
        else{
            valor= getInfinito();
        }
        return valor;
    }

    //este seria el areConnected de la clase MatrizGrafo...

    public boolean estanConectados(int origenId , int destinoId){
        boolean band= false;

        if(esValido(origenId) && esValido(destinoId)){
            if (this.matriz[origenId][destinoId]!= getInfinito()){
                band= true;
            }
        }
        return band;
    }

    //ya me muestra la matriz con el tiempo estimado de viaje(peso) guardado
    public void imprimirMatriz(int limiteVisualizacion) {
        System.out.println("\n--- MATRIZ DE ADYACENCIA (Mano única vs Doble mano) ---");
        int size = Math.min(getCantNodos(), limiteVisualizacion);

        // Encabezado de columnas
        System.out.print("N\t");
        for (int j = 0; j < size; j++) System.out.print("["+j+"] ");
        System.out.println();

        for (int i = 0; i < size; i++) {
            System.out.print("["+i+"]\t"); // Encabezado de fila
            for (int j = 0; j < size; j++) {
                System.out.print(matriz[i][j] + "   ");
            }
            System.out.println();
        }
    }

    // GETTERS Y SETTERS
    public int getCantNodos() {
        return cantNodos;
    }

    public double getInfinito() {
        return infinito;
    }

    public double[][] getMatriz() {
        return matriz;
    }

}