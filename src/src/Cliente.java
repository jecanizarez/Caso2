package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.NoSuchPaddingException;

import uniandes.gload.core.Task;

public class Cliente  extends Task{
	
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
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			socket = new Socket(SERVIDOR, PUERTO);
			escritor = new PrintWriter(socket.getOutputStream(), true);
			lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			ProtocoloCliente.protocoloInicio(stdIn, lector, escritor);
			escritor.close();
			lector.close();
			socket.close();
			stdIn.close();
		}
		catch (IOException | InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchPaddingException e)
		{
			System.err.println("Exception: " + e.getMessage());
			System.exit(1);
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
