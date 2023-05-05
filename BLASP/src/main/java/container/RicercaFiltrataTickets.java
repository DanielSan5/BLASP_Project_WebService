package container;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import classes.Ticket;

/**
 * Servlet implementation class RicercaFiltrataTickets
 */
@WebServlet("/Get_tickets")
public class RicercaFiltrataTickets extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String filter;
	private String value;
	private String jwtToken;
	private QueryHandler_filters queryForThis = new QueryHandler_filters();
	private String risposta;
	private ArrayList<Ticket> tickets = new ArrayList<Ticket>();
	private Set<String> filters;
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
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RicercaFiltrataTickets() {
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
		 * materia-località ok
		 * materia-classe ok
		 * materia-nome-località ok
		 * materia-nome-classe ok 
		 * materia-località-classe ok
		 * i ticket restituiti non dipendono dallo stato
		 */
		
		
		
		
		//Estrazione del token dall'header
		jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		
		if(isValidFilter(this.filters) && isValidAuthorization() ) {
			
			final JwtVal validator = new JwtVal();
			
			try{
				
				DecodedJWT jwtDecoded =  validator.validate(jwtToken);
				//String email = jwtDecoded.getClaim("sub").asString();
				//mappatura dei parametri "chiave":"valore" ("localita":"Granarolo")
				Map<String, String[]> params = request.getParameterMap();
				filters = params.keySet();
				//si presume che materia sia gia presente per via del controllo 
				if(filters.contains("localita")) {
					tickets = queryForThis.getFor("LM");
					
				}else if(filters.contains("classe")) {
					tickets = queryForThis.getFor("CM");
					
				}else if(filters.contains("nome")) {
					tickets = queryForThis.getFor("NM");
					
				}else if(filters.contains("localita") && filters.contains("classe")) {
					tickets = queryForThis.getFor("LCM");
					
				}else if(filters.contains("localita") && filters.contains("nome")) {
					tickets = queryForThis.getFor("LNM");
					
				}else if(filters.contains("classe") && filters.contains("nome")) {
					tickets = queryForThis.getFor("CNM");
				}else {
					tickets = queryForThis.getFor("M");
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

}
