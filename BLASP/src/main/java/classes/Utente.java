package classes;

public class Utente {

	private String nome;
	private String cognome;
	private int classe;
	private String indirizzo;
	private String descrizione;
	private String data_nascita;
	private String localita;
	private int num_segnalazioni;
	private boolean admin;
	
	public Utente(String nome, String cognome, int classe, String indirizzo, String descrizione, String data_nascita,
			String localita, boolean admin) {
		this.nome = nome;
		this.cognome = cognome;
		this.classe = classe;
		this.indirizzo = indirizzo;
		this.descrizione = descrizione;
		this.data_nascita = data_nascita;
		this.localita = localita;
		this.admin = admin;
	}
	
	public Utente(String nome, String cognome, int classe, String indirizzo, String descrizione, String data_nascita,
			String localita, int num_segnalazioni) {
		this.nome = nome;
		this.cognome = cognome;
		this.classe = classe;
		this.indirizzo = indirizzo;
		this.descrizione = descrizione;
		this.data_nascita = data_nascita;
		this.localita = localita;
		this.num_segnalazioni = num_segnalazioni;
	}

	
	
	public String getNome() {
		return nome;
	}



	public String getCognome() {
		return cognome;
	}



	public int getClasse() {
		return classe;
	}



	public String getIndirizzo() {
		return indirizzo;
	}



	public String getDescrizione() {
		return descrizione;
	}



	public String getData_nascita() {
		return data_nascita;
	}



	public String getLocalita() {
		return localita;
	}

	

	public int getNum_segnalazioni() {
		return num_segnalazioni;
	}

	public boolean isAdmin() {
		return admin;
	}

	@Override
	public String toString() {
		return "Utente [nome=" + nome + ", cognome=" + cognome + ", classe=" + classe + ", indirizzo=" + indirizzo
				+ ", descrizione=" + descrizione + ", data_nascita=" + data_nascita + ", localita=" + localita + "]";
	}
	
	

	
	
}
