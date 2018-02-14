import java.io.*;
import java.rmi.*;
import java.nio.file.FileSystems;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import static java.nio.file.StandardCopyOption.*;
import java.net.*;
import static java.lang.System.out;
import java.util.*;
import java.util.Scanner;

import javax.ws.rs.core.MediaType;
import sun.net.www.protocol.http.HttpURLConnection;

/**
 * @author Robert Pedros i Roger Orellana
 *
*/

public class CallbackClient {

  private static final String URL_WS = "http://localhost:8080/treballWSWeb4/treball/";
  private String codeServer = "";
  private static String versionCompile = "v WS .14";

  public static void main(String args[]) {


    try {
      int RMIPort;
      String hostName, portNum, resposta;
      Scanner entrarLinia = new Scanner (System.in);
      System.out.println("\n\t[INFO]: Esta apunt de ficar en marxa un nou client RMI. "+versionCompile);
      System.out.println("\t[INPUT]: Entrar adreça del SERVIDOR RMIRegistry, <127.0.0.1> per defecte.");
      hostName = entrarLinia.nextLine ();
      if(hostName.equals("")){
	      hostName = "127.0.0.1";	//	"localhost"

      }

      System.out.println("\n\t[INPUT]: Entrar el port del servei RMIregistry, <7007> per defecte.");
      portNum = entrarLinia.nextLine ();
      if(portNum.equals("")){
		      portNum = "7007";
      }

      System.out.println("\n\t[LOG] Accedint a " + hostName + ":" + portNum + "...");
      RMIPort = Integer.parseInt(portNum);
      String registryURL = "rmi://"+hostName+":" + portNum + "/callback";	// find the remote object and cast it to an interface object

      CallbackServerInterface serveiH = (CallbackServerInterface)Naming.lookup(registryURL);


      String nickDelUsuari;	//Entrar un NICk que el servidor accepta i registra només si és DIFERENT.
      do {
      	nickDelUsuari = entrarNick(serveiH);
      } while (nickDelUsuari.equals(""));

      // Quan ja he registrat correctament el NICK d'un nou usuari, puc posar-lo a la llista per rebre CallBack's
      // Tindré dos misatges de CallBack, un en registrar NICK i un altre en rebre continguts, esborrar-los, modificar-los ...

      CallbackClientInterface callbackObj = new CallbackClientImpl();
      serveiH.registerForCallback(callbackObj);
      System.out.println("\n\t[REGISTRE] "+nickDelUsuari+" registrat per a rebre callbacks.");

	// AFEGIR la posibilitat de CARREGAR els CONTINGUTS de la CARPETA
	// AUTOMATICAMENT i que comprovi que el IDENTIFICADOR sigui UNIC

      String opcioTriada;	//Entrar una opció


      do {
        System.out.println("\n\t[MENU] Opcions Principals\n\t\t1. Llistar tot el contingut\n\t\t2. Cercar contingut\n\t\t3. Gestionar Contingut propi");
        System.out.println("\t\t4. Pujar contingut");
        //System.out.println("\t\t5. Llistar contingut del WS\n\t\t6. Cercar contingut al WS\n\t\t7. [TEST] Peticio get WS\n\t\t8. [TEST] Peticio POST WS\n\t\t9. [TEST] Peticio PUT - Eliminar");
        System.out.println("\n\t\t0. Sortir. ");


        opcioTriada = entrarLinia.nextLine ();

        if (opcioTriada.equals("1")) {

          System.out.println("\n\t\t[LLISTAT]: Llista de tot el contingut.\n");
            int numLlista= serveiH.contingutMostrar();
            int numLlistaWS = serveiH.numContingutsWS(nickDelUsuari);

            boolean flag = false;

            List<Continguts> llistaTotContingut = new ArrayList<Continguts>();
            for(int aux=0;aux<numLlista;aux++){
              Continguts auxC= serveiH.demanarContingut(aux);
              llistaTotContingut.add(auxC);
              flag = true;
            }

            List<Continguts> llistaTotContingutWS = new ArrayList<Continguts>();
            for(int aux=0;aux<numLlistaWS;aux++){
              Continguts auxC= serveiH.contingutMostrarWS(nickDelUsuari,aux);
              llistaTotContingutWS.add(auxC);
              flag = true;
            }

            if(!flag){
              System.out.println("\t\t\t[RESULTAT]: No hi ha contigut.");
            }else{
              flag = true;
              boolean eleccion= false;
              String fitxerTriat;
              do{
                if(numLlista>0 && numLlistaWS>0){
                  mostrarContingutsLlistaDoble(llistaTotContingut,llistaTotContingutWS,nickDelUsuari,1);
                }else if(numLlista>0){
                  mostrarContingutsLlista(llistaTotContingut,nickDelUsuari,1);
                }else if(numLlistaWS>0){
                  mostrarContingutsLlista(llistaTotContingutWS,nickDelUsuari,4);
                }


                System.out.println("\n\t\t\t[-1]. Cancelar");
                System.out.println("\n\t\t\t[INPUT] Número de fitxer a descargar?");
                int auxTriat;
                fitxerTriat = entrarLinia.nextLine ();
                if(isNumeric(fitxerTriat)){
                  auxTriat = Integer.parseInt(fitxerTriat);
                  if((auxTriat+1)>0 && auxTriat<(llistaTotContingut.size()+llistaTotContingutWS.size())){
                    flag = false;
                    eleccion = true;
                  }else if(auxTriat==-1){
                    flag = false;
                  }else{
                    System.out.println("\n\t\t\t\t[ERROR] Número de fitxer no existeix");
                  }
                }else{
                  System.out.println("\n\t\t\t\t[ERROR] Número de fitxer no existeix");
                }
              }while(flag);

              if(eleccion){

                System.out.println("\n\t\t\t[DOWNLOAD]: Starting...");
                Continguts getCon;
                boolean serverExtern= false;
                boolean local = false;

                if(Integer.parseInt(fitxerTriat)<llistaTotContingut.size()){

                  getCon = llistaTotContingut.get(Integer.parseInt(fitxerTriat));

                  if(getCon.demanarNickUsuariPeli().equals(nickDelUsuari)){
                    resposta = "";
                    local = true;
                  }else{
                    resposta = serveiH.descargarContingut(nickDelUsuari,getCon);
                  }

                }else{
                  serverExtern = true;
                  fitxerTriat = Integer.toString(Integer.parseInt(fitxerTriat)-llistaTotContingut.size());

                  getCon = llistaTotContingutWS.get(Integer.parseInt(fitxerTriat));
                  resposta = serveiH.descargarContingutWS(nickDelUsuari,getCon);
                }

                if(!resposta.equals("") && local == false){

                  if(serverExtern==false){
                    System.out.println("\n\t\t\t[DOWNLOAD RMI PROPI]...");
                    // Fer dos fils, un que crei la espera dun fitxer per ServerSocket, perque te un while(true)
                    // i un que executi:   resposta = serveiH.descargarContingutPart2("192.168.0.1",8080,getCon);
                    // amb la ip i el port d'aquest client.
                    // O fer callback del callback

                    String respostaFile = serveiH.descargarContingutPart2(getCon,nickDelUsuari);
                    if(respostaFile.equals("OK")){
                      System.out.println("\n\t\t\t[DOWNLOAD] El fitxer ha set descargat correctament del client extern");
                    }else{
                      System.out.println("\n\t\t\t[ERROR] Error inesperat en la descarga - "+respostaFile);
                    }
                  }else{
                    System.out.println("\n\t\t\t[DOWNLOAD RMI EXTERN]...");

                    String registryURL2 = "rmi://"+getCon.getipServidor()+":" + getCon.getportServidor() + "/callback";	// find the remote object and cast it to an interface object
                    CallbackServerInterface serveiAux = (CallbackServerInterface)Naming.lookup(registryURL2);
                    //String nickOK = serveiAux.sayHello(nickUsuari, "1");
                    CallbackClientInterface callbackObjAux = new CallbackClientImpl();
                    String nouNickRemot = "rmi://"+getCon.getipServidor()+":" + getCon.getportServidor()+"?"+nickDelUsuari;
                    nouNickRemot = registraNick(serveiAux,nouNickRemot);
                    serveiAux.registerForCallback(callbackObjAux);

                    System.out.println("\n\t\t\t[QUESTION] "+serveiAux.returnServer());


                    String respostaFile = serveiAux.descargarContingutPart2(getCon,nouNickRemot);
                    if(respostaFile.equals("OK")){
                      System.out.println("\n\t\t\t[DOWNLOAD] El fitxer ha set descargat correctament del client extern");
                    }else{
                      System.out.println("\n\t\t\t[ERROR] Error inesperat en la descarga: "+respostaFile);
                    }
                    serveiAux.unregisterForCallback(callbackObjAux,nouNickRemot);

                  }

                }else if(local==true){
                  // Moure de carpeta descargas a local

                  if(moureFitxer(getCon)){
                    System.out.println("\n\t\t\t[DOWNLOAD] S'ha pogut descargar localment");

                  }else{
                    System.out.println("\n\t\t\t[ERROR] No s'ha pogut descargar localment");

                  }


                }else{
                  System.out.println("\n\t\t\t[ERROR] Problemes al descargar el fitxer, tornau a intentar més tard. "+resposta);
                }
              }else if(!eleccion){
              }else{
                System.out.println("\n\t\t\t[ERROR] Problemes al descargar el fitxer, tornau a intentar més tard.");
              }
            }

        }

        if (opcioTriada.equals("2")) {

      		System.out.println("\n\t\t[LLISTAT]: Cercar contingut.\n");
          String opcioTriada2;
          System.out.println("\n\t\t\t[INPUT]: Nom de contingut a cercar?");
          opcioTriada2 = entrarLinia.nextLine ().toUpperCase();

          int numLlista= serveiH.contingutMostrar();
          int numLlistaWS = serveiH.numContingutsWS(nickDelUsuari);

          boolean flag = false;
          List<Continguts> llistaCercarContingut = new ArrayList<Continguts>();
          int numRes = 0;
          int auxL = 0;


          for(int aux=0;aux<numLlista;aux++){
            flag = false;

            Continguts auxC= serveiH.demanarContingut(aux);
            String cadenaDondeBuscar = auxC.demanarTitolPeli().toUpperCase();
            String[] palabras = opcioTriada2.split("\\W+");
            for (String palabra : palabras) {
                if (cadenaDondeBuscar.toUpperCase().contains(palabra.toUpperCase()) && flag==false) {
                  llistaCercarContingut.add(auxC);
                  auxL ++;
                  flag = true;
                  numRes++;
                }
            }
            palabras = auxC.demanarTitolPeli().split("\\W+");
            for (String palabra : palabras) {
                if (opcioTriada2.toUpperCase().contains(palabra.toUpperCase()) && flag==false) {
                  llistaCercarContingut.add(auxC);
                  auxL ++;
                  flag = true;
                  numRes++;
                }
            }

            int resultado = auxC.demanarTitolPeli().toUpperCase().indexOf(opcioTriada2.toUpperCase());
            if(resultado != -1 && flag==false) {
              llistaCercarContingut.add(auxC);
              auxL ++;
              flag = true;
              numRes++;
            }
          }

          List<Continguts> llistaCercarContingutWS = new ArrayList<Continguts>();
          numRes = 0;

          for(int aux=0;aux<numLlistaWS;aux++){
            flag = false;

            Continguts auxC= serveiH.contingutMostrarWS(nickDelUsuari,aux);
            String cadenaDondeBuscar = auxC.demanarTitolPeli().toUpperCase();
            String[] palabras = opcioTriada2.split("\\W+");
            for (String palabra : palabras) {
                if (cadenaDondeBuscar.toUpperCase().contains(palabra.toUpperCase()) && flag==false) {
                  llistaCercarContingutWS.add(auxC);
                  auxL ++;
                  flag = true;
                  numRes++;
                }
            }

            palabras = auxC.demanarTitolPeli().split("\\W+");
            for (String palabra : palabras) {
                if (opcioTriada2.toUpperCase().contains(palabra.toUpperCase()) && flag==false) {
                  llistaCercarContingutWS.add(auxC);
                  auxL ++;
                  flag = true;
                  numRes++;
                }
            }

            int resultado = auxC.demanarTitolPeli().toUpperCase().indexOf(opcioTriada2.toUpperCase());
            if(resultado != -1 && flag==false) {
              llistaCercarContingutWS.add(auxC);
              auxL ++;
              flag = true;
              numRes++;
            }
          }

          if(auxL==0){
            System.out.println("\t\t\t[RESULTAT]: No hi ha contigut.");
          }else{

            flag = true;
            boolean eleccion= false;
            String fitxerTriat;

            do{

              System.out.println("\t\t\t[Resultats]: Hi han "+numRes+" coincidencies.\n");
              if(numLlista>0 && numLlistaWS>0){
                mostrarContingutsLlistaDoble(llistaCercarContingut,llistaCercarContingutWS,nickDelUsuari,1);
              }else if(numLlista>0){
                mostrarContingutsLlista(llistaCercarContingut,nickDelUsuari,1);
              }else if(numLlistaWS>0){
                mostrarContingutsLlista(llistaCercarContingutWS,nickDelUsuari,4);
              }

              System.out.println("\n\t\t\t[-1]. Cancelar");
              System.out.println("\n\t\t\t[INPUT] Número de fitxer a descargar?");
              int auxTriat;
              fitxerTriat = entrarLinia.nextLine ();
              if(isNumeric(fitxerTriat)){
                auxTriat = Integer.parseInt(fitxerTriat);
                if((auxTriat+1)>0 && auxTriat<(llistaCercarContingut.size()+llistaCercarContingutWS.size())){
                  flag = false;
                  eleccion = true;
                }else if(auxTriat==-1){
                  flag = false;
                }else{
                  System.out.println("\n\t\t\t\t[ERROR] Número de fitxer no existeix");
                }
              }else{
                System.out.println("\n\t\t\t\t[ERROR] Número de fitxer no existeix");
              }
            }while(flag);

            if(eleccion){

              System.out.println("\n\t\t\t[DOWNLOAD]: Starting...");
              Continguts getCon;
              boolean serverExtern= false;
              boolean local = false;

              if(Integer.parseInt(fitxerTriat)<llistaCercarContingut.size()){

                getCon = llistaCercarContingut.get(Integer.parseInt(fitxerTriat));

                if(getCon.demanarNickUsuariPeli().equals(nickDelUsuari)){
                  resposta = "";
                  local = true;
                }else{
                  resposta = serveiH.descargarContingut(nickDelUsuari,getCon);
                }

              }else{
                serverExtern = true;
                fitxerTriat = Integer.toString(Integer.parseInt(fitxerTriat)-llistaCercarContingut.size());

                getCon = llistaCercarContingutWS.get(Integer.parseInt(fitxerTriat));
                resposta = serveiH.descargarContingutWS(nickDelUsuari,getCon);
              }

              if(!resposta.equals("") && local == false){

                if(serverExtern==false){
                  System.out.println("\n\t\t\t[DOWNLOAD RMI PROPI]...");
                  // Fer dos fils, un que crei la espera dun fitxer per ServerSocket, perque te un while(true)
                  // i un que executi:   resposta = serveiH.descargarContingutPart2("192.168.0.1",8080,getCon);
                  // amb la ip i el port d'aquest client.
                  // O fer callback del callback

                  String respostaFile = serveiH.descargarContingutPart2(getCon,nickDelUsuari);
                  if(!respostaFile.equals("")){
                    System.out.println("\n\t\t\t[DOWNLOAD] El fitxer ha set descargat correctament del client extern");
                  }else{
                    System.out.println("\n\t\t\t[ERROR] Error inesperat en la descarga");
                  }
                }else{
                  System.out.println("\n\t\t\t[DOWNLOAD RMI EXTERN]...");

                  String registryURL2 = "rmi://"+getCon.getipServidor()+":" + getCon.getportServidor() + "/callback";	// find the remote object and cast it to an interface object
                  CallbackServerInterface serveiAux = (CallbackServerInterface)Naming.lookup(registryURL2);
                  //String nickOK = serveiAux.sayHello(nickUsuari, "1");
                  CallbackClientInterface callbackObjAux = new CallbackClientImpl();
                  String nouNickRemot = "rmi://"+getCon.getipServidor()+":" + getCon.getportServidor()+"?"+nickDelUsuari;


                  nouNickRemot = registraNick(serveiAux,nouNickRemot);
                  serveiAux.registerForCallback(callbackObjAux);

                  System.out.println("\n\t\t\t[QUESTION] "+serveiAux.returnServer());


                  String respostaFile = serveiAux.descargarContingutPart2(getCon,nouNickRemot);
                  if(!respostaFile.equals("")){
                    System.out.println("\n\t\t\t[DOWNLOAD] El fitxer ha set descargat correctament del client extern");
                  }else{
                    System.out.println("\n\t\t\t[ERROR] Error inesperat en la descarga: "+respostaFile);
                  }
                  serveiAux.unregisterForCallback(callbackObjAux,nouNickRemot);

                }

              }else if(local==true){
                // Moure de carpeta descargas a local

                if(moureFitxer(getCon)){
                  System.out.println("\n\t\t\t[DOWNLOAD] S'ha pogut descargar localment");

                }else{
                  System.out.println("\n\t\t\t[ERROR] No s'ha pogut descargar localment");

                }


              }else{
                System.out.println("\n\t\t\t[ERROR] Problemes al descargar el fitxer, tornau a intentar més tard. "+resposta);
              }
            }else if(!eleccion){
            }else{
              System.out.println("\n\t\t\t[ERROR] Problemes al descargar el fitxer, tornau a intentar més tard.");
            }

          }
        }

        // GESTIONAR CONTINGUT
        if (opcioTriada.equals("3")) {
      		System.out.println("\n\t\t[LLISTAT]: Llista del contingut propi.\n");
          int numLlista= serveiH.contingutMostrar();

          List<Continguts> llistaPropia = new ArrayList<Continguts>();
          int auxL = 0;
          for(int aux=0;aux<numLlista;aux++){
            Continguts auxC= serveiH.demanarContingut(aux);
            if(auxC.demanarNickUsuariPeli().equals(nickDelUsuari)){
              llistaPropia.add(auxC);
              auxL ++;
            }
          }

          if(auxL==0){
            System.out.println("\t\t\t[AVÍS]: No hi ha contigut propi.");
          }else{
            mostrarContingutsLlista(llistaPropia,nickDelUsuari,1);

            String opcioTriada2;
            System.out.println("\n\t\t\t[ACCIONS] Opcions \n\t\t\t\t1. Modificar\n\t\t\t\t2. Eliminar\n\t\t\t\t[*]. Tornar al menu principal");
            opcioTriada2 = entrarLinia.nextLine ();

            if (opcioTriada2.equals("1")) {
              // Modificar fitxers
              String fitxerTriat;
              boolean flag = true;
              boolean eleccion = false;
              do{
                System.out.println("\n\t\t\t[LLISTAT]: Llista del contingut propi que es vol MODIFICAR");
                mostrarContingutsLlista(llistaPropia,nickDelUsuari,1);
                System.out.println("\n\t\t\t[-1]. Cancelar");
                System.out.println("\n\t\t\t[INPUT] Número de fitxer a modificar?");
                int auxTriat = 0;
                fitxerTriat = entrarLinia.nextLine ();
                if(isNumeric(fitxerTriat)){
                  auxTriat = Integer.parseInt(fitxerTriat);
                  if((auxTriat+1)>0 && auxTriat<(llistaPropia.size())){
                    flag = false;
                    eleccion = true;
                  }else if(auxTriat==-1){
                    flag = false;
                  }else{
                    System.out.println("\n\t\t\t\t[ERROR] Número de fitxer no existeix");
                  }
                }else{
                  System.out.println("\n\t\t\t\t[ERROR] Número de fitxer no existeix");
                }
              }while(flag);

              if(eleccion){
                System.out.println("\n\t\t\t[INPUT] Quin es el nou nom del contingut?");
                String nouNom = entrarLinia.nextLine ();

                if(nouNom.equals("")){
                  System.out.println("\n\t\t\t[AVÍS] El nom no ha set modificat");
                }else{
                  if(serveiH.modificarContingut(nickDelUsuari,llistaPropia.get(Integer.parseInt(fitxerTriat)),nouNom)){
                    System.out.println("\n\t\t\t[UPDATE] El fitxer ha set modificat correctament");
                  }else{
                      System.out.println("\n\t\t\t[ERROR] Problemes al modificar el fitxer, tornau a intentar més tard.");
                  }

                }

              }else if(!eleccion){
              }else{
                System.out.println("\n\t\t\t[ERROR] Problemes al modificar el fitxer, tornau a intentar més tard.");
              }
            }else if(opcioTriada2.equals("2")){
              // Eliminar fitxers
              String fitxerTriat;
              boolean flag = true;
              boolean eleccion = false;
              do{
                System.out.println("\n\t\t\t[LLISTAT]: Llista del contingut propi que es vol ELIMINAR");
                mostrarContingutsLlista(llistaPropia,nickDelUsuari,1);
                System.out.println("\n\t\t\t[-1]. Cancelar");
                System.out.println("\n\t\t\t[INPUT] Número de fitxer a eliminar?");
                int auxTriat = 0;
                fitxerTriat = entrarLinia.nextLine ();
                if(isNumeric(fitxerTriat)){
                  auxTriat = Integer.parseInt(fitxerTriat);
                  if((auxTriat+1)>0 && auxTriat<(llistaPropia.size())){
                    flag = false;
                    eleccion = true;
                  }else if(auxTriat==-1){
                    flag = false;
                  }else{
                    System.out.println("\n\t\t\t\t[ERROR] Número de fitxer no existeix");
                  }
                }else{
                  System.out.println("\n\t\t\t\t[ERROR] Número de fitxer no existeix");
                }
              }while(flag);

              if(eleccion && serveiH.eliminarContingut(nickDelUsuari,llistaPropia.get(Integer.parseInt(fitxerTriat)))){
                System.out.println("\n\t\t\t[DELETE] El fitxer s'ha esborrat correctament.");
                // Eliminar de la carpeta local
              }else if(!eleccion){
              }else{
                System.out.println("\n\t\t\t[ERROR] Problemes al esborrar el fitxer, tornau a intentar més tard.");
              }

            }
          }

        }

        if (opcioTriada.equals("4")) {
          System.out.println("\n\t\t[OPCIÓ]: Pujar Contingut");
  	  		//ContingutsServerInterface pelicula = new ContingutsServerInterface();
  	  		Continguts pelicula = new Continguts();
  	  		pelicula.posarNickUsuariPeli(nickDelUsuari);
    		resposta = serveiH.contingutIniciar(pelicula);				//AtENCIÓ CREA la pelicula BUIDA
    		pelicula.posaridentificadorUNIC(resposta);					//ATENCIÓ --> GRABAR ID. UNIC

    	  pujarPeli(pelicula, resposta);								//OMPLIR les dades de la NOVA PELI
    		System.out.println("\t\t\t[Servidor][UPDATE]: "+resposta);	//Retorna OK

    		resposta = serveiH.contingutNotificar(pelicula);			//ATENCIÓ NOTIFICA la pelicula a tots els usuaris
    		System.out.println("\t\t\t[Servidor]: "+resposta);			//Retorna OK
        }




      } while (!opcioTriada.equals("0"));

      System.out.println("\n\t\t[OPCIÓ] Sortir, deixaras el registre per rebre callnack en 5 segons.");
      try {
      	Thread.sleep(5 * 1000);
      }
      catch (InterruptedException ex){ // sleep over
      }

      serveiH.unregisterForCallback(callbackObj,nickDelUsuari);
      System.out.println("\n\t\t\t[INFO] CLIENT TRET del registre per a rebre callback.");

    } catch (Exception e) {
      System.out.println("\n\t\t\t[INFO]Excepció en la crida de CallbackClient: " + e);
    } // end catch
    System.exit(1);

  }

