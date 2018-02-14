import java.rmi.*;
import java.rmi.server.*;
import java.util.Vector;
import java.util.*;
import java.util.UUID;
import java.nio.*;
//import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.*;


import javax.ws.rs.core.MediaType;
import sun.net.www.protocol.http.HttpURLConnection;


/**
 * @author Robert Pedros i Roger Orellana
 *
 */

/*	EXEMPLE de HASHTABLE per buscar la contrasenya de un NICK ????
https://beginnersbook.com/2014/07/hashtable-in-java-with-example/
import java.util.Hashtable;
import java.util.Enumeration;

public class HashtableExample {

 public static void main(String[] args) {

   Enumeration names;
   String key;

   // Creating a Hashtable
   Hashtable<String, String> hashtable =
              new Hashtable<String, String>();

   // Adding Key and Value pairs to Hashtable
   hashtable.put("Key1","Chaitanya");
   hashtable.put("Key2","Ajeet");
   hashtable.put("Key3","Peter");
   hashtable.put("Key4","Ricky");
   hashtable.put("Key5","Mona");

   names = hashtable.keys();
   while(names.hasMoreElements()) {
      key = (String) names.nextElement();
      System.out.println("Key: " +key+ " & Value: " +
      hashtable.get(key));
   }
 }
}
*/

public class CallbackServerImpl extends UnicastRemoteObject implements CallbackServerInterface {

  private static final String URL_WS = "http://localhost:8080/treballWSWeb4/treball/";
  private String codeServer = "";
  private String nameServer = "";
  private String portServer = "";

  private Vector clientList;						// Llista per a registrar els diferents clients pels CallBack's.
  private List llistaNicks = new ArrayList();		// Llista per enmagatzemar tots els Nick's, per no repetir.
  private List llistaSecrets = new ArrayList();		// Llista per enmagatzemar totes les CONTRASENYES associades als Nick's.
  //private ArrayList [][] llistaNicksSecret = new ArrayList[5][5];	// Llista per enmagatzemar tots els Nick's i les CONTRASENYES.
  // Llista de estructures per enmagatzemar tota la informació de cada película.
  private List<Continguts> llistaDeContinguts = new ArrayList<Continguts>();

  // Crear una HashTable per a tenir els continguts de la BASE de DADES amb possible ordenació per Nick, per Titol ???
  // HashMap no és sincronitzable i per tant no és indicat per aplicacions amb multiprocés (molts clients al mateix temps)
  //private Enumeration names;
  //private String key;

  // Creating a Hashtable per a poder trobar els CONTINGUTS mes RAPIDAMENT
  private Hashtable<String, String> taulaIndexada = new Hashtable<String, String>();

  public CallbackServerImpl() throws RemoteException {
    super( );
    clientList = new Vector();
  }

  public void defineCode(String code,String hostName,String portNum) throws RemoteException {
    codeServer = code;
    nameServer = hostName;
    portServer = portNum;
    System.out.println("\t[CODE SERVER]: " + code);
  }

  public String returnServer() throws RemoteException {
    System.out.println("\t[ID SERVER]: " + nameServer + " : " + portServer + "  || " + codeServer );
    return nameServer + " : " + portServer + "  || " + codeServer;
  }

