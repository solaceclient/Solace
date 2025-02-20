package com.bytespacegames.mcpauth;

import java.io.StringReader;
import java.net.URI;

import com.bytespacegames.mcpauth.http.HttpClient;
import com.bytespacegames.mcpauth.http.HttpRequest;
import com.bytespacegames.mcpauth.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class Authentication {
    
    // we are using essential's client id as we need to apply for a forum so minecraft verifies your azure app client id, so to prevent that process we are using a one that is allready accepted
    public static final String CLIENT_ID = "e39cc675-eb52-4475-b5f8-82aaae14eeba";
    public static final String REDIRECT_URI = "http://localhost:6921/microsoft/complete";

    /**
    * Retreives a minecraft access token that is then returned back as a String
    *
    * @param mscode       The microsoft code provided by our WebServer
    * @param recentPkce   The pkce/Proof Key for Code Exchange that we generated earlier
    */
    public String retrieveAccessToken(String mscode, String recentPkce) {
    	try {
    		// This sends a POST request to get microsoft account's token
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://login.live.com/oauth20_token.srf"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                    "client_id=" + CLIENT_ID +
                    "&code=" + mscode +
                    "&scope=Xboxlive.signin+Xboxlive.offline_access" +
                    "&code_verifier=" + recentPkce +
                    "&redirect_uri=http://localhost:6921/microsoft/complete" +
                    "&grant_type=authorization_code"))
                .build();
            
            HttpResponse response = client.send(request);
            JsonObject jsonObject = new Gson().fromJson(response.body(), JsonObject.class);

            // Retreive ms account token, this will be useful to login to xbox
            String accessTokenToLive = jsonObject.get("access_token").getAsString();
            
            // This sends a POST request to xboxlive.com to retreive the user's xbox live token, this is used to get the xsts token
            client = HttpClient.newHttpClient();
            request = HttpRequest.newBuilder()
                .uri(new URI("https://user.auth.xboxlive.com/user/authenticate"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d=" + accessTokenToLive + "\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}"))
                .build();
            
            response = client.send(request);
            jsonObject = new Gson().fromJson(response.body(), JsonObject.class);

            // Store xbl auth token & user hash
            String xblAuthToken = jsonObject.get("Token").getAsString();
            String userHashString = jsonObject
                    .getAsJsonObject("DisplayClaims")
                    .getAsJsonArray("xui")
                    .get(0).getAsJsonObject()
                    .get("uhs").getAsString();

            // This sends a POST request xboxlive.com in order to retreive the xsts token
            client = HttpClient.newHttpClient();
            request = HttpRequest.newBuilder()
                .uri(new URI("https://xsts.auth.xboxlive.com/xsts/authorize"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\"" + xblAuthToken + "\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}"))
                .build();
            
            response = client.send(request);
            jsonObject = new Gson().fromJson(response.body(), JsonObject.class);

            // Store xsts token for the final step
            String xstsToken = jsonObject.get("Token").getAsString();

            // This sends a request to minecraftservices.com in order to get the final access token to minecraft, this access token lasts 24 hours
            client = HttpClient.newHttpClient();
            request = HttpRequest.newBuilder()
                .uri(new URI("https://api.minecraftservices.com/authentication/login_with_xbox"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    "{\"identityToken\":\"XBL3.0 x=" + userHashString + ";" + xstsToken + "\",\"ensureLegacyEnabled\":\"true\"}"))
                .build();
            
            response = client.send(request);

            // We got the access token!
            jsonObject = new Gson().fromJson(response.body(), JsonObject.class);
            return jsonObject.get("access_token").getAsString();
    	} catch (Exception e) {
    		return e.getMessage();
    	}
    }
    
    /**
    * Fetches account info. Needed for the new session instance. This returns the JSON data that got returned after the GET request.
    *
    * @param accessToken  Minecraft authentication token provided from the retrieveAccessToken() function
    */
    public static JsonObject getAccountInfo(String accessToken){
        // This creates the request body needed in order to fetch the account info
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.minecraftservices.com/minecraft/profile"))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();
        
        // We to send the request & if it throws an error, return null, if it does not then return the request response
        try {
            // Send the request
            HttpResponse response = client.send(request);

            // Read & parse the json data provided, this is where it could error
            
            return new Gson().fromJson(response.body(), JsonObject.class);
        } catch (Exception e) {
            return null;
        }
    }
}
