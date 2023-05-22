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
			
			String [] toCheck = {jwtToken};
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
					QueryHandler queryUser = new QueryHandler();
					int utente_id = queryUser.getUserId(jwt.getClaim("sub-email").asString());
	
					QueryHandler_flags queryFlags = new QueryHandler_flags();
					ArrayList<String> flagsDesc = queryFlags.getFlags(utente_id);
	
					response.setStatus(200);
					jsonResponse.addProperty("stato", "confermato");
					jsonResponse.addProperty("descrizione", "ottenimento segnalazioni");
					jsonResponse.add("avvisi", g.toJsonTree(flagsDesc));
		        }
			}else {
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "errore nella sintassi");
			
			}
		}catch(InvalidParameterException e) {
			
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
		
		} catch (CredentialNotFoundException e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "nessn risultato");
			e.printStackTrace();
			
		} catch (SQLException | NoSuchAlgorithmException e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("descrizione", "non autorizzato");
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
		try {//trsformazione stringa in oggetto json
			
			Gson g = new Gson();
			JsonObject user = g.fromJson(body, JsonObject.class);
			
			//acquisizione valore delle chiavi
			String email = user.get("email").getAsString();
			String descrizione = user.get("descrizione").getAsString();
			//Estrazione del token dall'header
			String[] hd = request.getHeader("Cookie").split("[=]");
			String jwtToken = hd[1];
			//String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
			String[] toCheck = {jwtToken, email};
			
			
			
			if(Checks.isNotBlank(toCheck) && Checks.isValidEmail(email) ) {
				
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
					String segnalatore_email = jwt.getClaim("sub-email").asString();
					
					QueryHandler queryUser = new QueryHandler();
					
					int user_segnalatore_id = queryUser.getUserId(segnalatore_email);
					int user_segnalato_id = queryUser.getUserId(email);
					
					if(Checks.isNotBlockedUser(user_segnalato_id) && Checks.hasNotThreFlags(user_segnalato_id)) {
						
						QueryHandler_flags queryFlags = new QueryHandler_flags();
						
						int segnalazioneID = queryFlags.inserisciSegnalazioneGetId(user_segnalato_id, user_segnalatore_id, descrizione);
						
						response.setStatus(201);
						jsonResponse.addProperty("stato", "confermato");
						jsonResponse.addProperty("desc", "segnalazione:" + segnalazioneID + " creata");
						
					}else {
						
						response.setStatus(400);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("desc", "utente gia in stato di ban");
				
					}
				
		        }
			}else {
				
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("desc", "errore nella sintassi");
		
			}
				
		}catch(InvalidParameterException e) {
			
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
		
		} catch (CredentialNotFoundException e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "nessun risultato");
			System.out.println("no results");
			e.printStackTrace();
			
		} catch (SQLException | NoSuchAlgorithmException e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("descrizione", "errore nell'elaborazione della richiesta");
			e.printStackTrace();
			
		}catch(JsonSyntaxException | NullPointerException  | NumberFormatException e) {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "formato non supportato");
			e.printStackTrace();
		}finally {
			out.println(jsonResponse.toString());
		}
	
	}

}
