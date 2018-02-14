import java.io.OutputStream.*;
import java.io.Serializable;

/*
 * @author Robert Pedros i Roger Orellana
 */
public class Clients implements Serializable {

	public int id;
	public String nick;
	public int files;
	private String pwd;
	private int download;
	private int upload;
	private String lastIp;
	private int online;



}
