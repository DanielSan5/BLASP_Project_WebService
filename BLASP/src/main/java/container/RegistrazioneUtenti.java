package container;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

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
	String email;
	String password;
	String confirm_password;
	String nome;
	String cognome;
	String data_nascita;
	int classe;
	String indirizzo_scolastico;
	String sezione_scolastica;
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
    
    
  //***CONTROLLI NUOVI (BLASP)***
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
		email = user.get("Email").getAsString();
		password = user.get("Password").getAsString();
		confirm_password = user.get("Confirm_Password").getAsString();
		//altro
		
		
		//psw encription
		
		//controlli lato server e CREAZIONE UTENTE NEL DB
				if(isValidEmail()) {
					
					if(isConfirmedPassword()) {
					
						
						QueryHandler queryForThis = new QueryHandler();
						
						int hasEmail = queryForThis.hasEmail(email); 
						
						switch(hasEmail) {
						
							case 1:
								risposta = "utente gia registrato";
								break;
							case 0:
								int inserted = queryForThis.inserisciUtente(email, password, nome, cognome, data_nascita, classe, indirizzo_scolastico, sezione_scolastica, localita);
								if(inserted != -1) {
									risposta = "utente registrato";
								}else {
									risposta = "errore del database (inserimento utente)";
								}
								break;
								
							default:
								risposta = "errore del database (presenza email)";
								break;
						}
						
						//else del controllo password
					}else{
						risposta = "le password inserite non coincidono";
					}
					
					//else del controllo mail
				}else {
					risposta = "Errore nell'inserimento della mail";
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

