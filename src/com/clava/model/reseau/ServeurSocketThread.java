package com.clava.model.reseau;

import java.net.*;
import java.util.HashMap;

import com.clava.serializable.Message;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;

//public class ServeurSocketThread extends Observable implements Runnable {
/**
 * ServeurSocketThread permet la reception observable de messages d'une connexion TCP avec une certaine Personne
 */
public class ServeurSocketThread implements Runnable {
    Socket s;
    HashMap<Integer, Socket> hsock;
    HashMap<Integer, String> hkey;
    private PropertyChangeSupport support;
	private boolean on;
    /**
     * Constructeur ServeurSocketThread
     * <p>[Design Pattern Observers]</p>
     * @param soc
     * @param hkey 
     * @param hsock 
     */
    public ServeurSocketThread(Socket soc, HashMap<Integer, Socket> hsock, HashMap<Integer, String> hkey) {
        this.s = soc;
        this.hkey=hkey;
        this.hsock=hsock;
        support = new PropertyChangeSupport(this);
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
        	if(s != null) {
	        	on=false;
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
            byte[] data = new byte[len];
            if (len > 0) {
                dis.readFully(data);
            }
            
            try {
            	Message m=  Message.deserialize(data);
            	
            	if(hsock.get(m.getEmetteur().getId())==null)
            	hsock.put(m.getEmetteur().getId(), s);
            	
            	support.firePropertyChange("message","",m);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
            //Clore la connexion
            //s.close();
           }
        }
        catch (IOException e){
            System.out.println("I03xception :)");
        }
    }
}