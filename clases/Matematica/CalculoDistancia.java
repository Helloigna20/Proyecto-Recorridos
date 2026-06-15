package clases.Matematica;

public class CalculoDistancia {
    public static double RADIO_TIERRA= 6371*1000;

    /**
     * ¿Qué distancia fisica hay entre dos puntos GPS?
     * Haversine es una formula matematica geometrica que calcula eso, la distancia entre dos
     puntos sobre una esfera. Para eso debemos tener dos puntos con coordenadas en radianes, donde
     la distancia se calcula de la siguiente forma:

     Delta_phi = phi_2 - phi_1
     Delta_lambda = lambda_2 - lambda_1

     a = sin^2(Delta_phi/2) + cos(phi_1) * cos(phi_2) * sin^2(Delta_lambda/2)

     c = 2* atan2(raiz(a), raiz(1-a))
     d = R * c

     donde R es el radio de la tierra ;
     */

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double Lat = Math.toRadians(lat2 - lat1);
        double Lon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(Lat / 2) * Math.sin(Lat / 2) + Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) *
                Math.sin(Lon / 2) * Math.sin(Lon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        //Retorna la distancia
        return RADIO_TIERRA * c; // Resultado en metros
    }
}
