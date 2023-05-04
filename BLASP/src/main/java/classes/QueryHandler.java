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
 ;
  

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
				return -1;
			}
			
			
			
		}catch(SQLException e){
			
			System.out.println(e.getLocalizedMessage());
			return -1;
		
		}
	}
	
	
	
	public int inserisciUtente(String username, String email, String password, String nome, String cognome, String data_nascita, int classe, String indirizzo_scolastico, String localita) {
		
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
	
	
	//***DESCRIZIONE UTENTE campo facoltativo***
	public int inserisciDescrizioneUtente(int user_id, String descrizione) {
		
		establishConnection();
		String prepared_query = "UPDATE utenti SET UT_descrizione = ? WHERE UT_id = ?";		
		try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
				
				pr.setInt(1, user_id);
				pr.setString(2, descrizione);
	
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
		String prepared_query = "SELECT * FROM localita WHERE LC_nome = ?";
		
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
	
	/*
	 * returna null se non esistono ticket con quei filtri oppure se ci sono stati errori
	 */
	public ArrayList<Ticket> getTickets(String filter, String value) {
		
		establishConnection();
		
		String ticketStato = "SELECT * FROM tickets WHERE UT_stato = ?";
		String ticketLocalita = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE u.UT_localita = ?";
		//String ticketClasse = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE u.UT_classe = ?";
		ResultSet res;
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		
		try(
			java.sql.PreparedStatement ticketStato_query = conn.prepareStatement(ticketStato);
			java.sql.PreparedStatement ticketLocalita_query = conn.prepareStatement(ticketLocalita);
			//java.sql.PreparedStatement ticketClasse_query = conn.prepareStatement(ticketClasse);
			){
			
				switch(filter) {
			
					case "localita":
						
						ticketLocalita_query.setString(1, value);
						res = ticketLocalita_query.executeQuery();
						while(res.next()) {
							
							Ticket ticket = new Ticket(res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), res.getString("TIC_descrizione"), res.getInt("UT_id_apertura"), res.getInt("UT_id_accettazione"));
							tickets.add(ticket);
							
						}
						conn.close();
						break;
							
					case "stato":
						
						ticketStato_query.setString(1, value);
						res = ticketStato_query.executeQuery();
						while(res.next()) {
							
							Ticket ticket = new Ticket(res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), res.getString("TIC_descrizione"), res.getInt("UT_id_apertura"), res.getInt("UT_id_accettazione"));
							tickets.add(ticket);
							
						}
						conn.close();
						break;
						
					default:
						 return null;
					
				}
				
				if(tickets.isEmpty()) {
					return null;
				}else {
					return tickets;
				}
				
				
		}catch(SQLException e){
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
