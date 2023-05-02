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

import classes.QueryHandler;

/**
 * Servlet implementation class AutenticazioneUtenti
 */
public class AutenticazioneUtenti extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	String username;	//ancora da definire
	String email; 		//ancora da definire
	String password;
	String risposta;
	
	//jwt token
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
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
    
    
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		response.getWriter().append("Served at: ").append(request.getContextPath());
		*/
	}

	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
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
		jwtFormat.addProperty("aud", "*");
		
		/*
		 * valida del JWT
		 */
		
		//controlli input
		if(/*isNotBlank() &&*/ isValidUsername() && isValidEmail() /*&& isValidPassword()*/) {
			
			
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
		
		
	}


}
