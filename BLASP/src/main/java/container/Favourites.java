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
import java.sql.SQLException;
import java.util.ArrayList;

import javax.security.auth.login.CredentialNotFoundException;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import classes.Checks;
import classes.JwtVal;
import classes.QueryHandler;
import classes.QueryHandler_ticket;
import classes.Ticket;
import classes.Utente;

/**
 * Servlet implementation class favourites
 */
@WebServlet("/fav")
public class Favourites extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Favourites() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//ottenimento ticket preferiti
		response.setContentType("application/json");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET");

        PrintWriter out = response.getWriter(); 
        JsonObject jsonResponse = new JsonObject();
        Gson g = new Gson();
        try{
        	
	        String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
	        String [] toCheck = {jwtToken};
	
	        if(Checks.isNotBlank(toCheck)) {
	
	            final JwtVal validator = new JwtVal();

                //se non viene autorizzato lancia eccezzione gestita nel catch sotto
                DecodedJWT jwtDecoded = validator.validate(jwtToken);

                String email = jwtDecoded.getClaim("sub-email").asString();
                QueryHandler_ticket queryTickets = new QueryHandler_ticket();
                QueryHandler queryForThis = new QueryHandler();
                int user_id = queryForThis.getUserId(email);
                Utente userData = queryForThis.getUserData(user_id);
                ArrayList<Ticket> FavTickets = queryTickets.getFavTickets(user_id);

                response.setStatus(200);
                jsonResponse.addProperty("stato", "confermato");
                jsonResponse.addProperty("desc", " ottenimento preferiti");

                jsonResponse.add("Fav", g.toJsonTree(FavTickets));
                jsonResponse.add("user_info", g.toJsonTree(userData));
                
	        }else {
	            response.setStatus(400);
	            jsonResponse.addProperty("stato", "errore client");
	            jsonResponse.addProperty("descrizione", "sintassi errata");
	        }
        }catch(InvalidParameterException e) {

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
        }catch(JsonSyntaxException | NullPointerException e) {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("descrizione", "formato non supportato");
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
		response.setContentType("application/json");
		PrintWriter out = response.getWriter(); 
		BufferedReader in_body = request.getReader();
		JsonObject jsonResponse = new JsonObject();
		
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
		String ticket_id = user.get("numero_ticket").getAsString();	
		
		String [] toCheck = {jwtToken, ticket_id};
		
		if(Checks.isNotBlank(toCheck)) {
			
			final JwtVal validator = new JwtVal();
			
			try{
				
				DecodedJWT jwtDecoded =  validator.validate(jwtToken);
				String email = jwtDecoded.getClaim("sub-email").asString();
				QueryHandler_ticket queryForThis = new QueryHandler_ticket();
				
				boolean hasTicket = queryForThis.hasTicketId(Integer.parseInt(ticket_id));
				
				if(hasTicket) {

					QueryHandler queryForThis_user = new QueryHandler();
					
					int user_id = queryForThis_user.getUserId(email);
					queryForThis.saveFavourites(Integer.parseInt(ticket_id), user_id);
					
					response.setStatus(201);
					jsonResponse.addProperty("stato", "confermato");
					jsonResponse.addProperty("desc", "aggiunto ai preferiti");

				}else {
					response.setStatus(400);
					jsonResponse.addProperty("stato", "errore client");
					jsonResponse.addProperty("desc", "ticket inesistente");
				}
			
			}catch(InvalidParameterException e) {
			
				response.setStatus(403);
				jsonResponse.addProperty("stato", "errore");
				jsonResponse.addProperty("desc", "non autorizzato");
				System.out.println("not authorized token");
				e.printStackTrace();
			
			}catch(SQLException e) {
				
				response.setStatus(500);
				jsonResponse.addProperty("stato", "errore server");
				jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
				System.out.println("no results");
				e.printStackTrace();
				
			} catch (CredentialNotFoundException e) {
				
				response.setStatus(400);
				jsonResponse.addProperty("stato", "errore client");
				jsonResponse.addProperty("descrizione", "nessun risultato");
				e.printStackTrace();
			}finally {
				out.println(jsonResponse.toString());
			}
			
		}else {
			response.setStatus(400);
			jsonResponse.addProperty("stato", "errore client");
			jsonResponse.addProperty("desc", "errore nella sintassi");
		}
		
		out.println(jsonResponse.toString());
		
	}

}
