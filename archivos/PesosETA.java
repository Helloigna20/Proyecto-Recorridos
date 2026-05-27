public class PesosETA{

    /**
     * ¿Cuál es la velocidad promedio de una calle residencial?...
     * Esta clase solo me calcula la velocidad de una calle dependiendo de que tipo es.
     * Calcula el peso de cada arista.
     */

    public static double obtenerVelocidadMS(String tipoCalle) {
        switch (tipoCalle) {
            case "primary":
                return 45.0 / 3.6;   // 12.5 m/s
            case "secondary":
                return 35.0 / 3.6; // 9.7 m/s
            case "residential":
            case "tertiary":
                return 25.0 / 3.6;  // 6.9 m/s
            default:
                return 20.0 / 3.6;          // 5.5 m/s
        }
    }
}
