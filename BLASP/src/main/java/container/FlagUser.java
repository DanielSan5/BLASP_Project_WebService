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

import classes.Checks;
import classes.JwtVal;
import classes.QueryHandler;
import classes.QueryHandler_flags;
import classes.Ticket;
import classes.Utente;

/**
 * Servlet implementation class FlagUser
 */
@WebServlet("/FlagUser")
public class FlagUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FlagUser() {
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
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "POST");
		response.setContentType("application/json");
		//output writer
		PrintWriter out = response.getWriter(); 
		//input reader
		BufferedReader in_body = request.getReader();
		//stringBuilder per costruire una stringa dal messaggio in formato json
		JsonObject jsonResponse = new JsonObject();
		StringBuilder sb = new StringBuilder();
		String line;
		String body;
		
		//acquisizione stringa dal body
		while((line = in_body.readLine()) != null) {
			sb.append(line);
		}
		
		body = sb.toString();
		//trsformazione stringa in oggetto json
		Gson g = new Gson();
		JsonObject user = g.fromJson(body, JsonObject.class);
		
		//acquisizione valore delle chiavi
		String email = user.get("email").getAsString();
			//Estrazione del token dall'header
			String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
			String[] toCheck = {jwtToken, email};
			
		if(Checks.isNotBlank(toCheck) && Checks.isValidEmail(email)) {
			
			final JwtVal validator = new JwtVal();
			
			try {
				
				DecodedJWT jwt = validator.validate(jwtToken);
				String segnalatore_email = jwt.getClaim("sub-email").asString();
				
				QueryHandler queryUser = new QueryHandler();
				int user_segnalatore_id = queryUser.getUserId(segnalatore_email);
				int user_segnalato_id = queryUser.getUserId(email);
				
				QueryHandler_flags queryFlags = new QueryHandler_flags();
				
				int segnalazioneID = queryFlags.inserisciSegnalazione(user_segnalato_id, user_segnalatore_id);
				
				response.setStatus(201);
				jsonResponse.addProperty("stato", "confermato");
				jsonResponse.addProperty("desc", "segnalazione:" + segnalazioneID + " creata");
				
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
			jsonResponse.addProperty("stato", "errore");
			jsonResponse.addProperty("desc", "errore nella sintassi");
	
		}
		
		out.println(jsonResponse.toString());
	}

}
