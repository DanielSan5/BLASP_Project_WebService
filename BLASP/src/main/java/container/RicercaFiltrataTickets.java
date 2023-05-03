package container;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import classes.QueryHandler;

/**
 * Servlet implementation class RicercaFiltrataTickets
 */
@WebServlet("/Get_tickets")
public class RicercaFiltrataTickets extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	String filter;
	String value;
	String authorizationHeader;
	QueryHandler queryForThis = new QueryHandler();
	
	//Authorization empty check
	public boolean isValidAuthorization() {
		if(authorizationHeader == null || authorizationHeader.isBlank())
			return false;
		else 
			return true;
	}
       
	//Filter check
		public boolean isValidFilter() {
			if (filter.isBlank())
				return false;
			else {
				if (filter == "localita " || filter == "stato" || filter == "classe")
					return true;
				else
					return false;
			}
		}
	
	//Value check
	public boolean isValidValue() {
		
		boolean checkResult = false;
		
		if (value.isBlank()) {
			
			return false;
			
		}else {
			
			switch(filter) {
				
				case "localita":
					
					int hasLocalita = queryForThis.hasLocalita(value);
					if(hasLocalita == 0 || hasLocalita == -1) {
						return false;
						
					}else {
						return true;
						
					}
					
					
				case "stato":
					
					if (value == "libero" || value == "occupato") {
						return true;
						
					}else {
						return false;
						
					}
					
					
				/*case "classe":
					
					int valueInt = Integer.parseInt(value);
					if (valueInt >= 1 && valueInt <= 5) {
						checkResult = true;
						break;
					}else {
						checkResult = false;
						break;
					}*/	
				default:
					return false;
					
			}
			
		}
		
	}
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RicercaFiltrataTickets() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//Estrazione dei parametri dalla richiesta GET
		filter = request.getParameter("filter");
		value = request.getParameter("value");
		
		//Estrazione del token dall'header
		authorizationHeader = request.getHeader("Authorization");
		
		//output writer
		PrintWriter out = response.getWriter();
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(405);
	}

}
