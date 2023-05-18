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
import java.util.ArrayList;

import javax.security.auth.login.CredentialNotFoundException;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import classes.QueryHandler;
import classes.QueryHandler_filters;
import classes.QueryHandler_ticket;
import classes.QueryHandler_flags;
import classes.Ticket;
import classes.Avviso;
import classes.Checks;
import classes.JwtVal;
import classes.QueryHandler;

/**
 * Servlet implementation class Avvisi
 */
@WebServlet("/avvisi")
public class Avvisi extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Avvisi() {
        super();
    }
   
   
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter(); 
		JsonObject jsonResponse = new JsonObject();
		Gson g = new Gson();
		
		//Estrazione del token dall'header
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		String[] toCheck = {jwtToken};
		
		if(Checks.isNotBlank(toCheck)) {
			
			final JwtVal validator = new JwtVal();
			
			try {
			
				DecodedJWT jwt = validator.validate(jwtToken);
				String email =  jwt.getClaim("sub-email").asString();
				QueryHandler queryUser = new QueryHandler();
				int utente_id = queryUser.getUserId(email);

				QueryHandler_flags queryAvviso = new QueryHandler_flags();
				ArrayList<Avviso> avvisi = queryAvviso.getAvvisi(utente_id);

				response.setStatus(200);
				jsonResponse.addProperty("stato", "confermato");
				jsonResponse.addProperty("descrizione", "ottenimento avvisi");
				jsonResponse.add("avvisi", g.toJsonTree(avvisi));
			
						
			}catch(InvalidParameterException e) {
				
				response.setStatus(403);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "non autorizzato");
				System.out.println("not authorized token");
				e.printStackTrace();
			
			} catch ( SQLException e) {
				
				response.setStatus(500);
				jsonResponse.addProperty("stato", "errore server");
				jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
				e.printStackTrace();
				
			} catch (CredentialNotFoundException e) {
				
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "nessun risultato");
				e.printStackTrace();
			}finally {
				out.println(jsonResponse.toString());
			}
			
		}else {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "errore nella sintassi della richiesta");
		}
			
		out.println(jsonResponse.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "POST");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter(); 
		BufferedReader in_body = request.getReader();
		JsonObject jsonResponse = new JsonObject();
		Gson g = new Gson();
		StringBuilder sb = new StringBuilder();
		String line;
		String body;

		while((line = in_body.readLine()) != null) {
			sb.append(line);
		}
				
		body = sb.toString();
				
		JsonObject user = g.fromJson(body, JsonObject.class);
		
		String avviso = user.get("avviso").getAsString();
		String ticket_id = user.get("numero_ticket").getAsString();
		//Estrazione del token dall'header
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		
		String[] toCheck = {ticket_id, avviso, jwtToken};
		
		if(Checks.isNotBlank(toCheck)) {
			
			final JwtVal validator = new JwtVal();
			
			try {
			
				validator.validate(jwtToken);
	
				QueryHandler_ticket queryTicket = new QueryHandler_ticket();
					
				int utente_id = queryTicket.getUserIdFromTicket(Integer.parseInt(ticket_id));

				QueryHandler_flags queryUtenteAvviso = new QueryHandler_flags();
				
				int avviso_id = queryUtenteAvviso.inserisciAvvisoGetId(avviso,Integer.parseInt(ticket_id), utente_id);
	
				response.setStatus(201);
				jsonResponse.addProperty("stato", "confermato");
				jsonResponse.addProperty("desc", "avviso:" + avviso_id + " creato");
		
						
			}catch(InvalidParameterException e) {
				
				response.setStatus(403);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "non autorizzato");
				System.out.println("not authorized token");
				e.printStackTrace();
			
			} catch (SQLException | NumberFormatException e) {
				
				response.setStatus(500);
				jsonResponse.addProperty("stato", "errore server");
				jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
				e.printStackTrace();
			} catch (CredentialNotFoundException e) {
				
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "nessun risultato");
				e.printStackTrace();
			}finally {
				out.println(jsonResponse.toString());
			}
		}else {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore");
			jsonResponse.addProperty("desc", "errore nella sintassi");
	
		}
		
		out.println(jsonResponse.toString());

	}
	
	
}