  public String sayHello(String name, String pass) throws java.rmi.RemoteException {
  	int posicioNick;
    // Puc utilitzar la primera comunicació per REGISTRAR el nom del usuari, NICK i que sigui ÚNIC. ???
    // Sinó el torna ha demanar fins que sigui diferent als ja registrats.
    //System.out.println("\n	Soc el SERVIDOR i dins de -sayHello- rebo del CLIENT el NICK ?? ... " + name);
    if (llistaNicks.contains(name)) {
    	posicioNick = llistaNicks.indexOf(name);		// Buscar la posició del nick a la llista
      	System.out.println("\n\tEl nick <" + name + "> ja està registrat.");
      	//System.out.println("\tEn la posició [" + posicioNick + "] de la llista.");
      	System.out.println("\tAmb contrasenya $$" + llistaSecrets.get(posicioNick) + "$$");
      	System.out.println("\tQue ha de ser igual a $$" + pass + "$$");
      	if (pass.equals(llistaSecrets.get(posicioNick))){
      		System.out.println("\tSon IGUALS.");
      		doCallbacks("nick", name, "\n\t\t[WELCOME] "+ name +" saluda!" );	// NOTIFICAR -- nick -- als altres usuaris
      		return name;
      	} else {
      		System.out.println("\tSon DIFERENTS.");
      		System.out.println("\tDemanar-ne un de diferent.");
      		return "\n\t[INFO]: El nick " + name + " ja està registrat";
      	}
    } else {
      	System.out.println("\n\t[INFO]: El nick " + name + " és NOU, ara el registro  ...");
      	System.out.println("\t[INFO]: La contrasenya " + pass + " ha estat enmagatzemada.");
      	llistaNicks.add(name);
      	llistaSecrets.add(pass);

        if(name.toLowerCase().contains("rmi://")){
          doCallbacks("nick", name, "\n\t\t[WELCOME EXTERN] "+ name +" saluda!" );
        }else{
          if(accions_WS_PUT_IN_SERVER(this.codeServer, "addUser", name, "{\"name\":\""+name+"\",\"files\":0}")){
          //if(registrarUserWS(name)){
            doCallbacks("nick", name, "\n\t\t[WELCOME] "+ name +" saluda!" );
          }else{
            doCallbacks("nick", name, "\n\t\t[Error] "+ name +" error!" );
          }
        }
      	//List llistaNicks = new ArrayList();		// Diferents tipus de enmagatzemar LLISTES de dades, per NO REPETIR !!!
     	//doCallbacks("nick", name, "\n\t\t[WELCOME] "+ name +" saluda!" );	// NOTIFICAR -- nick -- als altres usuaris
      	return name;
    }
  }

  public synchronized void registerForCallback(CallbackClientInterface callbackClientObject) throws java.rmi.RemoteException{
    // store the callback object into the vector
    if (!(clientList.contains(callbackClientObject))) {
      	clientList.addElement(callbackClientObject);
      	System.out.println("\n\t\t[INFO]: Registrar un nou client " + callbackClientObject.hashCode());
      	String codiHash = Integer.toString(callbackClientObject.hashCode());
       	doCallbacks("registre", codiHash , " del client rebut.");
      	} // end if
  }

  public synchronized void unregisterForCallback(CallbackClientInterface callbackClientObject, String nick) throws java.rmi.RemoteException{
    if (clientList.removeElement(callbackClientObject)) {

         System.out.println("\n\t\t[BYE]: El CLIENT "+nick+"ha estat tret del registre.");

         if(!nick.toLowerCase().contains("rmi://")){

           System.out.println("\n\t\t[Server]: Llista continguts: "+llistaDeContinguts.size());

           /* Eliminar continguts del client al WS*/
           for(int aux=0;aux<llistaDeContinguts.size();aux++){
             System.out.println("\n\t\t[Server FOR IN]: "+aux+"/"+llistaDeContinguts.size());
             Continguts auxC= llistaDeContinguts.get(aux);
             System.out.println("\t[COMPARE]: "+auxC.demanarNickUsuariPeli()+" with "+nick);

             if(auxC.demanarNickUsuariPeli().equals(nick)){


               // Modificar al webService!
               try {
                 URL url = new URL (URL_WS+"contents/delete/"+auxC.demanaridentificadorUNIC()+"/");
                 System.out.println("\t[RMI URL]: "+URL_WS+"contents/delete/"+auxC.demanaridentificadorUNIC()+"/");
                 HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                 conn.setRequestMethod("GET");
                 conn.setRequestProperty("Accept", MediaType.TEXT_PLAIN);

                 if(conn.getResponseCode() != 200) {
                   //throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
                   System.out.println("Failed: HTTP error code: " + conn.getResponseCode());
                 }

                  BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                  String output;
                  String OutputFinal = "";
                  while((output = br.readLine()) != null){
                    OutputFinal = OutputFinal+output;
                  }
                  conn.disconnect();

                  if(OutputFinal.equals("true")){
                    llistaDeContinguts.remove(aux);
                    aux--;
                    System.out.print("[REMOVED]");
                  }

                } catch (MalformedURLException e) {
                  //return "ERROR WS";

                } catch (IOException e) {
                  //return "ERROR WS";
                }


               // modificar fitxer dels demes?
               // Avisar als altres
             }
             System.out.println("\n\t\t[Server FOR OUT]: "+aux+"/"+llistaDeContinguts.size());
           }
         }else{

           for (int i = 0; i < llistaNicks.size(); i++){
            String clientAux = (String)llistaNicks.get(i);
            if(clientAux.equals(nick)){
              llistaNicks.remove(i);
              llistaSecrets.remove(i);
            }

           }
         }

         System.out.println("\n\t\t[Server]: Llista continguts: "+llistaDeContinguts.size());


    } else {
         System.out.println("\n\t\t[BYE]: El client no estava registrat en la LLISTA.");
    }

    String codiHash = Integer.toString(callbackClientObject.hashCode());
   	doCallbacks("desconectar", codiHash, "Bye-bye" );	// NOTIFICAR -- desconectar -- als altres usuaris
    mostrarLlistaRegistrats();
    //mostrarLlistaCallbaks();
  }

