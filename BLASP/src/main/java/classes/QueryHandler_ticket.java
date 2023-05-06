package classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

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
	
//***CONTROLLA SE ESISTE L'ID DI UN TICKET***
public int hasTicketId(int ticket_id) {
		
		establishConnection();
		String prepared_query = "SELECT * FROM tickets WHERE TIC_id = ?";
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			pr.setInt(1, ticket_id);
			ResultSet res = pr.executeQuery();
			//per controllare se l'email istituzionale esiste basta vedere il risultato di next(), sar  false se non esistono righe
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
	public int modificaDatiTicket(int numero_ticket, String materia, String descrizione, int tag) {
			
		establishConnection();
		String prepared_query = "UPDATE tickets SET (TIC_materia, TIC_descrizione, TIC_tag) = (?, ?, ?) WHERE TIC_id = ?";		
		try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
					
				pr.setString(1, materia);
				pr.setString(2, descrizione);
				pr.setInt(3, tag);
		
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