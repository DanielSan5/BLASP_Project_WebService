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

import classes.QueryHandler;
import classes.QueryHandler_filters;
import classes.QueryHandler_ticket;
import classes.QueryHandler_avviso;
import classes.Ticket;

import classes.JwtVal;
import classes.QueryHandler;

/**
 * Servlet implementation class Avvisi
 */
@WebServlet("/Flag_ticket")
public class Avvisi extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String jwtToken;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Avvisi() {
        super();
        // TODO Auto-generated constructor stub
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
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
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
		jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		
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
					
					QueryHandler_avviso queryUtenteAvviso = new QueryHandler_avviso();
					
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