  private static List<Continguts> jsonToContingutsList(String OutputFinal){
    if(OutputFinal==""){
      return null;
    }
    List<Continguts> auxListJson = new ArrayList<Continguts>();

    String[] strs = OutputFinal.split("(?<=\\})(?=\\{)");
    System.out.println("\tpas33");
    int i = 0;
    int reset = 8;
    for (String s : strs) {

        String[] split2 = s.split(",");

        StringBuilder sb = new StringBuilder();

        System.out.println("Contingut "+i+":");
        Continguts p = new Continguts();
        for (int k = 0; k < split2.length; k++) {

          String id, value;
          String[] split3 = split2[k].split(":");

          if((k % reset)==0){
            p = new Continguts();
            System.out.println("New Continguts");
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
            //System.out.println("\nContingut p: " + p.toString() );
            auxListJson.add(p);
          }



        }

//                System.out.println(sb.toString());

        //System.out.println("\nClient text_plain: " + strs[i] );

    }
    return auxListJson;
  }

	// Puc utilitzar la primera comunicació per REGISTRAR el nom del usuari, NICK i que sigui ÚNIC. ???
	// Sinó el torna ha demanar fins que sigui diferent als ja registrats.
  private static String entrarNick(CallbackServerInterface serveiHello) throws RemoteException {
  	Scanner entradaTexte = new Scanner (System.in); 			//Creación de un objeto Scanner
  	String nickUsuari = " ";
  	String secretUsuari = " ";
  	StringBuilder espai = new StringBuilder();
  	espai.append(" ");
    do {
    	System.out.println("\n\t\t[INPUT] Quin és el teu NICK?:  " );
  		nickUsuari = entradaTexte.nextLine (); 				//Invocamos un método sobre un objeto Scanner
  		if (nickUsuari.contains(espai)){
  			System.out.println("\t\tEl NICK conté espais <" + nickUsuari + "> i NO es ACCEPTABLE.");
  		}
  		//else{System.out.println("\t\tEl NICK NO conté espais i es: <" + nickUsuari + ">.");}
    } while (nickUsuari.contains(espai));						// AFEGIR comprovació que en el nick no hi ha espais

    do {
      	System.out.println("\n\t\t[INPUT] Quina és la contrasenya?:  " );
  		secretUsuari = entradaTexte.nextLine (); 				//Invocamos un método sobre un objeto Scanner
  		if (secretUsuari.contains(espai)){
  			System.out.println("\t\tLa CONTRASENYA conté espais <" + secretUsuari + "> i NO es ACCEPTABLE.");
  		}
  		//else{System.out.println("\t\tEl NICK NO conté espais i es: <" + nickUsuari + ">.");}
    } while (secretUsuari.contains(espai));						// AFEGIR comprovació que en la contrasenya no hi ha espais

    String nickOK = serveiHello.sayHello(nickUsuari, secretUsuari);		    // invoke the REMOTE method sayHello(nickUsuari)
    boolean nickCorrecte = nickUsuari.equals(nickOK);


    if (!nickCorrecte) {
      System.out.println("\n\t[INFO]: Aquest nick pertany a un usuari ja REGISTRAT.");
      return "";
    } else {
    }
    return nickOK;
  }

