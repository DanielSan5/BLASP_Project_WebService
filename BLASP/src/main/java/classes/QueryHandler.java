package classes;

import java.rmi.NoSuchObjectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.security.auth.login.CredentialNotFoundException;

import com.mysql.jdbc.exceptions.MySQLDataException;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

public class QueryHandler {
	
	private static String db_url = "jdbc:mysql://localhost:3306/ticketing";
    private static String db_driver = "com.mysql.jdbc.Driver";
    private static String db_user = "root";
    private static String db_password = "";
    private Connection conn;

	public QueryHandler() {
		
		try {
			Class.forName(db_driver);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	private void establishConnection() {
		
		try{
			this.conn = DriverManager.getConnection(db_url, db_user, db_password); 
		}catch(SQLException e){
			System.err.println(e.getLocalizedMessage());
		}
		
	}
	
	public boolean hasEmail(String email) throws SQLException {

		
		establishConnection();
		String prepared_query = "SELECT * FROM utenti WHERE UT_email = ?";
	
		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
		pr.setString(1, email);
		ResultSet res = pr.executeQuery();
		//per controllare se l'email esiste basta vedere il risultato di next(), sar� false se non esistono righe
		boolean check = res.next();
		conn.close();
		return check; 
		
	}
	
	public boolean checkPass(int user_id, String password) throws SQLException, CredentialNotFoundException {
		
		establishConnection();
		String prepared_query = "SELECT UT_password FROM utenti WHERE UT_id = ?";
		Argon2 argon2 = Argon2Factory.create(Argon2Types.ARGON2id);

		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);

		pr.setInt(1, user_id);
		ResultSet res = pr.executeQuery();
		
		if(res.next()) {
			

			String hashedPass = res.getString("UT_password");
			conn.close();
			
			if(argon2.verify(hashedPass, password)){
				return true;
			}else {
				System.out.println("password non verificata");
				return false;
			}
			
		}else {
			conn.close();
			throw new CredentialNotFoundException("no results");
		}
		
	}
	
	//RECUPERO USER ID DALLA MAIL
	public int getUserId(String email) throws SQLException, CredentialNotFoundException {
		
		establishConnection();
		String prepared_query = "SELECT UT_id FROM utenti WHERE UT_email = ?";
		
		
		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
		
		
		pr.setString(1, email);
		ResultSet res = pr.executeQuery();
		if(res.next()) {
			
			int user_id = res.getInt("UT_id");
			conn.close();
			return user_id;
			
		}else {
			conn.close();
			throw new CredentialNotFoundException("no results in getUserId");
		}
			
			
			
	}
		

	
	public void inserisciUtente(String email, String password, String nome, String cognome, String data_nascita, int classe, String indirizzo_scolastico, String localita) throws SQLException {
		
		establishConnection();
		String prepared_query = "INSERT INTO utenti (UT_email, UT_password, UT_nome, UT_cognome, UT_data_nascita, UT_classe, UT_indirizzo_scolastico, UT_localita) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		
	
		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
	
		//pr.setString(1, username);
		pr.setString(1, email);
		pr.setString(2, password);
		pr.setString(3, nome);
		pr.setString(4, cognome);
		pr.setString(5, data_nascita);
		pr.setInt(6, classe);
		pr.setString(7, indirizzo_scolastico);
		pr.setString(8, localita);
		
		//executeUpdate returna  il numero di righe create o aggiornate, quindi se returna 0 non ha inserito/aggiornato nessuna riga
		
		if(pr.executeUpdate() != 1) {
			conn.close();
			throw new MySQLDataException("could not create row in utenti");
		}
		conn.close();
		
		
			
			
		
	}
	
	//***UPDATE DATI UTENTE***
	public void modificaDatiUtente(int user_id, String descrizione, String localita, int classe, String indirizzo) throws SQLException {
		
		establishConnection();
		String prepared_query = "UPDATE utenti SET UT_descrizione = ?, UT_localita = ?, UT_classe = ?, UT_indirizzo_scolastico = ? WHERE UT_id = ?";		

		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
		
		
		pr.setString(1, descrizione);
		pr.setString(2, localita);
		pr.setInt(3, classe);
		pr.setString(4, indirizzo);
		pr.setInt(5, user_id);

		//executeUpdate returna  il numero di righe create o aggiornate, quindi se returna 0 non ha inserito/aggiornato nessuna riga

		if(pr.executeUpdate() != 1) {
			conn.close();
			throw new MySQLDataException("could not update table utenti");
		}
		conn.close();
		
		
	}
	
	//***UPDATE PASSWORD UTENTE***
	public void changePass(int user_id, String password_encr) throws SQLException {
			
			establishConnection();
			String prepared_query = "UPDATE utenti SET UT_password = ? WHERE UT_id = ?";		
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);

			pr.setString(1, password_encr);
			pr.setInt(2, user_id);
			
			//executeUpdate returna  il numero di righe create o aggiornate, quindi se returna 0 non ha inserito/aggiornato nessuna riga
			
			if(pr.executeUpdate() != 1) {
				conn.close();
				throw new MySQLDataException("could not update table utenti");
			}
			conn.close();
			
				
			
			
		}
	
