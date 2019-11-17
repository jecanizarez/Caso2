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
 * Metodo que contiene todo el protocolo de comunicación con el servidor
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
		
		byte[] llaveSesionBytes = llaveSesion.getEncoded();
		
		
		//Se envia la llave  al servidor
		pOut.println(llaveSesionBytes);
		System.out.println("Se envió la llave de sesion");
		

		String reto = "1234";
		//Se verifica la longitud del reto
		
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
		

		//Se lee el documento del usuario
		String documento = "1234";
		//Se envia al servidor
		pOut.println(documento);
		System.out.println("Documento enviado");
		
		
		//Sw lee el cvv del usuario
		String contraseña = "1234";
		
		//Se envia al serviddor
		pOut.println(contraseña);
		System.out.println("contraseña enviada");
		
		
		//Se recibe el valor enviado por el servidor 
		String valor = pIn.readLine(); 
		
		
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
		byte[] valorServidoBytes = messageDigest.digest( valor.getBytes() );
		
		//Se recibe el valor del servidor se descifra con la llave publica para obtener el Hmac
		String valorServidor = pIn.readLine(); 
        byte[] hashValor = DatatypeConverter.parseBase64Binary(valorServidor);


		
		//Se verifican que los Hmac coincidan
		if(Arrays.equals(valorServidoBytes, hashValor))
		{
			System.out.println("El codigo de autenticación de los valores coincide, el valor no ha sido modificado");
			respuesta = "OK";
		}
		else
		{
			System.out.println("No coincide");
			respuesta = "ERROR";
		}
		//Se envia la respuesta al servidor
		pOut.println(respuesta);
		System.out.println("La conexión ha terminado");
		
		//Se cierra la conexion

		
		
		
		
		
		
		
		




	}

	public static String seleccionarAlgoritmos() 
	{
		String retorno = "ALGORITMOS:RC4:RSA:HMACSHA384";

		algSimetricoSeleccionado = RC4;
		algIntegridadSeleccionado = HMACSHA384;


		return retorno;
	}
	/**
	 * Metodo para verificar la respuesta del servidor. En caso de que esta sea de ERROR se cierra la comunicación y se detiene el programa.
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
	 * Metodo para generar la llave simetrica de sesión para poder comunicarse con el servidor
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
 * Metodo para cifrar con la llave de sesión de la comunicación
 * @param llave
 * @param texto
 * @return
 */
	

}

