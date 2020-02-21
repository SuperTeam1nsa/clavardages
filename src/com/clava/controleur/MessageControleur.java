package com.clava.controleur;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.swing.DefaultListModel;

import java.util.AbstractMap.SimpleEntry;

import com.clava.model.bd.BD;
import com.clava.model.crypt.AES;
import com.clava.model.crypt.RSA;
import com.clava.model.reseau.Reseau;
import com.clava.serializable.Group;
import com.clava.serializable.Interlocuteurs;
import com.clava.serializable.Message;

public class MessageControleur implements PropertyChangeListener{
	private ControleurApplication app;
	private DefaultListModel<Interlocuteurs> model;
	private ArrayList<Integer> localConnexion=new ArrayList<>();
	private boolean initialized=false;
	private BD maBD;
	
	public MessageControleur(ControleurApplication a, DefaultListModel<Interlocuteurs> model) {
		app=a;
		this.model=model;
		this.maBD=BD.getBD();
	}
	/**
	 * PropertyChange reçoit les messages de Reseau [Design Pattern Observers]
	 * et traite selon le type de message 
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		//on ne repond pas tant que l'on n'est pas initialis� (avec un pseudo)
		//System.out.print(" \n type d'évenements: : "+evt.getPropertyName());
		if(evt.getPropertyName().equals("serveur") && evt.getNewValue() instanceof Message) {
			Message message = (Message) evt.getNewValue();
			this.messageServeur(message);
		} else {
		if (evt.getNewValue() instanceof Message) {
	        Message message = (Message) evt.getNewValue();
	        //pas de réponse à notre propre broadcast ^^  
	        //pas d'affichage des messages qu'on envoie dans un groupe où on est présent (aussi envoyé à soi #même id everywhere))
	        //possibilité de se parler à soi même
        	/*if((message.getDestinataire() == null && message.getEmetteur().getId()!=user.getId()) 
        			|| !message.getEmetteur().getInterlocuteurs().contains(user)
        			|| (message.getDestinataire() != null 
        			&& (message.getDestinataire().getId()== user.getId() 
        			&& message.getEmetteur().getId()==user.getId())))*/
	        if(app.getPersonne()!=null && message.getEmetteur().getId()!=app.getPersonne().getId()){
	        	this.localMessage(message);
	        }
        	    }
	        }
		}

