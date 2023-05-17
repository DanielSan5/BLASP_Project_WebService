package classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mysql.jdbc.exceptions.MySQLDataException;

import container.Tickets;

public class QueryHandler_ticket {

	private static String db_url = "jdbc:mysql://localhost:3306/ticketing";
    private static String db_driver = "com.mysql.jdbc.Driver";
    private static String db_user = "root";
    private static String db_password = "";
    private Connection conn;
	
	public QueryHandler_ticket() {
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
	
	//***INSERISCI TICKET***
	public int inserisciTicketOttieniID(String materia, String livello_materia, String descrizione, String dataCreazione, int userID) throws SQLException {
		
		establishConnection();
		String prepared_query = "INSERT INTO tickets (TIC_materia, TIC_livello_materia, TIC_descrizione, TIC_data_cr, UT_id_apertura) VALUES (?, ?, ?, ?, ?)";
		

		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
	
		
		pr.setString(1, materia);
		pr.setString(2, livello_materia);
		pr.setString(3, descrizione);
		pr.setString(4, dataCreazione);
		pr.setInt(5, userID);
		//DA CAPIRE LA DATA DI CREAZIONE
		
		
		//executeUpdate returna  il numero di righe create o aggiornate, quindi se returna 0 non ha inserito/aggiornato nessuna riga
		
		if(pr.executeUpdate() != 1) {
			conn.close();
			throw new MySQLDataException("could not create row in utenti");
		}else {
			
			if(pr.getGeneratedKeys().next()) {
				conn.close();
				return pr.getGeneratedKeys().getInt(1);
			}else {
				conn.close();
				return 0;
			}
		}
		
		
	
			
			
		
	}
	
	public void saveFavourites(int ticket_id, int user_id) throws SQLException {
		
		establishConnection();
		String prepared_query = "INSERT INTO preferiti (UT_id, TIC_id) VALUES (?, ?)";
		
		
		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
		
		pr.setInt(1, ticket_id);
		pr.setInt(2, user_id);
		
		if(pr.executeUpdate() != 1) {
			conn.close();
			throw new MySQLDataException("could not create row in utenti");
		}
	
			
	}

	//***CONTROLLA SE ESISTE L'ID DI UN TICKET***
	public boolean hasTicketId(int ticket_id) throws SQLException {
		
		establishConnection();
		String prepared_query = "SELECT * FROM tickets WHERE TIC_id = ?";
		
		
		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
		
		
		pr.setInt(1, ticket_id);
		ResultSet res = pr.executeQuery();
		//per controllare se il ticket esiste basta vedere il risultato di next(), sar  false se non esistono righe
		boolean check = res.next();
		conn.close();
		
		return check; //se check true returna 1 altrimenti 0
			
			
		
		
	}
	
	//***MODIFICA TUTTE LE INFORMAZIONI DEL TICKET***
	public int modificaDatiTicket(int numero_ticket, String materia, String descrizione, String tag) {
			
		establishConnection();
		String prepared_query = "UPDATE tickets SET (TIC_materia, TIC_descrizione, TIC_tag) = (?, ?, ?) WHERE TIC_id = ?";		
		try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
					
				pr.setString(1, materia);
				pr.setString(2, descrizione);
				pr.setString(3, tag);
		
				//executeUpdate returna o 1 se   andato a buonfine o 0 se non   andato a buonfine
				int check = pr.executeUpdate();
					
				conn.close();
					
				return check;
				
			}catch(SQLException e){
					
				System.out.println(e.getLocalizedMessage());
				return -1;
				
			}
			
	}
	
	//***MODIFICA LO STATO DEL TICKET***
	public int modificaStatoTicket(int id_user, int numero_ticket) {
				
		establishConnection();
		String prepared_query = "UPDATE tickets SET (UT_id_accettazione, TIC_stato) = (?,'pending') WHERE TIC_id = ?";		
		try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
						
				pr.setInt(1, id_user);
				pr.setInt(2, numero_ticket);
			
				//executeUpdate returna o 1 se   andato a buonfine o 0 se non   andato a buonfine
				int check = pr.executeUpdate();
						
				conn.close();
						
				return check;
					
			}catch(SQLException e){
						
				System.out.println(e.getLocalizedMessage());
				return -1;
					
			}
				
	}
	
	//***RESTITUISCE I CAMPI DI UN TICKET DAL SUO ID***
	public Ticket getTicketFromId(int ticket_id) throws Exception{
			
			establishConnection();
			String prepared_query = "SELECT * FROM tickets WHERE TIC_id = ?";
			
			try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
				
				pr.setInt(1, ticket_id);
				ResultSet res = pr.executeQuery();
				
				if(res.next()) {
					
					Ticket ticket = new Ticket(res.getInt("TIC_id"), res.getString("TIC_data_cr"), res.getString("TIC_stato"), 
							res.getString("TIC_materia"), res.getString("TIC_livello_materia"), res.getString("TIC_decrizione"));
					return ticket;
				
				}else {
					throw new Exception("nessun ticket trovato");
				}
				
			
			
			}catch(SQLException e){
				e.printStackTrace();
				System.out.println("aa");
				System.out.println(e.getLocalizedMessage());
				return null;
			}
			
		}
	
	
	//***RESTITUISCE L'ID UTENTE DALL'ID TICKET***
	public int getUserIdFromTicket(int ticket_id) {
			
			establishConnection();
			String prepared_query = "SELECT UT_id FROM tickets WHERE TIC_id = ?";
			//DA MODIFICARE IL NOME DEL CAMPO
			
			try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
				
				pr.setInt(1, ticket_id);
				ResultSet res = pr.executeQuery();
				
				conn.close();
				
				if(res.next())
					return res.getInt("UT_id");				//se true ritorna l'ID utente
				else
					return 0;								//se false ritorna 0
			
			}catch(SQLException e){
				e.printStackTrace();
				System.out.println(e.getLocalizedMessage());
				return -1;
			}
			
		}
	
	//***CANCELLA UN TICKET***
	public int cancellaTicket(int numero_ticket) {
			
		establishConnection();
		String prepared_query = "DELETE FROM tickets WHERE TIC_id = ?";		
		try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
					
				pr.setInt(1, numero_ticket);
				
				//executeUpdate returna o 1 se   andato a buonfine o 0 se non   andato a buonfine
				int check = pr.executeUpdate();
					
				conn.close();
					
				return check;
				
			}catch(SQLException e){
					
				System.out.println(e.getLocalizedMessage());
				return -1;
				
			}
			
	}
	
	
	

}