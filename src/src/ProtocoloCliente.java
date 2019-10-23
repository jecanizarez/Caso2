package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

public class ProtocoloCliente {

	// Algoritmos cifrado simetrico
	private static final String DES = "DES";
	private static final String AES = "AES";
	private static final String BLOWFISH = "Blowfish";
	private static final String RC4 = "RC4";

	//Algoritmos cifrado asimetrico
	private static final String RSA = "RSA";
	//Algoritmos cifrado para integridad
	private static final String HMACMD5 = "HMACMD5";
	private static final String HMACSHA1 = "HMACSHA1";
	private static final String HMACSHA256 = "HMACSHA256";
	private static final String HMACSHA384 = "HMACSHA384";
	private static final String HMACSHA512 = "HMACSHA512";

	private static String algSimetricoSeleccionado = "";

	private static String algIntegridadSeleccionado = "";




	public static void procesar(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut) throws IOException
	{
		String palabra = stdIn.readLine();
		pOut.println(palabra);

		String respuesta = pIn.readLine();
		if(respuesta.equals("OK"))
		{
			System.out.println("Recibi un OK");
		}
	}


	public static void protocoloInicio(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut) throws IOException
	{

		pOut.println("HOLA");
		String respuesta = pIn.readLine();
		if(respuesta.equals("ERROR"))
		{
			System.out.println("Ha ocurrido un error");
			stdIn.close();
			pIn.close();
			pOut.close();
			System.exit(-1);
		}
		else if(!respuesta.equals("OK"))
		{
			System.out.println("No se ha recibido lo que se esperaba");
			stdIn.close();
			pIn.close();
			pOut.close();
			System.exit(-1);
		}
        String algoritmos = seleccionarAlgoritmos();
		pOut.println(algoritmos);
		byte[] certificadoEncoded = DatatypeConverter.parseBase64Binary(pIn.readLine());
		
		
	}

	public static String seleccionarAlgoritmos() 
	{

		String retorno = "ALGORITMOS:";

		String[] algoritmosSimetricos = new String[4];
		algoritmosSimetricos[1] = DES;
		algoritmosSimetricos[2] = AES;
		algoritmosSimetricos[3] = RC4;

		Random numberGenerator = new Random();
		int num = numberGenerator.nextInt(3) + 1; 

		algSimetricoSeleccionado = algoritmosSimetricos[num];
		retorno = retorno + algoritmosSimetricos[num] + ":" + RSA + ":";


		String[] algoritmosIntegridad = new String[5];
		algoritmosIntegridad[0] = HMACMD5;
		algoritmosIntegridad[0] = HMACSHA1;
		algoritmosIntegridad[0] = HMACSHA256;
		algoritmosIntegridad[0] = HMACSHA384;
		algoritmosIntegridad[0] = HMACSHA512;
		
		num = numberGenerator.nextInt(5); 	
		
		retorno = retorno + algoritmosIntegridad[num];
		
		
		return retorno;
	}

}
