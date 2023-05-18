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
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import jakarta.mail.*;
import java.util.UUID;

import javax.security.auth.login.CredentialNotFoundException;

import org.eclipse.angus.mail.util.MailSSLSocketFactory;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import classes.Checks;
import classes.JwtGen;
import classes.QueryHandler;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

@WebServlet("/auth")
public class AutenticazioneUtenti extends HttpServlet{
	
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
        	props.put("mail.smtp.starttls.enable", "true");
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
		try {
			
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
			
			if(Checks.isValidEmail(email)) {
				
				QueryHandler queryForThis = new QueryHandler();

				boolean hasEmail = queryForThis.hasEmail(email);
				
				if(hasEmail) {
				
					int user_id = queryForThis.getUserId(email);
					String userStatus =  queryForThis.getUserStatus(user_id);
					
					if(userStatus.equals("none")) {
						
						boolean checkPass = queryForThis.checkPass(user_id, password);
						
						if(checkPass) {
								
							//generazione jwt per la sessione
							JwtGen generator = new JwtGen();
							Map<String, String> claims = new HashMap<>();
							
							jwtFormat.keySet().forEach(keyStr ->
						    {
						        String keyvalue = jwtFormat.get(keyStr).getAsString();
						        claims.put(keyStr, keyvalue);
						      
						    });
							
							LocalDate oggi = LocalDate.now();
							LocalDate domani = oggi.plusDays(1);
							ZonedDateTime domaniUTC = domani.atStartOfDay(ZoneId.of("UTC"));
							String domaniUTCFormatted = domaniUTC.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
							
							String token = generator.generateJwt(claims);
							response.addHeader("Set-cookie","__refresh__token=" + token + "; HttpOnly; Secure; exp=" + domaniUTCFormatted);
							response.setStatus(200);
							jsonResponse.addProperty("stato", "confermato");
							jsonResponse.addProperty("desc", "utente autorizzato");
						
						}else{
							
							response.setStatus(401);
							jsonResponse.addProperty("stato", "errore client");
							jsonResponse.addProperty("descrizione", "credenziali invalide");
						}
					
					//pezzo nuovo da confermare
					}else if(userStatus.equals("blocked")) {
						
						response.setStatus(401);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("descrizione", "utente bloccato");
						
					}else if(userStatus.equals("unabled")){
						
						response.setStatus(401);
						jsonResponse.addProperty("stato", "errore");
						jsonResponse.addProperty("descrizione", "utente disabilitato");
						
					}
					
				}else {
					response.setStatus(400);
					jsonResponse.addProperty("stato", "errore client");
					jsonResponse.addProperty("descrizione", "utente inesistente");
						
				}
			}else {
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "errore nella sintassi");
			}	
				
		}catch(SQLException | IllegalArgumentException | JWTCreationException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
			e.printStackTrace();
			
		} catch (CredentialNotFoundException e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "nessun risultato");
			e.printStackTrace();
		}catch(JsonSyntaxException | NullPointerException e) {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "formato non supportato");
			e.printStackTrace();
		}finally {
			out.println(jsonResponse.toString());
		}

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
		try {
			Gson g = new Gson();
			JsonObject user = g.fromJson(body, JsonObject.class);
				
			String action = user.get("action").getAsString();
			String email = user.get("email").getAsString();
			
			String [] toCheck = {action};
			
			if(Checks.isValidEmail(email) && Checks.isNotBlank(toCheck)) {

				QueryHandler queryForThis = new QueryHandler();
				int user_id = queryForThis.getUserId(email);
				
				switch(action) {
	
					case "email_info":
	
						boolean hasEmail = queryForThis.hasEmail(email);
						
						if(hasEmail) {
								
							String ver_code = UUID.randomUUID().toString();
							//void
							queryForThis.inserisciCodice(user_id, ver_code);
							//void
							sendEmailCode(email, ver_code);
							
							response.setStatus(200);
							jsonResponse.addProperty("stato", "confermato");
							jsonResponse.addProperty("descrizione", "email inviata");
						
						}else{
							
							response.setStatus(400);
							jsonResponse.addProperty("stato", "errore client");
							jsonResponse.addProperty("descrizione", "email inesistente");
							
						}
							
						break;
						
					case "ver_code":
						
						String code = user.get("code").getAsString();
						String new_pass = user.get("new_pass").getAsString();
						String conf_pass = user.get("conf_new_pass").getAsString();
						
						if(queryForThis.checkCode(user_id, code)) {
							
							if(Checks.isValidPassword(new_pass) && Checks.isConfirmedPassword(new_pass, conf_pass)) {
								
								String new_pass_encr = passEncr(new_pass);
								//void
								queryForThis.changePass(user_id, new_pass_encr);
								
								response.setStatus(200);
								jsonResponse.addProperty("stato", "confermato");
								jsonResponse.addProperty("descrizione", "password modificata");
								
							}else {
								
								response.setStatus(400);
								jsonResponse.addProperty("stato", "errore client");
								jsonResponse.addProperty("descrizione", "errore nella sintassi della richiesta");
							}
							
						}else {
							
							response.setStatus(400);
							jsonResponse.addProperty("stato", "errore client");
							jsonResponse.addProperty("descrizione", "codice errato");
							
						}
						break;
						
					default:
						
						response.setStatus(400);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("descrizione", "errore nella sintassi della richiesta");
						break;
				}
			}else {
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "errore nella sintassi della richiesta");
			}
		}catch(GeneralSecurityException | MessagingException | SQLException e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("descrizione", "errore nell'elaborazione della richiesta");
			e.printStackTrace();
		}catch(JsonSyntaxException | NullPointerException e) {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "formato non supportato");
			e.printStackTrace();
		}finally {
			out.println(jsonResponse.toString());
			
		}

		
	}



}
