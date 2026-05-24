package clases.codigo;
/**
 * Clase que gestiona la Matriz de Adyacencia para representar las conexiones
 * físicas entre las esquinas/intersecciones de la ciudad de Salta.
 */
public class MatrizAdyacencia {
    // ATRIBUTOS
    private double[][] matriz;
    private final int cantNodos;
    private static final double infinito= Double.POSITIVE_INFINITY;

    //Busque en google cual era el infinto de java deah #flowerdudas

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

    //No necesito preguntar sobre si la calle es doble mano o no porque eso se supone que hace "simpleCodigo_Matriz"
    //Cabero utiliza:
    //Lógica de DOBLE MANO:
    //Si 'oneway' es 'yes', es mano única.
    //Si es 'no' o no existe el tag, es doble mano.
    //Tipo creo que con eso basta (si, no dormi leyendo e intentando entender que P* hace su codigo" >_<

    public void registrarConexion(int origenId, int destinoId, int valor){
        if (esValido(origenId) && esValido(origenId)) {
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


    public void imprimirMatriz(int limiteVisualizacion) {
        //este aun ando viendo como hacerlo...
    }

    // GETTERS Y SETTERS
    public int getCantNodos() {
        return cantNodos;
    }

    public static double getInfinito() {
        return infinito;
    }

    public double[][] getMatriz() {
        return matriz;
    }

}