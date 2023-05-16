package container;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import classes.JwtVal;
import classes.QueryHandler;
import classes.QueryHandler_filters;
import classes.QueryHandler_ticket;
import classes.Ticket;
/**
 * Servlet implementation class AccettazioneTicket
 */
@WebServlet("/Accettazione_ticket")
public class Accettazione_ticket extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String jwtToken;   
	
	
	 //Empty input check
	   public boolean isNotBlank(int numero_ticket) {
	  	   if(numero_ticket == 0) {
	  		   return false;
	  	   }
	  	   return true;	   
	     }
	
	 //Authorization empty check
		private boolean isValidAuthorization() {
			if(jwtToken == null || jwtToken.isBlank())
				return false;
			else 
				return true;
		}
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Accettazione_ticket() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(405);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(405);
	}

	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Methods", "PUT");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter(); 
		BufferedReader in_body = request.getReader();
		JsonObject jsonResponse = new JsonObject();
		
		//stringBuilder per costruire una stringa dal messaggio in formato json
		StringBuilder sb = new StringBuilder();
		String line;
		String body;
		
		while((line = in_body.readLine()) != null) {
			sb.append(line);
		}
		
		body = sb.toString();
		
		Gson g = new Gson();
		JsonObject user = g.fromJson(body, JsonObject.class);
		
		//Estrazione del token dall'header
		jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		
		//acquisizione delle chiavi
		int numeroTicket = user.get("numero_ticket").getAsInt();	
		
		if(isValidAuthorization() && isNotBlank(numeroTicket)) {
		
			QueryHandler_ticket queryForThis = new QueryHandler_ticket();
			int hasTicketId = queryForThis.hasTicketId(numeroTicket);
			
			final JwtVal validator = new JwtVal();
			
			try{
				
				DecodedJWT jwtDecoded =  validator.validate(jwtToken);
				String email = jwtDecoded.getClaim("sub-email").asString();
				QueryHandler queryUser = new QueryHandler();
				int user_id = queryUser.getUserId(email);
				
				//MI SERVE L'ID DELL'UTENTE CHE HA ACCETTATO IL TICKET
				
				switch(hasTicketId) {
				case 1:
					
					
					switch(user_id) {
					case 1:
						
						//esecuzione della query
						int accettazioneTicket = queryForThis.modificaStatoTicket(user_id, numeroTicket);
						
						if(accettazioneTicket == 1) {
							response.setStatus(200);
							jsonResponse.addProperty("stato", "confermato");
							jsonResponse.addProperty("desc", "stato modificato");
							
							Ticket ticket_info = queryForThis.getTicketFromId(numeroTicket);
							
							if(ticket_info == null) {
								response.setStatus(400);
								jsonResponse.addProperty("ticket_info", "impossibile restituire dati ticket");
							}
							
							jsonResponse.add("ticket_info", g.toJsonTree(ticket_info));
							
						}else if(accettazioneTicket == 0 || accettazioneTicket == -1) {
							response.setStatus(400);
							jsonResponse.addProperty("stato", "errore client");
							jsonResponse.addProperty("desc", "sintassi errata nella richiesta");
						}
						
						break;
					case 0:
						response.setStatus(400);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("descrizione", "utente non esistente");
						break;
						
					default:
						response.setStatus(500);
						jsonResponse.addProperty("stato", "errore server");
						jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");	
						break;
					}
					
					
					break;
				
				case 0:
					
					response.setStatus(400);
					jsonResponse.addProperty("stato", "errore client");
					jsonResponse.addProperty("desc", "sintassi errata nella richiesta");
					
					
					break;
					
				case -1:
					
					response.setStatus(500);
					jsonResponse.addProperty("stato", "errore server");
					jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
					
					break;
					
				
			}
				
				
			}catch(InvalidParameterException e) {
			
				response.setStatus(403);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("desc", "utente non autorizzato");
				System.out.println("not authorized token");
				e.printStackTrace();
			
			}catch(Exception e) {
				
				response.setStatus(500);
				jsonResponse.addProperty("stato", "errore server");
				jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
				System.out.println("no results");
				e.printStackTrace();
				
			}finally {
				out.println(jsonResponse.toString());
			}
			
		}else {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("desc", "sintassi errata nella richiesta");
		}
		
		out.println(jsonResponse.toString());
	}
	
	
}
