package com.clava.model.reseau;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.crypto.SecretKey;


//public class ServeurTCP extends Observable implements Observer, Runnable {
/**
 * ServeurTCP permet la reception (observable) de messages avec le protocole TCP remontés par ServeurSocketThread
 */
public class ServeurTCP implements PropertyChangeListener, Runnable{
	ServerSocket ssoc = null;
	boolean on=true;
	private int port;
	private PropertyChangeSupport support;
	HashMap<Integer, Socket> hsock;
	HashMap<Integer, SecretKey> hkeyr;
	/**
	 * Constructeur ServeurTCP
	 * <p>[Design Pattern Observers]</p>
	 * @param port
	 * @param hkey2 
	 * @param hsock 
	 */	
	public ServeurTCP(int port, HashMap<Integer, Socket> hsock, HashMap<Integer, SecretKey> hkey2) {
		this.port=port;
		this.hsock=hsock;
		this.hkeyr=hkey2;
		support = new PropertyChangeSupport(this);
	}
    /**
     * Ajoute un Listener à notifier (Reseau)
     * @param pcl Objet qui implémente PropertyChangeListener (à notifier)
     * @see Reseau
     */	
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }
	/**
	 * Permet de fermer en bonne et due forme le ServerSocket TCP et de libérer ainsi le port d'écoute pour la prochaine fois
	 */
	public void closeServeur() { 
        try {
        	if(ssoc != null) {
	        	on=false;
				ssoc.close();
				System.out.print("Collected socket TCP ! (closed)");
        	}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Le serveurTCP se met en état accept et à chaque demande de connexion lance un nouveau thread ServeurSocketThrad dédié
	 */
	@Override
    public void run() {
		Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
         closeServeur();
		    }});
		
		try {
			ssoc = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}        
        while(on){//Waiting connections
            Socket soc = null;
			try {
				soc = ssoc.accept();
				ServeurSocketThread st = new ServeurSocketThread(soc,hsock,hkeyr);
	            st.addPropertyChangeListener(this); 
	            Thread th = new Thread(st);
	            th.start();
			} catch (IOException e) {
				if(on)
				e.printStackTrace();
			}
        }
	}
	/**
	 * Reçoit message [Design Pattern Observers] de ServeurSocketThread et le transmet directement par le même moyen a la classe Reseau
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		support.firePropertyChange("message", evt.getOldValue(), evt.getNewValue());
	}
}