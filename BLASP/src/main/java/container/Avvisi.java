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
import java.util.ArrayList;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import classes.QueryHandler;
import classes.QueryHandler_filters;
import classes.QueryHandler_ticket;
import classes.QueryHandler_flags;
import classes.Ticket;
import classes.Avviso;
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
    //Empty input check
    public boolean isNotBlank(String avviso, int id_ticket) {
	
	 if(avviso.isBlank() || id_ticket == 0) {
	  	return false;
	  }
	   return true;	   
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
	
		final JwtVal validator = new JwtVal();
		
		try {
		
			DecodedJWT jwt = validator.validate(jwtToken);
			QueryHandler queryUser = new QueryHandler();
			int utente_id = queryUser.getUserId(jwt.getClaim("sub-email").asString());
			
			//controllo sull'ID utente del ticket
			if(utente_id == 0){
		
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "nessun risultato");
				
			}else if(utente_id == -1) {
				
				response.setStatus(500);
				jsonResponse.addProperty("stato", "errore server");
				jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
				
			}else{
			
				QueryHandler_flags queryAvviso = new QueryHandler_flags();
				ArrayList<Avviso> avvisi = queryAvviso.getAvvisi(utente_id);
				
				if(avvisi != null) {
					response.setStatus(200);
					jsonResponse.addProperty("stato", "confermato");
					jsonResponse.addProperty("descrizione", "ottenimento avvisi");
					jsonResponse.add("avvisi", g.toJsonTree(avvisi));
				}else {
					response.setStatus(500);
					jsonResponse.addProperty("stato", "errore server");
					jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
				}
				
			} 
					
		}catch(InvalidParameterException e) {
			
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
		
		}catch(Exception e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "nessun risultato");
			
			System.out.println("no results");
			e.printStackTrace();
			
		}finally {
			out.println(jsonResponse.toString());
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
		int ticket_id = user.get("numero_ticket").getAsInt();
		
		//Estrazione del token dall'header
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		
		if(isNotBlank(avviso, ticket_id)) {
			

			final JwtVal validator = new JwtVal();
			
			try {
			
				DecodedJWT jwt = validator.validate(jwtToken);
				
				QueryHandler_ticket queryTicket = new QueryHandler_ticket();
					
					int utente_id = queryTicket.getUtenteId(ticket_id);
					
					//controllo sull'ID utente del ticket
					if(utente_id == 0){
						
						response.setStatus(400);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("descrizione", "nessun risultato");
						
					}else if(utente_id == -1) {
						
						response.setStatus(500);
						jsonResponse.addProperty("stato", "errore server");
						jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
						
					}else{
					
					QueryHandler_flags queryUtenteAvviso = new QueryHandler_flags();
					
					switch(queryUtenteAvviso.inserisciAvviso(avviso,ticket_id, utente_id)) {
					
					case 0:
						
						response.setStatus(500);
						jsonResponse.addProperty("stato", "errore server");
						jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
						break;
						
					case -1:
					
						response.setStatus(500);
						jsonResponse.addProperty("stato", "errore server");
						jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
						break;
						
					default:
						
						response.setStatus(201);
						jsonResponse.addProperty("stato", "confermato");
						jsonResponse.addProperty("desc", "avviso creato");
					
					}
					
					
				}				
						
			}catch(InvalidParameterException e) {
				
				response.setStatus(403);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "non autorizzato");
				System.out.println("not authorized token");
				e.printStackTrace();
			
			}catch(Exception e) {
				
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "nessun risultato");
				
				System.out.println("not created");
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
