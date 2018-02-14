import java.rmi.*;
import java.rmi.server.*;
import java.io.*;
import java.net.*;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import static java.nio.file.StandardCopyOption.*;
import java.net.*;
import static java.lang.System.out;

/**
 * @author Robert Pedros i Roger Orellana
 *
 */


public class CallbackClientImpl extends UnicastRemoteObject implements CallbackClientInterface{


   public CallbackClientImpl() throws RemoteException {
      super( );
   }

   public String notifyMe(String message){
     String returnMessage = "\n\t[NOTIFICACIÓ]:\n\t"+message+"\n";
     System.out.println(returnMessage);
     return returnMessage;
   }

   public byte[] iniciarUpload(Continguts a, String nick, CallbackServerInterface server,CallbackClientInterface h){
     // Enviar file
     Path moveFrom = FileSystems.getDefault().getPath(a.getLocFile());
     System.out.println("\n\t[UPLOAD] iniciarUpload()");
     if (Files.exists(moveFrom)) {
       String path = moveFrom.toString();
       File userFile = new File(a.getLocFile());
       byte buffer[] = new byte[(int) userFile.length()]; //Server converts file into an array of bytes to be sent

       try {
         BufferedInputStream input = new BufferedInputStream(new FileInputStream(path));
         input.read(buffer, 0, buffer.length);
         input.close();
         String returnMessage = "\n\t\t[NOTIFICACIÓ UPLOAD]: Fitxer '"+userFile.getName()+"' enviat";

         System.out.println(returnMessage);
         String resposta = server.enviarContingutCAllback(buffer,nick,a.getNameFile());
         System.out.println("\n\t\t\t[CLIENT AMB ARXIU]: Fi del Upload");
         return (buffer); //Server sends the array of bytes
       } catch (IOException e) {
         System.out.println("Error!");
         return new byte[0];
       }
     } else {
       System.out.println("\n\tNo existeix el fitxer: "+moveFrom.toString());
       return new byte[0];
     }
  }

  public String downloadFile(byte[] a,String nameFile){
    Path moveFrom = FileSystems.getDefault().getPath("./descargues/"+nameFile);

    Path moveDefault = FileSystems.getDefault().getPath("./descargues/");

    if (!Files.exists(moveDefault)) {
      boolean success = (new File("./descargues/")).mkdirs();
    }

    System.out.println("\n\t\t[DOWNLOAD] downloadFile()");

      try {
        String path = moveFrom.toString();
        FileOutputStream FOS = new FileOutputStream(path);
        BufferedOutputStream Output = new BufferedOutputStream(FOS);
        Output.write(a, 0, a.length);
        Output.flush();
        Output.close();
        System.out.println("\n\tArxiu descargat correctament");
        return "OK";
      } catch (IOException e) {
        System.out.println("Error!" + e.getMessage());
      }

    return "";
  }

}// end CallbackClientImpl class
