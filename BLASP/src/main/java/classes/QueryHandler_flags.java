package classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.CredentialNotFoundException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mysql.jdbc.exceptions.MySQLDataException;

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
	public int inserisciAvvisoGetId(String descrizione_avviso, int ticket_id, int utente_id) throws SQLException {
		
		establishConnection();
		String prepared_query = "INSERT INTO avviso (AVV_descrizione, TIC_id_avvisato, UT_ID_avvisante) VALUES (?,?,?)";
	
		

		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query, Statement.RETURN_GENERATED_KEYS);
	
		
		pr.setString(1, descrizione_avviso);
		pr.setInt(2, ticket_id);
		pr.setInt(3, utente_id);
	
		//executeUpdate returna o 1 se  andato a buonfine o 0 se non  andato a buonfin

		if(pr.executeUpdate() != 1) {
			conn.close();
			throw new MySQLDataException("could not create row in utenti");
		}else {
			ResultSet res = pr.getGeneratedKeys();
			if(res.next()) {
				
				int key = res.getInt(1);
				conn.close();
				return key;
			}else {
				conn.close();
				throw new MySQLDataException("could get generated key");
			}
		}	
			
			
		
	}	
	
	public ArrayList<String> getFlags(int utente_id) throws SQLException, CredentialNotFoundException {
		
		establishConnection();
		
		String getUser = "SELECT COUNT(*) as num_segnalazioni , SEG_descrizione FROM segnalazione s WHERE UT_id_segnalato = ? GROUP BY UT_id _segnalato ";
		ResultSet res;
		ArrayList<String> flag_desc= new ArrayList<String>();
		
	
		java.sql.PreparedStatement getUser_query = conn.prepareStatement(getUser);
		
		
		getUser_query.setInt(1, utente_id);
		res = getUser_query.executeQuery();
		
		while(res.next()) {
			
			flag_desc.add(res.getString("SEG_descrizione"));
			
		}
		if(flag_desc.isEmpty()) {
			throw new CredentialNotFoundException("utente non esistente");
		}else {
			return flag_desc;
		}
			
	
	}
	
	public ArrayList<Avviso> getAvvisi(int utente_id) throws SQLException, CredentialNotFoundException {
		
		establishConnection();
		
		String getUser = "SELECT * FROM avvisi a INNER JOIN ticket t ON t.TIC_id = a.TIC_id_avvisato WHERE t.UT_id_apertura = ?";
		ResultSet res;
		ArrayList<Avviso> avvisi = new ArrayList<Avviso>();
		
	
		java.sql.PreparedStatement getUser_query = conn.prepareStatement(getUser);
	
	
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
			throw new CredentialNotFoundException("utente non esistente");
		}else {
			return avvisi;
		}
	
		
	}
	
	public String getUserEmail(int user_id) throws SQLException, CredentialNotFoundException {
		
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
				throw new CredentialNotFoundException("no results");
			}
			
		
	}
	
	public boolean hasNotThreeFlags(int user_id) throws SQLException{
		
		establishConnection();
		String prepared_query = "SELECT COUNT(*) as numeroSegnalazioni FROM segnalazione WHERE UT_id_segnalato = ? GROUP BY UT_id_segnalato";
		
		
			java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query);
			
			
			pr.setInt(1, user_id);
			ResultSet res = pr.executeQuery();
			
			if(res.getInt("numeroSegnalazioni") >= 3) {
				return false;
			
			}else {
				return true;
			}
			
		
	}
	
	//***INSERISCI SEGNALAZIONE***
	public int inserisciSegnalazioneGetId(int user_segnalato_id, int user_segnalatore_id, String segnalazioni) throws SQLException, CredentialNotFoundException{
		
		establishConnection();
		String prepared_query = "INSERT INTO segnalazione (UT_id_segnalato, UT_id_segnalatore, SEG_descrizione) VALUES (?, ?, ?)";
		
	
		java.sql.PreparedStatement pr = conn.prepareStatement(prepared_query, Statement.RETURN_GENERATED_KEYS);
	
		//pr.setString(1, username);
		pr.setInt(1, user_segnalato_id);
		pr.setInt(2, user_segnalatore_id);
		pr.setString(3, segnalazioni);
		//executeUpdate returna  il numero di righe create o aggiornate, quindi se returna 0 non ha inserito/aggiornato nessuna riga
		
		if(pr.executeUpdate() != 1) {
			conn.close();
			throw new MySQLDataException("could not create row in segnalazione");
		}else {
			
			ResultSet res = pr.getGeneratedKeys();
			if(res.next()) {
				
				int key = res.getInt(1);
				conn.close();
				return key;
			}else {
				conn.close();
				throw new MySQLDataException("could get generated key");
			}
		}
	}

}