package container;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import javax.security.auth.login.CredentialNotFoundException;

import org.apache.tomcat.jakartaee.commons.lang3.ArrayUtils;
import org.apache.tomcat.util.codec.binary.Base64;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mysql.cj.util.StringUtils;

import classes.Checks;
import classes.JwtVal;
import classes.QueryHandler;
import classes.QueryHandler_filters;
import classes.QueryHandler_ticket;
import classes.Ticket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;;

/**
 * Servlet implementation class RicercaFiltrataTickets
 */
@WebServlet("/tickets")
public class Tickets extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	//private QueryHandler_filters queryForThis = new QueryHandler_filters();
	
	private HashMap<String, String> getParametersFromQS(String queryString){
		
		String[] singleParameters = queryString.split("[&]");
		HashMap<String, String> parametersPair = new HashMap<String,String>();
		
		for(int i= 0; i < singleParameters.length; i++) {
			String[] p = singleParameters[i].split("[=]"); 
			parametersPair.put(p[0], p[1]);			
		}
		return parametersPair;
		
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
		
		response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		response.addHeader("Access-Control-Allow-Methods", "GET");
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		JsonObject jsonResponse = new JsonObject();
		Gson g = new Gson();
		try{
			//Estrazione del token dall'header
			String[] hd = request.getHeader("Authorization").split("[=]");
			String jwtToken = hd[1].split("[;]")[0];
			//String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
			HashMap<String, String> filters = getParametersFromQS(request.getQueryString());
			System.out.println(filters);
			Set<String> types = filters.keySet();
			
			String[] toCheck = {jwtToken};
			if(Checks.isValidFilter(types) && Checks.isNotBlank(toCheck) ) {
				
				QueryHandler queryForThis = new QueryHandler();
				
				//logica di logout
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
		        byte[] cipheredTokenDigest = digest.digest(jwtToken.getBytes());
		        String jwtTokenDigestInB64 = Base64.encodeBase64String(cipheredTokenDigest);
		        
		        if(queryForThis.isTokenRevoked(jwtTokenDigestInB64)) {
		        	
		        	response.setStatus(401);
					jsonResponse.addProperty("stato", "errore client");
					jsonResponse.addProperty("desc", "utente non autorizzato");
					
		        }else {
		        	
					final JwtVal validator = new JwtVal();
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
					 * ALL --> localita, classe, nome e materia
					 */
					ArrayList<Ticket> tickets = new ArrayList<Ticket>();
					QueryHandler_filters queryForFilters = new QueryHandler_filters();
					
					if(filters.containsKey("localita") && filters.containsKey("nome") && filters.containsKey("classe")) {
						
						String filtroMateria = filters.get("materia").toString()
								.replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
						String filtroLocalita = filters.get("localita").toString()
								.replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
						String filtroNome = filters.get("nome").toString()
								.replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
						
						System.out.println("get all " + filtroMateria + filtroLocalita + filtroNome);
						tickets = queryForFilters.getAll(filtroMateria, filtroNome, filters.get("classe"), filtroLocalita );
					}else if(filters.containsKey("localita") && filters.containsKey("classe")) {
						
						String filtroLocalita = filters.get("localita").toString()
								.replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
						String filtroMateria = filters.get("materia").toString()
								.replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
						System.out.println("get localita classe materia");
						tickets = queryForFilters.getLCM(filtroLocalita, filters.get("classe").toString(), filtroMateria);
						
					}else if(filters.containsKey("localita") && filters.containsKey("nome")) {
						
						String filtroLocalita = filters.get("localita").toString()
								.replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
						String filtroMateria = filters.get("materia").toString()
								.replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");					
						System.out.println("get localita nome materia");
						tickets = queryForFilters.getLNM(filtroLocalita, filters.get("nome").toString(), filtroMateria);
						
					}else if(filters.containsKey("classe") && filters.containsKey("nome")) {
						
						String filtroMateria = filters.get("materia").toString()
								.replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");				
						System.out.println("get classe nome materia");
						tickets = queryForFilters.getCNM(filters.get("classe").toString(), filters.get("nome").toString(), 
								filtroMateria);
						
					}else if(filters.containsKey("localita")) {
						
						String filtroMateria = filters.get("materia").toString()
								.replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");				
						String filtroLocalita = filters.get("localita").toString()
								.replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
						System.out.println("get localita materia");
						tickets = queryForFilters.getLM(filtroLocalita, filtroMateria);
						
					}else if(filters.containsKey("classe")) {
						
						String filtroMateria = filters.get("materia").toString()
								.replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
						System.out.println("get classe materia");
						tickets = queryForFilters.getCM(filters.get("classe").toString(), filtroMateria);
						
					}else if(filters.containsKey("nome")) {
						
						String filtroMateria = filters.get("materia").toString()
								.replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
						
						System.out.println("get nome materia");
						tickets = queryForFilters.getNM(filters.get("nome").toString(), filtroMateria);
						
					}
					else {
						
						String filtroMateria = filters.get("materia").toString().replaceAll("\\+", " ").replaceAll("%2C", ",").replaceAll("%27", "'");
						System.out.println("get materia");
						tickets = queryForFilters.getM(filtroMateria);
						
					}
					
				
					response.setStatus(200);
					jsonResponse.addProperty("stato", "confermato");
					jsonResponse.addProperty("descrizione", "ricerca filtrata");
					jsonResponse.add("filtered", g.toJsonTree(tickets));	
		        }
			}else {
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "errore nella sintassi");
				
			}
			
		}catch(InvalidParameterException  e) {
			
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
			
		}catch(SQLException | NoSuchAlgorithmException e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("descrizione", "errore nell'elaborazione della richiesta");
		
			e.printStackTrace();
			
		}finally {
			out.println(jsonResponse.toString());
		}
			
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		response.addHeader("Access-Control-Allow-Methods", "POST");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		response.addHeader("Access-Control-Expose-Headers", "Set-cookie");
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
		try {		
			
			JsonObject user = g.fromJson(body, JsonObject.class);
			
			String materia = user.get("materia").getAsString();
			String livello_materia = user.get("livello_materia").getAsString();
			String descrizione = user.get("desc").getAsString();
			Date dataOdierna = new Date();
	        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String dataStringa = formatter.format(dataOdierna);
			
			//Estrazione del token dall'header
			String[] hd = request.getHeader("Cookie").split("[=]");
			String jwtToken = hd[1].split("[;]")[0];
			//String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
			String[] toCheck = {jwtToken, materia, livello_materia, descrizione, dataStringa};
			
			
			if(Checks.isValidTag(livello_materia) && Checks.isValidMateria(materia) && Checks.isNotBlank(toCheck)) {
			
				QueryHandler queryForThis = new QueryHandler();
				
				//logica di logout
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
		        byte[] cipheredTokenDigest = digest.digest(jwtToken.getBytes());
		        String jwtTokenDigestInB64 = Base64.encodeBase64String(cipheredTokenDigest);
		        
		        if(queryForThis.isTokenRevoked(jwtTokenDigestInB64)) {
		        	
		        	response.setStatus(401);
					jsonResponse.addProperty("stato", "errore client");
					jsonResponse.addProperty("desc", "utente non autorizzato");
					
		        }else {
		        	
					final JwtVal validator = new JwtVal();
				
					DecodedJWT jwt = validator.validate(jwtToken);
					String email = jwt.getClaim("sub-email").asString();
					
					int user_id = queryForThis.getUserId(email);
		
					QueryHandler_ticket queryForTicket = new QueryHandler_ticket();		        
					
					int id_ticket = queryForTicket.inserisciTicketOttieniID(materia, livello_materia, descrizione, dataStringa, user_id);
			
					Ticket ticket = queryForTicket.getTicketFromId(id_ticket);
					
					response.setStatus(201);
					jsonResponse.addProperty("stato", "confermato");
					jsonResponse.addProperty("desc", "ticket creato");
					
					jsonResponse.add("ticket_info", g.toJsonTree(ticket));
				
		        }
				
			}else {
				
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("desc", "errore nella sintassi");
			}
				
		}catch(InvalidParameterException e) {
			
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
	
		} catch (SQLException | NoSuchAlgorithmException e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("descrizione", "errore nell'elaborazione della richiesta");
			e.printStackTrace();
		} catch (CredentialNotFoundException e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "nessun risultato");
			e.printStackTrace();
		}catch(JsonSyntaxException | NullPointerException  | NumberFormatException e) {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "formato non supportato");
			e.printStackTrace();
		}finally {
			out.println(jsonResponse.toString());
		}
	
		
	
		
	}
	
	/**
	 * @see HttpServlet#doPut(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		response.addHeader("Access-Control-Allow-Methods", "PUT");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		response.addHeader("Access-Control-Expose-Headers", "Set-cookie");
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

		try{
			
			Gson g = new Gson();
			JsonObject user = g.fromJson(body, JsonObject.class);
			
			//Estrazione del token dall'header
			String[] hd = request.getHeader("Cookie").split("[=]");
			String jwtToken = hd[1].split("[;]")[0];
			//String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
			
			//acquisizione delle chiavi
			String numeroTicket = user.get("numero_ticket").getAsString();	
			//acquisizione chiavi dell'ogetto "to_edit"
			String valoreMateria = user.get("to_edit").getAsJsonObject().get("materia").getAsString();
			String valoreDescrizione = user.get("to_edit").getAsJsonObject().get("descrizione").getAsString();
			String valoreTags = user.get("to_edit").getAsJsonObject().get("tags").getAsString();
			String[] toCheck = {jwtToken, valoreDescrizione, numeroTicket};
			
			
			if(Checks.isValidTag(valoreTags) && Checks.isNotBlank(toCheck) && Checks.isValidMateria(valoreMateria)) {
		
				QueryHandler queryForThis = new QueryHandler();
				
				//logica di logout
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
		        byte[] cipheredTokenDigest = digest.digest(jwtToken.getBytes());
		        String jwtTokenDigestInB64 = Base64.encodeBase64String(cipheredTokenDigest);
		        
		        if(queryForThis.isTokenRevoked(jwtTokenDigestInB64)) {
		        	
		        	response.setStatus(401);
					jsonResponse.addProperty("stato", "errore client");
					jsonResponse.addProperty("desc", "utente non autorizzato");
					
		        }else {
		        	
					QueryHandler_ticket queryForTicket = new QueryHandler_ticket();
					boolean hasTicketId = queryForTicket.hasTicketId(Integer.parseInt(numeroTicket));
					
					final JwtVal validator = new JwtVal();
	
					DecodedJWT jwt = validator.validate(jwtToken);
					String email = jwt.getClaim("sub-email").asString();
					int user_id = queryForThis.getUserId(email);
					
					if(hasTicketId) {
						
						//esecuzione della query (void)
						queryForTicket.modificaDatiTicket(Integer.parseInt(numeroTicket), valoreMateria, valoreDescrizione, valoreTags, user_id);
	
						response.setStatus(200);
						jsonResponse.addProperty("desc", "ticket modificato");
						jsonResponse.addProperty("stato", "confermato");
						
						Ticket ticket_info = queryForTicket.getTicketFromId(Integer.parseInt(numeroTicket));
						jsonResponse.add("ticket_info", g.toJsonTree(ticket_info));
		
					}else {
						
						response.setStatus(400);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("desc", "ticket inesistente");
						
					}
		        }
			}else {
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("desc", "sintassi errata nella richiesta");
			}
		
		}catch(InvalidParameterException e) {
		
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("desc", "utente non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
		
		} catch ( SQLException | NoSuchAlgorithmException e) {
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("desc", "errore nell'elaborazione della richiesta");
			e.printStackTrace();
		} catch (CredentialNotFoundException e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("desc", "nessun risultato");
			e.printStackTrace();
		}catch(JsonSyntaxException | NullPointerException  | NumberFormatException e) {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "formato non supportato");
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "oggetto inesistente");
			e.printStackTrace();
		}finally {
			out.println(jsonResponse.toString());
		}
		
	}
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
		response.addHeader("Access-Control-Allow-Methods", "DELETE");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		response.addHeader("Access-Control-Expose-Headers", "Set-cookie");
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
		try{	
			
			JsonObject ticket = g.fromJson(body, JsonObject.class);
			
			//Estrazione del token dall'header
			String[] hd = request.getHeader("Cookie").split("[=]");
			String jwtToken = hd[1].split("[;]")[0];
			//String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
			//acquisizione delle chiavi
			String  numeroTicket = ticket.get("numero_ticket").getAsString();	
			String[] toCheck = {jwtToken, numeroTicket};
			
			if(Checks.isNotBlank(toCheck)) {
				QueryHandler queryForThis = new QueryHandler();
				
				//logica di logout
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
		        byte[] cipheredTokenDigest = digest.digest(jwtToken.getBytes());
		        String jwtTokenDigestInB64 = Base64.encodeBase64String(cipheredTokenDigest);
		        
		        if(queryForThis.isTokenRevoked(jwtTokenDigestInB64)) {
		        	
		        	response.setStatus(401);
					jsonResponse.addProperty("stato", "errore client");
					jsonResponse.addProperty("desc", "utente non autorizzato");
					
		        }else {
		        	
					final JwtVal validator = new JwtVal();
					DecodedJWT jwt = validator.validate(jwtToken);
					String email = jwt.getClaim("sub-email").asString();
					int user_id = queryForThis.getUserId(email);
					//da controllare che il ticket sia veramente dell'utente
					QueryHandler_ticket queryForTicket = new QueryHandler_ticket();
					boolean hasTicketId = queryForTicket.hasTicketId(Integer.parseInt(numeroTicket));
					
					if(hasTicketId) {
						
						queryForTicket.cancellaTicket(Integer.parseInt(numeroTicket), user_id);
						response.setStatus(200);
						jsonResponse.addProperty("stato", "confermato");
						jsonResponse.addProperty("desc", "ticket cancellato");
					
					}else {
						response.setStatus(400);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("desc", "ticket inesistente");
					}
		        }
			}else {
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("desc", "sintassi errata nella richiesta");
			}
	
		}catch(InvalidParameterException e) {
			
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("desc", "utente non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
		
		} catch ( SQLException | NoSuchAlgorithmException e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
			e.printStackTrace();
			
		}catch(JsonSyntaxException | NullPointerException  | NumberFormatException e) {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "formato non supportato");
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "oggetto inesistente");
			e.printStackTrace();
		} catch (CredentialNotFoundException e) {
			
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "nessun risultato");
			e.printStackTrace();
		}finally {
			out.println(jsonResponse.toString());
		}	
		
	}

}

