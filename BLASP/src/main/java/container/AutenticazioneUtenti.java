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
    
    //Password check
    
    
   
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
		
		/*
		 * valida del JWT
		 */
		
		//controlli input
		if(/*isNotBlank() &&*/ isValidUsername() && isValidEmail() /*&& isValidPassword()*/) {
			
			QueryHandler queryForThis = new QueryHandler();
			
			int hasUsername = queryForThis.hasUsername(username);
			
			int user_id = queryForThis.getUserId(username);
			
			switch(hasUsername) {
			
				case 1:
					
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
		response.addHeader("set-cookie", cookieForClient);
		out.println(risposta);
		
		
	}


}
