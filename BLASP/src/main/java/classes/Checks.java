package classes;

import java.sql.SQLException;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class Checks {

	
	public static  boolean isConfirmedPassword(String password, String confirm_password) {
  	   if(!password.equals(confirm_password)) {
  		  System.out.println("password non coincidono ");
  		   return false;
     	   }
  	   
  	   return true;
	}
	    
	public static boolean isValidEmail(String email) {
     	
	 	String regexPattern = "^[a-zA-Z]+\\.[a-zA-Z]+@(aldini\\.istruzioneer\\.it|avbo\\.it)$";
	 	
	 	if((email.isBlank()) || (email.matches(regexPattern) == false)) {
	 		System.out.println("email errata");
	 		return false;
	 	}
	 	else 	
	 		return true;
     }
	    	  
	public static boolean isValidPassword(String password) {
    	
    	boolean hasLowerCase = false;
    	boolean hasUpperCase = false;
    	boolean hasDigit = false;
    	boolean hasSpecialChar = false;
    	String specialChars = "!?&$";
    	
    	for(int i=0; i < password.length(); i++) {
    		char passwordChar = password.charAt(i);
    		if(Character.isLowerCase(passwordChar))
    			hasLowerCase = true;
    		else if (Character.isUpperCase(passwordChar))
    			hasUpperCase = true;
    		else if(Character.isDigit(passwordChar)) 
                hasDigit = true;
            else if(specialChars.indexOf(passwordChar) != -1)
                hasSpecialChar = true;
    	}
    	
		if(password.length() > 8 && hasLowerCase && hasUpperCase && hasDigit && hasSpecialChar) 
			return true;
		else 
			return false;
    	
    }
   
	public static boolean isValidNameAndSurname(String nome, String cognome, String email) {
		
		//nome inserito
	   	String nomeNormalizzato = Normalizer.normalize(nome.toLowerCase(), Normalizer.Form.NFD);
	   	String cognomeNormalizzato = Normalizer.normalize(cognome.toLowerCase(), Normalizer.Form.NFD);
	   	
	   	String nomeNoAccenti = nomeNormalizzato.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	   	String cognomeNoAccenti = cognomeNormalizzato.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	   	
	   	String nomeNoSpazi = nomeNoAccenti.replaceAll("\\s+","");
	   	String cognomeNoSpazi = cognomeNoAccenti.replaceAll("\\s+","");
	   	
	   	String nomeNoApostrofo = nomeNoSpazi.replaceAll("'", "");
	   	String cognomeNoApostrofo = cognomeNoSpazi.replaceAll("'", "");
	   	
	   //nome vero
	   	String[] splittedEmail = email.split("[@]");
		String[] names = splittedEmail[0].split("[.]");
		
		String nomeCorretto = names[0];
		String cognomeCorretto = names[1];
		
	   	System.out.println(nomeCorretto + " " + cognomeCorretto);
	   	
	   	if(nomeCorretto.equals(nomeNoApostrofo) && cognomeCorretto.equals(cognomeNoApostrofo) && !nomeNoApostrofo.isBlank() && !cognomeNoApostrofo.isBlank())
	   		return true;
	   	else 
	   		System.out.println("nome errato");
	   		return false;
   }

	public static boolean isValidClass(int classe) {
   	
	   	if(classe < 1 || classe > 5) {
	   		System.out.println("classe errata");
	   		return false;
	   	}else
	   		return true;
	   	
    }
   
	public static boolean isValidSTA(String indirizzo_scolastico) throws SQLException {
   	
 		QueryHandler queryIndirizzo = new QueryHandler();
     	String indirizzo_upperCase = indirizzo_scolastico.toUpperCase();
     	return queryIndirizzo.hasIndirizzo(indirizzo_upperCase);
     	
    }
     
	public static boolean isValidLocation(String localita) throws SQLException {
		
	   	QueryHandler queryLocalita = new QueryHandler();
	   	return queryLocalita.hasLocalita(localita);
	   
   }
   
	public static boolean isValidAge(String date, int classe) {
		
	   	boolean result = false;
	   	int annoDataInput = Integer.parseInt(date.substring(0, 4)); //prende solo il primo valore (prime 4 cifre) quindi l'anno
	   	int currentYear = LocalDate.now().getYear();
	   	
	   	switch(classe) {
	   	case 5:
	   		if((currentYear - annoDataInput) >= 17)		
	   			result = true;
	   		break;
	   	case 4:
	   		if((currentYear - annoDataInput) >= 16)		
	   			result = true;
	   		break;
	   	case 3:
	   		if((currentYear - annoDataInput) >= 15)		
	   			result = true;
	   		break;
	   	case 2:
	   		if((currentYear - annoDataInput) >= 14)		
	   			result = true;
	   		break;
	   	case 1:
	   		if((currentYear - annoDataInput) >= 13)		
	   			result = true;
	   		break;
	   	}
	   	
	   	return result;
   	
   }
   
	public static boolean isValidDateOfBirth(String strDate){
	 	/* Check if date is 'null' */
		if (strDate.trim().equals("")){
		    return false;
		}else{
		 
			SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd");
			sdfrmt.setLenient(false);
			
			try{
			    sdfrmt.parse(strDate); 
			    System.out.println(strDate+" is valid date format");
			}catch (ParseException e){
			    System.out.println(strDate+" is Invalid Date format");
			    return false;
			}
		
			return true;
		}
   }
   
	public static boolean isNotBlank(String[] toCheck) {
	   
	   for(int i=0; i<toCheck.length; i++) {
		   if(toCheck[i].isBlank() || toCheck[i] == null) {
			   return false;
		   }
	   }
	   return true;
   }

	public static boolean isValidFilter(Set<String> filters) {
		
		if(filters.contains("materia")){
			
			while( filters.iterator().hasNext()) {
				
				String element = filters.iterator().next();
				if(element.equals("localita")  || element.equals("classe") || element.equals("nome")) {
					return true;
				}else {
					return false;
				}
			}
				
		}
		return false;
			
			
	}

	public static boolean isValidTag(String tag) {

		ArrayList<String> validStrings = new ArrayList<>(List.of("prima", "seconda", "terza", "quarta", "quinta"));
		String[] tagSeparati = tag.split(",");
		for(int i = 0; i<tagSeparati.length; i++) {
			if(!validStrings.contains(tagSeparati[i])) {
				return false;
			}
		}
		return true;
		
	}

	public static boolean isValidMateria(String materia) throws SQLException {
		
		QueryHandler queryForThis = new QueryHandler();
		return queryForThis.checkExistMateria(materia);
		
	}
}
