import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Servidor {
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String ip;
    @XmlAttribute
    private int puerto;
    @XmlAttribute
    private int files;
    @XmlAttribute
    private int online;

    public Servidor(String name, String ip, int puerto) {
    	this.files = 0;
    	this.online = 1;
    	this.name = name;
    	this.ip = ip;
    	this.puerto = puerto;
    }

    public String getName() {
        return name;
    }

    public void setName(String nombre) {
        this.name = nombre;
    }

    public int  getFiles() {
        return files;
    }

    public void setFiles(int files) {
        this.files = files;
    }

    public int  getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public String  getIP() {
        return ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    public int  getPort() {
        return puerto;
    }

    public void setPort(int puerto) {
        this.puerto = puerto;
    }


    @Override
    public String toString() {
        return "User{" +
                  "nombre='" + name + '\'' +
                   "ip='" + ip + ':' + puerto + '\'' +
                  ", files='" + files + '\'' +

                  '}';
    }
    public String json(){
      //return "{\"name\":\""+name+"\",\"ip\":\""+ip+"\",\"puerto\":"+puerto+",\"online\":"+online+",\"files\":"+files+"}";
      return "{\"name\":\""+name+"\",\"ip\":\""+ip+"\",\"puerto\":"+puerto+"}";

    }

}
