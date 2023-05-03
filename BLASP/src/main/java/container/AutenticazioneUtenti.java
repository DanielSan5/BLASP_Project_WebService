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
	String username;	//ancora da definire
	String email; 		//ancora da definire
	String password;
	String risposta;
	String cookieForClient;
	
	
       
 
    public AutenticazioneUtenti() {
        super();
       
    }

    //Username check
    public boolean isValidUsername() {
    	if(username == null || username.contains(" "))
    		return false;
    	else
    		return true;
    }
    
    //Email check (Aldini email)
    public boolean isValidEmail() {
     	
     	String regexPattern = "^[a-zA-Z]+\\.[a-zA-Z]+@(aldini\\.istruzioneer\\.it|avbo\\.it)$";
     	
     	if((email == null) || (email.matches(regexPattern) == false))
     		return false;
     	else 	
     		return true;
     }
    
    
    //Empty input check
    public boolean isNotBlank() {
  	   if(username.isBlank() || password.isBlank()) {
  		   return false;
  	   }
  	   return true;	   
     }
    
    //Password check
    public boolean isValidPassword() {
    	
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
		
		username = user.get("Username").getAsString();		//ancora da definire
		email = user.get("Email").getAsString();			//ancora da definire	
		password = user.get("Password").getAsString();
		
		JsonObject jwtFormat = new JsonObject();
		jwtFormat.addProperty("sub", username);
		jwtFormat.addProperty("sub-email", email);
		jwtFormat.addProperty("aud", "*");
		
		
		
		//controlli input
		if(isNotBlank() && isValidUsername() && isValidEmail() && isValidPassword()) {
			
			QueryHandler queryForThis = new QueryHandler();
			
			int hasUsername = queryForThis.hasUsername(username);
		
			int user_id = queryForThis.getUserId(username);
			
			int hasUserStatus =  queryForThis.hasUserStatus(user_id);
			
			switch(hasUsername) {
			
				case 1:
					
					//pezzo da confermare
					if(hasUserStatus == 0) {
						
						
					int checkPass = queryForThis.checkPass(user_id, password);
					
					if(checkPass == 1) {
							
						risposta = "password corretta";
						
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
							cookieForClient = token;
							
						} catch (Exception e) {
							
							e.printStackTrace();
						}
					
					}else if (checkPass == 0){
						
						risposta = "password errata";
						
					}else {
						risposta = "errore con il database (controllo password)";
					}
					break;
					
					//pezzo nuovo da confermare
					}else if (hasUserStatus == -1) {
						risposta = "L'utente è disabilitato";
					}else if (hasUserStatus == -2) {
						risposta = "L'utente è stato bloccato";
					}
					
				case 0:
					risposta = "utente inesistente";
					break;
					
				default:
					risposta = "errore del database (presenza username)";
					break;
			}
			
			
		}else {
			risposta = "errore nell'input";
		}

		//da trasformare in formato json
		/*
		 * le risposte in formato json conterranno:
		 * stati (verifica dell'email, andatura della richiesta, correttezza password, controlli sugli input...)
		 * descrizione
		 * eventuali dati
		 */
		response.addHeader("Set-cookie","__refresh__token=" + cookieForClient + "; HttpOnly; Secure");
		out.println(risposta);
		
		
	}


}
