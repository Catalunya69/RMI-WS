import java.rmi.*;

/**
 * @author Robert Pedros i Roger Orellana Castells
*/

public interface CallbackServerInterface extends Remote {

  // Per a realitzar el REGISTRE i comprovar la CONTRASENYA
  public String sayHello(String name, String pass) throws java.rmi.RemoteException;

  // Per a GESTIONAR els CallBack's
  public void registerForCallback(CallbackClientInterface callbackClientObject) throws java.rmi.RemoteException;
  public void unregisterForCallback(CallbackClientInterface callbackClientObject, String nick) throws java.rmi.RemoteException;

  // Per a GESTIONAR els CONTINGUTS
  public String contingutIniciar(Continguts pelicula) throws java.rmi.RemoteException;
  public String contingutPujar(Continguts pelicula) throws java.rmi.RemoteException;
  public int contingutMostrar() throws java.rmi.RemoteException;
  public boolean eliminarContingut(String nick, Continguts peli) throws java.rmi.RemoteException;
  public boolean modificarContingut(String nick, Continguts peli, String nouNom) throws java.rmi.RemoteException;
  public String descargarContingut(String nick, Continguts peli) throws java.rmi.RemoteException;
  public String descargarContingutPart2(Continguts peli,String nick) throws java.rmi.RemoteException;

  // Per a GESTONAR la TRANSFERÈNCIA de CONTINGUTS entre CLIENTS, possedor del contingut
  public String enviarContingutCAllback(byte[] buffer, String a, String b) throws java.rmi.RemoteException;
  public String enviarContingut(byte[] buffer, String a, CallbackClientInterface h) throws java.rmi.RemoteException;
  // Per a GESTONAR la TRANSFERÈNCIA de CONTINGUTS entre CLIENTS, solicitant del contingut
  public Continguts demanarContingut(int index) throws java.rmi.RemoteException;
  public String contingutNotificar(Continguts pelicula) throws java.rmi.RemoteException;

  // Per a les ACCIONS del Web Service
  public int numContingutsWS(String nick) throws java.rmi.RemoteException;
  public Continguts contingutMostrarWS(String nick,int index) throws java.rmi.RemoteException;
  public String descargarContingutWS(String nick, Continguts peli) throws java.rmi.RemoteException;
  public String returnServer() throws java.rmi.RemoteException;

  //public String accions_WS_PUT(String id,String urlBase, String jsonInput);
  //public String accioWS_GET(String nickDelUsuari) throws java.rmi.RemoteException;
}
