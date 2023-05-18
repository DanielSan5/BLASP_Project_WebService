package classes;

public class Segnalazione {

	String descrizione;
	String email_segnalatore;
	
	/**
	 * @param descrizione
	 * @param email_avvisatore
	 * @param ticket_info
	 */
	public Segnalazione(String descrizione, String email_segnalatore) {
		this.descrizione = descrizione;
		this.email_segnalatore = email_segnalatore;
	}
	
	public String getDescrizione() {
		return descrizione;
	}
	public String getEmail_segnalatore() {
		return email_segnalatore;
	}
	
}
