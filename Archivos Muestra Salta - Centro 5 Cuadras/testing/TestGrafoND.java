package testing;
import java.util.Scanner;
import grafoNoDirigido.GrafoNoDirigido;

public class TestGrafoND {
	public static void main(String[] args) {		
		GrafoNoDirigido grafo;
		Scanner scanner = new Scanner(System.in);
		int grado;
		System.out.println("Ingrese orden grafo ");
		grado=scanner.nextInt();
		if (grado>0){
			grafo=new GrafoNoDirigido(grado);
			grafo.cargarGrafo();
			grafo.muestraGrafo();
			//grafo.muestraPrim(0);
//			grafo.muestraBEA();
			grafo.muestraKruskal();
//				
		} else{
			System.out.println("Error carga inicial grafo.");
		}
		scanner.close();
	}

}
