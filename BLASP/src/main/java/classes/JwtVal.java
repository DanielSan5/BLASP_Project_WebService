package classes;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtVal {

	public JwtVal() {
		
	}
	
	public DecodedJWT validate(String token) {
		
		
		try {
			
			final DecodedJWT decoded = JWT.decode(token);
			
			
			
			
			
		}catch(Exception e) {
			
		}
		return null;
		
	}

}
