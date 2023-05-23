package classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.mysql.cj.jdbc.MysqlDataSource;

public class QueryHandler_filters {

	
    private Connection conn;
    MysqlDataSource d = new MysqlDataSource();

	public QueryHandler_filters() throws SQLException {
		
		this.d.setUser("root");
	    this.d.setPassword("");
	    this.d.setUrl("jdbc:mysql://localhost:3306/ticketing");
	    
	    try {
			this.conn =  (Connection) d.getConnection();
		} catch (SQLException e) {
			this.conn.close();
			e.printStackTrace();
		}
	}
	
	
	//filtraggio in base a classe, nome e materia
	public ArrayList<Ticket> getCNM(String classe, String nome, String materia) throws SQLException {
		// TODO Auto-generated method stub
		//establishConnection();
		this.conn =  (Connection) d.getConnection();
		//String ticketCM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_nome = ? AND u.UT_classe = ? ";
		String ticketCM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_nome = ? AND t.TIC_tags = ? ";

		ResultSet res;
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		java.sql.PreparedStatement ticketCM_query = conn.prepareStatement(ticketCM);
	
		ticketCM_query.setString(1, materia);
		ticketCM_query.setString(2, nome);
		ticketCM_query.setString(3, classe);
					
		res = ticketCM_query.executeQuery();
		while(res.next()) {
			
			Utente user_info = new Utente(res.getString("UT_nome"), res.getString("UT_cognome"), res.getInt("UT_classe"), 
					res.getString("UT_indirizzo_scolastico"), res.getString("UT_descrizione"), res.getString("UT_data_nascita"), res.getString("UT_localita"), res.getBoolean("UT_admin"));
			
			Ticket ticket = new Ticket(res.getInt("TIC_id"), res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), 
					res.getString("TIC_descrizione"), user_info);
			
			tickets.add(ticket);
			
		}
	
