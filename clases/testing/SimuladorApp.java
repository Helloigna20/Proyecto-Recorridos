package clases.testing;

import contenedores.ColaSLinkedList;
import clases.codigo.*;
import java.io.File;

public class SimuladorApp {
    public static void main(String[] args) {
        System.out.println("=========================================================");
        System.out.println("   INICIANDO SIMULADOR: MATCHING ENGINE - SALTA APP");
        System.out.println("=========================================================\n");

        // 1. Instanciar y cargar el Grafo con el mapa de Salta
        GestionGrafo motorGrafo = new GestionGrafo();
        
        // Colocá acá la ruta correcta a tu archivo geojson del mapa de Salta
        String rutaMapa = "archivos/CentroyMacroSALTA.geojson"; 
        
        File archivoMapa = new File(rutaMapa);
        if (!archivoMapa.exists()) {
            System.err.println("[ERROR CORRER PROGRAMA]: No se encontró el archivo '" + rutaMapa + "'.");
            System.err.println("Asegurate de ubicar el archivo .geojson en la raíz del proyecto.");
            return;
        }

        System.out.println("[CONFIG] -> Cargando calles e intersecciones de Salta desde GeoJSON...");
        motorGrafo.cargarMapaDesdeArchivo(rutaMapa);
        System.out.println("[CONFIG] -> Grafo cargado exitosamente.\n");

        // 2. Crear la flota urbana usando tu estructura ColaSLinkedList
ColaSLinkedList listaUnidadesSistema = new ColaSLinkedList();

// Registramos los taxis en esquinas/nodos reales existentes (entre 0 y 402)
// Usamos IDs bajos (0, 1, 2, 3, 4) ya que la matriz muestra que están conectados consecutivamente
Unidad taxi0 = new Unidad("Taxi 0 (Premium)", 0);
Unidad taxi1 = new Unidad("Taxi 1 (Estándar)", 1);
Unidad taxi2 = new Unidad("Taxi 2 (Eco)", 2);
Unidad taxi3 = new Unidad("Taxi 3 (Camioneta)", 3);
Unidad taxi4 = new Unidad("Taxi 4 (Estándar)", 4);

listaUnidadesSistema.meter(taxi0);
listaUnidadesSistema.meter(taxi1);
listaUnidadesSistema.meter(taxi2);
listaUnidadesSistema.meter(taxi3);
listaUnidadesSistema.meter(taxi4);

System.out.println("[SISTEMA] -> Flota inicial del sistema inicializada (5 unidades activas).");

// 3. Crear al usuario pasajero en su esquina de origen dentro del rango válido
// Ejemplo: Franco espera en el nodo 5 y quiere ir al nodo 15
int nodoOrigenFranco = 5;
int nodoDestinoFranco = 15;

Usuario pasajero = new Usuario("Franco", nodoOrigenFranco);
SolicitudViaje solicitud = new SolicitudViaje(pasajero, nodoDestinoFranco);

// 5. Procesar y despachar
solicitud.procesarYDespachar(listaUnidadesSistema, motorGrafo);

// 6. Simular la finalización del viaje: actualizar posición del taxi
solicitud.completarViaje();
System.out.println("[SISTEMA] -> Pasajero registrado: " + pasajero.getIdUsuario() 
                   + " esperando en Nodo Intersección ID: " + pasajero.getIdNodoInterseccion());
        System.out.println("=====================================================================");
        System.out.println("             FIN DE LA SIMULACIÓN DE DESPACHO");
        System.out.println("=========================================================");
    }
}