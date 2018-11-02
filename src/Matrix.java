import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Matrix {
	
	public static String buildUrl(String protocol, String homeServer, String version, String endpoint, String accessToken, Map<String, String> params) {
		String url = protocol + "://" + homeServer + "/_matrix/client/" + version + "/" + endpoint;
		String paramsStr = Matrix.makeParams(params);
		if(!paramsStr.isEmpty()) {
			url += "?" + paramsStr;
		} 
		if(!accessToken.isEmpty()) {
			url += "?" + "access_token=" + accessToken;
		}
		return url;
	}
	
	public static String makeParams(Map<String, String> params) {
		if(params.isEmpty()) {
			return "";
		}
		String paramsStr = "";
		for (Map.Entry<String,String> param : params.entrySet()) {
		    String key = param.getKey();
		    String value = param.getValue();
		    paramsStr += key + "=" + value + "&";
		}
		return paramsStr.substring(0, paramsStr.length() - 1); //strip end &
	}
	
	public static String GET(String protocol, String homeServer, String version, String endPoint, String accessToken, Map<String, String> params) throws IOException {
		String urlStr = Matrix.buildUrl(protocol, homeServer, version, endPoint, accessToken, params);
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		StringBuilder result = new StringBuilder();
		while((line = br.readLine()) != null) {
			result.append(line);
		}
		br.close();
		return result.toString();
	}

	public static String POSTJSON(String protocol, String homeServer, String version, String endPoint, String accessToken, Map<String, String> params, Object json) throws IOException {
		String postUrl = Matrix.buildUrl(protocol, homeServer, version, endPoint, accessToken, params);
		Gson gson = new Gson();
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(postUrl);
		StringEntity postingString = new StringEntity(gson.toJson(json));
		post.setEntity(postingString);
		post.setHeader("Content-type", "application/json");
		HttpResponse response = httpClient.execute(post);
		InputStream contentIS = response.getEntity().getContent();
		BufferedReader br = new BufferedReader(new InputStreamReader(contentIS));
		StringBuilder sb = new StringBuilder();
		String line = "";
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		contentIS.close();
		String jsonResponse = sb.toString();
		return jsonResponse;
	}

	public static String POST(String protocol, String homeServer, String version, String endPoint, String accessToken, Map<String, String> params) throws IOException {
		String postUrl = Matrix.buildUrl(protocol, homeServer, version, endPoint, accessToken, params);
		System.out.println("POST URL: " + postUrl);
		Gson gson = new Gson();
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(postUrl);
		HttpResponse response = httpClient.execute(post);
		InputStream contentIS = response.getEntity().getContent();
		BufferedReader br = new BufferedReader(new InputStreamReader(contentIS));
		StringBuilder sb = new StringBuilder();
		String line = "";
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		contentIS.close();
		String jsonResponse = sb.toString();
		return jsonResponse;
	}
	
	public static LoginJsonResponse login(String protocol, String homeServer, String version, String username, String password) throws IOException {
		LoginJson loginJson = new LoginJson("m.login.password", username, password);
		Map<String, String> params = new HashMap<>();
		String loginJsonResponseStr = Matrix.POSTJSON(protocol, homeServer, version, "login", "", params, loginJson);
		Gson gson = new Gson();
		LoginJsonResponse loginJsonResponse = gson.fromJson(loginJsonResponseStr, LoginJsonResponse.class);
		return loginJsonResponse;
	}

	public static String getLoginFlows(String protocol, String homeServer, String version) throws IOException {
		Map<String,String> params = new HashMap<>();
		String loginFlowsJsonStr = Matrix.GET(protocol, homeServer, version, "login", "", params);
	    return loginFlowsJsonStr;
	}

	public static String publicRooms(String protocol, String homeServer, String version) throws IOException {
		Map<String, String> params = new HashMap<>();
		String publicRoomsJson = Matrix.GET(protocol, homeServer, version, "publicRooms", "", params);
	    return publicRoomsJson;
	}

	public static List<Map<String, String>> getCanonicalAliasesAndRoomsIDs(ArrayList publicRoomsJsonList) {
		List<Map<String, String>> canonicalAliasAndRoomIDList = new ArrayList<>();
		Gson gson = new Gson();
		for(int i = 0; i < publicRoomsJsonList.size(); i++) {
			Map<String, String> canonicalAliasAndRoomID = new HashMap<>();
			PublicRoomsJson publicRoomsJson = gson.fromJson(gson.toJson(publicRoomsJsonList.get(i)), PublicRoomsJson.class);
			canonicalAliasAndRoomID.put("canonical alias", publicRoomsJson.getCanonicalAlias());
			canonicalAliasAndRoomID.put("room id", publicRoomsJson.getRoomID());
			canonicalAliasAndRoomIDList.add(canonicalAliasAndRoomID);
		}
  		return canonicalAliasAndRoomIDList;
	}

	public static String getRoomID(String canonicalAlias, List<Map<String, String>> canonicalAliasesAndRoomIDs) {
		String roomID = "";
		for(Map<String, String> canonicalAliasAndRoomID :  canonicalAliasesAndRoomIDs) {
			if(canonicalAlias.equals(canonicalAliasAndRoomID.get("canonical alias"))) {
				roomID = canonicalAliasAndRoomID.get("room id");
			}
		}
		return roomID;
	}

	public static void joinByID(String protocol, String homeServer, String version, String roomID, String accessToken) throws IOException {
		Map<String, String> params = new HashMap<>();
	    Matrix.POST(protocol, homeServer, version, "rooms/" + roomID + "/join", accessToken, params);
	}

	public static void sendMessage(String protocol, String homeServer, String version, String roomID, String accessToken, String messageBody) throws IOException {
		SendMessageJson sendMessageJson = new SendMessageJson("m.text", messageBody);
		Map<String, String> params = new HashMap<>();
		Matrix.POSTJSON(protocol, homeServer, version, "rooms/" + roomID + "/send/m.room.message", accessToken, params, sendMessageJson);
	}

	public static String sync(String protocol, String homeServer, String version, String accessToken) throws IOException {
		Map<String, String> params = new HashMap<>();
		String syncJsonStr = Matrix.GET(protocol, homeServer, version, "sync", accessToken, params);
	    return syncJsonStr;
	}

	public static MessageJson extractLastMessage(String protocol, String homeServer, String accessToken, String roomID) throws IOException {
		String syncStr = Matrix.sync(protocol, homeServer, "r0", accessToken);
		Gson gson = new Gson();
		JsonObject jsonObject = gson.fromJson(syncStr, JsonObject.class);
		JsonArray messagesList = jsonObject.get("rooms").getAsJsonObject().get("join").getAsJsonObject().get(roomID).getAsJsonObject().get("timeline").getAsJsonObject().get("events").getAsJsonArray();
		JsonElement lastMessage = messagesList.get(messagesList.size() - 1);
		MessageJson lastMessageJson = gson.fromJson(lastMessage.toString(), MessageJson.class);
		return lastMessageJson;
	}

	public static String markRead(String protocol, String homeServer, String version, String roomID, String eventID, String accessToken) throws IOException {
		MarkReadJson markReadJson = new MarkReadJson(eventID, eventID);
		Map<String, String> params = new HashMap<>();
		String markReadJsonResponseStr = Matrix.POSTJSON(protocol, homeServer, version, "rooms/" + roomID + "/read_markers", accessToken, params, markReadJson);
		return markReadJsonResponseStr;
	}
}
