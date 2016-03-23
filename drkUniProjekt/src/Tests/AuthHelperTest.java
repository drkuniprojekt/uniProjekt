/**
 * 
 */
package Tests;

import static org.junit.Assert.*;

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
	String token= AuthHelper.createJsonWebToken("User1", "Susi Sorglos", false, (long)10000);
	System.out.println(token);
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
