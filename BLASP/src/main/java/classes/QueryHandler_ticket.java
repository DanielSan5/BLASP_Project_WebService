package classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class QueryHandler_ticket {

	private static String db_url = "jdbc:mysql://localhost:3306/ticketing";
    private static String db_driver = "com.mysql.jdbc.Driver";
    private static String db_user = "root";
    private static String db_password = "";
    private static int numero_ticket_creato=0;
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
public int inserisciTicket(String materia, String livello_materia, String descrizione, String dataCreazione) {
		
		establishConnection();
		String prepared_query = "INSERT INTO utenti (TIC_materia, TIC_livello_materia, TIC_descrizione, TIC_data_cr) VALUES (?, ?, ?, ?)";
		
		try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
				
				pr.setString(1, materia);
				pr.setString(2, livello_materia);
				pr.setString(3, descrizione);
				pr.setString(4, dataCreazione);
				//DA CAPIRE LA DATA DI CREAZIONE
				
				//executeUpdate returna o 1 se  andato a buonfine o 0 se non  andato a buonfine
				int check = pr.executeUpdate();
				
				conn.close();
				
				if (check == 1) {
					numero_ticket_creato++;
					return numero_ticket_creato;
				}else {
					return 0;
				}
			
			}catch(SQLException e){
				
				System.out.println(e.getLocalizedMessage());
				return -1;
			
			}
		
	}

//***CONTROLLA SE ESISTE L'ID DI UN TICKET***
public int hasTicketId(int ticket_id) {
		
		establishConnection();
		String prepared_query = "SELECT * FROM tickets WHERE TIC_id = ?";
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			pr.setInt(1, ticket_id);
			ResultSet res = pr.executeQuery();
			//per controllare se il ticket esiste basta vedere il risultato di next(), sar  false se non esistono righe
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
	
	//***RESTITUISCE I CAMPI DI UN TICKET DAL SUO ID***
	public String getTicketFromId(int ticket_id) {
			
			establishConnection();
			String prepared_query = "SELECT * FROM tickets WHERE TIC_id = ?";
			
			try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
				
				pr.setInt(1, ticket_id);
				ResultSet res = pr.executeQuery();
				
				if(res.next()) {
					  
					JsonObject jobj = new JsonObject();
					  
					//Recupero i valori della risposta
					int numero_ticket = res.getInt("TIC_id");
					String data_cr = res.getString("TIC_data_cr");
					String materia = res.getString("TIC_materia");
					String livello_materia = res.getString("TIC_livello_materia");
					String desc = res.getString("TIC_descrizione");
					
					//Inserisco i valori nell'oggetto Json
					jobj.addProperty("numero_ticket", numero_ticket);
					jobj.addProperty("data_cr", data_cr);
					jobj.addProperty("materia", materia);
					jobj.addProperty("livello_materia", livello_materia);
					jobj.addProperty("desc", desc);
					
					return jobj.toString();
				
				}else {
					return "errore";
				}
				
			
			
			}catch(SQLException e){
				e.printStackTrace();
				System.out.println("aa");
				System.out.println(e.getLocalizedMessage());
				return "errore";
			}
			
		}
	
	

}