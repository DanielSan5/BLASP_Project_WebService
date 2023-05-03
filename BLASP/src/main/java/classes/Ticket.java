package classes;

public class Ticket {

	
	private String stato;
	private String materia;
	private String tags;
	private String descrizione;
	private int  UT_id_apertura;
	private int UT_id_accettazione;
	
	public Ticket(String stato, String materia, String tags, String descrizione, int UT_id_apertura, int UT_id_accettazione) {
		this.stato = stato;
		this.materia = materia;
		this.tags = tags;
		this.descrizione = descrizione;
		this.UT_id_accettazione = UT_id_apertura;
		this.UT_id_apertura = UT_id_accettazione;
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


	public int getUT_id_apertura() {
		return this.UT_id_apertura;
	}


	public int getUT_id_accettazione() {
		return this.UT_id_accettazione;
	}


	@Override
	public String toString() {
		return "Ticket [stato=" + this.stato + ", materia=" + this.materia + ", tags=" + this.tags + ", descrizione=" + this.descrizione
				+ ", UT_id_apertura=" + this.UT_id_apertura + ", UT_id_accettazione=" + this.UT_id_accettazione + "]";
	}

	
	
	
}
