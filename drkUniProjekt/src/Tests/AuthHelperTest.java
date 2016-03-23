/**
 * 
 */
package Tests;

import static org.junit.Assert.*;

import java.security.SignatureException;
import java.util.Calendar;

import org.junit.Test;

import drkprojekt.auth.AuthHelper;
import drkprojekt.auth.TokenInfo;

/**
 * @author Steffen Terheiden
 *
 */
public class AuthHelperTest {

    @Test
    public void test() {
	String token="";
	try {
	    token = AuthHelper.createJsonWebToken("User1", "Susi Sorglos", false, (long)10000);
	} catch (SignatureException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	System.out.println(token);
	//token="eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJEUktVbmlQcm9qZWt0IiwiYXVkIjoiQ2xpZW50Rm9yRFJLTWVtYmVyIiwiaWF0IjoxNDU4NzQ3NTM0LCJleHAiOjIzMjI3NDc1MzQsImluZm8iOnsidXNlcklkIjoiVXNlcjEiLCJ1c2VyUm9sZSI6InRlYW1NZW1iZXIifX0.nsp9FGp7oOn1SQBXnFv-un7s3ePEI0zIx9FYDsaMINs";
	TokenInfo info = AuthHelper.verifyToken(token);
	System.out.println("USer:" +info.getUserId());
	if(info.getExpires().getMillis()<System.currentTimeMillis()/1000){
	    System.out.println("Expired");
	}
	System.out.println(System.currentTimeMillis());
	System.out.println(info.getIssued().getMillis());
	System.out.println(info.getExpires().getMillis());
	assertEquals("Userid gleich","User1",info.getUserId());
    }

}
