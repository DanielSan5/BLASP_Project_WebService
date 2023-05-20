package container;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.text.Normalizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.CredentialNotFoundException;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import classes.Checks;
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//ottenimento solo dei dati personali
		response.setContentType("application/json");
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "GET");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		response.addHeader("Access-Control-Expose-Headers", "Set-cookie");
		
		PrintWriter out = response.getWriter(); 
		JsonObject jsonResponse = new JsonObject();
		Gson g = new Gson();
		try{
			
			String[] hd = request.getHeader("Cookie").split("[=]");
			String jwtToken = hd[1];
			//String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
			
			String [] toCheck = {jwtToken};
			
			if(Checks.isNotBlank(toCheck)) {
			
				final JwtVal validator = new JwtVal();
	
				//se non viene autorizzato lancia eccezzione gestita nel catch sotto
				DecodedJWT jwtDecoded = validator.validate(jwtToken);
				
				String email = jwtDecoded.getClaim("sub-email").asString();
				QueryHandler queryForThis = new QueryHandler();
				
				int user_id = queryForThis.getUserId(email);
				Utente userData = queryForThis.getUserData(user_id);
				ArrayList<Ticket> userTickets = queryForThis.getUserTickets(user_id);
				boolean isAdmin = queryForThis.isUserAdmin(user_id);
				

				response.setStatus(200);
				jsonResponse.addProperty("stato", "confermato");
				jsonResponse.addProperty("desc", " ottenimento dati personali");
				jsonResponse.add("user_info", g.toJsonTree(userData));
				jsonResponse.add("user_tickets", g.toJsonTree(userTickets));
				
				if(isAdmin) {
					
					ArrayList<Utente> toBlock = queryForThis.getToBlock();
					jsonResponse.add("to_block", g.toJsonTree(toBlock));
				
				}
					
			}else {
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "sintassi errata");
			}	
		}catch(InvalidParameterException | NullPointerException e) {
			
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
			
		} catch (SQLException e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("descrizione", "errore nell'elaborazione della richiesta");
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "POST");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		response.addHeader("Access-Control-Expose-Headers", "Set-cookie");
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
		try {
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
		
			
			if(Checks.isValidDateOfBirth(data_nascita) && Checks.isValidPassword(password) && Checks.isValidEmail(email) && Checks.isValidClass(classe) && 
					Checks.isConfirmedPassword(password, confirm_password) && Checks.isValidNameAndSurname(nome, cognome, email) && 
					Checks.isValidAge(data_nascita, classe) && Checks.isValidLocation(localita) && Checks.isValidSTA(indirizzo_scolastico)) {
				/*
				 * psw encryption
				 */
				String encryptedPass = passEncr(password);
				QueryHandler queryForThis = new QueryHandler();
				
				boolean hasEmail = queryForThis.hasEmail(email); 
				
				if(hasEmail) {
					
					response.setStatus(400);
					jsonResponse.addProperty("stato", "errore client");
					jsonResponse.addProperty("descrizione", "utente gia esistente");
						
				}else {
					
					//gestito nel catch
					queryForThis.inserisciUtente(email, encryptedPass, nome, cognome, data_nascita, classe, indirizzo_scolastico, localita);

					JsonObject jwtFormat = new JsonObject();
					
					jwtFormat.addProperty("sub-email", email);
					jwtFormat.addProperty("aud", "*");
					
					JwtGen generator = new JwtGen();
					Map<String, String> claims = new HashMap<>();
					
					jwtFormat.keySet().forEach(keyStr ->
				    {
				    	
				        String keyvalue = jwtFormat.get(keyStr).getAsString();
				        claims.put(keyStr, keyvalue);
				        
				    });
					
					LocalDate oggi = LocalDate.now();
					LocalDate domani = oggi.plusDays(1);
					ZonedDateTime domaniUTC = domani.atStartOfDay(ZoneId.of("UTC"));
					String domaniUTCFormatted = domaniUTC.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
					
					String token = generator.generateJwt(claims);
					response.addHeader("Set-cookie","__refresh__token=" + token + "; HttpOnly; Secure; exp=" + domaniUTCFormatted);
					response.setStatus(201);
					jsonResponse.addProperty("stato", "confermato");
					jsonResponse.addProperty("desc", "utente creato");
	
				}
				
			}else {
				
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "errore nella sintassi");
				
			}
			
		}catch(SQLException | JWTCreationException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
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
		
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "PUT");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		response.addHeader("Access-Control-Expose-Headers", "Set-cookie");
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
		try {
			
			Gson g = new Gson();
			JsonObject user = g.fromJson(body, JsonObject.class);
			//acquisizione valore delle chiavi
			boolean action_modifica_password = user.get("action_modificaPassword").getAsBoolean();
			String descrizione = user.get("descrizione").getAsString();
			String localita = user.get("localita").getAsString();
			int classe = user.get("classe").getAsInt();
			String indirizzo = user.get("indirizzo").getAsString();
			//Estrazione del token dall'header
			String[] hd = request.getHeader("Cookie").split("[=]");
			String jwtToken = hd[1];
			//String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
			String[] toCheck = {jwtToken};
			
			
			if( Checks.isValidClass(classe) && Checks.isValidLocation(localita) && Checks.isValidSTA(indirizzo) && Checks.isNotBlank(toCheck)) {
			
				final JwtVal validator = new JwtVal();
				DecodedJWT jwt = validator.validate(jwtToken);
			
				String email = jwt.getClaim("sub-email").asString();
				QueryHandler queryUser = new QueryHandler();
				int user_id = queryUser.getUserId(email);
				
				if(action_modifica_password) {
					
					//acquisizione valore delle chiavi
					
					String old_password = user.get("old_password").getAsString();
					String new_password = user.get("new_password").getAsString();
					String confirm_new_password = user.get("confirm_new_password").getAsString();
					if(Checks.isValidPassword(new_password) && Checks.isConfirmedPassword(new_password, confirm_new_password)) {
						
						boolean checkPassword = queryUser.checkPass(user_id, old_password);
						
						if(checkPassword) {
						
							/*
							 * psw encryption
							 */
							String encryptedPass = passEncr(new_password);
							
							queryUser.modificaDatiUtente(user_id, descrizione, localita, classe, indirizzo);

							queryUser.changePass(user_id, encryptedPass);
								
							response.setStatus(200);
							jsonResponse.addProperty("stato", "confermato");
							jsonResponse.addProperty("desc", "dati utente e psw modificati");
				
						}else{
							
							response.setStatus(401);
							jsonResponse.addProperty("stato", "errore client");
							jsonResponse.addProperty("descrizione", "credenziali non valide");
							
						}
						
					}else {
						response.setStatus(400);
						jsonResponse.addProperty("stato", "errore client");
						jsonResponse.addProperty("desc", "sintassi errata nella richiesta");
					}

				}else{
					
					queryUser.modificaDatiUtente(user_id, descrizione, localita, classe, indirizzo);
					response.setStatus(200);
					jsonResponse.addProperty("stato", "confermato");
					jsonResponse.addProperty("desc", "dati utente modificati");
					
				}
				
			}else {
				 response.setStatus(400);
				 jsonResponse.addProperty("stato", "errore client");
				 jsonResponse.addProperty("desc", "sintassi errata nella richiesta");
			}
			
		}catch(InvalidParameterException e) {
			
			response.setStatus(403);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "non autorizzato");
			System.out.println("not authorized token");
			e.printStackTrace();
	
		}catch (SQLException e) {
			
			response.setStatus(500);
			jsonResponse.addProperty("stato", "errore server");
			jsonResponse.addProperty("descrizione", "problema nell'elaborazione della richiesta");
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
		response.setStatus(405);
	}
	
}

