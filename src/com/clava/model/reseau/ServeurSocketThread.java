package com.clava.model.reseau;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;

import com.clava.model.crypt.AES;
import com.clava.serializable.Message;

//public class ServeurSocketThread extends Observable implements Runnable {
/**
 * ServeurSocketThread permet la reception observable de messages d'une connexion TCP avec une certaine Personne
 */
public class ServeurSocketThread implements Runnable {
    Socket s;
    private PropertyChangeSupport support;
	private boolean on=true;
	private boolean first=true;
	private int id=0;
	HashMap<Integer, Socket> hsock;
    /**
     * Constructeur ServeurSocketThread
     * <p>[Design Pattern Observers]</p>
     * @param soc
     * @param hkey2 
     * @param hsock 
     */
    public ServeurSocketThread(Socket soc, HashMap<Integer, Socket> hsock) {
        this.s = soc;
        support = new PropertyChangeSupport(this);
        this.hsock=hsock;
    }
    /**
     * Ajoute un Listener à notifier (ServeurTCP)
     * @param pcl
     * @see ServeurTCP
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }
    public void closeServeur() { 
        try {
        	on=false;
        	if(s != null) {
				s.close();
				s=null;
				System.out.print("Collected a conversation socket ! (closed)");
        	}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    /**
     * Remonte le message reçu et déserializé au ServeurTCP du reseau
     */
	@Override
    public void run() {
		Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
	         closeServeur();
			    }});
        try{
           while(on) {
            InputStream is = s.getInputStream();
            DataInputStream dis = new DataInputStream(is);
            int len = dis.readInt();
            int idkey = dis.readInt();
            byte[] IV=new byte[16];
            dis.read(IV, 0, 16);
            byte[] data = new byte[len];
            if (len > 0) {
                dis.readFully(data);
            }
            
            try {
            	//byte[] mess=RSA.decrypt(idkey, data);
            	Message m=null;
	            	try {
	            		m=Message.deserialize(data);
	            		if(m.getType()==Message.Type.DEFAULT || m.getType()==Message.Type.FILE)
	            			System.out.print("\n warning reception of unencrypted message");
	            		else
	            			System.out.print("\n reception of safe unencrypted data");
	            	}catch(Exception e) {
	            		if(AES.canDecrypt(idkey)){	
	            			m=Message.deserialize(AES.decrypt(idkey, data,IV));
	            		}
	            		else
	            			System.out.print("\n warning reception of a crypted message without the key to decrypt");
	            	}
	            	
            	id=m.getEmetteur().getId();
            	if(hsock.get(id)==null && first)
            		hsock.put(m.getEmetteur().getId(), s);
            	else if(hsock.get(id)==null) //la personne s'est déconnectée
            	{
            		closeServeur();
            		first=false;
                	support.firePropertyChange("message","",m);
                	break;
            	}
            	first=false;
            	support.firePropertyChange("message","",m);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
            //Clore la connexion
            //s.close();
           }
        }
        catch (IOException e){
            hsock.remove(id);
            System.out.println("\n Fermeture distante du socket :)");
            //e.printStackTrace();
        }
    }
}