  private static String registraNick(CallbackServerInterface serveiHello,String nick) throws RemoteException {

    String nickOK = serveiHello.sayHello(nick, "123");		    // invoke the REMOTE method sayHello(nickUsuari)
    boolean nickCorrecte = nick.equals(nickOK);


    if (!nickCorrecte) {
      System.out.println("\n\t[INFO]: Aquest nick pertany a un usuari ja REGISTRAT.");
      return "";
    } else {
    }
    return nickOK;
  }



  private static boolean crearCarpeta(String id,String namePath, Continguts peli ){
    File miDir = new File (".");
    try {
      // Existeix el contingut???
      Path moveFrom = FileSystems.getDefault().getPath(namePath);
      if (Files.exists(moveFrom)) {
        File moveFile = new File (namePath);
        boolean success = (new File("./continguts/"+id)).mkdirs();
        if (success) {


          Path moveTo = FileSystems.getDefault().getPath("./continguts/"+id+"/"+moveFile.getName());
          System.out.println ("\t\t\t*Directori actual: " + moveFrom);
          System.out.println ("\t\t\t*Directori destí: " + moveTo);
          Files.copy(moveFrom, moveTo, StandardCopyOption.REPLACE_EXISTING);

          File finalFile = moveTo.toFile();
          peli.setNameFile(finalFile.getName());
          peli.setLocFile(finalFile.getAbsolutePath());
          return true;
        }
        return false;

      }else{
        System.out.println ("\n\t\t\t[Error]: No existe contingut en aquesta ubicació: "+moveFrom.toString());
        return false;
      }

		} catch(Exception e) {
			e.printStackTrace();
			}
		return false;
  	}

