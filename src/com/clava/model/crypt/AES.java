package com.clava.model.crypt;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
/**
 * Rqs: it will be way more secure to use 
 * @author RF
 *
 */
public class AES {
	static 	HashMap<Integer, SecretKey> hkey=new HashMap<>();//clef d'envoi
	static HashMap<Integer, SecretKey> hkeyr=new HashMap<>();//clef de reception
	
	static public byte[] generateKey(int id) {
		KeyGenerator generator;
		SecretKey secKey =null;
		try {
			generator = KeyGenerator.getInstance("AES");
		
		generator.init(256); // The AES key size in number of bits
		secKey = generator.generateKey();
		hkey.put(id,secKey);
		
		return secKey.getEncoded();
		
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	static public byte[] generateIV() {
		// Generating IV.
        byte[] IV = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(IV);
        return IV;
	}
	static public void storeKey(int id,SecretKey key) {
		hkeyr.put(id,key);
	}
	static public byte[] encrypt(int id, byte[] data, byte[] IV) 
	{	
		try {
			//CBC more secure than ECB
		Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		
        //Create IvParameterSpec
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        
        //Initialize Cipher for ENCRYPT_MODE
        aesCipher.init(Cipher.ENCRYPT_MODE, hkey.get(id), ivSpec);
		
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
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	static public byte[] decrypt(int id, byte[] data,byte[] IV) {
		try {
			System.out.print("\n key used reception:"+new String(hkeyr.get(id).getEncoded())+" id recu: "+id+"\n");
			//Get Cipher Instance
	        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	        
	        //Create SecretKeySpec
	        //SecretKeySpec keySpec = new SecretKeySpec(hkeyr.get(id).getEncoded(), "AES");
	        
	        //Create IvParameterSpec
	        IvParameterSpec ivSpec = new IvParameterSpec(IV);
	        
	        //Initialize Cipher for DECRYPT_MODE
	        aesCipher.init(Cipher.DECRYPT_MODE, hkeyr.get(id), ivSpec);
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
		int id1=42;
		int id2=0;
		generateKey(id1);
		generateKey(id2);
		assert(hkey.get(id1)!=hkey.get(id2));
		byte[] iva=AES.generateIV();
		byte[] ivb=AES.generateIV();
		System.out.print("\n clef a :"+new String(hkey.get(id1).getEncoded()));
		System.out.print("\n clef b :"+new String(hkey.get(id2).getEncoded()));
		
		byte[] crypted=AES.encrypt(id1,"hola mi amor <3 ".getBytes(),iva);
		System.out.println("\n Texte decrypte :"+new String(AES.decrypt(id1,crypted,iva)));
		System.out.println("\n Texte crypte :" +new String(crypted));
		
		byte[] crypted2=AES.encrypt(id2,"hola mi amor <3 ".getBytes(),ivb);
		System.out.println("\n Texte decrypte :"+new String(AES.decrypt(id2,crypted2,ivb)));
		System.out.println("\n Texte crypte :" +new String(crypted2));
		
		//semantic secure #thx to CBC and not ECB
		assert(crypted!=crypted2);
		//will get en error
		/*crypted=AES.encrypt(a,"hola mi amor <3 ".getBytes());
		System.out.println("\n Texte decrypte :"+new String(AES.decrypt(b,crypted)));
		System.out.println("\n Texte crypte :" +new String(crypted));*/
	}
	public static boolean canDecrypt(int idkey) {
		return (hkeyr.get(idkey)!=null);
	}
	public static boolean canCrypt(int id) {
		return (hkey.get(id)!=null);
	}
	public static void removeKey(int id) {
		System.out.print("\n \n\nsuppression des clefs de session pour l'id: "+id);
		hkeyr.remove(id);
		hkey.remove(id);
	}

}
