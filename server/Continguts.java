import java.io.OutputStream.*;
import java.io.Serializable;

/*
 * @author Robert Pedros i Roger Orellana
 */
public class Continguts implements Serializable {

	public String identificadorUNIC;
	public String nickUsuari;
	public String titolPelicula;
	private String ubicacioPelicula;
	private String ubicacioTotalPelicula;
	private String nomFitxer;
	private String ipUbicacio;
	private int puntuacioPelicula;
	public String ipServidor;
	public String portServidor;

	public String getipServidor() { return ipServidor; }
	public void setipServidor(String ipServidor) {this.ipServidor = ipServidor;}
	public String getportServidor() { return portServidor; }
	public void setportServidor(String portServidor) {this.portServidor = portServidor;}


	public String demanaridentificadorUNIC() { return identificadorUNIC; }
	public void posaridentificadorUNIC(String identificadorUNIC) {this.identificadorUNIC = identificadorUNIC;}

	public String demanarNickUsuariPeli() { return nickUsuari; }
	public void posarNickUsuariPeli(String nickUsuari) {this.nickUsuari = nickUsuari;}

	public String demanarTitolPeli() {	return titolPelicula; }
	public void posarTitolPeli(String titolPelicula) {this.titolPelicula = titolPelicula;}

	public String demanarUbicacioPeli() { return ubicacioPelicula; }
	public void posarUbicacioPeli(String ubicacioPelicula) {this.ubicacioPelicula = ubicacioPelicula;}

	public int valoracioPeli() { return puntuacioPelicula; }
	public void puntuarPeli(int puntuacioPelicula) {this.puntuacioPelicula = puntuacioPelicula;}

	public String getLocFile() { return this.ubicacioTotalPelicula; }
	public String getNameFile() { return this.nomFitxer; }
	public String getIPFile() { return this.ipUbicacio; }
	public void setLocFile(String text) {	this.ubicacioTotalPelicula = text.replace("./", "");	}
	public void setNameFile(String text) {this.nomFitxer = text;}
	public void setIPFile(String text) {this.ipUbicacio = text;}



	public void updateContingut(Continguts aux) {
		this.nickUsuari = aux.demanarNickUsuariPeli();
		this.titolPelicula = aux.demanarTitolPeli();
		this.ubicacioPelicula = aux.demanarUbicacioPeli();
		this.puntuacioPelicula = aux.valoracioPeli();
		this.ubicacioTotalPelicula = aux.getLocFile();

		this.nomFitxer = aux.getNameFile();
		this.ipUbicacio = aux.getIPFile();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\t\t[CONTINGUT]");
        sb.append("\n\t\t\tID: ");
        sb.append(identificadorUNIC);
        sb.append("\n\t\t\tTitol: ");
        sb.append(titolPelicula);
        sb.append("\n\t\t\tContingut pujat per: ");
        sb.append(nickUsuari);
        sb.append("\n\t\t\tNom del fitxer: ");
        sb.append(nomFitxer);
        sb.append("\n\t\t\tUbicació del fitxer: ");
        sb.append(ubicacioTotalPelicula);
        sb.append("\n\t\t\tIP del fitxer: ");
        sb.append(ipServidor+" : "+portServidor);
        return sb.toString();
	}

	public String json(){

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("\"id\":\""+identificadorUNIC+"\",");
		sb.append("\"titulo\":\""+titolPelicula+"\",");
		sb.append("\"ubicacioPelicula\":\""+ubicacioPelicula+"\",");
		sb.append("\"ubicacioTotalPelicula\":\""+ubicacioTotalPelicula+"\",");
		sb.append("\"nomFitxer\":\""+nomFitxer+"\",");
		sb.append("\"ipUbicacio\":\""+ipUbicacio+"\",");
		sb.append("\"ipServidor\":\""+ipServidor+"\",");
		sb.append("\"portServidor\":\""+portServidor+"\"");
		sb.append("}");
		return sb.toString();

	}


}
