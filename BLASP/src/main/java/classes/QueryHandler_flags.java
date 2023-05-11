package classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class QueryHandler_flags {

	private static String db_url = "jdbc:mysql://localhost:3306/ticketing";
    private static String db_driver = "com.mysql.jdbc.Driver";
    private static String db_user = "root";
    private static String db_password = "";
    private Connection conn;
	
	public QueryHandler_flags() {
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
	
	
	public ArrayList<String> getFlags(int utente_id) throws Exception {
		
		establishConnection();
		
		String getUser = "SELECT COUNT(*) as num_segnalazioni , SEG_descrizione FROM segnalazione s WHERE UT_id_segnalato = ? GROUP BY UT_id _segnalato ";
		ResultSet res;
		ArrayList<String> flag_desc= new ArrayList<String>();
		
		try(
			java.sql.PreparedStatement getUser_query = conn.prepareStatement(getUser);
			){
			
				getUser_query.setInt(1, utente_id);
				res = getUser_query.executeQuery();
				
				while(res.next()) {
					flag_desc.add(res.getString("SEG_descrizione"));
					
				}
				if(flag_desc.isEmpty()) {
					throw new Exception("utente non esistente");
				}else {
					return flag_desc;
				}
				
		}catch(SQLException e){
			System.out.println(e.getLocalizedMessage());
			return null;
		}
		
	}
	
	public ArrayList<Avviso> getAvvisi(int utente_id) throws Exception {
		
		establishConnection();
		
		String getUser = "SELECT * FROM avvisi a INNER JOIN ticket t ON t.TIC_id = a.TIC_id_avvisato WHERE t.UT_id_apertura = ?";
		ResultSet res;
		ArrayList<Avviso> avvisi = new ArrayList<Avviso>();
		
		try(
			java.sql.PreparedStatement getUser_query = conn.prepareStatement(getUser);
			){
			
				getUser_query.setInt(1, utente_id);
				res = getUser_query.executeQuery();
				
				while(res.next()) {
					
					String email = getUserEmail(res.getInt("UT_id_avvisante"));
					
					Ticket tic = new Ticket(res.getInt("TIC_id"),res.getString("TIC_data_cr"), res.getString("TIC_stato"), 
							res.getString("TIC_materia"), res.getString("TIC_livello_materia"), res.getString("TIC_decrizione"));
					
					Avviso avv = new Avviso(res.getString("AVV_descrizione"), email , tic);
					
					avvisi.add(avv);
					
				}
				if(avvisi.isEmpty()) {
					throw new Exception("utente non esistente");
				}else {
					return avvisi;
				}
				
		}catch(SQLException e){
			System.out.println(e.getLocalizedMessage());
			return null;
		}
		
	}
	
	public String getUserEmail(int user_id) throws Exception {
		
		establishConnection();
		String prepared_query = "SELECT UT_email FROM utenti WHERE UT_id = ?";
		
		try(
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			){
			
			pr.setInt(1, user_id);
			ResultSet res = pr.executeQuery();
			
			if(res.next()) {
				
				String email = res.getString("UT_email");
				conn.close();
				return email;
				
			}else {
				conn.close();
				throw new Exception("no results");
			}
			
		}catch(SQLException e){
			
			System.out.println(e.getLocalizedMessage());
			return null;
		
		}
	}

}