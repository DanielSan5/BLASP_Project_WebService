package classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class QueryHandler_filters {

	private static String db_url = "jdbc:mysql://localhost:3306/ticketing";
    private static String db_driver = "com.mysql.jdbc.Driver";
    private static String db_user = "root";
    private static String db_password = "";
    private Connection conn;
  

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
	

	public ArrayList<Ticket> getFor(String filter_comb) {
		// TODO Auto-generated method stub
		/*
		 * combinazioni possibili:
		 * M -> materia
		 * LM -> localita e materia
		 * CM -> classe e materia
		 * NM -> nome e materia
		 * LCM -> localita, classe e materia
		 * LNM -> localita, nome e materia
		 * CNM -> classe, nome e materia
		 */
		switch(filter_comb) {
		
			case "M":
				return getM();
				
			case "LM":
				return getLM();
				
			case "CM":
				return getCM();
				
			case "NM":
				return getNM();
				
			case "LCM":
				return getLCM();
				
			case "LNM":
				return getLNM();
				
			case "CNM":
				return getCNM();
			default:
				return null;
		
		
		}
	}
	
	
	
	private ArrayList<Ticket> getCNM() {
		// TODO Auto-generated method stub
		return null;
	}

	private ArrayList<Ticket> getLNM() {
		// TODO Auto-generated method stub
		return null;
	}

	private ArrayList<Ticket> getLCM() {
		// TODO Auto-generated method stub
		return null;
	}

	private ArrayList<Ticket> getNM() {
		// TODO Auto-generated method stub
		return null;
	}

	private ArrayList<Ticket> getCM() {
		// TODO Auto-generated method stub
		return null;
	}

	private ArrayList<Ticket> getLM() {
		// TODO Auto-generated method stub
		return null;
	}

	private ArrayList<Ticket> getM() {
		// TODO Auto-generated method stub
		return null;
	}

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
}
