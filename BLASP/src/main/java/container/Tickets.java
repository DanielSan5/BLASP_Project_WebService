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
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import classes.JwtVal;
import classes.QueryHandler;
import classes.QueryHandler_filters;
import classes.QueryHandler_ticket;
import classes.Ticket;

/**
 * Servlet implementation class RicercaFiltrataTickets
 */
@WebServlet("/tickets")
public class Tickets extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String filter;
	private String value;
	private String jwtToken;
	private QueryHandler_filters queryForThis = new QueryHandler_filters();
	private String risposta;
	private ArrayList<Ticket> tickets = new ArrayList<Ticket>();
	boolean check;
	
	//Authorization empty check
	private boolean isValidAuthorization() {
		if(jwtToken == null || jwtToken.isBlank())
			return false;
		else 
			return true;
	}
       
	//Filter check
	private boolean isValidFilter(Set<String> filters) {
		
		if(filters.contains("materia")){
		
			filters.forEach((element) -> {
				
				if(element.equals("localita") || element.equals("classe") || element.equals("nome")){
					
					check = true;
					
				}else {
					check = false;
				}
			
			});
				
		}else {
			return false;
		}
		return check;
			
			
				
			
	}
	
	//Tag valid check
		private boolean isValidTag(int tag) {
			if(tag >= 1 || tag <= 5)
				return true;
			else 
				return false;
		}
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Tickets() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		//Estrazione dei parametri dalla richiesta GET
		/*
		 * materia ok
		 * materia-nome ok
		 * materia-localit� ok
		 * materia-classe ok
		 * materia-nome-localit� ok
		 * materia-nome-classe ok 
		 * materia-localit�-classe ok
		 * i ticket restituiti non dipendono dallo stato
		 */
		
		
		
		
		//Estrazione del token dall'header
		jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		Map<String, String[]> filters = request.getParameterMap();
		Set<String> types = filters.keySet();
		
		if(isValidFilter(types) && isValidAuthorization() ) {
			
			final JwtVal validator = new JwtVal();
			
			try{
				
				DecodedJWT jwtDecoded =  validator.validate(jwtToken);
				//String email = jwtDecoded.getClaim("sub").asString();
				//mappatura dei parametri "chiave":"valore" ("localita":"Granarolo") il valore in array 
				
				//si presume che materia sia gia presente per via del controllo 
				/*
				 * combinazioni possibili:
				 * M -> materia
				 * LM -> localita e materia
				 * CM -> classe e materia
				 * NM -> nome e materia
				 * LCM -> localita, classe e materia
				 * LNM -> localita, nome e materia
				 * CNM -> classe, nome e materia
				 */
				if(filters.containsKey("localita") && filters.containsKey("classe")) {
					tickets = queryForThis.getLCM(filters);
					
				}else if(filters.containsKey("localita") && filters.containsKey("nome")) {
					tickets = queryForThis.getLNM(filters);
					
				}else if(filters.containsKey("classe") && filters.containsKey("nome")) {
					tickets = queryForThis.getCNM(filters);
				}else if(filters.containsKey("localita")) {
					tickets = queryForThis.getLM(filters);
					
				}else if(filters.containsKey("classe")) {
					tickets = queryForThis.getCM(filters);
					
				}else if(filters.containsKey("nome")) {
					tickets = queryForThis.getNM(filters);
					
				}
				else {
					tickets = queryForThis.getM(filters);
				}
				
				
			}catch(InvalidParameterException e) {
				
				response.setStatus(401);
				risposta = "non autorizzato";
				System.out.println("not authorized token");
				e.printStackTrace();
				
			}
			
		}else {
			response.setStatus(400);
			risposta = "bad request";
		}
		
		//da trasformare in formato json fare calsse risposta
		/*
		 * le risposte in formato json conterranno:
		 * stati (andatura della richiesta, coincidenza password, controlli sugli input...)
		 * descrizione
		 * eventuali dati
		 */
		out.println(risposta);
		
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(405);
	}
	
	/**
	 * @see HttpServlet#doPut(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
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
		
		//Estrazione del token dall'header
		jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		
		//acquisizione delle chiavi
		int numeroTicket = user.get("numero_ticket").getAsInt();	
		
		//acquisizione chiavi dell'ogetto "to_edit"
		String valoreMateria = user.get("to_edit").getAsJsonObject().get("materia").getAsString();
		String valoreDescrizione = user.get("to_edit").getAsJsonObject().get("descrizione").getAsString();
		int valoreTag = user.get("to_edit").getAsJsonObject().get("tag").getAsInt();
		
		
		if(isValidTag(valoreTag) && isValidAuthorization() /*isValidMateria(valoreMateria)*/) {
		
			QueryHandler_ticket queryForThis = new QueryHandler_ticket();
			int hasTicketId = queryForThis.hasTicketId(numeroTicket);
			
			final JwtVal validator = new JwtVal();
			
			try{
				
				DecodedJWT jwtDecoded =  validator.validate(jwtToken);
				
				switch(hasTicketId) {
					case 1:
						
						//esecuzione della query
						int modificaDatiTicket = queryForThis.modificaDatiTicket(hasTicketId, valoreMateria, valoreDescrizione, valoreTag);
						
						if(modificaDatiTicket == 1) {
							risposta = "Dati modificati correttamente";
						}else if(modificaDatiTicket == 0 || modificaDatiTicket == -1) {
							risposta = "Errore nella modifica dei dati";
						}
						
						break;
					
					case 0:
						
						risposta = "Ticket non esistente";
						
						break;
						
					case -1:
						
						risposta = "Errore";
						
						break;
						
					
				}
				
		
			}catch(InvalidParameterException e) {
			
			response.setStatus(401);
			risposta = "non autorizzato";
			System.out.println("not authorized token");
			e.printStackTrace();
			
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
				
		out.println(risposta);
		
	}

}