  private static void pujarPeli(Continguts laPelicula, String idPeli) throws RemoteException {
    //private static void pujarPeli(ContingutsServerInterface laPelicula, String idPeli) throws RemoteException {
    // AL SERVIDOR: CREAR un identificador UNIC per a cada parell NICK-Película per a que pugui identificar UNIVOCAMENT
    // cada contingut pujat al SERVIDOR, i evitar que el mateix USUARI pugui pujar la mateixa película 2 cops.
    Scanner entrarLinia = new Scanner (System.in); 					//Creación de un objeto Scanner
    System.out.println("\t\t\t[INFO]: "+laPelicula.demanarNickUsuariPeli()+" comença a pujar contingut...");

    System.out.println("\t\t\t[INPUT]: Titol? ");
    String titolDeLaPeli = entrarLinia.nextLine ();					//Invocamos un método sobre un objeto Scanner
    laPelicula.posarTitolPeli(titolDeLaPeli);

  	while(true){
  		//System.out.println("\t\t\tidPeli ... "+idPeli);
  		System.out.println("\t\t\t[INPUT]: Ubicació del contingut? (ruta relativa)");
  		//System.out.println("\t\t\tsi és al directori actual <Nom.term> " + laPelicula.demanarNickUsuariPeli());
  		String ubicacioDeLaPeli = entrarLinia.nextLine (); 			//Invocamos un método sobre un objeto Scanner

      if(crearCarpeta(idPeli,ubicacioDeLaPeli,laPelicula)){
        laPelicula.posarUbicacioPeli(ubicacioDeLaPeli);
        //laPelicula.setIPFile(InetAddress.getLocalHost().getHostAddress());
  			//System.out.println("\t\t\tContingut COPIAT correctament");
  	   		break;
  		}else{
  	   		//System.out.println("\t\t\tError: no s'ha pogut TROBAR el contingut, torna a intentar-ho.");
  	   		//break;
  		}
  	//resposta = serveiH.contingutPujar(pelicula);
    }

    String valoracioDeLaPeli = "7";
  	laPelicula.puntuarPeli(Integer.parseInt(valoracioDeLaPeli));
  }