  private synchronized void buscarUsuariFitxer(Continguts fitxer) throws java.rmi.RemoteException{
    for (int i = 0; i < clientList.size(); i++){
      String clientAux = (String)llistaNicks.get(i);
      CallbackClientInterface nextClient = (CallbackClientInterface)clientList.elementAt(i);
      CallbackClientInterface oldClient = nextClient;

      try {
        if(clientAux.equals(fitxer.demanarNickUsuariPeli())){
          System.out.println("\n\t\t[SERVIDOR]: El usuari "+fitxer.demanarNickUsuariPeli()+" esta a punt d'enviar un fitxer...");
          nextClient.notifyMe("\n\t\t[UPLOAD]: Sol·licitut de DOWNLOAD");
        }
      } catch (RemoteException e) { // No valid registry at that port.
        System.out.println ("\n\t\t[SERVIDOR]: Algun client ha caigut sense desconectar-se correctament.");
        clientList.removeElement(oldClient);
        System.out.println ("\n\t\t[SERVIDOR]: El client caigut ha estat eliminat correctament.");
      }
    }
  }

  private synchronized void goFitxer(Continguts fitxer, String nick, CallbackServerInterface server) throws java.rmi.RemoteException{

    if(clientList.size()!=llistaNicks.size()){

        System.out.println("\n\t\t[SERVIDOR]: Inconsistencia llistes:  - clients "+clientList.size()+" \t - nicks: "+llistaNicks.size());

      mostrarLlistaRegistrats();
    }

    for (int i = 0; i < clientList.size(); i++){
      String clientAux = (String)llistaNicks.get(i);
      CallbackClientInterface nextClient = (CallbackClientInterface)clientList.elementAt(i);
      CallbackClientInterface oldClient = nextClient;

      try {
        if(clientAux.equals(fitxer.demanarNickUsuariPeli())){
           System.out.println("\n\t\t[SERVIDOR]: El usuari que demana el fitxer te el server obert:");
           System.out.println("\n\t\t[SERVIDOR]: Avisar a "+fitxer.demanarNickUsuariPeli()+" que envii el fitxer.");
           nextClient.iniciarUpload(fitxer, nick, server, nextClient);
        }

      } catch (RemoteException e) { // No valid registry at that port.
        System.out.println ("\n\t\t[SERVIDOR]: Algun client ha caigut sense desconectar-se correctament.");
        clientList.removeElement(oldClient);
        System.out.println ("\n\t\t[SERVIDOR]: El client caigut ha estat eliminat correctament.");
      }

    }
  }

  // funcio en proves, callback dins dun callback - No va...
  public String enviarContingutCAllback(byte[] buffer, String nick, String nameFile) throws java.rmi.RemoteException{

    System.out.println("\n\t\tenviarContingut() serverImpl");

    for (int i = 0; i < clientList.size(); i++){
      String clientAux = (String)llistaNicks.get(i);
      CallbackClientInterface nextClient = (CallbackClientInterface)clientList.elementAt(i);
      CallbackClientInterface oldClient = nextClient;

      try {
       if(clientAux.equals(nick)){

        System.out.println("\n\t\t[SERVIDOR]: Iniciant DOWNLOAD final");
           nextClient.downloadFile(buffer,nameFile);
        }
	//a.downloadFile(buffer);

      } catch (RemoteException e) { // No valid registry at that port.
        System.out.println ("\n\t\t[SERVIDOR]: Algun client ha caigut sense desconectar-se correctament.");
        clientList.removeElement(oldClient);
        System.out.println ("\n\t\t[SERVIDOR]: El client caigut ha estat eliminat correctament.");
 	    }

    }
    return "";

  }

