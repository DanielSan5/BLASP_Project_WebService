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
	String risposta;
	String cookieForClient;
	
	
       
 
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
		//jwtFormat.addProperty("sub", username);
		jwtFormat.addProperty("sub-email", email);
		jwtFormat.addProperty("aud", "*");
		
		
		
		//controlli input
		if(isValidPassword(password)) {
			
			QueryHandler queryForThis = new QueryHandler();
			
			int hasEmail = queryForThis.hasEmail(email);
			int user_id = queryForThis.getUserId(email);
			String userStatus =  queryForThis.getUserStatus(user_id);
			
			switch(hasEmail/*hasUsername*/) {
			
				case 1:
					
					//pezzo da confermare
					if(userStatus.equals("none")) {
						
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
						
					
					//pezzo nuovo da confermare
					}else if (userStatus.equals("banned")) {
						risposta = "utente bloccato";
					}else if(userStatus.equals("unabled")){
						risposta = "utente disabilitato";
					}else if(userStatus.isBlank()){
						risposta = "errore del database (controllo stato)";
					}
					break;
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
