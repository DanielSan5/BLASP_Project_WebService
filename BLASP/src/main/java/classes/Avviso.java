  package classes;

public class Avviso {
	
	String descrizione;
	String email_avvisatore;
	Ticket ticket_info;
	/**
	 * @param descrizione
	 * @param email_avvisatore
	 * @param ticket_info
	 */
	public Avviso(String descrizione, String email_avvisatore, Ticket ticket_info) {
		this.descrizione = descrizione;
		this.email_avvisatore = email_avvisatore;
		this.ticket_info = ticket_info;
	}
	
	public String getDescrizione() {
		return descrizione;
	}
	public String getEmail_avvisatore() {
		return email_avvisatore;
	}
	public Ticket getTicket_info() {
		return ticket_info;
	}

	
	

}
