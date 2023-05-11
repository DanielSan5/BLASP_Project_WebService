package classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class QueryHandler_avviso {

	private static String db_url = "jdbc:mysql://localhost:3306/ticketing";
    private static String db_driver = "com.mysql.jdbc.Driver";
    private static String db_user = "root";
    private static String db_password = "";
    private Connection conn;
	
	public QueryHandler_avviso() {
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
	
//***INSERISCI AVVISO***
public int inserisciAvviso(String descrizione_avviso, int ticket_id, int utente_id) {
		
		establishConnection();
		String prepared_query = "INSERT INTO avviso (AVV_descrizione, ID_TIC_avvisato, UT_ID_avviso) VALUES (?,?,?)";
		//DA CORREGGERE I NOME DI CAMPI DEL DB
		
		try(
				java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
				){
				
				pr.setString(1, descrizione_avviso);
				pr.setInt(2, ticket_id);
				pr.setInt(3, utente_id);
			
				//executeUpdate returna o 1 se  andato a buonfine o 0 se non  andato a buonfine
				int check = pr.executeUpdate();
				
				
				if (check == 1) {
					if(pr.getGeneratedKeys().next()) {
						conn.close();
						return pr.getGeneratedKeys().getInt(1);
					}else {
						conn.close();
						return 0;
					}
				
				}else {
					return 0;
				}
			
			}catch(SQLException e){
				
				System.out.println(e.getLocalizedMessage());
				return -1;
			
			}
		
	}	

}