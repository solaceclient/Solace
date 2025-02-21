package com.bytespacegames.mcpauth;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;

import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

import static huysuh.UI.GuiAlts.cacheAccount;


public class SessionUtils {

	static String recentPkce;
	
	/**
    * Sets the minecraft session to the provided argument session, you may need to set "session" to public
    *
    * @param session      Session instance from net.minecraft.util.Session
    */
	public static void setSession(Session session) {
		Minecraft.getMinecraft().session = session;
		cacheAccount(session.getUsername(), session.getPlayerID(), session.getToken());
	}
	
	/**
    * Generates Proof Key for Code Exchange or a PKCE
    *
	* @return random PKCE
    */
	public static String generatePKCE() {
		SecureRandom secureRandom = new SecureRandom();
		byte[] codeVerifierBytes = new byte[32];
		secureRandom.nextBytes(codeVerifierBytes);
		String codeVerifier = Base64.encodeBase64URLSafeString(codeVerifierBytes);

		return codeVerifier;
	}

	
	/**
	* Tries to login using browser
	* 
	* @throws IOException If an I/O error occurs during the process.
 	* @throws URISyntaxException If an URI Syntax error occurs during the process.
    */
	public static void tryLoginBrowser() throws IOException, URISyntaxException {
		recentPkce = generatePKCE();
		System.out.print("PKCE: " + recentPkce);
		
		WebServer.initWebServer();
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			Desktop.getDesktop()
					.browse(new URI("https://login.live.com/oauth20_authorize.srf?"
							+ "client_id=" + Authentication.CLIENT_ID + "&prompt=select_account"
							+ "&scope=Xboxlive.signin+Xboxlive.offline_access" + "&code_challenge_method=S256"
							+ "&code_challenge=" + Base64.encodeBase64URLSafeString(DigestUtils.sha256(recentPkce))
							+ "&response_type=code" + "&redirect_uri=" + Authentication.REDIRECT_URI));
		} else {
			WebServer.server.stop(0);
		}
	}

	/**
    * Called when the webserver got the response with the code.
	* 
	* @param code          the code provided from login.live.com/oauth20_authorize.srf
	* @return A status string you can put on your GUI
    */
	public static String recieveResponse(String code) {
		try {
			String accessToken = new Authentication().retrieveAccessToken(code,recentPkce);
			JsonObject loginProfileInfo = Authentication.getAccountInfo(accessToken);
			//System.out.println(loginProfileInfo);

			if (loginProfileInfo == null) { return "Error logging in, please try again."; }

			String name = loginProfileInfo.get("name").getAsString();
            		String id = loginProfileInfo.get("id").getAsString();

			setSession(new Session(name, id, accessToken, "legacy"));

			return "Logged in successfully as " + name +"!";
		} catch (Exception e) {
			e.printStackTrace();
			return "Could not login. Please check console!";
		}
	}
}