  private synchronized void doCallbacks(String opcio, String usuari, String missatge) throws java.rmi.RemoteException{
    // make callback to each registered client
    System.out.println("\n\t\t[CALLBACKS]: Inicialitzant Callback...");
    for (int i = 0; i < clientList.size(); i++){
      CallbackClientInterface nextClient = (CallbackClientInterface)clientList.elementAt(i);
      CallbackClientInterface oldClient = nextClient;

      try {
        System.out.println("\n\t\t\t[CALLBACK "+i+"]:	OK");

        if (opcio.equals("nick")) {
          // NOTIFICAR -- nick -- als altres usuaris
          nextClient.notifyMe("Nou NICK a la llista de "+clientList.size()+"\n\t\t\telements, per al usuari "+usuari+"\n\t\t\tque ha dit "+missatge);
        }
        if (opcio.equals("registre")) {
          // NOTIFICAR -- registre -- als altres usuaris
          nextClient.notifyMe("Client registrat número "+clientList.size()+"\n\t\t\ten la llista amb .hashCode() = "+usuari+"\n\t\t\tha estat REGISTRAT."+missatge);
        }
        if (opcio.equals("pujar")) {
          // NOTIFICAR -- pujar -- als altres usuaris
          nextClient.notifyMe("Clients registrats = "+clientList.size()+"\n\t\t\tEl usuari "+usuari+"\n\t\t\tha pujat "+missatge);
        }
        if (opcio.equals("desconectar")) {
          // NOTIFICAR -- desconectar -- als altres usuaris
          nextClient.notifyMe("Client desconectant "+clientList.size()+"\n\t\t\tEl usuari "+usuari+"\n\t\t\tdiu "+missatge);
        }

      } catch (RemoteException e) { // No valid registry at that port.
        System.out.println ("\n\t\t[SERVIDOR]: Algun client ha caigut sense desconectar-se correctament.");
        clientList.removeElement(oldClient);
        System.out.println ("\n\t\t[SERVIDOR]: El client caigut ha estat eliminat correctament.");
      }

      //CallbackClientInterface nextClient = (CallbackClientInterface)clientList.elementAt(i);

    }// end for
    System.out.println("\n\t [CALLBACKS]: El SERVIDOR ha completat els callbacks.");
  } // end doCallbacks


  public String contingutIniciar(Continguts pelicula) throws java.rmi.RemoteException {
    System.out.println("\n\t\t[SERVIDOR]: " + pelicula.nickUsuari + " puja contingut.");

    String uuid = java.util.UUID.randomUUID().toString();
    /* Demanar id al Servidor */

    try {
      URL url = new URL (URL_WS+"contents/newId/");
      System.out.println("\t[RMI URL]: "+URL_WS+"contents/newId/");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", MediaType.TEXT_PLAIN);

      if(conn.getResponseCode() != 200) {
        //throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
        System.out.println("Failed: HTTP error code: " + conn.getResponseCode());
      }

       BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
       String output;
       String OutputFinal = "";
       while((output = br.readLine()) != null){
         OutputFinal = OutputFinal+output;
       }
       conn.disconnect();

       uuid = OutputFinal;

     } catch (MalformedURLException e) {
       //return "ERROR WS";

     } catch (IOException e) {
       //return "ERROR WS";
     }


    pelicula.posaridentificadorUNIC(uuid);
    llistaDeContinguts.add(pelicula);

    System.out.println("\n\t\t[SERVIDOR]: Contingut registrat "+pelicula.toString());
    int indexContingut = (llistaDeContinguts.size())-1;
    System.out.println("\n\t[SERVIDOR]: Posició del contingut: " + indexContingut);

    return uuid;
  }

  public String enviarContingut(byte[] buffer, String a, CallbackClientInterface h) throws java.rmi.RemoteException{
      System.out.println("\n\t\tenviarContingut() serverImpl");
      //enviarContingutCAllback(buffer, h);
      return "";
  }

  public int contingutMostrar() throws java.rmi.RemoteException {
    return llistaDeContinguts.size();
  }

