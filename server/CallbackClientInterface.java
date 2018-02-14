import java.rmi.*;

/**
 * @author Robert Pedros i Roger Orellana
 */

public interface CallbackClientInterface extends Remote{

  public String notifyMe(String message) throws java.rmi.RemoteException;
  public byte[] iniciarUpload(Continguts a, String nick, CallbackServerInterface servei,CallbackClientInterface h ) throws java.rmi.RemoteException;
  public String downloadFile(byte[] a,String nameFile) throws java.rmi.RemoteException;

}
