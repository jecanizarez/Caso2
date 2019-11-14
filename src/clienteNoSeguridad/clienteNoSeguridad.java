package clienteNoSeguridad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import src.ProtocoloCliente;
import uniandes.gload.core.Task;

public class clienteNoSeguridad extends Task{

	public final static int PUERTO = 3400;
	public static final String SERVIDOR = "localhost";
	
	public void execute()
	{
		Socket socket = null;
		PrintWriter escritor = null;
		BufferedReader lector = null;
		
		System.out.println("Cliente esta conectado al servidor");
		
		try
		{
			socket = new Socket(SERVIDOR, PUERTO);
			escritor = new PrintWriter(socket.getOutputStream(), true);
			lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch (IOException e)
		{
			System.err.println("Exception: " + e.getMessage());
			System.exit(1);
		}
		
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			ProtocoloSinSeguridad.protocoloInicio(stdIn, lector, escritor);
			escritor.close();
			lector.close();
			socket.close();
			stdIn.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		
	}
	@Override
	public void fail() {
		System.out.println(Task.MENSAJE_FAIL);
	}

	@Override
	public void success() {
		System.out.println(Task.OK_MESSAGE);
	}
	
	
		
	

}
