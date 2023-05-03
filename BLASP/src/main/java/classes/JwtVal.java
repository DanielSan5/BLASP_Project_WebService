package classes;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

public class JwtVal {

	//lista di emittenti fidati
    private static final List<String> allowedIsses = Collections.singletonList("https://blasp.mooo.com");

    private RSAPublicKey loadPublicKey() throws JwkException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        
    	//ottengo la chiave pubblica dalla variabile di ambiente PUBLIC_KEY
			String path = System.getenv("PUBLIC_KEY");
			File filePub = new File(path);
			String key = new String(Files.readAllBytes(filePub.toPath()), Charset.defaultCharset());
			
			String publicKeyPEM = key
				      .replace("-----BEGIN PUBLIC KEY-----", "")
				      .replaceAll(System.lineSeparator(), "")
				      .replace("-----END PUBLIC KEY-----", "");
			
			
		    byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

		    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
		    return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }
    
	public DecodedJWT validate(String token) {
		
		
		try {
			
			final DecodedJWT decodedjwt = JWT.decode(token);
			
			if (!allowedIsses.contains(decodedjwt.getIssuer())) {
                throw new InvalidParameterException(String.format("Unknown Issuer %s", decodedjwt.getIssuer()));
            }

            RSAPublicKey publicKey = loadPublicKey();

            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(decodedjwt.getIssuer())
                    .build();

            verifier.verify(token);
            
            return decodedjwt;
			
			
			
		}catch(Exception e) {
			
            throw new InvalidParameterException("JWT validation failed: " + e.getMessage());
		}
		
		
	}

}
