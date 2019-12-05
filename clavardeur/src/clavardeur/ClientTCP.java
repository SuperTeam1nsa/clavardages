package clavardeur;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.Date;
import java.util.HashMap;
public class ClientTCP {
	HashMap <Personne, Byte[]> bufferEnvoi;
	
    public void sendMessage (String data, Personne dest, Personne emmet) throws IOException{
        //Initier la connexion
        Socket s = new Socket ("127.0.0.1",1025); //127.0.0.1 == localhost
        //Set up OUTput streams
        OutputStream os = s.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);

        //Envoyer les datas       
        Date date = new Date(); //exo3
        /////////PrintWriter out = new PrintWriter(os,true);
        Message message = new Message(data, emmet, dest, date);
        //System.out.println("-> envoi : "+data+emmet.getPseudo()+" -> "+dest.getPseudo());
        byte[] byteMessage = Message.serialize(message);
        int len = byteMessage.length;
        dos.writeInt(len);
        if (len > 0) {
            dos.write(byteMessage, 0, len);
        }
        /////////out.write(Message.serialize(message));
        /////////out.flush();
        //Clore la connexion
        s.close();	    
	}
}