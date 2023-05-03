package classes;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.mysql.jdbc.Connection;

public class QueryHandler {
	
	private static String db_url = "jdbc:mysql://localhost:3306/utenti";
    private static String db_driver = "com.mysql.jdbc.Driver";
    private static String db_user = "root";
    private static String db_password = "";
    private Connection conn;
  

	public QueryHandler() {
		
		try {
			Class.forName(db_driver).newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	public void establishConnection() {
		
		try{
			conn = (Connection) DriverManager.getConnection(db_url, db_user, db_password); 
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
		
		try(
				
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				
				){

			 
				pr.setInt(1, user_id);
				ResultSet res = pr.executeQuery();
				if(res.next()) {
					
					String pass = res.getString("UT_password");
					conn.close();
					
					if(password.equals(pass)){
						
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
		String prepared_query = "INSERT INTO utenti (UT_username, UT_email, UT_password, UT_nome, UT_cognome, UT_data_nascita, UT_classe, UT_indirizzo_scolastico, UT_localita) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
				
				pr.setString(1, username);
				pr.setString(2, email);
				pr.setString(3, password);
				pr.setString(4, nome);
				pr.setString(5, cognome);
				pr.setString(6, data_nascita);
				pr.setInt(7, classe);
				pr.setString(8, indirizzo_scolastico);
				pr.setString(9, localita);
				
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
	 * Metodo per la query di modifica password
	 * Metodo per la query di modifica data di nascita
	 * Metodo per la query di modifica indirizzo  
	 * Metodo per la query della sezione scolastica
	 * Metodo per la query di modifica del paese
	 */

}
