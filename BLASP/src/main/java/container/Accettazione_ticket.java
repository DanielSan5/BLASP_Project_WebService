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
import java.sql.SQLException;

import javax.security.auth.login.CredentialNotFoundException;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import classes.Checks;
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
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		
		//acquisizione delle chiavi
		String numeroTicket = user.get("numero_ticket").getAsString();	
		
		String [] toCheck = {jwtToken, numeroTicket};
		
		if(Checks.isNotBlank(toCheck)) {
		
			QueryHandler_ticket queryForThis = new QueryHandler_ticket();
			
			try{
				
				boolean hasTicketId = queryForThis.hasTicketId(Integer.parseInt(numeroTicket));
				
				final JwtVal validator = new JwtVal();
		
				DecodedJWT jwtDecoded =  validator.validate(jwtToken);
				String email = jwtDecoded.getClaim("sub-email").asString();
				QueryHandler queryUser = new QueryHandler();
				int user_id = queryUser.getUserId(email);
				
				//MI SERVE L'ID DELL'UTENTE CHE HA ACCETTATO IL TICKET
				
				if(hasTicketId) {
	
					switch(user_id) {
						case 1:
							
							//esecuzione della query
							queryForThis.modificaStatoTicket(user_id, Integer.parseInt(numeroTicket));
							
							response.setStatus(200);
							jsonResponse.addProperty("stato", "confermato");
							jsonResponse.addProperty("desc", "stato modificato");
							
							Ticket ticket_info = queryForThis.getTicketFromId(Integer.parseInt(numeroTicket));
							
							if(ticket_info == null) {
								response.setStatus(400);
								jsonResponse.addProperty("ticket_info", "impossibile restituire dati ticket");
							}
							
							jsonResponse.add("ticket_info", g.toJsonTree(ticket_info));
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
					
				
				}else {
					response.setStatus(400);
					jsonResponse.addProperty("stato", "errore client");
					jsonResponse.addProperty("desc", "sintassi errata nella richiesta");
				}
				
				
			}catch(InvalidParameterException e) {
			
				response.setStatus(403);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("desc", "utente non autorizzato");
				System.out.println("not authorized token");
				e.printStackTrace();
			
			}catch(SQLException | NumberFormatException | CredentialNotFoundException e) {
				
				response.setStatus(500);
				jsonResponse.addProperty("stato", "errore server");
				jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
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
