package com.clava.model.rsa;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class RSA {
	
	static public void generateKey(int id) {
		// Création d'un générateur RSA
        KeyPairGenerator generateurCles = null;
        try {
            generateurCles = KeyPairGenerator.getInstance("RSA");
            generateurCles.initialize(4096);
        } catch(NoSuchAlgorithmException e) {
            System.err.println("Erreur lors de l'initialisation du générateur de clés : " + e);
            e.printStackTrace();
        }
 
        // Génération de la paire de clés
        KeyPair pkey = generateurCles.generateKeyPair();
        
        Base64.Encoder encoder = Base64.getEncoder();
		try {
			 new File("keys/"+id).mkdirs();
			 File f = new File("keys/"+id+"/private" + ".key");
	         FileOutputStream fis = new FileOutputStream(f);
	         DataOutputStream dis = new DataOutputStream(fis);
	         dis.write(encoder.encode(pkey.getPrivate().getEncoded()));
	         dis.close();
	         File f2 = new File("keys/"+id+"/public" + ".key");
	         FileOutputStream fis2 = new FileOutputStream(f2);
	         DataOutputStream dis2 = new DataOutputStream(fis2);
	         dis2.write(encoder.encode(pkey.getPublic().getEncoded()));
	         dis2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	static public byte[] crypt(int id, byte[] data) {
        byte[] bytes = null;
		 // Recuperation de la cle publique
	    try{
	    	 File f = new File("keys/"+id+"/public" + ".key");
	         FileInputStream fis = new FileInputStream(f);
	         DataInputStream dis = new DataInputStream(fis);
	         byte[] buff = new byte[(int) f.length()];
	         dis.readFully(buff);
	         dis.close();
	         
	        Base64.Decoder decoder = Base64.getDecoder();
	        byte[] byteKey= decoder.decode(buff);
	        
	        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
	        KeyFactory kf = KeyFactory.getInstance("RSA");
	       PublicKey clePublique= kf.generatePublic(X509publicKey);
        

        // Chiffrement du message
            Cipher chiffreur = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            chiffreur.init(Cipher.ENCRYPT_MODE, clePublique);
            bytes = chiffreur.doFinal(data);
        } catch(Exception e) {
            e.printStackTrace();
        } 
	    if(bytes==null) {
	    	System.out.print("(Unencrypted message)");
	    	return data;
	    }else
	    	return bytes;
	}
	static public byte[] decrypt(int id, byte[] data) {
		 byte[] bytes = null;
        try {
		//recuperation de la clef privee
        	 File f = new File("keys/"+id+"/private" + ".key");
	         FileInputStream fis = new FileInputStream(f);
	         DataInputStream dis = new DataInputStream(fis);
	         byte[] buff = new byte[(int) f.length()];
	         dis.readFully(buff);
	         dis.close();
	         
	        Base64.Decoder decoder = Base64.getDecoder();
	        byte[] byteKey= decoder.decode(buff);
	        
	        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(byteKey);
	        KeyFactory kf = KeyFactory.getInstance("RSA");
	        PrivateKey clePrivee= kf.generatePrivate(ks);
        
		 // Déchiffrement du message
            Cipher dechiffreur = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            dechiffreur.init(Cipher.DECRYPT_MODE, clePrivee);
            bytes = dechiffreur.doFinal(data);
        }catch(Exception e) {
        	e.printStackTrace();
        }
        if(bytes==null) {
	    	System.out.print("(Unencrypted message)");
	    	return data;
	    }else
	    	return bytes;
	}
	static public void init(int id) {
		if (!(new File("keys/"+id).exists())) {
			System.out.print("\n Génération des clefs de cryptage..");
			RSA.generateKey(id);
			System.out.print("done \n");
		}
	}
	static public void RSAtest() {
		int id=42; //42 obviously an hasard
		RSA.generateKey(id);
		byte[] crypted=RSA.crypt(id,"hola mi amor <3 ".getBytes());
		System.out.println("\n Texte decrypte :"+new String(RSA.decrypt(id,crypted)));
		System.out.println("\n Texte crypte :" +new String(crypted));
	}
}
