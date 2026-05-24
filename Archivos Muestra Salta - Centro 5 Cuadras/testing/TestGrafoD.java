package testing;
import grafoDirigido.*;
import java.util.Scanner;

public class TestGrafoD {
	
	public static void main(String[] args) {
		GrafoDirigido grafo;
		int grado,i,j;
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Ingrese orden grafo ");
		grado=scanner.nextInt();
		if (grado>0){
			grafo=new GrafoDirigido(grado);
			grafo.cargarGrafo();
			grafo.muestraGrafo();/*
			for(int k=0;k<grado;k++){grafo.muestraDijkstra(k);} 
			grafo.muestraBPF();
			grafo.muestraFloyd();
			System.out.println();
			grafo.muestraGrafo();
			System.out.println("buscar camino Floyd (-1 para terminar): ");
			i=scanner.nextInt();
			while(i!=-1){
				System.out.print("Camino entre ");
				i=scanner.nextInt();
				System.out.print(" y ");
				j=scanner.nextInt();
				grafo.muestraCaminoFloyd(i, j);
				System.out.println("buscar camino Floyd (-1 para terminar): ");
				i=scanner.nextInt();
			}*/
			grafo.muestraBEA();
			
			
		} else{
			System.out.println("Error carga inicial grafo.");
		}
		scanner.close();
	}

}



