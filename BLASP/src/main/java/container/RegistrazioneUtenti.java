package container;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
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
    
	//***CAMPI OBBLIGATORI***
	String username;		//ancora da discutere
	String email;
	String password;
	String confirm_password;
	String nome;
	String cognome;
	String data_nascita;
	int classe;
	String indirizzo_scolastico;
	String localita;
	
	//altro
	
	//***CAMPI FACOLTATIVI***
	String descrizione;

	
	String risposta;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegistrazioneUtenti() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public String passEncr() {
    	
    	Argon2 argon2 = Argon2Factory.create(Argon2Types.ARGON2id);
    	String hash = argon2.hash(4, 1024 * 1024, 8, password);

    	return hash;
  
    	
    	
    }
    
    //Email check (Aldini email)
    public boolean isValidEmail() {
     	
     	String regexPattern = "^[a-zA-Z]+\\.[a-zA-Z]+@(aldini\\.istruzioneer\\.it|avbo\\.it)$";
     	
     	if((email == null) || (email.matches(regexPattern) == false))
     		return false;
     	else 	
     		return true;
     }
    
    //Confirm password check
    public boolean isConfirmedPassword() {
 	   if(!password.equals(confirm_password)) {
 		   return false;
    	   }
 	   
 	   return true;
    }
    
    //Username check
    public boolean isValidUsername() {
    	if((username == null) || username.contains(" "))
    		return false;
    	else
    		return true;
    }
    
    //Class check
    public boolean isValidClass() {
    	
    	if(classe == 1 || classe == 2 || classe == 3 || classe == 4 || classe == 5) {
    		return true;
    	}else
    		return false;
    }
    
    //Empty input check
    
    //Password check
    

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
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
		username = user.get("Username").getAsString();
		email = user.get("Email").getAsString();
		password = user.get("Password").getAsString();
		confirm_password = user.get("Confirm_Password").getAsString();
		nome = user.get("Nome").getAsString();
		cognome = user.get("Cognome").getAsString();
		data_nascita = user.get("Data_Nascita").getAsString();
		classe = user.get("Classe").getAsInt();
		indirizzo_scolastico = user.get("Indirizzo_Scolastico").getAsString();
		localita = user.get("Localita").getAsString();
		
		
		
		if(/*isNotBlank() && */ isValidUsername() /*&& isValidPassword()*/ && isValidEmail() && isValidClass() && isConfirmedPassword()) {
			
				/*
				 * psw encryption
				 */
				passEncr();
				QueryHandler queryForThis = new QueryHandler();
				
				int hasUsername = queryForThis.hasUsername(username);
				int hasEmail = queryForThis.hasEmail(email); 
				
				switch(hasUsername) {
				
					case 1:
						risposta = "username gia esistente";
						break;
					case 0:
						
						if(hasEmail != -1) {
							if(hasEmail == 0) {
							
								int inserted = queryForThis.inserisciUtente(username ,email, password, nome, cognome, data_nascita, classe, indirizzo_scolastico, localita);
								
								if(inserted != -1) {
									risposta = "utente registrato";
								}else {
									risposta = "errore del database (inserimento utente)";
								}
							}else {
								risposta = "email gia esistente";
							}
						}else {
							risposta = "errore del database (presenza email)";
						}
						break;
						
					default:
						risposta = "errore del database (presenza username)";
						break;
				}
		}else {
			risposta = "errore nell'input";
		}
		
		
		
				response.addHeader("Access-Control-Allow-Origin", "*");
				response.addHeader("Access-Control-Allow-Methods", "PUT,POST");
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

