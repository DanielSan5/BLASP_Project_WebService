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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.security.auth.login.CredentialNotFoundException;

import org.apache.tomcat.util.codec.binary.Base64;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

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
		
		response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		response.addHeader("Access-Control-Allow-Methods", "GET");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		response.addHeader("Access-Control-Expose-Headers", "Set-cookie");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter(); 
		JsonObject jsonResponse = new JsonObject();
		Gson g = new Gson();
		try {
			//Estrazione del token dall'header
			String[] hd = request.getHeader("Cookie").split("[=]");
			String jwtToken = hd[1];
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
		        }
			}else {
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "errore nella sintassi della richiesta");
			}
					
		}catch(InvalidParameterException e) {
			
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
		
		} catch ( SQLException | NoSuchAlgorithmException e) {
			
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
			

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		response.addHeader("Access-Control-Allow-Methods", "POST");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		response.addHeader("Access-Control-Expose-Headers", "Set-cookie");
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
		try {		
			
			JsonObject user = g.fromJson(body, JsonObject.class);
			
			String avviso = user.get("avviso").getAsString();
			String ticket_id = user.get("numero_ticket").getAsString();
			//Estrazione del token dall'header
			String[] hd = request.getHeader("Cookie").split("[=]");
			String jwtToken = hd[1];
			//String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
			
			String[] toCheck = {ticket_id, avviso, jwtToken};
			
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
	
					validator.validate(jwtToken);
		
					QueryHandler_ticket queryTicket = new QueryHandler_ticket();
						
					int utente_id = queryTicket.getUserIdFromTicket(Integer.parseInt(ticket_id));
	
					QueryHandler_flags queryUtenteAvviso = new QueryHandler_flags();
					
					int avviso_id = queryUtenteAvviso.inserisciAvvisoGetId(avviso,Integer.parseInt(ticket_id), utente_id);
		
					response.setStatus(201);
					jsonResponse.addProperty("stato", "confermato");
					jsonResponse.addProperty("desc", "avviso:" + avviso_id + " creato");
					
		        }
			}else {
				
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore");
				jsonResponse.addProperty("desc", "errore nella sintassi");
		
			}		
		}catch(InvalidParameterException e) {
			
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
		
		} catch (SQLException | NoSuchAlgorithmException e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
			e.printStackTrace();
		} catch (CredentialNotFoundException e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "nessun risultato");
			e.printStackTrace();
		}catch(JsonSyntaxException | NullPointerException | NumberFormatException e) {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "formato non supportato");
			e.printStackTrace();
		}finally {
			out.println(jsonResponse.toString());
		}

	}
	
	
}