		conn.close();
		return tickets;

	}

	//filtraggio in base a localita, nome e materia
	public ArrayList<Ticket> getLNM(String localita, String nome, String materia) throws SQLException {
		// TODO Auto-generated method stub
		
		//establishConnection();
		this.conn =  (Connection) d.getConnection();
		String ticketLNM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_localita = ? AND u.UT_nome = ?";
		ResultSet res;
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		
		
		java.sql.PreparedStatement ticketLNM_query = conn.prepareStatement(ticketLNM);
		

		ticketLNM_query.setString(1, materia);
		ticketLNM_query.setString(2, localita);
		ticketLNM_query.setString(3, nome);
						
		res = ticketLNM_query.executeQuery();
		while(res.next()) {
			Utente user_info = new Utente(res.getString("UT_nome"), res.getString("UT_cognome"), res.getInt("UT_classe"), 
					res.getString("UT_indirizzo_scolastico"), res.getString("UT_descrizione"), res.getString("UT_data_nascita"), res.getString("UT_localita"), res.getBoolean("UT_admin"));
			
			Ticket ticket = new Ticket(res.getInt("TIC_id"), res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), 
					res.getString("TIC_descrizione"), user_info);
			tickets.add(ticket);
			
		}
		
		conn.close();
		return tickets;
		
				
	

	}

	//filtraggio in base a localita, classe e materia
	public ArrayList<Ticket> getLCM(String localita, String classe, String materia) throws SQLException{
		// TODO Auto-generated method stub
		//establishConnection();
		this.conn =  (Connection) d.getConnection();
		//String ticketLCM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_localita = ? AND u.UT_classe = ?";
		String ticketLCM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_localita = ? AND t.TIC_tags = ?";

		ResultSet res;
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		
	
		java.sql.PreparedStatement ticketLCM_query = conn.prepareStatement(ticketLCM);
			

		ticketLCM_query.setString(1, materia);
		ticketLCM_query.setString(2, localita);
		ticketLCM_query.setString(3, classe);		
		
		res = ticketLCM_query.executeQuery();
		while(res.next()) {
			
			Utente user_info = new Utente(res.getString("UT_nome"), res.getString("UT_cognome"), res.getInt("UT_classe"), 
					res.getString("UT_indirizzo_scolastico"), res.getString("UT_descrizione"), res.getString("UT_data_nascita"), res.getString("UT_localita"), res.getBoolean("UT_admin"));
			
			Ticket ticket = new Ticket(res.getInt("TIC_id"), res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), 
					res.getString("TIC_descrizione"), user_info);
			tickets.add(ticket);
			
		}
	
		conn.close();
		return tickets;

	
	}
	
	//filtraggio in base a nome e materia
	public ArrayList<Ticket> getNM(String nome, String materia) throws SQLException {
		// TODO Auto-generated method stub
		//establishConnection();
		this.conn =  (Connection) d.getConnection();
		String ticketNM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_nome = ? ";
		ResultSet res;
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
	
		java.sql.PreparedStatement ticketNM_query = conn.prepareStatement(ticketNM);
			
		ticketNM_query.setString(1, materia);
		ticketNM_query.setString(2, nome);
		
		
		res = ticketNM_query.executeQuery();
		while(res.next()) {
			
			Utente user_info = new Utente(res.getString("UT_nome"), res.getString("UT_cognome"), res.getInt("UT_classe"), 
					res.getString("UT_indirizzo_scolastico"), res.getString("UT_descrizione"), res.getString("UT_data_nascita"), res.getString("UT_localita"), res.getBoolean("UT_admin"));
			
			Ticket ticket = new Ticket(res.getInt("TIC_id"), res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), 
					res.getString("TIC_descrizione"), user_info);
			tickets.add(ticket);
			
		}
		
		conn.close();
		return tickets;
		
		
		

		
	}

	//filtraggio in base a classe e materia
	public ArrayList<Ticket> getCM(String classe, String materia) throws SQLException {
		// TODO Auto-generated method stub
		//establishConnection();
		this.conn =  (Connection) d.getConnection();
		//String ticketCM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_classe = ?";
		String ticketCM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND t.TIC_tags = ?";
		ResultSet res;
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		
	
		java.sql.PreparedStatement ticketCM_query = conn.prepareStatement(ticketCM);
		
		ticketCM_query.setString(1, materia);
		ticketCM_query.setString(2, classe);	
		
		res = ticketCM_query.executeQuery();
		while(res.next()) {
			
			Utente user_info = new Utente(res.getString("UT_nome"), res.getString("UT_cognome"), res.getInt("UT_classe"), 
					res.getString("UT_indirizzo_scolastico"), res.getString("UT_descrizione"), res.getString("UT_data_nascita"), res.getString("UT_localita"), res.getBoolean("UT_admin"));
			
			Ticket ticket = new Ticket(res.getInt("TIC_id"), res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), 
					res.getString("TIC_descrizione"), user_info);
			tickets.add(ticket);
			
		}
	
		conn.close();
		return tickets;
	
	
	
	}

	//filtraggio in base a localita e materia
	public ArrayList<Ticket> getLM(String localita, String materia) throws SQLException {
		
		//establishConnection();
		this.conn =  (Connection) d.getConnection();
		String ticketLM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_localita = ?";
		ResultSet res;
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();	
	
		java.sql.PreparedStatement ticketLM_query = conn.prepareStatement(ticketLM);
			
		ticketLM_query.setString(1, materia);
		ticketLM_query.setString(2, localita);	
		
		res = ticketLM_query.executeQuery();
		while(res.next()) {
			
			Utente user_info = new Utente(res.getString("UT_nome"), res.getString("UT_cognome"), res.getInt("UT_classe"), 
					res.getString("UT_indirizzo_scolastico"), res.getString("UT_descrizione"), res.getString("UT_data_nascita"), res.getString("UT_localita"), res.getBoolean("UT_admin"));
			
			Ticket ticket = new Ticket(res.getInt("TIC_id"), res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), 
					res.getString("TIC_descrizione"), user_info);
			tickets.add(ticket);
			
		}
	
		conn.close();
		return tickets;
		
		
	}

	//filtraggio in base alla materia
	public ArrayList<Ticket> getM(String materia) throws SQLException {
		
		//establishConnection();
		this.conn =  (Connection) d.getConnection();
		String ticketM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ?";
		ResultSet res;
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		
		java.sql.PreparedStatement ticketM_query = conn.prepareStatement(ticketM);
		
		ticketM_query.setString(1, materia);
							
		res = ticketM_query.executeQuery();
		while(res.next()) {
			
			Utente user_info = new Utente(res.getString("UT_nome"), res.getString("UT_cognome"), res.getInt("UT_classe"), 
					res.getString("UT_indirizzo_scolastico"), res.getString("UT_descrizione"), res.getString("UT_data_nascita"), res.getString("UT_localita"), res.getBoolean("UT_admin"));
			
			Ticket ticket = new Ticket(res.getInt("TIC_id"), res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), 
					res.getString("TIC_descrizione"), user_info);
			tickets.add(ticket);
			
		}
		
		conn.close();
		return tickets;
	
				
	
	}

	
	public ArrayList<Ticket> getAll(String materia, String nome, String classe, String localita) throws SQLException {
		
		//establishConnection();
		this.conn =  (Connection) d.getConnection();
		//String ticketALL = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_nome = ? AND u.UT_classe = ? AND u.UT_localita = ?";
		String ticketALL = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_nome = ? AND t.TIC_tags = ? AND u.UT_localita = ?";

		ResultSet res;
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		java.sql.PreparedStatement ticketALL_query = conn.prepareStatement(ticketALL);
	
		ticketALL_query.setString(1, materia);
		ticketALL_query.setString(2, nome);
		ticketALL_query.setString(3, classe);
		ticketALL_query.setString(4, localita);		
		
		res = ticketALL_query.executeQuery();
		
		while(res.next()) {
			
			Utente user_info = new Utente(res.getString("UT_nome"), res.getString("UT_cognome"), res.getInt("UT_classe"), 
					res.getString("UT_indirizzo_scolastico"), res.getString("UT_descrizione"), res.getString("UT_data_nascita"), res.getString("UT_localita"), res.getBoolean("UT_admin"));
			
			Ticket ticket = new Ticket(res.getInt("TIC_id"), res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), 
					res.getString("TIC_descrizione"), user_info);
			
			tickets.add(ticket);
			
		}
	
		conn.close();
		return tickets;

	}
	
	

	
	
}
