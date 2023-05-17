package classes;

import java.rmi.NoSuchObjectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.security.auth.login.CredentialNotFoundException;

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
			conn = DriverManager.getConnection(db_url, db_user, db_password); 
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
	public int getUserId(String email) {
		
		establishConnection();
		String prepared_query = "SELECT UT_id FROM utenti WHERE UT_email = ?";
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			pr.setString(1, email);
			ResultSet res = pr.executeQuery();
			if(res.next()) {
				
				int user_id = res.getInt("UT_id");
				conn.close();
				return user_id;
				
			}else {
				conn.close();
				return 0;
			}
			
			
			
		}catch(SQLException e){
			
			System.out.println(e.getLocalizedMessage());
			return -1;
		
		}
	}
	
	//RECUPERO USER ID DALLA MAIL --> SOLO SE ADMIN
	public int getUserIdAdmin(String email) {
		
		establishConnection();
		String prepared_query = "SELECT UT_id FROM utenti WHERE UT_email = ? AND UT_tipo = 'admin'";
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			pr.setString(1, email);
			ResultSet res = pr.executeQuery();
			if(res.next()) {
				
				int user_id = res.getInt("UT_id");
				conn.close();
				return user_id;
				
			}else {
				conn.close();
				return 0;
			}
			
			
			
		}catch(SQLException e){
			
			System.out.println(e.getLocalizedMessage());
			return -1;
		
		}
	}
	
	public int inserisciUtente(String email, String password, String nome, String cognome, String data_nascita, int classe, String indirizzo_scolastico, String localita) {
		
		establishConnection();
		String prepared_query = "INSERT INTO utenti (UT_email, UT_password, UT_nome, UT_cognome, UT_data_nascita, UT_classe, UT_indirizzo_scolastico, UT_localita) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		
		try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
				
				//pr.setString(1, username);
				pr.setString(1, email);
				pr.setString(2, password);
				pr.setString(3, nome);
				pr.setString(4, cognome);
				pr.setString(5, data_nascita);
				pr.setInt(6, classe);
				pr.setString(7, indirizzo_scolastico);
				pr.setString(8, localita);
				
				//executeUpdate returna o 1 se  andato a buonfine o 0 se non  andato a buonfine
				int check = pr.executeUpdate();
				
				conn.close();
				
				return check;
			
			}catch(SQLException e){
				
				System.out.println(e.getLocalizedMessage());
				return -1;
			
			}
		
	}
	
	//***UPDATE DATI UTENTE***
	public int modificaDatiUtente(int user_id, String descrizione, String localita, int classe, String indirizzo) {
		
		establishConnection();
		String prepared_query = "UPDATE utenti SET (UT_descrizione, UT_localita, UT_classe, UT_indirizzo_scolastico) = (?, ?, ?, ?) WHERE UT_id = ?";		
		try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
				
				pr.setString(1, descrizione);
				pr.setString(2, localita);
				pr.setInt(3, classe);
				pr.setString(4, indirizzo);
				pr.setInt(5, user_id);
	
				//executeUpdate returna o 1 se � andato a buonfine o 0 se non � andato a buonfine
				int check = pr.executeUpdate();
				
				conn.close();
				
				return check;
			
			}catch(SQLException e){
				
				System.out.println(e.getLocalizedMessage());
				return -1;
			
			}
		
	}
	
	//***UPDATE PASSWORD UTENTE***
	public int changePass(int user_id, String password_encr) {
			
			establishConnection();
			String prepared_query = "UPDATE utenti SET UT_password = ? WHERE UT_id = ?";		
			try(
					java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
					){
					
					pr.setString(1, password_encr);
					pr.setInt(2, user_id);
					
					//executeUpdate returna o 1 se � andato a buonfine o 0 se non � andato a buonfine
					int check = pr.executeUpdate();
					
					conn.close();
					
					return check;
				
				}catch(SQLException e){
					
					System.out.println(e.getLocalizedMessage());
					return -1;
				
				}
			
		}
	
	//***BLOCCAGGIO UTENTE***
	public int blockUser(int user_id) {
				
		establishConnection();
		String prepared_query = "UPDATE utenti SET UT_stato = 'bloccato' WHERE UT_id = ?";		
		try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
				
				pr.setInt(1, user_id);
				
				//executeUpdate returna o 1 se � andato a buonfine o 0 se non � andato a buonfine
				int check = pr.executeUpdate();
				
				conn.close();
				
				return check;
			
			}catch(SQLException e){
				
				System.out.println(e.getLocalizedMessage());
				return -1;
			
			}
				
	}
			
	//Controllo se la località inserita esiste
	public int hasLocalita(String localita) {
		
		establishConnection();
		String prepared_query = "SELECT * FROM localita WHERE LC_descrizione = ?";
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			pr.setString(1, localita);
			
			ResultSet res = pr.executeQuery();
			//per controllare se la località esiste basta vedere il risultato di next(), sar� false se non esistono righe
			boolean check = res.next();
			
			conn.close();
			return check ? 1 : 0; //se check true returna 1 altrimenti 0
		
		}catch(SQLException e){
			
			System.out.println(e.getLocalizedMessage());
			return -1;
		
		}
		
	}
	
	//Controllo se l'indirizzo di studio inserito esiste
	public int hasIndirizzo(String indirizzo) {
			
		establishConnection();
		String prepared_query = "SELECT * FROM indirizzo_scolastico WHERE INS_nome = ?";
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			pr.setString(1, indirizzo);
			
			ResultSet res = pr.executeQuery();
			
			boolean check = res.next();
			
			conn.close();
			return check ? 1 : 0; //se check true returna 1 altrimenti 0
		
		}catch(SQLException e){
			
			System.out.println(e.getLocalizedMessage());
			return -1;
		
		}
			
	}
	
	//Controllo lo stato dell'utente --> DA CONFERMARE
	public String getUserStatus(int user_id) {
		
		establishConnection();
		String prepared_query = "SELECT UT_status FROM utenti WHERE UT_id = ?";
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			pr.setInt(1, user_id);
			
			ResultSet res = pr.executeQuery();
			
			if(res.next()) {
				
				
				String user_status = res.getString("UT_status");
				conn.close();
				return user_status;
				
				
			}else {
				return "";
			}
			
		
		}catch(SQLException e){
			
			e.printStackTrace();
			System.out.println(e.getLocalizedMessage());
			return "";
		
		}
		
	}

	//CONTROLLO SE UNA MATERIA ESISTE NEL DATABASE
	public int checkExistMateria(String materia) {
		
		establishConnection();
		String prepared_query = "SELECT * FROM materia WHERE MAT_nome = ?";
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			pr.setString(1, materia);
			ResultSet res = pr.executeQuery();
			//per controllare se l'username esiste basta vedere il risultato di next(), sarà false se non esistono righe
			boolean check = res.next();
			
			conn.close();
			return check ? 1 : 0; //se check true returna 1 altrimenti 0
		
		}catch(SQLException e){
			
			System.out.println(e.getLocalizedMessage());
			return -1;
		
		}
		
	}

	public Utente getUserData(int user_id) throws CredentialNotFoundException {


		
		establishConnection();
		
		String getUser = "SELECT * FROM utenti WHERE UT_id = ?";
		//String ticketLocalita = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE u.UT_localita = ?";
		//String ticketClasse = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE u.UT_classe = ?";
		ResultSet res;
		
		
		try(
			java.sql.PreparedStatement getUser_query = conn.prepareStatement(getUser);
			//java.sql.PreparedStatement ticketLocalita_query = conn.prepareStatement(ticketLocalita);
			//java.sql.PreparedStatement ticketClasse_query = conn.prepareStatement(ticketClasse);
			){
			
			
				
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
				
						
					
				
				
		}catch(SQLException e){
			System.out.println(e.getLocalizedMessage());
			return null;
		}
		
	}
	
	public ArrayList<Ticket> getUserTickets(int user_id) throws CredentialNotFoundException {
		
		establishConnection();
		
		String getUser = "SELECT * FROM tickets WHERE UT_id_apertura = ?";
		ResultSet res;
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		
		try(
			java.sql.PreparedStatement getUser_query = conn.prepareStatement(getUser);
			){
			
				getUser_query.setInt(1, user_id);
				res = getUser_query.executeQuery();
				
				while(res.next()) {
					
					Ticket ticket = new Ticket(res.getInt("TIC_id"), res.getString("TIC_data_cr"), res.getString("TIC_stato"), 
							res.getString("TIC_materia"), res.getString("TIC_livello_materia"), res.getString("TIC_decrizione"));
					
					tickets.add(ticket);
					
				}
				if(tickets.isEmpty()) {
					throw new CredentialNotFoundException("utente non esistente");
				}else {
					return tickets;
				}
				
		}catch(SQLException e){
			System.out.println(e.getLocalizedMessage());
			return null;
		}
		
	}

	public int isUserAdmin(int user_id) throws CredentialNotFoundException {
		
		establishConnection();
		String prepared_query = "SELECT UT_admin FROM utenti WHERE UT_id = ?";
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			pr.setInt(1, user_id);
			ResultSet res = pr.executeQuery();
			
			if(res.next()) {
				
				if(res.getInt("UT_admin") == 1) {
					conn.close();
					return 1;
				}else {
					conn.close();
					return 0;
				}
				
			}else {
				conn.close();
				throw new CredentialNotFoundException("no results");
			}
	
		}catch(SQLException e){
			e.printStackTrace();
			System.out.println("aa");
			System.out.println(e.getLocalizedMessage());
			return -1;
		}
		
	}
	
	public ArrayList<Utente> getToBlock() throws CredentialNotFoundException{

		
		establishConnection();
		String prepared_query = "SELECT COUNT(*) as num_flags, u.UT_email, u.UT_nome, u.UT_cognome"
				+ " FROM utenti u INNER JOIN segnalazione s ON u.UT_id = s.UT_id_segnalato GROUP BY u.UT_id";
		ArrayList<Utente> toBlock = new ArrayList<Utente>();
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			
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
	
		}catch(SQLException e){
			e.printStackTrace();
			System.out.println("aa");
			System.out.println(e.getLocalizedMessage());
			return null;
		}
	}
	/*
	 * da sistemare ogni metodo gestendo gli errori nel codice principale in modo da poter returnare un tipo boolean, non si chiude la connessione nel catch 
	 */

	public int inserisciCodice(int user_id, String ver_code) {
		
		establishConnection();
		String prepared_query = "INSERT INTO utenti (UT_ver_code) VALUES (?) WHERE UT_id = ?";
		
		try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
				
				//pr.setString(1, username);
				pr.setString(1, ver_code);
				pr.setInt(2, user_id);
				
				//executeUpdate returna o 1 se  andato a buonfine o 0 se non  andato a buonfine
				int check = pr.executeUpdate();
				conn.close();
				
				return check;
			
			}catch(SQLException e){
				
				System.out.println(e.getLocalizedMessage());
				return -1;
			
			}
		
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

		
	

}