private void localMessage(Message message) {

    System.out.print("\n Reception de :"+message.getType().toString()+" de la part de "+message.getEmetteur().getPseudo()+
		   "("+message.getEmetteur().getAddressAndPorts().toString()+")"+"\n" );
if(message.getType()==Message.Type.DEFAULT) {
	if(initialized) {
		if(message.getDestinataire().getInterlocuteurs().size()>1)
			app.getVuePrincipale().update(message.getDestinataire(),message,false); 
    	else
    		app.getVuePrincipale().update(message.getEmetteur(),message,false);
        maBD.addData(message); //SAVE BD LE MESSAGE RECU
        // maBD.printMessage();
	}
}
else if(message.getType()==Message.Type.FILE) {
	try {
		String basePath=app.getDownloadPath().getCanonicalPath()+"/";
		File newFile=new File(basePath+message.getNameFile());
		int index=0;
		if(newFile.exists())
		    while ((newFile = new File(basePath+"("+index+")"+message.getNameFile())).exists()) {
			    index++;
			}
		Path p=newFile.toPath();
		Files.write(p, message.getData());
    	message.setNameFile(newFile.getAbsolutePath());
    	System.out.print(message.getNameFile());
	} catch (IOException e) {
		e.printStackTrace();
	}
	//si groupe, graphiquement l'emetteur apparait comme étant le groupe
	if(message.getDestinataire().getInterlocuteurs().size()>1)
		app.getVuePrincipale().update(message.getDestinataire(),message,false); 
	else
		app.getVuePrincipale().update(message.getEmetteur(),message,false);
    maBD.addFile(message); //SAVE BD LE MESSAGE RECU
}
else if(message.getType()==Message.Type.SWITCH) {
	// long id=maBD.getIdPersonne(message.getEmetteur().getPseudo());
	/* maBD.delIdPseudoLink(message.getEmetteur().getPseudo());*/
		maBD.setIdPseudoLink(message.getEmetteur().getPseudo(),message.getEmetteur().getId());
	for(Object ob: model.toArray()) {
		Interlocuteurs p =(Interlocuteurs)ob;
		if(p.getId()==message.getEmetteur().getId()) {
			try {
				p.setPseudo(message.getEmetteur().getPseudo());
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			   break;
		}
	} 
    if(initialized)
    	app.getVuePrincipale().updateList();
}
else if(message.getType()==Message.Type.DECONNECTION) {
	AES.removeKey(message.getEmetteur().getId());
	// int index = model.indexOf(message.getEmetteur()); // not working
	//fix via equals redefinition => refactoring possible ! 
	int index=localConnexion.indexOf(message.getEmetteur().getId());
	if(index!= -1)
	localConnexion.remove(index);
	for(Object ob: model.toArray()) {
		Interlocuteurs p =(Interlocuteurs)ob;
			if(p.getId()==message.getEmetteur().getId()) {
			try {
				p.setConnected(false);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			   break;
		}
	}
	  if(initialized) {
		  app.getVuePrincipale().updateList();
	  }
}
else if(message.getType()==Message.Type.ALIVE || message.getType()==Message.Type.CONNECTION) {
	//ONLY PERSON SEND IT (le groupe n'a pas de vie propre, quand 
	//tous ses membres sont connectés il devient connecté (absence de broadcast donc )) 
	localConnexion.add(message.getEmetteur().getId());
	boolean found=false;
	for(Object ob: model.toArray()) {
		Interlocuteurs p =(Interlocuteurs)ob;
	    if(p.getId()==message.getEmetteur().getId()) {
       		found=true;
       		try {
				p.setConnected(true);
	        	p.setPseudo(message.getEmetteur().getPseudo());
				p.setAddressAndPorts(new SimpleEntry<>(message.getEmetteur().getAddressAndPorts().get(0)));
				} catch (NoSuchMethodException e) {
					System.out.print(" Erreur ! Un groupe s'est connecté ^^");
					e.printStackTrace();
				}
        			break;
		}
	}
    if(!found) {
    	model.add(0, message.getEmetteur());
    }
    Reseau.getReseau().cryptProtocole(message.getEmetteur().getId(),app.getPersonne(),message.getEmetteur());
    
    maBD.setIdPseudoLink(message.getEmetteur().getPseudo(), message.getEmetteur().getId());
    if(initialized)
    	app.getVuePrincipale().updateList();
}else if(message.getType()==Message.Type.KEY ) {
	int otherId=message.getEmetteur().getId();
	AES.storeKey(otherId,AES.fromByte(RSA.decrypt(app.getPersonne().getId(),message.getData())));
	//Reseau.getReseau().addKey(message.getEmetteur().getId(),app.getPersonne().getId(),message.getData());
}
else if(message.getType()==Message.Type.GROUPCREATION ) { 	        	   
	   if(!model.contains(message.getDestinataire())) {
	   //recréation du groupe en local #avec les pointeurs sur lespersonnes que l'on a crées en local #
	   //#auto connexion/deconnexion, pseudo switch
	   ArrayList<Interlocuteurs> array=new ArrayList<>();
	   for(Interlocuteurs i:message.getDestinataire().getInterlocuteurs()) {
		    int index =model.indexOf(i);
		    if(index != -1)
		    array.add(model.get(index));
		    else {
		    System.out.print("\n Warning !, un de vos amis a créé un groupe avec une personne que vous ne connaissez pas, "
		   		+ "ceci peut être dû à un délai réseau, nous ajoutons cette personne" );
		    try {
				i.setConnected(false);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		    model.add(0,i);
		    array.add(i);
		    }
	    }
	    Group g=new Group(array);
	    //g.addInterlocuteur(message.getEmetteur());
	    // g.removeInterlocuteur(user);
	    maBD.addGroup(g.getId(),g.getInterlocuteurs());
	    model.add(1, g);
	    if(initialized)
	    	app.getVuePrincipale().updateList();
	    }
    }
    else if(message.getType()==Message.Type.WHOISALIVE ) { 
	    if(initialized)
	    app.sendActiveUserPseudo(message.getEmetteur());
    }
    else if(message.getType()==Message.Type.ASKPSEUDO) {
	    synchronized (app.getMutexPseudoWaiting()) {
	    if(app.getPseudoWaiting().equals(message.getEmetteur().getPseudo()))
	    Reseau.getReseau().sendUDP(Message.Factory.usernameAlreaydTaken(app.getPersonne(), message.getEmetteur()));
	    }
    }
    else if(message.getType()==Message.Type.REPLYPSEUDO)
	    app.setAnswerPseudo(false);
    else if(message.getType()==Message.Type.REVERSALCONNECTION)
    	System.out.print(" \n Reversal connection established ! Well done ! ");
    else
	    System.out.print("WARNING unknow message type : " + message.getType().toString());
	}

	private void messageServeur(Message message) {
		
		//si une personne nous envoie les infos localement pas besoin d'écouter à retard les mêmes infos venant du serveur
		//if(!localConnexion.contains(message.getEmetteur().getId())) {
		/*System.out.print(" \n Serveur send us :" +message.getType()+" avec ");
		for(Interlocuteurs i:message.getEmetteur().getInterlocuteurs()) {
			System.out.print( "\n pseudo :"+i.getPseudo()+"  "+i.getAddressAndPorts());
		}*/
		if(message.getType()==Message.Type.OKSERVEUR) {
			
		}
		else if(message.getType()==Message.Type.REPLYPSEUDO)
        	   app.setAnswerPseudo(false);
		else if(message.getType()==Message.Type.ALIVE){
		
			for(Object ob: model.toArray()) {
        		   Interlocuteurs p =(Interlocuteurs)ob;
        		   boolean found=false;
        		   for(Interlocuteurs i:message.getEmetteur().getInterlocuteurs()) {
		        		if(p.getId()==i.getId() && !localConnexion.contains(p.getId())) {
		        			try {
		        				//le serveur contient tjrs le pseudo le + à jour
		        				if(!p.getPseudo().equals(i.getPseudo())) {
				        			System.out.print(" \n [serveur] MAJ du pseudo de : "+p.getPseudo());
									p.setPseudo(i.getPseudo());
		        				}//si app crashe, on ignore l'info du serveur disant qu'on était déjà connecté (notre connexion va régulariser la situation)
		        				if(!p.getConnected() && i.getId()!=app.getPersonne().getId()) {
	        					    System.out.print(" \n [serveur] Connexion de : "+p.getPseudo()+" à "+i.getAddressAndPorts().get(0));
	        					    p.setConnected(true);
	        					    /// ok if NAT well config #upnp or manually 
	        					    p.setAddressAndPorts(i.getAddressAndPorts().get(0));
	        					    //envoie de notre clef AES calculée
	        					    Reseau.getReseau().cryptProtocole(p.getId(),app.getPersonne(),p);
	        					    ///nat reversal
	        					    Reseau.getReseau().sendTCP(Message.Factory.reversalConnexionConfig(this.app.getPersonne(),p));
		        				}
								found=true;
							} catch (NoSuchMethodException e) {
								e.printStackTrace();
							}
		        		}
        		    }	
        		    //si la personne n'est pas présente dans la liste retournée par le serveur et n'est pas un groupe
        		    //(absent du serveur), c'est qu'elle s'est déconnectée
        		   
        		    if(!found && p.getInterlocuteurs().size()<2 && p.getConnected() && !localConnexion.contains(p.getId()))
						try {
							System.out.print(" \n Deconnexion de: "+p.getPseudo());
							AES.removeKey(p.getId());
							p.setConnected(false);
						} catch (NoSuchMethodException e) {
							e.printStackTrace();
						}
			}
			//si la liste du serveur contient un nouveau venu on l'ajoute
			for(Interlocuteurs i:message.getEmetteur().getInterlocuteurs()) {
        		boolean found=false;
				for(Object ob: model.toArray()) {
	        		Interlocuteurs p =(Interlocuteurs)ob;
	        		if(i.getId()==p.getId() )
	        			found=true;
				}
				//on ne se connecte pas soi même #régularisation 
				if(!found && i.getId()!= app.getPersonne().getId() && !localConnexion.contains(i.getId())) {
					System.out.print(" \n Connexion de: "+i.getPseudo());
					model.add(0, i);
					maBD.setIdPseudoLink(i.getPseudo(),i.getId());
				}
			}
		} else
			System.out.print(" Warning unknow message type !");
		
	}
	public void setInitialized(boolean b) {
		initialized=b;
	}
	
	}
