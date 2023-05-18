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

import classes.Checks;
import classes.JwtVal;
import classes.QueryHandler;
import classes.Ticket;
import classes.Utente;

/**
 * Servlet implementation class Users
 */
@WebServlet("/users")
public class Users extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Users() {
        super();
       
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET");
		
		PrintWriter out = response.getWriter(); 
		JsonObject jsonResponse = new JsonObject();
		Gson g = new Gson();
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		
		String[] toCheck = {jwtToken};
		if(Checks.isNotBlank(toCheck)) {
			final JwtVal validator = new JwtVal();
			
			try{
				
				//se non viene autorizzato lancia eccezzione gestita nel catch sotto
				validator.validate(jwtToken);
			
				String email = request.getParameter("email");
				QueryHandler queryForThis = new QueryHandler();
				int user_id = queryForThis.getUserId(email);
				Utente userData = queryForThis.getUserData(user_id);
				
				ArrayList<Ticket> userTickets = queryForThis.getUserTickets(user_id);
				
				if(userData != null) {
					
					response.setStatus(200);
					jsonResponse.addProperty("stato", "confermato");
					jsonResponse.addProperty("desc", " ottenimento profilo dell'utente");
					jsonResponse.add("user_info", g.toJsonTree(userData));
					jsonResponse.add("user_tickets", g.toJsonTree(userTickets));
					
				}else {
					
					response.setStatus(500);
					jsonResponse.addProperty("stato", "errore server");
					jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
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
			}
			finally {
				out.println(jsonResponse.toString());
			}
		}else {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "sintassi errata");
		}
		
		out.println(jsonResponse.toString());
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(405);
	}

}
