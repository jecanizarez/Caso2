package src;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
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

	private static PublicKey llavePublicaServidor; 

	private static SecretKey llaveSesion;






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


	public static void protocoloInicio(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut) throws IOException, CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException
	{

		pOut.println("HOLA");

		verificarRespuesta(stdIn, pIn, pOut, pIn.readLine());

		String algoritmos = seleccionarAlgoritmos();
		System.out.println("El cliente esta enviando los algoritmos seleccionados: " + algoritmos);
		pOut.println(algoritmos);

		verificarRespuesta(stdIn, pIn, pOut, pIn.readLine());

		byte[] certificadoServidorBytes = DatatypeConverter.parseBase64Binary(pIn.readLine());
		CertificateFactory creator = CertificateFactory.getInstance("X.509");
		InputStream in = new ByteArrayInputStream(certificadoServidorBytes);
		X509Certificate certificado = (X509Certificate) creator.generateCertificate(in);
		certificado.checkValidity();
		System.out.println("El certificado enviado por el cervidor es valido");

		llavePublicaServidor = certificado.getPublicKey();
		llaveSesion = generarLlaveSesion();
		byte[] llaveSesionBytes = llaveSesion.getEncoded();
		byte[] llaveSesionEncriptada = cifrarAsimetrico(llavePublicaServidor, llaveSesionBytes);
		String llaveSesionEncriptadaString = DatatypeConverter.printBase64Binary(llaveSesionEncriptada);
		pOut.println(llaveSesionEncriptadaString);
		System.out.println("Se envió la llave de sesion");
		
		System.out.println("Digite el reto");
		String reto = stdIn.readLine();
		reto = verificarLongitud(reto);
		pOut.println(reto);
		System.out.println("Se envio el reto");
		
		String retoCifrado = pIn.readLine();
		byte[] retoCifradoBytes =  DatatypeConverter.parseBase64Binary(retoCifrado);
		byte[] retoDescifrado = descifrarSimetrico(llaveSesion,retoCifradoBytes);
		String respuestaServidor = DatatypeConverter.printBase64Binary(retoDescifrado);
		String respuesta = "";
		if(respuestaServidor.equals(reto))
		{
			respuesta = "OK";
		}
		else
		{
			respuesta = "ERROR";
		}
		pOut.println(respuesta);
		
		
		System.out.println("Digite su documento");
		String documento = stdIn.readLine();
		documento = verificarLongitud(documento);
		byte[] documentoBytes = DatatypeConverter.parseBase64Binary(documento);
		byte[] documentoCifrada = cifrarSimetrico(llaveSesion, documentoBytes);
		String documentoCifradaString = DatatypeConverter.printBase64Binary(documentoCifrada);
		pOut.println(documentoCifradaString);
		System.out.println("Documento enviado");
		
		System.out.println("Digite su contraseña");
		String contraseña = stdIn.readLine();
		contraseña = verificarLongitud(contraseña);
		byte[] contraseñaBytes = DatatypeConverter.parseBase64Binary(contraseña);
		byte[] contraseñaCifrada = cifrarSimetrico(llaveSesion, contraseñaBytes);
		String contraseñaCifradaString = DatatypeConverter.printBase64Binary(contraseñaCifrada);
		pOut.println(contraseñaCifradaString);
		System.out.println("contraseña enviada");
		
		
		String valor = pIn.readLine(); 
		System.out.println("Recibo el valor sifrado con la llave de sesion");
		byte[] valorBytes = DatatypeConverter.parseBase64Binary(valor);
		byte[] valorDescifrado = descifrarSimetrico(llaveSesion, valorBytes);
		
		
		
		String valorServidor = pIn.readLine(); 
		System.out.println("Recibo el hmac cifrado con la privada del servidor");
		byte[] valorServidorBytes = DatatypeConverter.parseBase64Binary(valorServidor);
		byte[] valorServidorHMAC = descifrarAsimetrico(llavePublicaServidor, valorServidorBytes);
		
		Mac cifradorMAC = Mac.getInstance(algIntegridadSeleccionado);
		cifradorMAC.init(llaveSesion);
		byte[] valorHmac = cifradorMAC.doFinal(valorDescifrado);


		
		
		if(Arrays.equals(valorHmac, valorServidorHMAC))
		{
			System.out.println("El codigo de autenticación de los valores coincide, el valor no ha sido modificado");
			respuesta = "OK";
		}
		else
		{
			System.out.println("No coincide");
			respuesta = "ERROR";
		}
		pOut.println(respuesta);
		System.out.println("La conexión ha terminado");
		
		
		
		
		
		
		
		




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
		algoritmosIntegridad[1] = HMACSHA1;
		algoritmosIntegridad[2] = HMACSHA256;
		algoritmosIntegridad[3] = HMACSHA384;
		algoritmosIntegridad[4] = HMACSHA512;

		num = numberGenerator.nextInt(5); 	
        algIntegridadSeleccionado = algoritmosIntegridad[num];
		retorno = retorno + algoritmosIntegridad[num];


		return retorno;
	}
	public static void verificarRespuesta(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut, String respuesta) throws IOException
	{

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
	}
	public static SecretKey generarLlaveSesion() throws NoSuchAlgorithmException
	{
		KeyGenerator keyGenerator = KeyGenerator.getInstance(algSimetricoSeleccionado);
		SecureRandom secureRandom = new SecureRandom();
		int keyBitSize = 0;
		if (algSimetricoSeleccionado.equals("DES")) 
		{
			keyBitSize = 56;
		} else if (algSimetricoSeleccionado.equals("AES")) 
		{
			keyBitSize = 128;
		} else if (algSimetricoSeleccionado.equals("Blowfish")) 
		{
			keyBitSize = 128;
		} else if (algSimetricoSeleccionado.equals("RC4")) 
		{
			keyBitSize = 128;
		}

		keyGenerator.init(keyBitSize, secureRandom);

		return keyGenerator.generateKey();
	}

	public static byte[] cifrarSimetrico(SecretKey llave, byte[] texto)
	{
		byte[] textoCifrado;
		try
		{
			Cipher cifrador = Cipher.getInstance(algSimetricoSeleccionado);
			cifrador.init(Cipher.ENCRYPT_MODE, llave);
			textoCifrado = cifrador.doFinal(texto);
			return textoCifrado;
		}
		catch(Exception e)
		{
			System.out.println("Ocurrio un error al cifrar el mensaje");
			System.exit(-1);
			return null;
		}
	}

	public static byte[] descifrarSimetrico(SecretKey llave, byte[] texto)
	{
		byte[] textoClaro;
		try
		{
			Cipher descifrador = Cipher.getInstance(algSimetricoSeleccionado);
			descifrador.init(Cipher.DECRYPT_MODE, llave);
			textoClaro = descifrador.doFinal(texto);
		}
		catch(Exception e)
		{
			System.out.println("Ocurrio un error al descifrar el mensaje");
			System.exit(-1);
			return null;
		}
		return textoClaro;
	}
	
	public static byte[] cifrarAsimetrico(Key llave, byte[] texto)
	{
		byte[] textoCifrado;
		try
		{
			Cipher cifrador = Cipher.getInstance(RSA);
			cifrador.init(Cipher.ENCRYPT_MODE, llave);
			textoCifrado = cifrador.doFinal(texto);
			return textoCifrado;
		}
		catch(Exception e)
		{
			System.out.println("Ocurrio un error al cifrar el mensaje");
			System.exit(-1);
			return null;
		}
	}
	
	public static byte[] descifrarAsimetrico(Key llave, byte[] texto)
	{
		byte[] textoClaro;
		try
		{
			Cipher descifrador = Cipher.getInstance(RSA);
			descifrador.init(Cipher.DECRYPT_MODE, llave);
			textoClaro = descifrador.doFinal(texto);
		}
		catch(Exception e)
		{
			System.out.println("Ocurrio un error al descifrar el mensaje");
			System.exit(-1);
			return null;
		}
		return textoClaro;
	}
	
	public static String verificarLongitud(String palabra)
	{
		  while(palabra.length()%4 != 0)
		    {
		    	palabra += "0";
		    }
		  return palabra;
	}

}
