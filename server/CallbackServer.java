import java.io.*;
import java.rmi.*;
import java.util.*;
import java.io.IOException;
import java.rmi.server.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.*;
import java.security.*;

import javax.ws.rs.core.MediaType;
import sun.net.www.protocol.http.HttpURLConnection;


/**
 * @author Robert Pedros i Roger Orellana
 *
 */

public class CallbackServer  {

  private static String URL_WS = "http://localhost:8080/treballWSWeb4/treball/";
  private static String codeServer = "";
  private static String versionCompile = "v WS .14";

  public static void main(String args[]) {
    try{
      InputStreamReader is = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(is);
      String hostName, portNum, registryURL;
      System.out.println("\n\t[INFO]: Esta apunt de ficar en marxa un nou servidor RMI. "+versionCompile);

	  Scanner entradaTexte = new Scanner (System.in); 			//Creación de un objeto Scanner
	 StringBuilder espai = new StringBuilder();
  	  espai.append(" ");
	  	String ipRMI;
	  	
		System.out.println("\n\t\t[INPUT] Quin és la IP i port de S_RMI?: [per defecte: localhost:8080] " );
		ipRMI = br.readLine();
		if(ipRMI.equals("")){
			ipRMI = "localhost:8080";				//	"localhost"
		}
		// !!! Com no tenim implementat al JBoss que arranqui amb la "IP", i arranca com a Localhost, el servidor sempre sera localhost:8080
		ipRMI = "localhost:8080";
		

		URL_WS = "http://"+ipRMI+"/treballWSWeb4/treball/";	

      System.out.println("\n\t[INPUT]: Entrar adreça del SERVIDOR RMIRegistry, <127.0.0.1> per defecte.");
      hostName = br.readLine();
      if(hostName.equals("")){
        hostName = "127.0.0.1";				//	"localhost"
      }


      System.out.println("\n\t[INFO]: SERVIDOR de RMI preparat amb IP ... "+hostName);
      System.setProperty("java.rmi.server.hostname",hostName);
      try{
        System.out.println("\n\t[INPUT]: Entrar el port del servei RMIregistry, <7007> per defecte.");
      		portNum = br.readLine();
      		if(portNum.equals("")){
    			portNum = "7007";
    		}

    		System.out.println("\n\t[INFO]:SERVIDOR de RMI preparat al PORT ... "+portNum);
        int RMIPortNum = Integer.parseInt(portNum);
        startRegistry(RMIPortNum);
        CallbackServerImpl exportedObj = new CallbackServerImpl();
        registryURL = "rmi://"+hostName+":" + portNum + "/callback";
        Naming.rebind(registryURL, exportedObj);

        System.out.println("\n\t[INFO]:SERVIDOR registrat.");		/*Important*/
        listRegistry(registryURL);							/*Important*/		// list names currently in the registry

        System.out.println("\n\t[INFO]:SERVIDOR de Callback preparat.");

        codeServer = addServerWS(hostName, Integer.parseInt(portNum));
        exportedObj.defineCode(codeServer,hostName,portNum);


      }// end try
      catch (Exception re) {
        System.out.println("\n\t[ERROR]: Excepció en la crida CallbackServer.main: " + re);
      } // end catch
    }
    catch (Exception re) {
    	System.out.println("\n\t[ERROR]: Excepció en la crida CallbackServer.main: " + re);
    }
  }


  //This method starts a RMI registry on the local host, if it does not already exists at the specified port number.
  private static void startRegistry(int RMIPortNum) throws RemoteException{
    try { // This call will throw an exception if the registry does not already exist
      Registry registry = LocateRegistry.getRegistry(RMIPortNum);
      registry.list( );
    }
    catch (RemoteException e) { // No valid registry at that port.
      System.out.println ("\n\t\t[WARNING]: El registre RMI NO ha estat localitzat al port " + RMIPortNum);
      Registry registry = LocateRegistry.createRegistry(RMIPortNum);
      System.out.println ("\t\t[INFO]: El registre RMI ha estat creat al port " + RMIPortNum);
    }
  } // end startRegistry

  private static void listRegistry(String registryURL) throws RemoteException, MalformedURLException {
    System.out.println("\n\t\t[INFO]: El registre RMI situat a "+registryURL);
    System.out.println("\n\t\t\t[REGISTRY]:");
    String [ ] names = Naming.list(registryURL);
    for (int i=0; i < names.length; i++)
    System.out.println("\n\t\t\t\t[REG"+i+"]: "+ names[i]);
  }

  private static String addServerWS(String ip, int port){
    try {
      String randomCode = generateRandom(10);

      Servidor servidor = new Servidor(randomCode,ip,port);
      String jsonInString = servidor.json();

      System.out.println(URL_WS+"servers/add/"+randomCode+"/");
      System.out.println(jsonInString);
      URL url = new URL (URL_WS+"servers/add/"+randomCode+"/");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("PUT");
      // String name, String ip, int puerto
      conn.setRequestProperty("Content-Type", "application/json");
      //String input = "{\"name\":\""+randomCode+"\",\"ip\":"+ip+",\"puerto\":"+port+"}";
      OutputStream os = conn.getOutputStream();
      os.write(jsonInString.getBytes());
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
      return randomCode;
    } catch (MalformedURLException e) {
      //e.printStackTrace();
      return "";
    } catch (IOException e) {
      //e.printStackTrace();
      return "";
    }

  }

  public static String generateRandom(int length) {
    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    Random random = new SecureRandom();

    if (length <= 0) {
      throw new IllegalArgumentException("String length must be a positive integer");
    }

    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(characters.charAt(random.nextInt(characters.length())));
    }
    //return "XJTHMXDNNI";
    return sb.toString();
  }

} // end class
