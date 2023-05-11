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
import java.util.Arrays;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;;

/**
 * Servlet implementation class RicercaFiltrataTickets
 */
@WebServlet("/tickets")
public class Tickets extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String jwtToken;
	private QueryHandler_filters queryForThis = new QueryHandler_filters();
	boolean check;
	
	 //Empty input check
	   public boolean isNotBlank(String materia, String livello_materia, String descrizione, String dataStringa) {
	  	   if(materia.isBlank() || livello_materia.isBlank() || descrizione.isBlank() || dataStringa.isBlank()) {
	  		   return false;
	  	   }
	  	   return true;	   
	     }
	  
	
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
	private boolean isValidTag(String tag) {
		if(tag == "prima" || tag == "seconda" || tag == "terza" || tag == "quarta" || tag == "quinta")
			return true;
		else 
			return false;
	}
	
	//Materia valid check
	private boolean isValidMateria(String materia) {
		
		QueryHandler queryForThis = new QueryHandler();
		if(queryForThis.checkExistMateria(materia) == 1)
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
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "POST");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JsonObject jsonResponse = new JsonObject();
		Gson g = new Gson();
		//Estrazione del token dall'header
		jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		Map<String, String[]> filters = request.getParameterMap();
		Set<String> types = filters.keySet();
		
		if(isValidFilter(types) && isValidAuthorization() ) {
			
			final JwtVal validator = new JwtVal();
			
			try{
				
				//se non viene autorizzato lancia eccezzione gestita nel catch sotto
				//DecodedJWT jwtDecoded =
				validator.validate(jwtToken);
				
				//String email = jwtDecoded.getClaim("sub").asString();
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
				List<Ticket> tickets = new ArrayList<Ticket>();
				if(filters.containsKey("localita") && filters.containsKey("classe")) {
					tickets = queryForThis.getLCM(filters.get("localita").toString(), filters.get("classe").toString(), 
							filters.get("materia").toString());
					
				}else if(filters.containsKey("localita") && filters.containsKey("nome")) {
					tickets = queryForThis.getLNM(filters.get("localita").toString(), filters.get("nome").toString(), 
							filters.get("materia").toString());
					
				}else if(filters.containsKey("classe") && filters.containsKey("nome")) {
					tickets = queryForThis.getCNM(filters.get("classe").toString(), filters.get("nome").toString(), 
							filters.get("materia").toString());
					
				}else if(filters.containsKey("localita")) {
					tickets = queryForThis.getLM(filters.get("localita").toString(), filters.get("materia").toString());
					
				}else if(filters.containsKey("classe")) {
					tickets = queryForThis.getCM(filters.get("classe").toString(), filters.get("materia").toString());
					
				}else if(filters.containsKey("nome")) {
					tickets = queryForThis.getNM(filters.get("nome").toString(), filters.get("materia").toString());
					
				}
				else {
					tickets = queryForThis.getM(filters.get("materia").toString());
				}
				
				if(tickets == null) {
					response.setStatus(500);
					jsonResponse.addProperty("stato", "errore server");
					jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
				}else if(tickets.isEmpty()){
					response.setStatus(200);
					jsonResponse.addProperty("stato", "confermato");
					jsonResponse.addProperty("descrizione", "ricerca filtrata");
					jsonResponse.addProperty("filtered", "nessun risultato");
				}else {
					response.setStatus(200);
					jsonResponse.addProperty("stato", "confermato");
					jsonResponse.addProperty("descrizione", "ricerca filtrata");
					jsonResponse.add("filtered", g.toJsonTree(tickets));	
				}
				
			}catch(InvalidParameterException e) {
				
				response.setStatus(403);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "non autorizzato");
				System.out.println("not authorized token");
				e.printStackTrace();
				
			}finally {
				out.println(jsonResponse.toString());
			}
			
		}else {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "errore nella sintassi");
			
		}
	
		out.println(jsonResponse.toString());
		
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "POST");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter(); 
		BufferedReader in_body = request.getReader();
		JsonObject jsonResponse = new JsonObject();
		Gson g = new Gson();
		StringBuilder sb = new StringBuilder();
		String line;
		String body;

		while((line = in_body.readLine()) != null) {
			sb.append(line);
		}
				
		body = sb.toString();
				
		JsonObject user = g.fromJson(body, JsonObject.class);
		
		String materia = user.get("materia").getAsString();
		String livello_materia = user.get("livello_materia").getAsString();
		String descrizione = user.get("desc").getAsString();
		Date dataOdierna = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dataStringa = formatter.format(dataOdierna);
		
		//Estrazione del token dall'header
		jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		
		if(isValidTag(livello_materia) && isValidAuthorization() && isValidMateria(materia) && isNotBlank(materia, livello_materia, descrizione, dataStringa)) {
			
			final JwtVal validator = new JwtVal();
			
			try {
			
				DecodedJWT jwt = validator.validate(jwtToken);
				String email = jwt.getClaim("sub-email").asString();
				QueryHandler queryUser = new QueryHandler();
				int user_id = queryUser.getUserId(email);
				
				switch(user_id) {
				
					case 0:
						response.setStatus(400);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("descrizione", "utente non esistente");
						break;
						
					case 1:
						QueryHandler_ticket queryForThis = new QueryHandler_ticket();		        
						
						int id_ticket = queryForThis.inserisciTicket(materia, livello_materia, descrizione, dataStringa, user_id);
						
						if(id_ticket == 0){
							response.setStatus(500);
							jsonResponse.addProperty("stato", "errore server");
							jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
							
						}else if(id_ticket == 1){
							
							Ticket ticket = queryForThis.getTicketFromId(id_ticket);
							
							if(ticket != null) {
								
								response.setStatus(201);
								jsonResponse.addProperty("stato", "confermato");
								jsonResponse.addProperty("desc", "ticket creato");
								
								JsonObject ticket_info = new JsonObject();
								ticket_info.addProperty("numero_ticket", id_ticket);
								ticket_info.addProperty("data_cr", ticket.getData_cr());
								ticket_info.add("ticket_info", g.toJsonTree(ticket));
								
								jsonResponse.add("ticket_inserito", ticket_info);
							
							}else {
								response.setStatus(500);
								jsonResponse.addProperty("stato", "errore server");
								jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
								
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
				
				System.out.println("not created");
				e.printStackTrace();
				
			}finally {
				out.println(jsonResponse.toString());
			}
		}else {
				
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore");
			jsonResponse.addProperty("desc", "errore nella sintassi");
		}
		
		out.println(jsonResponse.toString());
		
	}
	
	/**
	 * @see HttpServlet#doPut(HttpServletRequest request, HttpServletResponse response)
	 */
	//da mettere a posto
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "PUT");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter(); 
		BufferedReader in_body = request.getReader();
		JsonObject jsonResponse = new JsonObject();
		
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
		String valoreTag = user.get("to_edit").getAsJsonObject().get("tag").getAsString();
		
		if(isValidTag(valoreTag) && isValidAuthorization() && isValidMateria(valoreMateria)) {
		
			QueryHandler_ticket queryForThis = new QueryHandler_ticket();
			int hasTicketId = queryForThis.hasTicketId(numeroTicket);
			
			final JwtVal validator = new JwtVal();
			
			try{
				
				DecodedJWT jwtDecoded =  validator.validate(jwtToken);
				
				switch(hasTicketId) {
					case 1:
						
						//esecuzione della query
						int modificaDatiTicket = queryForThis.modificaDatiTicket(numeroTicket, valoreMateria, valoreDescrizione, valoreTag);
						
						if(modificaDatiTicket == 1) {
							response.setStatus(200);
							jsonResponse.addProperty("desc", "ticket modificato");
							jsonResponse.addProperty("stato", "confermato");
						}else if(modificaDatiTicket == 0 || modificaDatiTicket == -1) {
							response.setStatus(400);
							jsonResponse.addProperty("desc", "errore modifica ticket");
							jsonResponse.addProperty("stato", "errore");
						}
						
						Ticket ticket_info = queryForThis.getTicketFromId(numeroTicket);
						
						if(ticket_info == null) {
							response.setStatus(400);
							jsonResponse.addProperty("ticket_info", "impossibile restituire dati ticket");
						}
						jsonResponse.add("ticket_info", g.toJsonTree(ticket_info));
						break;
					
					case 0:
						
						response.setStatus(400);
						jsonResponse.addProperty("stato", "errore");
						jsonResponse.addProperty("desc", "ticket inesistente");
						
						
						break;
						
					case -1:
						
						response.setStatus(500);
						jsonResponse.addProperty("stato", "errore server");
						jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
						
						break;
						
					
				}
				
		
			}catch(InvalidParameterException e) {
			
				response.setStatus(403);
				jsonResponse.addProperty("stato", "errore");
				jsonResponse.addProperty("desc", "non autorizzato");
				System.out.println("not authorized token");
				e.printStackTrace();
			
			}catch(Exception e) {
				
				response.setStatus(500);
				jsonResponse.addProperty("stato", "errore server");
				jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
				System.out.println("no results");
				e.printStackTrace();
				
			}finally {
				out.println(jsonResponse.toString());
			}
		
		}else {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore");
			jsonResponse.addProperty("desc", "errore nella sintassi");
		}
		
			
		
		out.println(jsonResponse.toString());
		
	}
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "DELETE");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter(); 
		BufferedReader in_body = request.getReader();
		JsonObject jsonResponse = new JsonObject();
		Gson g = new Gson();
		StringBuilder sb = new StringBuilder();
		String line;
		String body;

		while((line = in_body.readLine()) != null) {
			sb.append(line);
		}
				
		body = sb.toString();
				
		JsonObject ticket = g.fromJson(body, JsonObject.class);
		
		//Estrazione del token dall'header
		jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		
		//acquisizione delle chiavi
		int numeroTicket = ticket.get("numero_ticket").getAsInt();	
		
		QueryHandler_ticket queryForThis = new QueryHandler_ticket();
		int hasTicketId = queryForThis.hasTicketId(numeroTicket);
		
		final JwtVal validator = new JwtVal();
		
		try{
			
			DecodedJWT jwtDecoded =  validator.validate(jwtToken);
			
			switch(hasTicketId) {
			
			case 1:
				
				response.setStatus(400);
				jsonResponse.addProperty("stato", "confermato");
				jsonResponse.addProperty("desc", "ticket cancellato");
			
				break;
			
			case 0:
				
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore");
				jsonResponse.addProperty("desc", "ticket inesistente");
				
				
				break;
				
			case -1:
				
				response.setStatus(500);
				jsonResponse.addProperty("stato", "errore server");
				jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
				
				break;
				
			}
		
		}catch(InvalidParameterException e) {
			
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore");
			jsonResponse.addProperty("desc", "non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
		
		}catch(Exception e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
			System.out.println("no results");
			e.printStackTrace();
			
		}finally {
			out.println(jsonResponse.toString());
		}
		
		out.println(jsonResponse.toString());
		
	}

}

