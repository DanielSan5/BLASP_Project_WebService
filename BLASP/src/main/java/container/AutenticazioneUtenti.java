package container;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import classes.JwtGen;
import classes.QueryHandler;

@WebServlet("/AutenticazioneUtenti")
public class AutenticazioneUtenti extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
    public AutenticazioneUtenti() {
        super();
       
    }
    
    //Password check
    public boolean isValidPassword(String password) {
    	
    	boolean hasLowerCase = false;
    	boolean hasUpperCase = false;
    	boolean hasDigit = false;
    	boolean hasSpecialChar = false;
    	String specialChars = "!?&$";
    	
    	for(int i=0; i < password.length(); i++) {
    		char passwordChar = password.charAt(i);
    		if(Character.isLowerCase(passwordChar))
    			hasLowerCase = true;
    		else if (Character.isUpperCase(passwordChar))
    			hasUpperCase = true;
    		else if(Character.isDigit(passwordChar)) 
                hasDigit = true;
            else if(specialChars.indexOf(passwordChar) != -1)
                hasSpecialChar = true;
      }
    	
    	if(password.length() > 8 && hasLowerCase && hasUpperCase && hasDigit && hasSpecialChar) 
    		return true;
    	else 
    		return false;
    	
    }
    
   
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setStatus(405);
	}

	

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "PUT,POST");
		PrintWriter out = response.getWriter(); 
		JsonObject jsonResponse = new JsonObject();
		BufferedReader in_body = request.getReader();
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
		
		//String username = user.get("Username").getAsString();		//ancora da definire
		String email = user.get("email").getAsString();			
		String password = user.get("password").getAsString();
		
		JsonObject jwtFormat = new JsonObject();
		jwtFormat.addProperty("sub-email", email);
		jwtFormat.addProperty("aud", "*");
		
		
		//controlli input
		if(isValidPassword(password)) {
			
			QueryHandler queryForThis = new QueryHandler();
			
			int hasEmail = queryForThis.hasEmail(email);
			int user_id = queryForThis.getUserId(email);
			String userStatus =  queryForThis.getUserStatus(user_id);
			
			switch(hasEmail) {
			
				case 1:
					
					//pezzo da confermare
					if(userStatus.equals("none")) {
						
						int checkPass = queryForThis.checkPass(user_id, password);
						
						if(checkPass == 1) {
							
							//generazione jwt per la sessione
							try {
								
								JwtGen generator = new JwtGen();
								Map<String, String> claims = new HashMap<>();
								
								jwtFormat.keySet().forEach(keyStr ->
							    {
							        String keyvalue = jwtFormat.get(keyStr).getAsString();
							        claims.put(keyStr, keyvalue);
							      
							    });
								
								String token = generator.generateJwt(claims);
								
								response.addHeader("Set-cookie","__refresh__token=" + token + "; HttpOnly; Secure");
								response.setStatus(200);
								jsonResponse.addProperty("stato", "confermato");
								jsonResponse.addProperty("descrizione", "utente autenticato");
								out.println(jsonResponse.toString());
								
							} catch (Exception e) {
								
								response.setStatus(500);
								jsonResponse.addProperty("stato", "errore server");
								jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
								out.println(jsonResponse.toString());
								e.printStackTrace();
							}
						
						}else if (checkPass == 0){
							
							response.setStatus(401);
							jsonResponse.addProperty("stato", "errore client");
							jsonResponse.addProperty("descrizione", "credenziali non valide");
							out.println(jsonResponse.toString());
							
							
						}else {
							response.setStatus(500);
							jsonResponse.addProperty("stato", "errore server");
							jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
							out.println(jsonResponse.toString());
						}
						
					
					//pezzo nuovo da confermare
					}else if (userStatus.equals("banned")) {
						
						response.setStatus(403);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("descrizione", "utente bloccato");
						out.println(jsonResponse.toString());
						
					}else if(userStatus.equals("unabled")){
						
						response.setStatus(200);
						jsonResponse.addProperty("stato", "confermato");
						jsonResponse.addProperty("descrizione", "utente disabilitato");
						out.println(jsonResponse.toString());
						
					}else if(userStatus.isBlank()){
						
						response.setStatus(500);
						jsonResponse.addProperty("stato", "errore server");
						jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
						out.println(jsonResponse.toString());
					}
					break;
				case 0:
					response.setStatus(400);
					jsonResponse.addProperty("stato", "errore client");
					jsonResponse.addProperty("descrizione", "utente inesistente");
					out.println(jsonResponse.toString());
					break;
					
				default:
					response.setStatus(500);
					jsonResponse.addProperty("stato", "errore server");
					jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
					out.println(jsonResponse.toString());
					break;
			}
			
			
		}else {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "errore nella sintassi");
			out.println(jsonResponse.toString());
		}
		
	
		
		
	}


}
