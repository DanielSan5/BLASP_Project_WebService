package classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
	
	public int hasUsername(String username) {
		
		establishConnection();
		String prepared_query = "SELECT * FROM utenti WHERE UT_username = ?";
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			pr.setString(1, username);
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
	
	public int hasEmail(String email) {

		
		establishConnection();
		String prepared_query = "SELECT * FROM utenti WHERE UT_email = ?";
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			pr.setString(1, email);
			ResultSet res = pr.executeQuery();
			//per controllare se l'email istituzionale esiste basta vedere il risultato di next(), sar� false se non esistono righe
			boolean check = res.next();
			conn.close();
			return check ? 1 : 0; //se check true returna 1 altrimenti 0
		
		}catch(SQLException e){
			e.printStackTrace();
			System.out.println("aa");
			System.out.println(e.getLocalizedMessage());
			return -1;
		}
		
	}
	
	public int checkPass(int user_id, String password) {
		
		establishConnection();
		String prepared_query = "SELECT UT_password FROM utenti WHERE UT_id = ?";
		Argon2 argon2 = Argon2Factory.create(Argon2Types.ARGON2id);
		
		try(
				
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				
				){

			 
				pr.setInt(1, user_id);
				ResultSet res = pr.executeQuery();
				if(res.next()) {
					

					String hashedPass = res.getString("UT_password");
					conn.close();
					
					if(argon2.verify(hashedPass, password)){
						return 1;
					}else {
						System.out.println("password non verificata");
						return 0;
					}
					
				}else {
					conn.close();
					return -1;
				}
				
			}catch(SQLException e){
				
				System.out.println(e.getLocalizedMessage());
				return -1;
			
			}
	}
	
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
	public int modificaDatiUtente(int user_id, String descrizione, String localita, String classe, String indirizzo) {
		
		establishConnection();
		String prepared_query = "UPDATE utenti SET (UT_descrizione, UT_localita, UT_classe, UT_indirizzo_scolastico) = (?, ?, ?, ?) WHERE UT_id = ?";		
		try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
				
				pr.setString(1, descrizione);
				pr.setString(2, localita);
				pr.setString(3, classe);
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
	public int modificaPasswordUtente(int user_id, String password_cr) {
			
			establishConnection();
			String prepared_query = "UPDATE utenti SET UT_password = ? WHERE UT_id = ?";		
			try(
					java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
					){
					
					pr.setString(1, password_cr);
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

	public Utente getUserData(int user_id) throws Exception {


		
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
					throw new Exception("utente non esistente");
				
						
					
				
				
		}catch(SQLException e){
			System.out.println(e.getLocalizedMessage());
			return null;
		}
		
	}
	
	public ArrayList<Ticket> getUserTickets(int user_id) throws Exception {
		
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
					throw new Exception("utente non esistente");
				}else {
					return tickets;
				}
				
		}catch(SQLException e){
			System.out.println(e.getLocalizedMessage());
			return null;
		}
		
	}

	public int isUserAdmin(int user_id) throws Exception {
		
		establishConnection();
		String prepared_query = "SELECT UT_admin FROM utenti WHERE UT_id = ?";
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			pr.setInt(1, user_id);
			ResultSet res = pr.executeQuery();
			//per controllare se l'email istituzionale esiste basta vedere il risultato di next(), sar� false se non esistono righe
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
				throw new Exception("no results");
			}
	
		}catch(SQLException e){
			e.printStackTrace();
			System.out.println("aa");
			System.out.println(e.getLocalizedMessage());
			return -1;
		}
		
	}
	
	public ArrayList<Utente> getToBlock() throws Exception{

		
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
				throw new Exception("no results");
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
	 * Metodo per la query di modifica password
	 * Metodo per la query di modifica data di nascita
	 * Metodo per la query di modifica indirizzo  
	 * Metodo per la query della sezione scolastica
	 * Metodo per la query di modifica del paese
	 */

}