  private static void pantallaEstatActual() throws RemoteException {
  }
   // Crear una HashTable per a tenir els continguts de la BASE de DADES amb possible ordenació per Nick, per Titol ???
   // HashMap no és sincronitzable i per tant no és indicat per aplicacions amb multiprocés (molts clients al mateix temps)
   private Enumeration names;
   private String key;
   // Creating a Hashtable
   private Hashtable<String, String> hashtable = new Hashtable<String, String>();

   public static void mostrarContingutsLlista(List<Continguts> l, String nickDelUsuari, int type){
     int auxL =0;
     for(int aux=0;aux<l.size();aux++){
       Continguts auxC= l.get(aux);
       if(type==2 && auxC.demanarNickUsuariPeli().equals(nickDelUsuari)){
         System.out.println("\t\t\t" + auxL+". "+auxC.demanarTitolPeli());
       }
       if(type == 1 && nickDelUsuari.equals(auxC.demanarNickUsuariPeli())){
         System.out.println("\t\t\t" + auxL+". [P] "+auxC.demanarTitolPeli());
       }
       if(type == 1 && !(nickDelUsuari.equals(auxC.demanarNickUsuariPeli()))){
         System.out.println("\t\t\t" + auxL+". "+auxC.demanarTitolPeli());
       }

       if(type == 4){
         System.out.println("\t\t\t" + auxL+". [EXTERN]"+auxC.demanarTitolPeli());
       }
       auxL++;
     }

   }

