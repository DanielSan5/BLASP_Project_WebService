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

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import classes.JwtVal;
import classes.QueryHandler;
import classes.QueryHandler_ticket;
import classes.Ticket;

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
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Methods", "PUT");
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
		int ticket_id = user.get("numero_ticket").getAsInt();	
		
		final JwtVal validator = new JwtVal();
		
		try{
			
			DecodedJWT jwtDecoded =  validator.validate(jwtToken);
			String email = jwtDecoded.getClaim("sub-email").asString();
			QueryHandler_ticket queryForThis = new QueryHandler_ticket();
			QueryHandler queryForThis_user = new QueryHandler();
			
			int user_id = queryForThis_user.getUserId(email);
			int check = queryForThis.saveFavourites(ticket_id, user_id);
			
			if( check != -1) {
				
				if(check==1) {
					response.setStatus(201);
					jsonResponse.addProperty("stato", "confermato");
					jsonResponse.addProperty("desc", "aggiunto ai preferiti");
				}else
					response.setStatus(500);
					jsonResponse.addProperty("stato", "errore server");
					jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
			}else {
				response.setStatus(500);
				jsonResponse.addProperty("stato", "errore server");
				jsonResponse.addProperty("desc", "problema nell'elaborazione della richiesta");
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