  public int numContingutsWS(String nick) throws java.rmi.RemoteException {

    try {
      List<Continguts> aux = new ArrayList<Continguts>();
      URL url = new URL (URL_WS+"contents/listItems/"+nick+"/");
      System.out.println("\t[RMI URL]: "+URL_WS+"contents/listItems/"+nick+"/");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);

      if(conn.getResponseCode() != 200) {
        //throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
        System.out.println("Failed: HTTP error code: " + conn.getResponseCode());
      }

       BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
       String output;
       String OutputFinal = "";
       while((output = br.readLine()) != null){
         OutputFinal = OutputFinal+output;
       }
       if(OutputFinal.equals("")){
         return 0;
       }

      List<Continguts> auxListJson = jsonToContingutsList(OutputFinal);



       //p.posarTitolPeli(obj.getString("titolPelicula"));
       //p.posarNickUsuariPeli(obj.getString("nickUsuari"));
       //p.posaridentificadorUNIC(obj.getString("identificadorUNIC"));

       //System.out.println("\nClient C: " + p.toString() );

       conn.disconnect();
       return auxListJson.size();

     } catch (MalformedURLException e) {
       return 0;

     } catch (IOException e) {
       return 0;

     }
  }

  public Continguts contingutMostrarWS(String nick, int index) throws java.rmi.RemoteException {

    try {
      List<Continguts> aux = new ArrayList<Continguts>();
      URL url = new URL (URL_WS+"contents/listItems/"+nick+"/");
      System.out.println("\t[RMI URL]: "+URL_WS+"contents/listItems/"+nick+"/");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);

      if(conn.getResponseCode() != 200) {
        //throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
        System.out.println("Failed: HTTP error code: " + conn.getResponseCode());
      }

       BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
       String output;
       String OutputFinal = "";
       while((output = br.readLine()) != null){
         OutputFinal = OutputFinal+output;
       }
       if(OutputFinal.equals("")){
         return null;
       }

      List<Continguts> auxListJson = jsonToContingutsList(OutputFinal);

       System.out.println("\nClient C: " + auxListJson.get(index).toString() );


       //Continguts search = auxListJson.get(index);
       //System.out.println(index+" "+search.toString());
       conn.disconnect();

       return auxListJson.get(index);

     } catch (MalformedURLException e) {
       return null;

     } catch (IOException e) {
       return null;

     }

  }

  public boolean eliminarContingut(String nick, Continguts peli) throws java.rmi.RemoteException {

    for(int aux=0;aux<llistaDeContinguts.size();aux++){
      Continguts auxC= llistaDeContinguts.get(aux);
      if(auxC.demanarNickUsuariPeli().equals(nick) && auxC.demanaridentificadorUNIC().equals(peli.demanaridentificadorUNIC())){

        //Eliminar al WS
        try {
          URL url = new URL (URL_WS+"contents/delete/"+auxC.demanaridentificadorUNIC()+"/");
          System.out.println("\t[RMI URL]: "+URL_WS+"contents/delete/"+auxC.demanaridentificadorUNIC()+"/");
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("GET");
          conn.setRequestProperty("Accept", MediaType.TEXT_PLAIN);

          if(conn.getResponseCode() != 200) {
            //throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
            System.out.println("Failed: HTTP error code: " + conn.getResponseCode());
          }

           BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
           String output;
           String OutputFinal = "";
           while((output = br.readLine()) != null){
             OutputFinal = OutputFinal+output;
           }
           conn.disconnect();

           if(OutputFinal.equals("true")){
             llistaDeContinguts.remove(aux);
             return true;
           }

         } catch (MalformedURLException e) {
           //return "ERROR WS";

         } catch (IOException e) {
           //return "ERROR WS";
         }



        // Eliminar fitxer dels demes?
        // Avisar als altres

      }
    }
    return false;
  }

  public boolean modificarContingut(String nick, Continguts peli, String nouNom) throws java.rmi.RemoteException {

    for(int aux=0;aux<llistaDeContinguts.size();aux++){
      Continguts auxC= llistaDeContinguts.get(aux);
      if(auxC.demanarNickUsuariPeli().equals(nick) && auxC.demanaridentificadorUNIC().equals(peli.demanaridentificadorUNIC())){

        // Modificar al webService!
        try {
          URL url = new URL (URL_WS+"contents/update/"+peli.demanaridentificadorUNIC()+"/"+nouNom+"/");
          System.out.println("\t[RMI URL]: "+URL_WS+"contents/update/"+peli.demanaridentificadorUNIC()+"/"+nouNom+"/");
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          conn.setRequestMethod("GET");
          conn.setRequestProperty("Accept", MediaType.TEXT_PLAIN);

          if(conn.getResponseCode() != 200) {
            //throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
            System.out.println("Failed: HTTP error code: " + conn.getResponseCode());
          }

           BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
           String output;
           String OutputFinal = "";
           while((output = br.readLine()) != null){
             OutputFinal = OutputFinal+output;
           }
           conn.disconnect();

           if(OutputFinal.equals("true")){
             llistaDeContinguts.get(aux).posarTitolPeli(nouNom);
             return true;
           }

         } catch (MalformedURLException e) {
           //return "ERROR WS";

         } catch (IOException e) {
           //return "ERROR WS";
         }


        // modificar fitxer dels demes?
        // Avisar als altres
        return false;
      }
    }
    return false;
  }

  public String descargarContingut(String nick, Continguts peli) throws java.rmi.RemoteException {

    for(int aux=0;aux<llistaDeContinguts.size();aux++){
      Continguts auxC= llistaDeContinguts.get(aux);
      if(auxC.demanarNickUsuariPeli().equals(nick) && auxC.demanaridentificadorUNIC().equals(peli.demanaridentificadorUNIC())){
        StringBuilder sb = new StringBuilder();
        sb.append("\n\t\t Descargar d'ell mateix:");
        sb.append("\n\t\t\t Nom fitxer: "+llistaDeContinguts.get(aux).getNameFile());
        sb.append("\n\t\t\t Localització fitxer: "+llistaDeContinguts.get(aux).getLocFile());
        return sb.toString();
      }else if(auxC.demanaridentificadorUNIC().equals(peli.demanaridentificadorUNIC())){
        StringBuilder sb = new StringBuilder();
        sb.append("\n\t\t Descargar d'un altre client:");
        sb.append("\n\t\t\t Nom fitxer: "+llistaDeContinguts.get(aux).getNameFile());
        sb.append("\n\t\t\t Localització fitxer: "+llistaDeContinguts.get(aux).getLocFile());
        sb.append("\n\t\t\t IP fitxer: "+llistaDeContinguts.get(aux).getIPFile());
        buscarUsuariFitxer(auxC);

        return sb.toString();
      }
    }
    return "";
  }

  public String descargarContingutWS(String nick, Continguts peli) throws java.rmi.RemoteException {

    try {
      List<Continguts> aux = new ArrayList<Continguts>();
      URL url = new URL (URL_WS+"contents/"+peli.demanaridentificadorUNIC()+"/");
      System.out.println("\t[RMI URL]: "+URL_WS+"contents/"+peli.demanaridentificadorUNIC()+"/");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON);

      if(conn.getResponseCode() != 200) {
        //throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
        System.out.println("Failed: HTTP error code: " + conn.getResponseCode());
      }

       BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
       String output;
       String OutputFinal = "";
       while((output = br.readLine()) != null){
         OutputFinal = OutputFinal+output;
       }
       if(OutputFinal.equals("")){
         return "";
       }

      List<Continguts> auxListJson = jsonToContingutsList(OutputFinal);


       System.out.println("\nContenido 0: " + auxListJson.get(0).toString() );

       conn.disconnect();

       return auxListJson.get(0).toString();

     } catch (MalformedURLException e) {
       System.out.println("ERROR 1");
       return "";

     } catch (IOException e) {
       System.out.println("ERROR 2");
       return "";

     }

  }

  public String descargarContingutPart2(Continguts peli,String nick) throws java.rmi.RemoteException {

    for(int aux=0;aux<llistaDeContinguts.size();aux++){
      Continguts auxC= llistaDeContinguts.get(aux);
      System.out.println("\n[CallbackServerImpl] -----------------------------------");//ROB
      System.out.println("\n[descargarContingutPart "+aux+" / "+llistaDeContinguts.size()+" ]:" +auxC.toString());//ROB
      if(auxC.demanaridentificadorUNIC().equals(peli.demanaridentificadorUNIC())){
        goFitxer(auxC, nick, this);
        return "OK";
      }
    }
    return "No s'ha trobat el contingut en el servidor";
  }


  public Continguts demanarContingut(int index) throws java.rmi.RemoteException {
    Continguts search = llistaDeContinguts.get(index);
    System.out.println(index+" "+search.toString());
    return search;
  }

  public String contingutPujar(Continguts pelicula) throws java.rmi.RemoteException {

    System.out.println("\n\t\t[SERVIDOR]: El NICK que puja una película és ... " + pelicula.nickUsuari);
	  System.out.println("\n\t\t[SERVIDOR]: La película pujada és ... " + pelicula.titolPelicula);

    return "OK";
  }

  public String contingutNotificar(Continguts pelicula) throws java.rmi.RemoteException {

    System.out.println("\n\t\t[SERVIDOR]: " + pelicula.nickUsuari);
	  System.out.println("\n\t\t[SERVIDOR]: " + pelicula.identificadorUNIC);
	  System.out.println("\n\t\t[SERVIDOR]: " + pelicula.titolPelicula);

    boolean flag = false;
    for (Continguts i : llistaDeContinguts) {
      if(i.demanaridentificadorUNIC().equals(pelicula.demanaridentificadorUNIC())){
        //System.out.println("[SERVIDOR][SEARCH]: trobat "+i.toString());
        i.updateContingut(pelicula);
        try{
          i.setIPFile(getClientHost());
          System.out.println("\n\t\t[IP Client]: "+getClientHost()); // display message
        }catch(Exception e){

        }

        pelicula.portServidor = portServer;
        pelicula.ipServidor = nameServer;

        if(!accions_WS_CONTENTS_PUT("user/",pelicula.nickUsuari,"add/",pelicula.identificadorUNIC,pelicula.json())){
          return "ERROR RMI accept.";
        }

        flag = true;
      }
    }
      doCallbacks("pujar", pelicula.nickUsuari, pelicula.titolPelicula);
      if(flag==true){
        return "OK";
      }
      return "ERROR";
   }
   private boolean accions_WS_CONTENTS_PUT(String action, String nickUser, String subAction, String id, String jsonInput){
     try {
        URL url = new URL (URL_WS+"contents/"+action+nickUser+"/"+subAction+id+"/");
        System.out.println("\t[RMI URL]: "+URL_WS+"contents/"+action+nickUser+"/"+subAction+id+"/");
        System.out.println("\t\t[RMI CONTENT]: "+jsonInput);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");

        conn.setRequestProperty("Content-Type", "application/json");
        OutputStream os = conn.getOutputStream();
        os.write(jsonInput.getBytes());
        os.flush();

        if(conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
          //throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String output;

        while((output = br.readLine()) != null){
          System.out.println("\nClient POST. Resposta: " + output );
        }
        conn.disconnect();
        return true;
      } catch (MalformedURLException e) {
        //e.printStackTrace();
        return false;
      } catch (IOException e) {
        //e.printStackTrace();
        return false;
      }
   }
   private boolean accions_WS_PUT_IN_SERVER(String idServer,String action, String id, String jsonInput){
     try {
        URL url = new URL (URL_WS+"servers/"+idServer+"/"+action+"/"+id+"/");
        System.out.println("\t[RMI URL]: "+URL_WS+"servers/"+idServer+"/"+action+"/"+id+"/");
        System.out.println("\t\t[RMI CONTENT]: "+jsonInput);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");

        conn.setRequestProperty("Content-Type", "application/json");
        OutputStream os = conn.getOutputStream();
        os.write(jsonInput.getBytes());
        os.flush();

        if(conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
          //throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String output;

        while((output = br.readLine()) != null){
          System.out.println("\nClient POST. Resposta: " + output );
        }
        conn.disconnect();
        return true;
      } catch (MalformedURLException e) {
        //e.printStackTrace();
        return false;
      } catch (IOException e) {
        //e.printStackTrace();
        return false;
      }

   }

   private boolean accions_WS_PUT(String id,String urlBase, String jsonInput){
     try {
        URL url = new URL (URL_WS+urlBase+id+"/");
        System.out.println("\t[RMI URL]: "+URL_WS+urlBase+id+"/");
        System.out.println("\t\t[RMI CONTENT]: "+jsonInput);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");

        conn.setRequestProperty("Content-Type", "application/json");
        OutputStream os = conn.getOutputStream();
        os.write(jsonInput.getBytes());
        os.flush();

        if(conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
          //throw new RuntimeException("Failed: HTTP error code: " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String output;

        while((output = br.readLine()) != null){
          System.out.println("\nClient POST. Resposta: " + output );
        }
        conn.disconnect();
        return true;
      } catch (MalformedURLException e) {
        //e.printStackTrace();
        return false;
      } catch (IOException e) {
        //e.printStackTrace();
        return false;
      }
   }

   private void mostrarLlistaRegistrats(){

     StringBuilder sb = new StringBuilder();
     sb.append("Llistat nicks ["+llistaNicks.size()+"]:");
     for (int i = 0; i < llistaNicks.size(); i++){
      String clientAux = (String)llistaNicks.get(i);
   		sb.append(clientAux+", ");

     }
     System.out.println(sb.toString());
   }

   private void mostrarLlistaCallbaks(){


     StringBuilder sb = new StringBuilder();
     sb.append("Llistat callbaks ["+clientList.size()+"]:");
     for (int i = 0; i < clientList.size(); i++){
       String clientAux = (String)llistaNicks.get(i);
       CallbackClientInterface nextClient = (CallbackClientInterface)clientList.elementAt(i);
       sb.append(clientAux+", ");
     }

     System.out.println(sb.toString());
   }



   private static List<Continguts> jsonToContingutsList(String OutputFinal){

     if(OutputFinal==""){
       return null;
     }
     List<Continguts> auxListJson = new ArrayList<Continguts>();

     String[] strs = OutputFinal.split("(?<=\\})(?=\\{)");

     int i = 0;
     int reset = 8;
     for (String s : strs) {

         String[] split2 = s.split(",");

         StringBuilder sb = new StringBuilder();

         Continguts p = new Continguts();
         for (int k = 0; k < split2.length; k++) {

           String id, value;
           String[] split3 = split2[k].split(":");

           if((k % reset)==0){
             p = new Continguts();
             id = split3[0].substring(2,split3[0].length()-1);
             if(k==0){
               id = split3[0].substring(3,split3[0].length()-1);
             }
             value = split3[1].substring(1, split3[1].length() - 1);
             //value = value.substring(1);
           }else if(k % reset ==reset-1){
             id = split3[0].substring(1,split3[0].length()-1);
             value = split3[1].substring(1, split3[1].length() - 2);
             if(k ==split2.length-1){
               value = split3[1].substring(1, split3[1].length() - 3);
             }



           }else{
             id = split3[0].substring(1,split3[0].length()-1);
             value = split3[1].substring(1, split3[1].length() - 1);
             //value = value.substring(1);
           }

           System.out.println("ID: "+id+" V: "+value);

           if(id.equals("titolPelicula")){
             //System.out.println("Add posarTitolPeli: "+value);
             p.posarTitolPeli(value);
           }
           if(id.equals("identificadorUNIC")){
             //System.out.println("Add identificadorUNIC: "+value);
             p.posaridentificadorUNIC(value);
           }
           if(id.equals("nickUsuari")){
             //System.out.println("Add nickUsuari: "+value);
             p.posarNickUsuariPeli(value);
           }
           if(id.equals("locFile")){
             //System.out.println("Add nickUsuari: "+value);
             p.setLocFile(value);
           }
           if(id.equals("nameFile")){
             //System.out.println("Add nickUsuari: "+value+" - "+(k % reset-1)+ "=="+ reset);
             p.setNameFile(value);
           }
           if(id.equals("ipfile")){
             //System.out.println("Add nickUsuari: "+value+" - "+(k % reset-1)+ "=="+ reset);
             p.setIPFile(value);
           }

           if(id.equals("ipServidor")){
             //System.out.println("Add nickUsuari: "+value+" - "+(k % reset-1)+ "=="+ reset);
             p.setipServidor(value);
           }
           if(id.equals("portServidor")){
             //System.out.println("Add nickUsuari: "+value+" - "+(k % reset-1)+ "=="+ reset);
             p.setportServidor(value);
           }

           if(k % reset == reset-1){
             //System.out.println("Fi Continguts");
             System.out.println("\nContingut pt1: " + p.toString() );
             auxListJson.add(p);
           }



         }

 //                System.out.println(sb.toString());

         //System.out.println("\nClient text_plain: " + strs[i] );

     }
     return auxListJson;
   }

}
