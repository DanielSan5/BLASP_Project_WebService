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

import classes.JwtVal;
import classes.QueryHandler;

/**
 * Servlet implementation class BloccaggioUtente
 */
@WebServlet("/BloccaggioUtente")
public class BloccaggioUtente extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String jwtToken;  
	
	
	 //Email check (Aldini email)
    private boolean isValidEmail(String email) {
     	
     	String regexPattern = "^[a-zA-Z]+\\.[a-zA-Z]+@(aldini\\.istruzioneer\\.it|avbo\\.it)$";
     	
     	if((email.isBlank()) || (email.matches(regexPattern) == false))
     		return false;
     	else 	
     		return true;
     }
	
	
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
		
	
	response.addHeader("Access-Control-Allow-Methods", "PUT");
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
	
	Gson g = new Gson();
	JsonObject user = g.fromJson(body, JsonObject.class);
	
	//Estrazione del token dall'header
	jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
	
	//acquisizione delle chiavi
	String toBlock_email = user.get("email").getAsString();
	
		if(isValidEmail(toBlock_email)) {
		
			final JwtVal validator = new JwtVal();
			
			try{
				
				DecodedJWT jwtDecoded =  validator.validate(jwtToken);
				String email = jwtDecoded.getClaim("sub-email").asString();
				
				QueryHandler queryUser = new QueryHandler();
				int userAdmin_id = queryUser.getUserIdAdmin(email);			//USER ID dell'ADMIN
				int user_id_toBlock = queryUser.getUserId(toBlock_email);	//USER ID di quello da bloccare
				
				switch(userAdmin_id) {
				case 0:
					
					response.setStatus(400);
					jsonResponse.addProperty("stato", "errore client");
					jsonResponse.addProperty("descrizione", "utente non Admin o inesistente");
					break;
				
				case -1:
					response.setStatus(500);
					jsonResponse.addProperty("stato", "errore server");
					jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");			
					break;
					
				default:
					
					switch(user_id_toBlock) {
					case 0:
						
						response.setStatus(400);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("descrizione", "utente da bloccare non presente");
						break;
					
					case -1:
						response.setStatus(500);
						jsonResponse.addProperty("stato", "errore server");
						jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");			
						break;
						
					default:
						if(queryUser.blockUser(user_id_toBlock) == 1) {
							response.setStatus(200);
							jsonResponse.addProperty("stato", "confermato");
							jsonResponse.addProperty("desc", "utente bloccato");
						}else {
							response.setStatus(500);
							jsonResponse.addProperty("stato", "errore server");
							jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
						}
						break;
					}
					
					break;
				}
				
				
				
			}catch(InvalidParameterException e) {
			
				response.setStatus(403);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("desc", "utente non autorizzato");
				System.out.println("not authorized token");
				e.printStackTrace();
			
			}catch(Exception e) {
				
				response.setStatus(500);
				jsonResponse.addProperty("stato", "errore server");
				jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
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
