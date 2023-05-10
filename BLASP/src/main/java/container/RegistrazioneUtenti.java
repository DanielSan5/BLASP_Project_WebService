
package container;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import classes.JwtGen;
import classes.QueryHandler;




/**
 * Servlet implementation class RegistrazioneUtenti
 */

@WebServlet("/RegistrazioneUtenti")
public class RegistrazioneUtenti extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegistrazioneUtenti() {
        super();
        
    }
    
    private String passEncr(String password) {
    	
    	Argon2 argon2 = Argon2Factory.create(Argon2Types.ARGON2id);
    	String hash = argon2.hash(4, 1024 * 1024, 8, password);

    	return hash;
  
    	
    	
    }
    
    /*private boolean isValidLocation(String localita) {
    	
    }*/
    
    /*private boolean isValidDateFormat(String data_nascita) {
	
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

   
  /* public class DateValidatorUsingLocalDate implements DateValidator {
       private DateTimeFormatter dateFormatter;
   	
   	public DateValidatorUsingLocalDate(DateTimeFormatter dateFormatter) {
           this.dateFormatter = dateFormatter;
       }
   		
   	private boolean isValidDateFormat(String data_nascita) {
           try {
               LocalDate.parse(data_nascita, this.dateFormatter);
           } catch (DateTimeParseException e) {
               return false;
           }
           return true;
       }
   }
   DateTimeFormatter dateFormatter = DateTimeFormatter.BASIC_ISO_DATE;
   DateValidator validator = new DateValidatorUsingLocalDate(dateFormatter);
           
   assertTrue(validator.isValid("20190228"));
   assertFalse(validator.isValid("20190230"));*/
       
    
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
		response.setStatus(405);
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "PUT,POST");
		JsonObject jsonResponse = new JsonObject();
		//output writer
		PrintWriter out = response.getWriter(); 
		//input reader
		BufferedReader in_body = request.getReader();
		//stringBuilder per costruire una stringa dal messaggio in formato json
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
		
		if(isNotBlank(nome, cognome) /*&& isValidDateOfBirth(data_nascita)*/ && isValidPassword(password) && isValidEmail(email) && isValidClass(classe) && isConfirmedPassword(password, confirm_password)) {
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
						jsonResponse.addProperty("descrizione", "utente già esistente");
						out.println(jsonResponse.toString());
						break;
						
					case 0:
						
						int inserted = queryForThis.inserisciUtente(email, encryptedPass, nome, cognome, data_nascita, classe, indirizzo_scolastico, localita);
						
						if(inserted != -1) {
	
							JsonObject jwtFormat = new JsonObject();
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
								jsonResponse.addProperty("descrizione", "utente creato");
								out.println(jsonResponse.toString());
								
							} catch (Exception e) {
								
								response.setStatus(500);
								jsonResponse.addProperty("stato", "errore server");
								jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
								out.println(jsonResponse.toString());
								e.printStackTrace();
							}
						}else {
							response.setStatus(500);
							jsonResponse.addProperty("stato", "errore server");
							jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
							out.println(jsonResponse.toString());
						}
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

