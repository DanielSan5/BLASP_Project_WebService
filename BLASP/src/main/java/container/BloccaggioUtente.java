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

import javax.security.auth.login.CredentialNotFoundException;

import org.apache.tomcat.util.codec.binary.Base64;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import classes.Checks;
import classes.JwtVal;
import classes.QueryHandler;

/**
 * Servlet implementation class BloccaggioUtente
 */
@WebServlet("/block")
public class BloccaggioUtente extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BloccaggioUtente() {
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
		
		response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		response.addHeader("Access-Control-Allow-Methods", "PUT");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		response.addHeader("Access-Control-Expose-Headers", "Set-cookie");
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
		try{
			Gson g = new Gson();
			JsonObject user = g.fromJson(body, JsonObject.class);
			
			//Estrazione del token dall'header
			String[] hd = request.getHeader("Cookie").split("[=]");
			String jwtToken = hd[1];
			//String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
			String toBlock_email = user.get("email").getAsString();
			QueryHandler queryUser = new QueryHandler();
			
			
			String [] toCheck = {jwtToken};
			final JwtVal validator = new JwtVal();

			if(Checks.isValidEmail(toBlock_email) && Checks.isNotBlank(toCheck)) {
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
				
					DecodedJWT jwtDecoded =  validator.validate(jwtToken);
					String email = jwtDecoded.getClaim("sub-email").asString();
					
					
					int userAdmin_id = queryUser.getUserId(email);	
					boolean isAdmin = queryUser.isUserAdmin(userAdmin_id);
					
					if(isAdmin) {
						
							
						int user_id_toBlock = queryUser.getUserId(toBlock_email);	//USER ID di quello da bloccare
						queryUser.blockUser(user_id_toBlock);
						response.setStatus(200);
						jsonResponse.addProperty("stato", "confermato");
						jsonResponse.addProperty("desc", "utente bloccato");
						
					}else {
						
						response.setStatus(403);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("desc", "utente non autorizzato");
						System.out.println("not authorized action");
						
					}
		        }
			}else {
				
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore");
				jsonResponse.addProperty("desc", "errore nella sintassi");
			}	
			
		}catch(InvalidParameterException e) {
		
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("desc", "utente non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
		
		}catch(SQLException | NoSuchAlgorithmException e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
			System.out.println("no results");
			e.printStackTrace();
			
		} catch (CredentialNotFoundException e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "nessun risultato");
			e.printStackTrace();
		}catch(JsonSyntaxException | NullPointerException  | NumberFormatException e) {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "formato non supportato");
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "oggetto inesistente");
			e.printStackTrace();
			
		}finally {
			out.println(jsonResponse.toString());
		}
		
	}

}
