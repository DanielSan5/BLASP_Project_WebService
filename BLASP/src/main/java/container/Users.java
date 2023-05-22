package container;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.security.auth.login.CredentialNotFoundException;

import org.apache.tomcat.util.codec.binary.Base64;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import classes.Avviso;
import classes.Checks;
import classes.JwtVal;
import classes.QueryHandler;
import classes.Segnalazione;
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
		response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		response.addHeader("Access-Control-Allow-Methods", "GET");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		response.addHeader("Access-Control-Expose-Headers", "Set-cookie");
		PrintWriter out = response.getWriter(); 
		JsonObject jsonResponse = new JsonObject();
		Gson g = new Gson();
		
		try{
			
			String[] hd = request.getHeader("Cookie").split("[=]");
			String jwtToken = hd[1].split("[;]")[0];
			//String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
			
			String[] toCheck = {jwtToken};
			if(Checks.isNotBlank(toCheck)) {
				
				QueryHandler queryForThis = new QueryHandler();
				
				//logica di logout
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
		        byte[] cipheredTokenDigest = digest.digest(jwtToken.getBytes());
		        String jwtTokenDigestInB64 = Base64.encodeBase64String(cipheredTokenDigest);
		        
		        if(queryForThis.isTokenRevoked(jwtTokenDigestInB64)) {
		        	
		        	response.setStatus(401);
					jsonResponse.addProperty("stato", "errore client");
					jsonResponse.addProperty("desc", "utente non autorizzato");
					
		        }else {
					final JwtVal validator = new JwtVal();
	
					//se non viene autorizzato lancia eccezzione gestita nel catch sotto
					validator.validate(jwtToken);
					
					String email = request.getParameter("email");
					
					int user_id = queryForThis.getUserId(email);
					Utente userData = queryForThis.getUserData(user_id);
					
					ArrayList<Ticket> userTickets = queryForThis.getUserTickets(user_id);
					ArrayList<Avviso> userAvvisi = queryForThis.getUserAvvisi(user_id);
					ArrayList<Segnalazione> userSegnalazioni = queryForThis.getUserSegnalazioni(user_id);
					
					response.setStatus(200);
					jsonResponse.addProperty("stato", "confermato");
					jsonResponse.addProperty("desc", " ottenimento profilo dell'utente");
					jsonResponse.add("user_info", g.toJsonTree(userData));
					jsonResponse.add("user_tickets", g.toJsonTree(userTickets));
					jsonResponse.add("user_avvisi", g.toJsonTree(userAvvisi));	
					jsonResponse.add("user_segnalazioni", g.toJsonTree(userSegnalazioni));	
		        }
			}else {
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "sintassi errata");
			}
				
		}catch(InvalidParameterException | NullPointerException e) {
			
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
			
		} catch (CredentialNotFoundException e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "nessun risultato");
			e.printStackTrace();
			
		} catch (SQLException | NoSuchAlgorithmException e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("descrizione", "errore nell'elaborazione della richiesta");
			e.printStackTrace();
		}finally {
			out.println(jsonResponse.toString());
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(405);
	}

}
