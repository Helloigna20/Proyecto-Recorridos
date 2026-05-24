package grafoNoDirigido;

import java.util.Scanner;

public class GrafoNoDirigido extends AbsGrafoND{
	public GrafoNoDirigido(int ordenGrafo){
		super(ordenGrafo);
	}
	
	@Override
	public void cargarGrafo(){
		double currCost;		
		Scanner scanner = new Scanner(System.in);
		
		for (int i=0; i<getOrden();i++){
			for (int j=i;j<getOrden();j++){
				if (i!=j){
					System.out.println("Ingrese costo[" + i + "," + j + "] (sino -1)");
					currCost=scanner.nextDouble();
					if (currCost!=-1){
						this.matrizCosto.actualizar(currCost, i, j);
						this.matrizCosto.actualizar(currCost, j, i);
					}else{
						this.matrizCosto.actualizar(infinito, i, j);
						this.matrizCosto.actualizar(infinito, j, i);
					}					
				}else{
					this.matrizCosto.actualizar(infinito, i, j);
				}
			}
		} 
   	
	}
}
