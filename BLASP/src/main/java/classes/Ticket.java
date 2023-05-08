package classes;

public class Ticket {

	private int numero_ticket;
	private String data_cr;
	private String stato;
	private String materia;
	private String tags;
	private String descrizione;
	private Utente user_info;
	
	
	public Ticket(int numero_ticket, String data_cr, String stato, String materia, String tags, String descrizione) {
		
		this.numero_ticket = numero_ticket;
		this.data_cr = data_cr;
		this.stato = stato;
		this.materia = materia;
		this.tags = tags;
		this.descrizione = descrizione;
	}

	public Ticket(int numero_ticket, String stato, String materia, String tags, String descrizione, Utente user_info) {
		
		this.numero_ticket = numero_ticket;
		this.stato = stato;
		this.materia = materia;
		this.tags = tags;
		this.descrizione = descrizione;
		this.user_info = user_info;
	}

	public Utente getUser_info() {
		return this.user_info;
	} 
	
	public String getStato() {
		return this.stato;
	}


	public String getMateria() {
		return this.materia;
	}


	public String getTags() {
		return this.tags;
	}


	public String getDescrizione() {
		return this.descrizione;
	}

	public int getNumero_ticket() {
		return numero_ticket;
	}

	public String getData_cr() {
		return data_cr;
	}
	
	
	
}
