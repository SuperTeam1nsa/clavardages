package com.clava.model.crypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	static public byte[] generateKey() {
		KeyGenerator generator;
		SecretKey secKey =null;
		try {
			generator = KeyGenerator.getInstance("AES");
		
		generator.init(256); // The AES key size in number of bits
		secKey = generator.generateKey();
		return secKey.getEncoded();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	static public byte[] encrypt(SecretKey key, byte[] data) 
	{	
		try {
		Cipher aesCipher = Cipher.getInstance("AES");
			aesCipher.init(Cipher.ENCRYPT_MODE, key);
		
		byte[] byteCipherText = aesCipher.doFinal(data);
		return byteCipherText;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	static public byte[] decrypt(SecretKey key, byte[] data) {
		try {
		Cipher aesCipher = Cipher.getInstance("AES");
		aesCipher.init(Cipher.DECRYPT_MODE, key);
		byte[] bytePlainText = aesCipher.doFinal(data);
		return bytePlainText;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	static public SecretKey fromByte(byte[] key) {
		//Convert bytes to AES SecertKey
		return new SecretKeySpec(key , 0, key .length, "AES");
	}
	public static void test() {
		SecretKey a=fromByte(generateKey());
		SecretKey b=fromByte(generateKey());
		assert(a!=b);
		System.out.print("\n clef a :"+new String(a.getEncoded()));
		System.out.print("\n clef b :"+new String(b.getEncoded()));
		
		byte[] crypted=AES.encrypt(a,"hola mi amor <3 ".getBytes());
		System.out.println("\n Texte decrypte :"+new String(AES.decrypt(a,crypted)));
		System.out.println("\n Texte crypte :" +new String(crypted));
		
		crypted=AES.encrypt(b,"hola mi amor <3 ".getBytes());
		System.out.println("\n Texte decrypte :"+new String(AES.decrypt(b,crypted)));
		System.out.println("\n Texte crypte :" +new String(crypted));
		
		//will get en error
		/*crypted=AES.encrypt(a,"hola mi amor <3 ".getBytes());
		System.out.println("\n Texte decrypte :"+new String(AES.decrypt(b,crypted)));
		System.out.println("\n Texte crypte :" +new String(crypted));*/
	}

}
