package clases.codigo;
/**
 * Clase que gestiona la Matriz de Adyacencia para representar las conexiones
 * físicas entre las esquinas/intersecciones de la ciudad de Salta.
 */
public class MatrizAdyacencia {
    // ATRIBUTOS
    // Matriz de enteros o dobles que representará si hay conexión o no (1 ó 0) 
    // o que guardará temporalmente las distancias calculadas.
    private double[][] matriz;
    private int cantidadNodos;

    /**
     * Constructor que inicializa la dimensión N x N de la matriz.
     */
    public MatrizAdyacencia(int cantidadNodos) {
        // TODO: Inicializar la matriz cuadrada basándose en el total de puntos únicos.
    }

    /**
     * Registra una conexión dirigida o doble mano en la matriz.
     * Si no es mano única, debe marcar la conexión en ambos sentidos.
     */
    public void registrarConexion(int origenId, int destinoId, boolean esManoUnica) {
        // TODO: Asignar el valor correspondiente en la posición [origenId][destinoId].
        // TODO: Si 'esManoUnica' es falso, asignar también en [destinoId][origenId].
    }

    /**
     * Método auxiliar para visualizar un subconjunto de la matriz en consola.
     */
    public void imprimirMatriz(int limiteVisualizacion) {
        // TODO: Iterar sobre las filas y columnas hasta el límite indicado.
        // TODO: Mostrar en formato tabular para comprobar la conectividad del GeoJSON.
    }

    // GETTERS Y SETTERS
    public double[][] getMatriz() { return matriz; }
    public int getCantidadNodos() { return cantidadNodos; }
}