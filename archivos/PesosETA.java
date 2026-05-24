// 1. Definir un método para obtener velocidad según el tipo de calle
public static double obtenerVelocidadMS(String tipoCalle) {
    switch (tipoCalle) {
        case "primary": return 45.0 / 3.6;   // 12.5 m/s
        case "secondary": return 35.0 / 3.6; // 9.7 m/s
        case "residential":
        case "tertiary": return 25.0 / 3.6;  // 6.9 m/s
        default: return 20.0 / 3.6;          // 5.5 m/s
    }
}

// 2. En el bucle donde llenas la matriz:
String tipoCalle = props.optString("highway", "residential");
double velocidad = obtenerVelocidadMS(tipoCalle);

for (int j = 0; j < coords.length() - 1; j++) {
    Node u = puntos.get(coords.get(j));
    Node v = puntos.get(coords.get(j+1));
    
    double distancia = haversine(u, v);
    double tiempoSegundos = distancia / velocidad; // Este es el PESO real

    matrizTiempo[idU][idV] = tiempoSegundos;
    if (!esManoUnica) {
        matrizTiempo[idV][idU] = tiempoSegundos;
    }
}