package clienteNoSeguridad;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
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

public class ProtocoloSinSeguridad {

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

/**
 * Metodo que contiene todo el protocolo de comunicaci�n con el servidor
 * @param stdIn
 * @param pIn
 * @param pOut
 * @throws IOException
 * @throws CertificateException
 * @throws NoSuchAlgorithmException
 * @throws NoSuchPaddingException
 * @throws InvalidKeyException
 */
	public static void protocoloInicio(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut) throws IOException, CertificateException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException
	{
        //Se envia la palabra necesaria para iniciar el protocolo con el servidor
		pOut.println("HOLA");
		
        //Se verifica la respuesta del servidor
		verificarRespuesta(stdIn, pIn, pOut, pIn.readLine());

		//Se seleccionas los algoritmos que se van usar durante la comunicacion con el servidor aleatoriamente.
		String algoritmos = seleccionarAlgoritmos();
		System.out.println("El cliente esta enviando los algoritmos seleccionados: " + algoritmos);
		
		//Se envian los algoritmos al servidor
		pOut.println(algoritmos);
        //Se verifica la respuesta del servidor
		verificarRespuesta(stdIn, pIn, pOut, pIn.readLine());
		
        //Se recibe el certificado del servidor
		byte[] certificadoServidorBytes = DatatypeConverter.parseBase64Binary(pIn.readLine());
		CertificateFactory creator = CertificateFactory.getInstance("X.509");
		InputStream in = new ByteArrayInputStream(certificadoServidorBytes);
		X509Certificate certificado = (X509Certificate) creator.generateCertificate(in);
		
		//Se verifica que el certificado del servidor sea valido
		certificado.checkValidity();
		System.out.println("El certificado enviado por el cervidor es valido");
        
		//Se guarda la llave publica del cervidor
		llavePublicaServidor = certificado.getPublicKey();

		//Se genera la llave de sesion.
		llaveSesion = generarLlaveSesion();
		
		//Se cifra la llave de sesion con la llave publica del servidor
		byte[] llaveSesionBytes = llaveSesion.getEncoded();
		byte[] llaveSesionEncriptada = cifrarAsimetrico(llavePublicaServidor, llaveSesionBytes);
		String llaveSesionEncriptadaString = DatatypeConverter.printBase64Binary(llaveSesionEncriptada);
		
		//Se envia la llave cifrada al servidor
		pOut.println(llaveSesionEncriptadaString);
		System.out.println("Se envi� la llave de sesion");
		
		System.out.println("Digite el reto");
		String reto = stdIn.readLine();
		//Se verifica la longitud del reto
		reto = verificarLongitud(reto);
		
		//Se envia el reto al servidor
		pOut.println(reto);
		System.out.println("Se envio el reto");
		
		//Se lee el reto cifrado por la llave de sesion
		String retoServidor = pIn.readLine();

		String respuesta = "";
		//Se verifica que el reto digitado por el usuario y el enviado encriptado por el servidor coincidan. Esto con el fin de probrar que la llave simetrica funcione
		if(retoServidor.equals(reto))
		{
			respuesta = "OK";
		}
		else
		{
			respuesta = "ERROR";
		}
		//Se envia la respuesta al servidor
		pOut.println(respuesta);
		
		
		System.out.println("Digite su documento");
		//Se lee el documento del usuario
		String documento = stdIn.readLine();
		
		//Se envia al servidor
		pOut.println(documento);
		System.out.println("Documento enviado");
		
		System.out.println("Digite su contrase�a");
		//Sw lee el cvv del usuario
		String contrase�a = stdIn.readLine();
		
		//Se envia al serviddor
		pOut.println(contrase�a);
		System.out.println("contrase�a enviada");
		
		
		//Se recibe el valor enviado por el servidor se descifra con la llave de sesion
		String valor = pIn.readLine(); 
		
		
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
		byte[] valorServidoBytes = messageDigest.digest( valor.getBytes() );
		
		//Se recibe el valor del servidor se descifra con la llave publica para obtener el Hmac
		String valorServidor = pIn.readLine(); 
        byte[] hashValor = valorServidor.getBytes();


		
		//Se verifican que los Hmac coincidan
		if(Arrays.equals(valorServidoBytes, hashValor))
		{
			System.out.println("El codigo de autenticaci�n de los valores coincide, el valor no ha sido modificado");
			respuesta = "OK";
		}
		else
		{
			System.out.println("No coincide");
			respuesta = "ERROR";
		}
		//Se envia la respuesta al servidor
		pOut.println(respuesta);
		System.out.println("La conexi�n ha terminado");
		
		//Se cierra la conexion
		stdIn.close();
		pIn.close();
		pOut.close();
		System.exit(-1);
		
		
		
		
		
		
		
		




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
	/**
	 * Metodo para verificar la respuesta del servidor. En caso de que esta sea de ERROR se cierra la comunicaci�n y se detiene el programa.
	 * @param stdIn
	 * @param pIn
	 * @param pOut
	 * @param respuesta
	 * @throws IOException
	 */
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
	/**
	 * Metodo para generar la llave simetrica de sesi�n para poder comunicarse con el servidor
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
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
/**
 * Metodo para cifrar con la llave de sesi�n de la comunicaci�n
 * @param llave
 * @param texto
 * @return
 */
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

	
	/**
	 * Metodo para descifrar con la llave de sesi�n durante el protocolo de comunicacion
	 * @param llave
	 * @param texto
	 * @return
	 */
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
	/**
	 * Metodo para cifrar con llaves publicas o privadas para el protocolo de comunicacion con el servidor
	 * @param llave
	 * @param texto
	 * @return
	 */
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
	
	/**
	 * Metodo para verificar la longitud de las palabras que se necesitan enviar al servidor. Se verifica que su longitud sea un multiplo de 4.
	 * @param palabra
	 * @return
	 */
	public static String verificarLongitud(String palabra)
	{
		  while(palabra.length()%4 != 0)
		    {
		    	palabra += "0";
		    }
		  return palabra;
	}

}

