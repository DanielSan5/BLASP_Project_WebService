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

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import classes.Avviso;
import classes.Checks;
import classes.JwtVal;
import classes.QueryHandler;
import classes.QueryHandler_flags;

/**
 * Servlet implementation class Segnalazioni
 */
@WebServlet("/flags")
public class Segnalazioni extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Segnalazioni() {
        super();
        // TODO Auto-generated constructor stub
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
		
		String [] toCheck = {jwtToken};
		if(Checks.isNotBlank(toCheck)) {
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
				
					QueryHandler_flags queryFlags = new QueryHandler_flags();
					ArrayList<String> flagsDesc = queryFlags.getFlags(utente_id);
					
					if(flagsDesc != null) {
						response.setStatus(200);
						jsonResponse.addProperty("stato", "confermato");
						jsonResponse.addProperty("descrizione", "ottenimento segnalazioni");
						jsonResponse.add("avvisi", g.toJsonTree(flagsDesc));
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
		}else {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "errore nella sintassi");
		
		}
		
		
		out.println(jsonResponse.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	
	}

}
