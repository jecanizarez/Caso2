package clienteNoSeguridad;


import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

public class GeneradorClienteNoSeguridad {
	
	private LoadGenerator generator;
	
	public GeneradorClienteNoSeguridad()
	{
		Task work = createTask();
		int numeroTransacciones = 2;
		int tiempo = 20;
		generator = new LoadGenerator("Client - Server Load Test", numeroTransacciones, work, tiempo);
		generator.generate();
	}
	
	private Task createTask()
	{
		return new clienteNoSeguridad();
	}
	
	public static void main(String[] args) {
		
		@SuppressWarnings("unused")
		GeneradorClienteNoSeguridad sefuepa = new GeneradorClienteNoSeguridad();
	}

}