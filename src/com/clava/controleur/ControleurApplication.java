package com.clava.controleur;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.bitlet.weupnp.NatInit;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import com.clava.model.bd.BD;
import com.clava.model.crypt.AES;
import com.clava.model.crypt.RSA;
import com.clava.model.reseau.Reseau;
import com.clava.serializable.Group;
import com.clava.serializable.Interlocuteurs;
import com.clava.serializable.Message;
import com.clava.serializable.Personne;
import com.clava.vue.VueChoixPseudo;
import com.clava.vue.VuePrincipale;

//tips: ctrl +r =run (me) ctrl+F11 (standard)
//ctrl+maj+F11=code coverage (standard)
//ObjectAid UML (retro URL)
//public class ControleurApplication implements Observer {
/**
 * Main class of application 
 * Initialize all components and deal with crafting and calling Reseau to send adequate Message
 * The reception is performed by MessageControleur
 */
public class ControleurApplication {
	private Personne user;
	private VuePrincipale main;
	private MessageControleur mc;
	private BD maBD=BD.getBD();
	private DefaultListModel<Interlocuteurs> model = new DefaultListModel<Interlocuteurs>();
	private File pathDownload;
	private String pseudoWaiting="";
	private boolean answerPseudo;
	private Object mutex = new Object();
	Wini ini;
	private InetAddress localIp;
	/**
	 * main thread
	 * @param args
	 */
	public static void main(String[] args) {
			new ControleurApplication();

	}
	 /**
	 * Interroge le serveur toute les 5s pour mettre à jour les destinataires présents "ALIVE".
	 */
	void configServeur() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			  @Override
			  public void run() {
				  Reseau.getReseau().sendHttp(Message.Factory.whoIsAliveBroadcast(user));
			  }} , 10000, 10000);
		}
	 /**
	 * Fonction de découverte automatique de l'ip (IPV4) locale de l'utilisateur 
	 * @return ip locale
	 * NB : si l'application echoue à trouver l'ip locale on peut toujour la rentrer manuellement dans config.ini
	 * @see ControleurApplication#init() 
	 */
	InetAddress findIp() {
		InetAddress localIp;
		try {
			DatagramSocket so=new DatagramSocket();
			so.connect(InetAddress.getByAddress(new byte[]{1,1,1,1}), 0);
			localIp=so.getLocalAddress();
			so.close();
			//System.out.print(" adresse  ip : "+ localIp.toString());
	 	    if(localIp.isLoopbackAddress() || !(localIp instanceof Inet4Address)) {
			for(Enumeration<NetworkInterface> enm = NetworkInterface.getNetworkInterfaces(); enm.hasMoreElements();){
				NetworkInterface network = (NetworkInterface) enm.nextElement();
				//si getLoaclHost n'a pas marché correctement (on veut de l'IPV4) 
				for(Enumeration<InetAddress> s = network.getInetAddresses(); s.hasMoreElements();){
				 	InetAddress in = (InetAddress) s.nextElement();
				 	// System.out.print(" \nlocalIP s found : " +in.toString() + " ? "+ (!in.isLoopbackAddress() && in instanceof Inet4Address));
				 	// System.out.print(" \nloop: " +in.toString() + " ? "+ (in.isLoopbackAddress()));
				 	if(!in.isLoopbackAddress() && in instanceof Inet4Address)
				 		localIp=in;
				 	}
				}
				//find good local ip last chance
			    if(localIp.isLoopbackAddress() || !(localIp instanceof Inet4Address)) {
					localIp = InetAddress.getLocalHost(); //buggy one
				}
		 	}
			return localIp;
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
			JOptionPane.showMessageDialog(null, "Hum...Il semblerait que nous ne soyons pas capables d'obtenir votre adresse ip,"
					+"vous pouvez essayer de la fournir manuellement dans config.ini partie avancée :  \n" + 
					"doNotUseAutoIpAndUseThisOne", "ErrorBox " + "📛", JOptionPane.ERROR_MESSAGE);	
			System.exit(0);
		} catch (SocketException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Hum...Il semblerait que nous ne soyons pas capables d'obtenir votre adresse ip,"
					+"vous pouvez essayer de la fournir manuellement dans config.ini partie avancée :  \n" + 
					"doNotUseAutoIpAndUseThisOne", "ErrorBox " + "📛", JOptionPane.ERROR_MESSAGE);	
			System.exit(0);
		}
		//on n'y arrive jamais
		return (InetAddress) new Object();
				
	}
	/**
	 * Fonction de découverte automatique de l'@mac locale de l'utilisateur 
	 * @return mac locale
	 * NB : si l'application echoue à trouver l'@mac locale on peut toujour la rentrer manuellement dans config.ini
	 * @see ControleurApplication#init() 
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	String findMac() throws SocketException, UnknownHostException {
		String mac="";
			for(Enumeration<NetworkInterface> enm = NetworkInterface.getNetworkInterfaces(); enm.hasMoreElements();){
			  NetworkInterface network = (NetworkInterface) enm.nextElement();
			  byte[] m=network.getHardwareAddress();
			    if((null != m) && (m.length>0)){
			    	 StringBuilder sb = new StringBuilder();
			 		for (int i = 0; i < m.length; i++) {
			 			sb.append(String.format("%02X%s", m[i], (i < m.length - 1) ? "-" : ""));
			 		}
			 	    mac=sb.toString();
			 	   break;
			    }
		}
		if(mac.equals("")) {
			JOptionPane.showMessageDialog(null, "Hum...Il semblerait que nous ne soyons pas capables d'obtenir votre adresse mac,"
					+"vous pouvez essayer de la fournir manuellement dans config.ini partie avancée :  \n" + 
					"doNotUseAutoMacAndUseThisOne", "ErrorBox " + "📛", JOptionPane.ERROR_MESSAGE);	
			System.exit(0);
		}
		return mac;
	}
	/**
	 * initialisation du reseau (ip, mac et port locaux) + [Design Pattern Observers]
	 */
	void init() {
		String ipServer=null;
		InetAddress ipForceLocal=null;
		String mac =null;
		boolean forceUseIp=false;
		boolean forceUseMac=false;
		try {
			ini = new Wini(new File("config.ini"));
		} catch (InvalidFileFormatException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		int portTcp =ini.get("IP", "TCPport", int.class);
		int portUDP =ini.get("IP", "UDPport", int.class);
		int portServer =ini.get("IP", "publicServerPort",int.class);
		pathDownload=new File(ini.get("DOWNLOAD", "path",String.class));
		ipServer =ini.get("IP", "publicServerIp", String.class);//InetAddress.getByName(
				try {
					String s=ini.get("ADVANCED", "doNotUseAutoIpAndUseThisOne", String.class);
					if(!s.equals("")) {
						forceUseIp=true;
						ipForceLocal =InetAddress.getByName(s);
					}
				} catch (UnknownHostException e1) {
					JOptionPane.showMessageDialog(null, " L'adresse IP fournie avec le flag doNotUseAutoIpAndUseThisOne dans config.ini n'est pas au format correct,"+
				" vérifiez votre saisie ou supprimez ce champs", "Web Server", JOptionPane.ERROR_MESSAGE);	
					System.exit(0);
				}
				String mac_manuel=ini.get("ADVANCED", "doNotUseAutoMacAndUseThisOne", String.class);
				if(!mac_manuel.equals("")) {
					forceUseMac=true;
					mac=mac_manuel;
				}
		//System.out.print("data :" +portTcp+" "+portUDP+" "+portServer+" "+ini.get("IP", "publicServerIp", String.class)+" "+ini.get("IP", "doNotUseAutoIpAndUseThisOne", String.class));
		new NatInit(portTcp);
		Reseau.getReseau().init(portTcp,portUDP,ipServer,portServer);
		mc=new MessageControleur(this,model);
		Reseau.getReseau().addPropertyChangeListener(mc);//.addObserver(this);
		try {
		if(forceUseIp)
			localIp=ipForceLocal;
		else
			localIp=findIp();
		if(!forceUseMac)
			mac=findMac();
		
		System.out.print("ip: "+localIp.toString()+" id: "+mac.hashCode());
		user= new Personne(new SimpleEntry<InetAddress, Integer>(localIp, portTcp),"moi",true,
				mac.hashCode()); //fixe par poste (adresse mac by eg) 
		//note: not cryptographic sure id, the cryptographic strenght relies on keys (as the pseudo it's easy to spoof an id)
		RSA.init(user.getId());



		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	void test() {
		/*
	    Reseau.getReseau().cryptProtocole(user.getId(),user,user);
	    Reseau.getReseau().addKey(user.getId(),user.getId(),
	    		Message.Factory.keyExchange(RSA.crypt(user.getId(),AES.generateKey()),user,user).getData());
	    */
		AES.test();
	}
	/**
	 * Constructeur ControleurApplication 
	 * <p>initialise le réseau, la Vue Pseudo (bloquante) et affiche la Vue Principale en consultant la BD + [Design Pattern Observers]</p>
	 */
	ControleurApplication(){
		init();		
		test();
	    //on récupère les gens avec qui on a déjà parlé #offline reading
	    for(Interlocuteurs p: maBD.getInterlocuteursTalked(user)) {
			if(p.getId()!=user.getId())
			model.addElement(p);
	    }
	    Reseau.getReseau().sendLocalOnly(Message.Factory.whoIsAliveBroadcast(user));
	    new VueChoixPseudo(this,false);
	    //after pseudo, to let the time for local message to be sent.
	    Reseau.getReseau().sendServeurOnly(Message.Factory.whoIsAliveBroadcast(user));
	    main=new VuePrincipale(this,model);
	    mc.setInitialized(true);
	    Reseau.getReseau().sendDataBroadcast(Message.Factory.userConnectedBroadcast(user));
	    //on s'ajoute après que le serveur sait que l'on est connecte
	    //rq: on s'ajoute à la main car on bloque les broadcast venant de nous même en réception (pollution)
	    model.addElement(user);
	    main.firstSelection();
	    main.updateList();
		configServeur();
	}
	/**
	 * Retourne le pseudo utilisateur
	 * @return pseudo utilisateur
	 */
	public String getPseudo() {
		return user.getPseudo();
	}
	/**
	 * Retourne la personne utilisateur
	 * @return personne utilisateur
	 */
	public Interlocuteurs getPersonne() {
		return user;
	}
	/**
	 * Envoie un message ALIVE (emetteur = utilisateur) en broadcast UDP sur le réseau local
	 * @param to Interlocuteurs destinataire
	 */
	public void sendActiveUserPseudo(Interlocuteurs to) {
		Reseau.getReseau().sendUDP(Message.Factory.userIsAlive(user, to));
	}
	/**
	 * checkUnicity envoie un message (emetteur = user, user.pseudo = newpseudo) ASKPSEUDO à tout le monde
	 * s'il ne reçoit pas de réponse (message REPLYPSEUDO) dans les 3 secondes alors le pseudo n'a pas déjà ete pris
	 * De plus, à partir de l'appel de cette fonction, le pseudo demandé est réservé, et toute demande ASKPSEUDO reçue avec le même
	 * pseudo sera refusée (réponse REPLYPSEUDO), afin de garantir l'unicité du pseudo.
	 * @param pseudo a tester
	 * @return false si deja pris true sinon
	 */
	public boolean checkUnicity(String pseudo) {
		synchronized (mutex) {
		answerPseudo=true;
		pseudoWaiting=pseudo;
		user.setPseudo(pseudo);
		}
		Reseau.getReseau().sendDataBroadcast(Message.Factory.askPseudoOkBroadcast(user));
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(answerPseudo) {
			for(Object i : model.toArray()) {
				Interlocuteurs v=(Interlocuteurs) i;
				if((v.getId()!=user.getId() && v.getConnected() && v.getPseudo().equals(pseudo)))
					return false;
			}
			return true;
		} else {
			synchronized (mutex) {
			pseudoWaiting="";//on arrête de l'attendre (déjà attribué ou va l'être)
			}
			return false;
		}
	}

	/**
	 * Envoie message de déconnexion à tout le monde
	 */
	public void sendDisconnected() {
		Reseau.getReseau().sendDataBroadcast(Message.Factory.userDisconnectedBroadcast(user));			
	}
	/**
	 * Getter du chemin de téléchargement de fichier (dossier d'enregistrement local)
	 * @return File path
	 */
	public File getDownloadPath() {
		return pathDownload;
	}
	/**
	 * Setter du chemin de téléchargement de fichier (dossier d'enregistrement local) et l'enregistre dans le fichier config.ini
	 * @param file
	 */
	public void setDownloadPath(File file) {
	pathDownload=file;
	//maBD.setDownloadPath(file);
	ini.put("DOWNLOAD", "path", file.getAbsolutePath());
		try {
			ini.store();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Mets à jour la BD, la Vue Principale et la Personne utilisateur du changement de pseudo
	 * Et notifie les autres utilisateurs du changement de pseudo
	 * @param uname nouveau pseudo
	 */
	public void setPseudoUserSwitch(String uname) {
		/*maBD.delIdPseudoLink(user.getPseudo());*/
		maBD.setIdPseudoLink(uname,user.getId());
		main.changePseudo(uname);
		user.setPseudo(uname);
		Reseau.getReseau().sendDataBroadcast(Message.Factory.switchPseudoBroadcast(user));	
	}
	/**
	 * Setter de pseudo
	 * @param uname
	 */
	public void setPseudoUserConnexion(String uname) {
		user.setPseudo(uname);
	}
	/** Envoie d'un message texte à un utilisateur
	* @param tosend texte à envoyer à activeUser
	 */
	public void sendMessage(String tosend, Interlocuteurs to) {
		Message m =Message.Factory.sendText(tosend.getBytes(), user, to);
		Reseau.getReseau().sendTCP(m);
		main.update(to,m,true);
		maBD.addData(m);
	}
	/**
	 * Envoie d'un fichier à un utilisateur
	 * @param file fichier à envoyer
	 * @param f nom du fichier
	 */
	public void sendFileMessage(byte[] file, File f, Interlocuteurs to) {
		
		Message m =Message.Factory.sendFile(file, user, to,f.getName());//new Message(file, user, to,f.getName());
		Reseau.getReseau().sendTCP(m);
		Message m2 =Message.Factory.sendFile(file, user, to,f.getAbsolutePath());
		main.update(to,m2,true);
		maBD.addFile(m2); 
	}
	/**
	 * Récupère historique des messages stockés dans la BD associé à un interlocuteur donné 
	 * @param activeUser interlocuteur 
	 * @return liste de message historique de la conversation
	 */
	public ArrayList<Message> getHistorique(Interlocuteurs activeUser) {
			return maBD.getHistorique(user,activeUser,(activeUser.getInterlocuteurs().size()>1));
	}
	/**
	 * Crée un groupe dans la BD
	 * @param array d'interlocuteurs, membres du groupe
	 * @return true si le groupe a été créé, false s'il ne peut pas l'être
	 */
	public boolean creationGroupe(ArrayList<Interlocuteurs> array) {
		array.add(user);
		Group g=new Group(array);
		if(!model.contains(g)) {
			model.add(1,g);
			maBD.addGroup(g.getId(),array);		
			Reseau.getReseau().sendTCP(Message.Factory.createGroupe(user, g));
		    // g.removeInterlocuteur(user);
			return true;
		}
		else {
			return false;	
		}		
	}
	public Object getMutexPseudoWaiting() {
		return mutex;
	}
	public void setAnswerPseudo(boolean b) {
		answerPseudo=b;
	}
	public VuePrincipale getVuePrincipale() {
		return main;
	}
	public String getPseudoWaiting() {
		return pseudoWaiting;
	}
}
