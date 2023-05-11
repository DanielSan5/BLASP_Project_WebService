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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import classes.JwtGen;
import classes.JwtVal;
import classes.QueryHandler;
import classes.Ticket;
import classes.Utente;

/**
 * Servlet implementation class RegistrazioneUtenti
 */

@WebServlet("/user")
public class User extends HttpServlet {
	
	private static final long serialVersionUID = 1L;	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public User() {
        super();
        
    }
    
    private String passEncr(String password) {
    	
    	Argon2 argon2 = Argon2Factory.create(Argon2Types.ARGON2id);
    	String hash = argon2.hash(4, 1024 * 1024, 8, password);

    	return hash;
  
    	
    	
    }
    
    private boolean isValidDateOfBirth(String strDate)
    {
	 	/* Check if date is 'null' */
	 	if (strDate.trim().equals(""))
	 	{
	 	    return false;
	 	}
	 	/* Date is not 'null' */
	 	else
	 	{
	 	    /*
	 	     * Set preferred date format,
	 	     * For example MM-dd-yyyy, MM.dd.yyyy,dd.MM.yyyy etc.*/
	 	    SimpleDateFormat sdfrmt = new SimpleDateFormat("YYYY/mm/dd");
	 	    sdfrmt.setLenient(false);
	 	    /* Create Date object
	 	     * parse the string into date 
	              */
	 	    try
	 	    {
	 	        sdfrmt.parse(strDate); 
	 	        System.out.println(strDate+" is valid date format");
	 	    }
	 	    /* Date format is invalid */
	 	    catch (ParseException e)
	 	    {
	 	        System.out.println(strDate+" is Invalid Date format");
	 	        return false;
	 	    }
	 	    /* Return true if date format is valid */
	 	    return true;
	 	}
    }
    /*private boolean isValidLocation(String localita) {
    	
    }*/
    
    
    
    /*private boolean isValidSTA(String indirizzo_scolastico) {
	
    }*/
    //Email check (Aldini email)
    private boolean isValidEmail(String email) {
     	
     	String regexPattern = "^[a-zA-Z]+\\.[a-zA-Z]+@(aldini\\.istruzioneer\\.it|avbo\\.it)$";
     	
     	if((email.isBlank()) || (email.matches(regexPattern) == false))
     		return false;
     	else 	
     		return true;
     }
    
    //Confirm password check
    private boolean isConfirmedPassword(String password, String confirm_password) {
 	   if(!password.equals(confirm_password)) {
 		   return false;
    	   }
 	   
 	   return true;
    }
 
    
    //Class check
    private boolean isValidClass(int classe) {
    	
    	if(classe == 1 || classe == 2 || classe == 3 || classe == 4 || classe == 5) {
    		return true;
    	}else
    		return false;
    	
    }
    
  //Empty input check
   public boolean isNotBlank(String nome, String cognome) {
  	   if(nome.isBlank() || cognome.isBlank()) {
  		   return false;
  	   }
  	   return true;	   
     }
  
    
    //Password check
    public boolean isValidPassword(String password) {
    	
    	boolean hasLowerCase = false;
    	boolean hasUpperCase = false;
    	boolean hasDigit = false;
    	boolean hasSpecialChar = false;
    	String specialChars = "!@#$%^&*(),.?\":{}|<>";
    	
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
    

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
		//ottenimento solo dei dati personali
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "PUT,POST");
		
		PrintWriter out = response.getWriter(); 
		JsonObject jsonResponse = new JsonObject();
		Gson g = new Gson();
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		
		final JwtVal validator = new JwtVal();
		
		try{
			
			//se non viene autorizzato lancia eccezzione gestita nel catch sotto
			DecodedJWT jwtDecoded = validator.validate(jwtToken);
			
			String email = jwtDecoded.getClaim("sub").asString();
			QueryHandler queryForThis = new QueryHandler();
			int user_id = queryForThis.getUserId(email);
			Utente userData = queryForThis.getUserData(user_id);
			ArrayList<Ticket> userTickets = queryForThis.getUserTickets(user_id);
			
			if(userData != null) {
				
				response.setStatus(200);
				jsonResponse.addProperty("stato", "confermato");
				jsonResponse.addProperty("desc", " ottenimento dati personali");
				jsonResponse.add("user_info", g.toJsonTree(userData));
				jsonResponse.add("user_tickets", g.toJsonTree(userTickets));
				
			}else {
				
				response.setStatus(500);
				jsonResponse.addProperty("stato", "errore server");
				jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
			}
		}catch(InvalidParameterException e) {
			
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
			
		}catch(Exception e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "nessun risultato");
		}
		finally {
			out.println(jsonResponse.toString());
		}
		
		out.println(jsonResponse.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "PUT,POST");
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
		//trsformazione stringa in oggetto json
		Gson g = new Gson();
		JsonObject user = g.fromJson(body, JsonObject.class);
		//acquisizione valore delle chiavi
		String email = user.get("email").getAsString();
		String password = user.get("password").getAsString();
		String confirm_password = user.get("conferma_pass").getAsString();
		String nome = user.get("nome").getAsString();
		String cognome = user.get("cognome").getAsString();
		String data_nascita = user.get("data_nascita").getAsString();
		int classe = user.get("classe").getAsInt();
		String indirizzo_scolastico = user.get("indirizzo").getAsString();
		String localita = user.get("localita").getAsString();
		
		if(isNotBlank(nome, cognome) && isValidDateOfBirth(data_nascita) && isValidPassword(password) && isValidEmail(email) && isValidClass(classe) && isConfirmedPassword(password, confirm_password)) {
				/*
				 * psw encryption
				 */
				String encryptedPass = passEncr(password);
				QueryHandler queryForThis = new QueryHandler();
				
				int hasEmail = queryForThis.hasEmail(email); 
				
				switch(hasEmail) {
				
					case 1:
						response.setStatus(400);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("descrizione", "errore nella sintassi");
						break;
					case 0:
						int inserted = queryForThis.inserisciUtente(email, encryptedPass, nome, cognome, data_nascita, classe, indirizzo_scolastico, localita);
						
						if(inserted != -1) {
							
							JsonObject jwtFormat = new JsonObject();
							//jwtFormat.addProperty("sub", username);
							jwtFormat.addProperty("sub-email", email);
							jwtFormat.addProperty("aud", "*");
							
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
								response.setStatus(201);
								jsonResponse.addProperty("stato", "confermato");
								jsonResponse.addProperty("desc", "utente creato");
								
							} catch (Exception e) {
								
								response.setStatus(500);
								jsonResponse.addProperty("stato", "errore server");
								jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
								e.printStackTrace();
							}finally {
								
								out.println(jsonResponse.toString());
							}
						}else {
							response.setStatus(500);
							jsonResponse.addProperty("stato", "errore server");
							jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
						}
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
		
}

