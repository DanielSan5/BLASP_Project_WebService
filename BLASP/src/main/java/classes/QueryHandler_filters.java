package classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class QueryHandler_filters {

	private static String db_url = "jdbc:mysql://localhost:3306/ticketing";
    private static String db_driver = "com.mysql.jdbc.Driver";
    private static String db_user = "root";
    private static String db_password = "";
    private Connection conn;
    private ArrayList<Ticket> tickets = new ArrayList<Ticket>();

	public QueryHandler_filters() {
		
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
	
	
	
	
	public ArrayList<Ticket> getCNM(Map<String, String[]> params) {
		// TODO Auto-generated method stub
		establishConnection();
		String ticketCM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_nome = ? AND u.UT_classe = ? ";
		ResultSet res;
		
		
		try(
			java.sql.PreparedStatement ticketCM_query = conn.prepareStatement(ticketCM);
			){
							
				res = ticketCM_query.executeQuery();
				while(res.next()) {
					
					Ticket ticket = new Ticket(res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), res.getString("TIC_descrizione"), res.getInt("UT_id_apertura"), res.getInt("UT_id_accettazione"));
					tickets.add(ticket);
					
				}
				conn.close();
				
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

	public ArrayList<Ticket> getLNM(Map<String, String[]> params) {
		// TODO Auto-generated method stub
		establishConnection();
		String ticketLNM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_localita = ? AND u.UT_nome = ?";
		ResultSet res;
		
		
		try(
			java.sql.PreparedStatement ticketLNM_query = conn.prepareStatement(ticketLNM);
			){
							
				res = ticketLNM_query.executeQuery();
				while(res.next()) {
					
					Ticket ticket = new Ticket(res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), res.getString("TIC_descrizione"), res.getInt("UT_id_apertura"), res.getInt("UT_id_accettazione"));
					tickets.add(ticket);
					
				}
				conn.close();
				
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

	public ArrayList<Ticket> getLCM(Map<String, String[]> params) {
		// TODO Auto-generated method stub
		establishConnection();
		String ticketLCM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_localita = ? AND u.UT_classe = ?";
		ResultSet res;
		
		
		try(
			java.sql.PreparedStatement ticketLCM_query = conn.prepareStatement(ticketLCM);
			){
							
				res = ticketLCM_query.executeQuery();
				while(res.next()) {
					
					Ticket ticket = new Ticket(res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), res.getString("TIC_descrizione"), res.getInt("UT_id_apertura"), res.getInt("UT_id_accettazione"));
					tickets.add(ticket);
					
				}
				conn.close();
				
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

	public ArrayList<Ticket> getNM(Map<String, String[]> params) {
		// TODO Auto-generated method stub
		establishConnection();
		String ticketNM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_nome = ? ";
		ResultSet res;
		
		try(
			java.sql.PreparedStatement ticketNM_query = conn.prepareStatement(ticketNM);
			){
							
				res = ticketNM_query.executeQuery();
				while(res.next()) {
					
					Ticket ticket = new Ticket(res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), res.getString("TIC_descrizione"), res.getInt("UT_id_apertura"), res.getInt("UT_id_accettazione"));
					tickets.add(ticket);
					
				}
				conn.close();
				
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

	public ArrayList<Ticket> getCM(Map<String, String[]> params) {
		// TODO Auto-generated method stub
		establishConnection();
		String ticketCM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_classe = ?";
		ResultSet res;
		
		
		try(
			java.sql.PreparedStatement ticketCM_query = conn.prepareStatement(ticketCM);
			){
							
				res = ticketCM_query.executeQuery();
				while(res.next()) {
					
					Ticket ticket = new Ticket(res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), res.getString("TIC_descrizione"), res.getInt("UT_id_apertura"), res.getInt("UT_id_accettazione"));
					tickets.add(ticket);
					
				}
				conn.close();
				
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

	public ArrayList<Ticket> getLM(Map<String, String[]> params) {
		
		establishConnection();
		String ticketLM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ? AND u.UT_localita = ?";
		ResultSet res;
			
			try(
				java.sql.PreparedStatement ticketLM_query = conn.prepareStatement(ticketLM);
				){
								
					res = ticketLM_query.executeQuery();
					while(res.next()) {
						
						Ticket ticket = new Ticket(res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), res.getString("TIC_descrizione"), res.getInt("UT_id_apertura"), res.getInt("UT_id_accettazione"));
						tickets.add(ticket);
						
					}
					conn.close();
					
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

	public ArrayList<Ticket> getM(Map<String, String[]> params) {
		
		establishConnection();
		
		
		String ticketM = "SELECT * FROM tickets t INNER JOIN utenti u ON t.UT_id_apertura = u.UT_id WHERE t.TIC_materia = ?";
		ResultSet res;
		
		
		try(
			java.sql.PreparedStatement ticketM_query = conn.prepareStatement(ticketM);
			){
							
				res = ticketM_query.executeQuery();
				while(res.next()) {
					
					Ticket ticket = new Ticket(res.getString("TIC_stato"), res.getString("TIC_materia"), res.getString("TIC_tags"), res.getString("TIC_descrizione"), res.getInt("UT_id_apertura"), res.getInt("UT_id_accettazione"));
					tickets.add(ticket);
					
				}
				conn.close();
				
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
	
	

}
