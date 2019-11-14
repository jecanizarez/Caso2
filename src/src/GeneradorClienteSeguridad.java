package src;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

public class GeneradorClienteSeguridad {
	
	private LoadGenerator generator;
	
	public GeneradorClienteSeguridad()
	{
		Task work = createTask();
		int numeroTransacciones = 2;
		int tiempo = 20;
		generator = new LoadGenerator("Client - Server Load Test", numeroTransacciones, work, tiempo);
		generator.generate();
	}
	
	private Task createTask()
	{
		return new Cliente();
	}
	
	public static void main(String[] args) {
		
		@SuppressWarnings("unused")
		GeneradorClienteSeguridad sefuepa = new GeneradorClienteSeguridad();
	}

}
