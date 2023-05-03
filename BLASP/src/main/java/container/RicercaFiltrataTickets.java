package container;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import classes.JwtVal;
import classes.QueryHandler;
import classes.Ticket;

/**
 * Servlet implementation class RicercaFiltrataTickets
 */
@WebServlet("/Get_tickets")
public class RicercaFiltrataTickets extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	String filter;
	String value;
	String jwtToken;
	QueryHandler queryForThis = new QueryHandler();
	String risposta;
	ArrayList<Ticket> tickets = new ArrayList<Ticket>();
	
	
	//Authorization empty check
	public boolean isValidAuthorization() {
		if(jwtToken == null || jwtToken.isBlank())
			return false;
		else 
			return true;
	}
       
	//Filter check
		public boolean isValidFilter() {
			if (filter.isBlank())
				return false;
			else {
				if (filter == "localita " || filter == "stato" || filter == "classe")
					return true;
				else
					return false;
			}
		}
	
	//Value check
	public boolean isValidValue() {
		
		boolean checkResult = false;
		
		if (value.isBlank()) {
			
			return false;
			
		}else {
			
			switch(filter) {
				
				case "localita":
					
					int hasLocalita = queryForThis.hasLocalita(value);
					if(hasLocalita == 0 || hasLocalita == -1) {
						return false;
						
					}else {
						return true;
						
					}
					
					
				case "stato":
					
					if (value == "libero" || value == "occupato") {
						return true;
						
					}else {
						return false;
						
					}
					
					
				/*case "classe":
					
					int valueInt = Integer.parseInt(value);
					if (valueInt >= 1 && valueInt <= 5) {
						checkResult = true;
						break;
					}else {
						checkResult = false;
						break;
					}*/	
				default:
					return false;
					
			}
			
		}
		
	}
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RicercaFiltrataTickets() {
        super();
        
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		PrintWriter out = response.getWriter();
		//Estrazione dei parametri dalla richiesta GET
		filter = request.getParameter("filter");
		value = request.getParameter("value");
		
		//Estrazione del token dall'header
		jwtToken = request.getHeader("Authorization");
		
		if(isValidValue() && isValidFilter() && isValidAuthorization() ) {
			
			
			final JwtVal validator = new JwtVal();
			
			try{
				
				DecodedJWT jwtDecoded =  validator.validate(jwtToken);
				
				String username = jwtDecoded.getClaim("sub").asString();
				
				tickets = queryForThis.getTickets(filter, value);
				
				risposta = tickets.toString();
				
			}catch(InvalidParameterException e) {
				
				response.setStatus(401);
				risposta = "non autorizzato";
				System.out.println("not authorized token");
				e.printStackTrace();
				
			}
			
		}else {
			response.setStatus(400);
			risposta = "bad request";
		}
		
		
		
		
		//da trasformare in formato json
		/*
		 * le risposte in formato json conterranno:
		 * stati (andatura della richiesta, coincidenza password, controlli sugli input...)
		 * descrizione
		 * eventuali dati
		 */
		out.println(risposta);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(405);
	}

}
