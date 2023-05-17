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

import classes.Checks;
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
	private QueryHandler_filters queryForThis = new QueryHandler_filters();
			
				
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
		response.addHeader("Access-Control-Allow-Methods", "GET");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JsonObject jsonResponse = new JsonObject();
		Gson g = new Gson();
		//Estrazione del token dall'header
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		Map<String, String[]> filters = request.getParameterMap();
		Set<String> types = filters.keySet();
		
		String[] toCheck = {jwtToken};
		if(Checks.isValidFilter(types) && Checks.isNotBlank(toCheck) ) {
			
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
					String filtroLocalita = filters.get("localita").toString().replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
					String filtroMateria = filters.get("materia").toString().replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
					
					tickets = queryForThis.getLCM(filtroLocalita, filters.get("classe").toString(), 
							filtroMateria);
					
				}else if(filters.containsKey("localita") && filters.containsKey("nome")) {
					String filtroLocalita = filters.get("localita").toString().replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
					String filtroMateria = filters.get("materia").toString().replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");					
					
					tickets = queryForThis.getLNM(filtroLocalita, filters.get("nome").toString(), 
							filtroMateria);
					
				}else if(filters.containsKey("classe") && filters.containsKey("nome")) {
					String filtroMateria = filters.get("materia").toString().replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");				
					
					tickets = queryForThis.getCNM(filters.get("classe").toString(), filters.get("nome").toString(), 
							filtroMateria);
					
				}else if(filters.containsKey("localita")) {
					String filtroMateria = filters.get("materia").toString().replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");				
					String filtroLocalita = filters.get("localita").toString().replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
					
					tickets = queryForThis.getLM(filtroLocalita, filtroMateria);
					
				}else if(filters.containsKey("classe")) {
					String filtroMateria = filters.get("materia").toString().replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
					
					tickets = queryForThis.getCM(filters.get("classe").toString(), filtroMateria);
					
				}else if(filters.containsKey("nome")) {
					String filtroMateria = filters.get("materia").toString().replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
					
					tickets = queryForThis.getNM(filters.get("nome").toString(), filtroMateria);
					
				}
				else {
					String filtroMateria = filters.get("materia").toString().replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
					
					tickets = queryForThis.getM(filtroMateria);
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
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		String[] toCheck = {jwtToken, materia, livello_materia, descrizione, dataStringa};
		
		if(Checks.isValidTag(livello_materia) && Checks.isValidMateria(materia) && Checks.isNotBlank(toCheck)) {
			
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
					case -1:
						response.setStatus(500);
						jsonResponse.addProperty("stato", "errore server");
						jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");	
						break;
						
					default:
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
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		
		//acquisizione delle chiavi
		String numeroTicket = user.get("numero_ticket").getAsString();	
		//acquisizione chiavi dell'ogetto "to_edit"
		String valoreMateria = user.get("to_edit").getAsJsonObject().get("materia").getAsString();
		String valoreDescrizione = user.get("to_edit").getAsJsonObject().get("descrizione").getAsString();
		String valoreTag = user.get("to_edit").getAsJsonObject().get("tag").getAsString();
		String[] toCheck = {jwtToken, valoreDescrizione, numeroTicket};
		
		if(Checks.isValidTag(valoreTag) && Checks.isNotBlank(toCheck) && Checks.isValidMateria(valoreMateria)) {
		
			QueryHandler_ticket queryForThis = new QueryHandler_ticket();
			int hasTicketId = queryForThis.hasTicketId(Integer.parseInt(numeroTicket));
			
			final JwtVal validator = new JwtVal();
			
			try{
				
				validator.validate(jwtToken);
				
				switch(hasTicketId) {
					case 1:
						
						//esecuzione della query
						int modificaDatiTicket = queryForThis.modificaDatiTicket(Integer.parseInt(numeroTicket), valoreMateria, valoreDescrizione, valoreTag);
						
						if(modificaDatiTicket == 1) {
							response.setStatus(200);
							jsonResponse.addProperty("desc", "ticket modificato");
							jsonResponse.addProperty("stato", "confermato");
						}else if(modificaDatiTicket == 0 || modificaDatiTicket == -1) {
							response.setStatus(400);
							jsonResponse.addProperty("desc", "errore modifica ticket");
							jsonResponse.addProperty("stato", "errore");
						}
						
						Ticket ticket_info = queryForThis.getTicketFromId(Integer.parseInt(numeroTicket));
						
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
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("desc", "utente non autorizzato");
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
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("desc", "sintassi errata nella richiesta");
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
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		//acquisizione delle chiavi
		String  numeroTicket = ticket.get("numero_ticket").getAsString();	
		String[] toCheck = {jwtToken, numeroTicket};
		
		if(Checks.isNotBlank(toCheck)) {
			
			QueryHandler_ticket queryForThis = new QueryHandler_ticket();
			int hasTicketId = queryForThis.hasTicketId(Integer.parseInt(numeroTicket));
			
			final JwtVal validator = new JwtVal();
			
			try{
				
				validator.validate(jwtToken);
				
				switch(hasTicketId) {
				
					case 1:
						
						response.setStatus(200);
						jsonResponse.addProperty("stato", "confermato");
						jsonResponse.addProperty("desc", "ticket cancellato");
					
						break;
					
					case 0:
						
						response.setStatus(400);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("desc", "sintassi errata nella richiesta");
						
						
						break;
						
					case -1:
						
						response.setStatus(500);
						jsonResponse.addProperty("stato", "errore server");
						jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
						
						break;
					
				}
			
			}catch(InvalidParameterException e) {
				
				response.setStatus(403);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("desc", "utente non autorizzato");
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
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("desc", "sintassi errata nella richiesta");
		}
		
		out.println(jsonResponse.toString());
		
	}

}

