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
import java.util.Date;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import classes.QueryHandler;

/**
 * Servlet implementation class RegistrazioneUtenti
 */

@WebServlet("/RegistrazioneUtenti")
public class RegistrazioneUtenti extends HttpServlet {
	
	private static final long serialVersionUID = 1L;	
	String risposta;
	
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
    
   
public class DateValidatorUsingLocalDate implements DateValidator {
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
assertFalse(validator.isValid("20190230"));
    
    
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
   
	
   //Date of birth check --> da testare
  /* public boolean isValidDateOfBirth(String data_nascita) {
	
	   SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	   dateFormat.setLenient(false);
   
	   try {
		   dateFormat.parse(data_nascita);
		   return true;
	   } catch (Exception e) {
		   return false;
	   }
   }*/
    
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
						risposta = "utente gia esistente";
						break;
					case 0:
						int inserted = queryForThis.inserisciUtente(/*username , descrizione*/email, encryptedPass, nome, cognome, data_nascita, classe, indirizzo_scolastico, localita);
						
						if(inserted != -1) {
							risposta = "utente registrato";
						}else {
							risposta = "errore del database (inserimento utente)";
						}
						break;
						
					default:
						risposta = "errore del database (presenza utente)";
						break;
				}
		}else {
			risposta = "errore nell'input";
		}
		
		
		
	
		//da trasformare in formato json
		/*
		 * le risposte in formato json conterranno:
		 * stati (andatura della richiesta, coincidenza password, controlli sugli input...)
		 * descrizione
		 * eventuali dati
		 */
		out.println(risposta);
				
				
				
	}
		
}

