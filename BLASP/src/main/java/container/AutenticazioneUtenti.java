package container;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import jakarta.mail.*;
import java.util.UUID;

import org.eclipse.angus.mail.util.MailSSLSocketFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import classes.JwtGen;
import classes.QueryHandler;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

@WebServlet("/auth")
public class AutenticazioneUtenti extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	
       
 
    public AutenticazioneUtenti() {
        super();
       
    }
    
	private String passEncr(String password) {
	    	
	    	Argon2 argon2 = Argon2Factory.create(Argon2Types.ARGON2id);
	    	String hash = argon2.hash(4, 1024 * 1024, 8, password);
	
	    	return hash;
	  
	    	
	    	
	    }
    
    public void sendEmailCode(String email, String code) throws GeneralSecurityException, MessagingException{
    	
    	MailSSLSocketFactory sf = null;
    	
		sf = new MailSSLSocketFactory();
		
		if(sf != null) {
    		String host = "smtp.gmail.com";
    		String from = "zuccoroulette.co@gmail.com";
    		String to = email;
    		String username = "zuccoroulette.co@gmail.com";
    		String password = "draviynfjvrgjvjy";
    		
    		Properties props = new Properties();
        	props.put("mail.smtp.host", host);
        	props.put("mail.smtp.port", 587);
        	props.put("mail.smtp.starttks.enable", "true");
        	props.put("mail.smtp.auth", "true");
        	
        	Authenticator authenticator = new Authenticator() {
        		protected PasswordAuthentication getPasswordAuthentication() {
        			return new PasswordAuthentication(username, password);
        		}
        	};
        	
    		Session session =Session.getInstance(props, authenticator);
        	
        		
    		MimeMessage mi = new MimeMessage(session);
    		mi.setFrom(InternetAddress.parse(from) [0]);
    		mi.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
    		mi.setSubject("Codice di verifica");
    		mi.setSentDate(new Date());
    		mi.setText(code);
    		Transport.send(mi);
        	
    	}
    		
    	
   
    
    	
    }
    
    private boolean isConfirmedPassword(String password, String confirm_password) {
  	   if(!password.equals(confirm_password)) {
  		  System.out.println("password non coincidono ");
  		   return false;
     	   }
  	   
  	   return true;
     }
    
    private boolean isValidEmail(String email) {
     	
     	String regexPattern = "^[a-zA-Z]+\\.[a-zA-Z]+@(aldini\\.istruzioneer\\.it|avbo\\.it)$";
     	
     	if((email.isBlank()) || (email.matches(regexPattern) == false)) {
     		System.out.println("email errata");
     		return false;
     	}
     	else 	
     		return true;
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
		response.setContentType("application/json");
		PrintWriter out = response.getWriter(); 
		JsonObject jsonResponse = new JsonObject();
		BufferedReader in_body = request.getReader();
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
								jsonResponse.addProperty("desc", "utente autorizzato");
								
							} catch (Exception e) {
								response.setStatus(500);
								jsonResponse.addProperty("stato", "errore server");
								jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
								e.printStackTrace();
							}finally {
								out.println(jsonResponse.toString());
							}
						
						}else if (checkPass == 0){
							
							response.setStatus(401);
							jsonResponse.addProperty("stato", "errore client");
							jsonResponse.addProperty("descrizione", "credenziali invalide");
							
						}else {
							response.setStatus(500);
							jsonResponse.addProperty("stato", "errore server");
							jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
						}
						
					
					//pezzo nuovo da confermare
					}else if (userStatus.equals("banned")) {
						
						response.setStatus(401);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("descrizione", "utente bloccato");
						
					}else if(userStatus.equals("unabled")){
						
						response.setStatus(401);
						jsonResponse.addProperty("stato", "errore");
						jsonResponse.addProperty("descrizione", "utente disabilitato");
						
					}else if(userStatus.isBlank()){
						response.setStatus(500);
						jsonResponse.addProperty("stato", "errore server");
						jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
					}
					break;
				case 0:
					response.setStatus(400);
					jsonResponse.addProperty("stato", "errore client");
					jsonResponse.addProperty("descrizione", "errore nella sintassi");
					break;
					
				default:
					response.setStatus(500);
					jsonResponse.addProperty("stato", "errore server");
					jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
					break;
			}
			
			
		}else {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "errore nella sintassi");
		}

		
		out.println(jsonResponse.toString());
		
		
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "PUT,POST");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter(); 
		JsonObject jsonResponse = new JsonObject();
		BufferedReader in_body = request.getReader();
		StringBuilder sb = new StringBuilder();
		String line;
		String body;
		
		while((line = in_body.readLine()) != null) {
			sb.append(line);
		}
		
		body = sb.toString();
		
		Gson g = new Gson();
		JsonObject user = g.fromJson(body, JsonObject.class);
			
		String action = user.get("action").getAsString();
		String email = user.get("email").getAsString();
		
		if(isValidEmail(email)) {
			
			QueryHandler queryForThis = new QueryHandler();
			int user_id = queryForThis.getUserId(email);
			
			switch(action) {
			
			
				case "email_info":
					
					int check = queryForThis.hasEmail(email);
					
					if(check == 1) {
						
						
						String ver_code = UUID.randomUUID().toString();
						
						if(queryForThis.inserisciCodice(user_id, ver_code) == 1) {
							
							try {
								
								sendEmailCode(email, ver_code);
								//da testare
							} catch (GeneralSecurityException | MessagingException e) {
								//errore database
								e.printStackTrace();
							}
							
						}else {
							//errore database
						}
						
					}else if(check == 0){
						//email inesistente
					}else {
						//errore database
					}
					break;
					
				case "ver_code":
					
					String code = user.get("code").getAsString();
					try {
						
						if( queryForThis.checkCode(user_id, code)) {
							//success
							
						}else {
							//codice errato
						}
						
					} catch (Exception e) {
						//errore database
						e.printStackTrace();
					}
					
					break;
					
				case "change_pass":
					
					String new_pass = user.get("new_pass").getAsString();
					String conf_pass = user.get("conf_new_pass").getAsString();
					if(isValidPassword(new_pass) && isConfirmedPassword(new_pass, conf_pass)) {
						
						String new_pass_encr = passEncr(new_pass);
						int checkPass = queryForThis.changePass(user_id, new_pass_encr);
						if(checkPass == 1) {
							//password cambiata
						}else {
							//errore
						}
						
					}else {
						//errore input
					}
					break;
					
				default:
					//errore input
					break;
			}
		}else {
			//errore input
		}
	
		
		
		

		
		
	}



}