   public static void mostrarContingutsLlistaDoble(List<Continguts> l,List<Continguts> m, String nickDelUsuari, int type){
     int auxL =0;
     for(int aux=0;aux<l.size();aux++){
       Continguts auxC= l.get(aux);
       if(type==2 && auxC.demanarNickUsuariPeli().equals(nickDelUsuari)){
         System.out.println("\t\t\t" + auxL+". "+auxC.demanarTitolPeli());
       }
       if(type == 1 && nickDelUsuari.equals(auxC.demanarNickUsuariPeli())){
         System.out.println("\t\t\t" + auxL+". [P] "+auxC.demanarTitolPeli());
       }
       if(type == 1 && !(nickDelUsuari.equals(auxC.demanarNickUsuariPeli()))){
         System.out.println("\t\t\t" + auxL+". "+auxC.demanarTitolPeli());
       }
       auxL++;
     }
     for(int aux=0;aux<m.size();aux++){
       Continguts auxC= m.get(aux);
       System.out.println("\t\t\t" + auxL+". [EXTERN]"+auxC.demanarTitolPeli());
       auxL++;
     }
   }

   private static boolean moureFitxer(Continguts peli){
     File miDir = new File (".");

     try {

       Path moveFrom = FileSystems.getDefault().getPath("./continguts/"+peli.demanaridentificadorUNIC()+"/"+peli.getNameFile());
       Path moveTo = FileSystems.getDefault().getPath("./descargues/"+peli.getNameFile());

       // Existeix el contingut???
       Path moveDefault = FileSystems.getDefault().getPath("./descargues/");

       if (!Files.exists(moveDefault)) {
         boolean success = (new File("./descargues/")).mkdirs();
       }

       if (Files.exists(moveFrom)) {

         System.out.println ("\t\t\t*Directori actual: " + moveFrom);
         System.out.println ("\t\t\t*Directori destí: " + moveTo);

         Files.copy(moveFrom, moveTo, StandardCopyOption.REPLACE_EXISTING);

         return true;

       }else{

         System.out.println ("\n\t\t\t[Error]: No existe contingut en aquesta ubicació: "+moveFrom.toString());
         return false;
       }
     } catch(Exception e) {
       e.printStackTrace();
     }

     return false;
   }



   public static boolean isNumeric(String str)
  {
    try
    {
      double d = Double.parseDouble(str);
    }
    catch(NumberFormatException nfe)
    {
      return false;
    }
    return true;
  }




}//end class