	//***BLOCCAGGIO UTENTE***
	public void blockUser(int user_id) throws  SQLException {
				
		establishConnection();
		String prepared_query = "UPDATE utenti SET UT_stato = 'blocked' WHERE UT_id = ?";		
	
		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
		
		
		pr.setInt(1, user_id);
		
		//executeUpdate returna  il numero di righe create o aggiornate, quindi se returna 0 non ha inserito/aggiornato nessuna riga
		
		if(pr.executeUpdate() != 1) {
			conn.close();
			throw new MySQLDataException("could not update table utenti");
		}
		conn.close();
				
	
			
				
	}
			
	//Controllo se la località inserita esiste
	public boolean hasLocalita(String localita) throws SQLException {
		
		establishConnection();
		String prepared_query = "SELECT * FROM localita WHERE LC_descrizione = ?";

		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);

		pr.setString(1, localita);
		
		ResultSet res = pr.executeQuery();
		//per controllare se la località esiste basta vedere il risultato di next(), sar� false se non esistono righe
		boolean check = res.next();
		conn.close();
		
		return check; 
		
		
		
	}
	
	//Controllo se l'indirizzo di studio inserito esiste
	public boolean hasIndirizzo(String indirizzo) throws SQLException {
			
		establishConnection();
		String prepared_query = "SELECT * FROM indirizzo_scolastico WHERE INS_nome = ?";
		
	
		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
		
		
		pr.setString(1, indirizzo);
		
		ResultSet res = pr.executeQuery();
		
		boolean check = res.next();
		
		conn.close();
		return check ; //se check true returna 1 altrimenti 0

		
			
	}
	
	//Controllo lo stato dell'utente --> DA CONFERMARE
	public String getUserStatus(int user_id) throws CredentialNotFoundException, SQLException {
		
		establishConnection();
		String prepared_query = "SELECT UT_stato FROM utenti WHERE UT_id = ?";
		
		
		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
	
		
		pr.setInt(1, user_id);
		
		ResultSet res = pr.executeQuery();
		
		if(res.next()) {
	
			String user_status = res.getString("UT_stato");
			conn.close();
			return user_status;

		}else {
			conn.close();
			throw new CredentialNotFoundException("no results in getUserStatus");
		}
			
		
		
		
	}

	//CONTROLLO SE UNA MATERIA ESISTE NEL DATABASE
	public boolean checkExistMateria(String materia) throws SQLException {
		
		establishConnection();
		String prepared_query = "SELECT MAT_nome FROM materia WHERE MAT_nome = ?";
		

		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
	
		pr.setString(1, materia);
		ResultSet res = pr.executeQuery();
		//per controllare se l'username esiste basta vedere il risultato di next(), sarà false se non esistono righe
		boolean check = res.next();
		
		conn.close();
		return check; 
	
	
		
	}

	public Utente getUserData(int user_id) throws CredentialNotFoundException, SQLException {


		
		establishConnection();
		
		String getUser = "SELECT * FROM utenti WHERE UT_id = ?";
		ResultSet res;
		
		
	
		java.sql.PreparedStatement getUser_query = conn.prepareStatement(getUser);
	
		
		
			
		getUser_query.setInt(1, user_id);
		res = getUser_query.executeQuery();
		
		if(res.next()) {
			
			Utente user_info = new Utente(res.getString("UT_nome"), res.getString("UT_cognome"), res.getInt("UT_classe"), 
					res.getString("UT_indirizzo_scolastico"), res.getString("UT_descrizione"), res.getString("UT_data_nascita"), res.getString("UT_localita"), res.getBoolean("UT_admin"));
			conn.close();
			return user_info;	
		}
		else
			throw new CredentialNotFoundException("utente non esistente");
			
						
					
				
				
	
		
	}
	
	public ArrayList<Ticket> getUserTickets(int user_id) throws SQLException {
		
		establishConnection();
		
		String getUser = "SELECT * FROM tickets WHERE UT_id_apertura = ?";
		ResultSet res;
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		
	
		java.sql.PreparedStatement getUser_query = conn.prepareStatement(getUser);
		
		getUser_query.setInt(1, user_id);
		res = getUser_query.executeQuery();
		
		while(res.next()) {
			
			Ticket ticket = new Ticket(res.getInt("TIC_id"), res.getString("TIC_data_creazione"), res.getString("TIC_stato"), 
					res.getString("TIC_materia"), res.getString("TIC_tags"), res.getString("TIC_descrizione"));
			
			tickets.add(ticket);
			
		}
		
		return tickets;
		

		
	}

	public boolean isUserAdmin(int user_id) throws CredentialNotFoundException, SQLException {
		
		establishConnection();
		String prepared_query = "SELECT UT_admin FROM utenti WHERE UT_id = ?";
		

		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
	
		
		pr.setInt(1, user_id);
		ResultSet res = pr.executeQuery();
		
		if(res.next()) {
			
			if(res.getInt("UT_admin") == 1) {
				conn.close();
				return true;
			}else {
				conn.close();
				return false;
			}
			
		}else {
			conn.close();
			throw new CredentialNotFoundException("no results");
		}
	
		
	}
	
	public ArrayList<Utente> getToBlock() throws CredentialNotFoundException, SQLException{

		
		establishConnection();
		String prepared_query = "SELECT COUNT(*) as num_flags, u.UT_email, u.UT_nome, u.UT_cognome"
				+ " FROM utenti u INNER JOIN segnalazione s ON u.UT_id = s.UT_id_segnalato GROUP BY u.UT_id";
		ArrayList<Utente> toBlock = new ArrayList<Utente>();
		
		
		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
	
		
		
		ResultSet res = pr.executeQuery();
		
		while(res.next()) {
			
			Utente ut = new Utente(res.getString("UT_nome"), res.getString("UT_cognome"), res.getInt("UT_classe"), 
					res.getString("UT_indirizzo_scolastico"), res.getString("UT_descrizione"), res.getString("UT_data_nascita"), res.getString("UT_localita"), res.getInt("num_flags"));
			
			toBlock.add(ut);
			
		}
		if(toBlock.isEmpty()) {
			throw new CredentialNotFoundException("no results");
		}else {
			return toBlock;
		}
	
		
	}
	
	public void inserisciCodice(int user_id, String ver_code) throws SQLException {
		
		establishConnection();
		String prepared_query = "UPDATE utenti SET UT_ver_code = ? WHERE UT_id = ?";
			
		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
	
		pr.setString(1, ver_code);
		pr.setInt(2, user_id);
		
		//executeUpdate returna  il numero di righe create o aggiornate, quindi se returna 0 non ha inserito/aggiornato nessuna riga
		if(pr.executeUpdate() != 1) {
			conn.close();
			throw new MySQLDataException("could not create row in utenti");
		}
		conn.close();
	
	}
	
	public boolean checkCode(int user_id, String ver_code) throws SQLException, CredentialNotFoundException {
		
		establishConnection();
		String prepared_query = "SELECT UT_ver_code FROM utenti WHERE UT_id = ?";
		

		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);

		pr.setInt(1, user_id);
		ResultSet res = pr.executeQuery();
		
		if(res.next()) {
			
			if(res.getString("UT_ver_code").equals(ver_code)) {
				
				conn.close();
				return true;
				
			}else {
				
				conn.close();
				return false;
			}
			
		}else {
			conn.close();
			throw new CredentialNotFoundException("no results");
		}
			
	}
	
	public ArrayList<Avviso> getUserAvvisi(int user_id) throws SQLException, CredentialNotFoundException {
		
		establishConnection();
		
		String getUser = "SELECT * FROM avviso WHERE TIC_id_avvisato IN (SELECT TIC_id FROM tickets t WHERE t.UT_id_apertura = ?)";
		
		ResultSet res;
		ArrayList<Avviso> avvisi = new ArrayList<Avviso>();
		QueryHandler_ticket queryTickets = new QueryHandler_ticket();
	
		java.sql.PreparedStatement getUser_query = conn.prepareStatement(getUser);
		
		getUser_query.setInt(1, user_id);
		res = getUser_query.executeQuery();
		
		while(res.next()) {
			
			String email_avvisatore = getEmailFromId(res.getInt("UT_id_avvisante"));
			Ticket ticket_info = queryTickets.getTicketFromId(res.getInt("TIC_id_avvisato"));
			
			Avviso avv = new Avviso(res.getString("AVV_descrizione"), email_avvisatore, ticket_info);		
			avvisi.add(avv);
	
		}
		
		return avvisi;
		
	}

	private String getEmailFromId(int user_id) throws SQLException, CredentialNotFoundException {
		
		establishConnection();
		String prepared_query = "SELECT UT_email FROM utenti WHERE UT_id = ?";
		
		
		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
		
		
		pr.setInt(1, user_id);
		ResultSet res = pr.executeQuery();
		if(res.next()) {
			
			String email = res.getString("UT_email");
			conn.close();
			return email;
			
		}else {
			conn.close();
			throw new CredentialNotFoundException("no results in getUserEmailFromId");
		}
	}

	public ArrayList<Segnalazione> getUserSegnalazioni(int user_id) throws SQLException, CredentialNotFoundException {
establishConnection();
		
		String getUser = "SELECT * FROM segnalazione WHERE UT_id_segnalato = ?";
		
		ResultSet res;
		ArrayList<Segnalazione> segnalazioni = new ArrayList<Segnalazione>();
	
		java.sql.PreparedStatement getUser_query = conn.prepareStatement(getUser);
		
		getUser_query.setInt(1, user_id);
		res = getUser_query.executeQuery();
		
		while(res.next()) {
			
			String email_segnalatore = getEmailFromId(res.getInt("UT_id_segnalatore"));
			
			
			Segnalazione seg = new Segnalazione(res.getString("SEG_descrizione"), email_segnalatore);		
			segnalazioni.add(seg);
	
		}
		
		return segnalazioni;
		
	}

	

}
