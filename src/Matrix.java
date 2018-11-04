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

class Matrix {
	private String protocol;
	private String homeServer;
	private String username;
	private String password;

	// If more than one version must be supported, duplicate the appropriate
    // methods, i.e. buildUrl, and pass a version parameter to them too.
	private String defaultVersion = "unstable";

	private String accessToken;
	private String roomName;
	private String userID;

	public Matrix(SettingsJson settings) {
		this.protocol = settings.getProtocol();
		this.homeServer = settings.getHomeServer();
		this.username = settings.getUsername();
		this.password = settings.getPassword();
		this.roomName = settings.getRoomName();
		this.accessToken = "";
		this.userID = "";
	}

	private String buildUrl(String endpoint, Map<String, String> params) {
	    String url = this.protocol + "://" + this.homeServer + "/_matrix/client/" +
                     this.defaultVersion + "/" + endpoint;
	    char concat = '?';

        String paramsStr = this.makeParams(params);
	    if (!paramsStr.isEmpty()) {
	        url += concat + paramsStr;
	        concat = '&';
        }
        if (!this.accessToken.isEmpty()) {
            url += concat + "access_token=" + this.accessToken;
        }

        return url;
    }

    private String makeParams(Map<String, String> params) {
	    if (params == null || params.isEmpty()) {
	        return "";
        }
        String paramsStr = "";
	    for (Map.Entry<String, String> param : params.entrySet()) {
	        paramsStr += param.getKey() + '=' + param.getValue() + '&';
        }
        return paramsStr.substring(0, paramsStr.length() - 1); // strip end &
    }

    private String GET(String endpoint, Map<String, String> params) throws IOException {
	    URL url = new URL(this.buildUrl(endpoint, params));
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("GET");

	    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    StringBuilder result = new StringBuilder();

	    String line;
	    while ((line = br.readLine()) != null) {
	        result.append(line);
        }

        br.close();
	    return result.toString();
    }

    private String POST(String endpoint, Map<String, String> params, Object json) throws IOException {
	    // This version posts with data
        String url = this.buildUrl(endpoint, params);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        if (json != null) {
            Gson gson = new Gson();
            StringEntity postingString = new StringEntity(gson.toJson(json));
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/json");
        }

        HttpResponse response = httpClient.execute(post);
        InputStream content = response.getEntity().getContent();
        BufferedReader br = new BufferedReader(new InputStreamReader(content));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        content.close();
        return sb.toString();
    }

    private String POST(String endpoint, Map<String, String> params) throws IOException {
	    // This version posts without data
        return this.POST(endpoint, params, null);
    }

    public LoginJsonResponse login() throws IOException {
	    LoginJson json = new LoginJson("m.login.password", this.username, this.password);
	    String loginJsonResponseStr = this.POST("login", null, json);
	    Gson gson = new Gson();
	    LoginJsonResponse response = gson.fromJson(loginJsonResponseStr, LoginJsonResponse.class);
	    this.accessToken = response.getAccessToken();
	    this.userID = response.getUserID();
	    return response;
    }

    public String getLoginFlows() throws IOException {
	    return this.GET("login", null);
    }

    public String publicRooms() throws IOException {
	    return this.GET("publicRooms", null);
    }

    public List<Map<String, String>> getCanonicalAliasesAndRoomsIDs(ArrayList publicRoomsJsonList) {
	    List<Map<String, String>> canonicalAliasAndRoomIDList = new ArrayList<>();
	    Gson gson = new Gson();
	    for (int i = 0; i < publicRoomsJsonList.size(); i++) {
            Map<String, String> canonicalAliasAndRoomID = new HashMap<>();
            PublicRoomsJson publicRoomsJson = gson.fromJson(gson.toJson(publicRoomsJsonList.get(i)),
                                                            PublicRoomsJson.class);
            canonicalAliasAndRoomID.put("canonical alias", publicRoomsJson.getCanonicalAlias());
            canonicalAliasAndRoomID.put("room id", publicRoomsJson.getRoomID());
            canonicalAliasAndRoomIDList.add(canonicalAliasAndRoomID);
        }
        return canonicalAliasAndRoomIDList;
    }

    public String getRoomID(List<Map<String, String>> canonicalAliasesAndRoomIDs) {
	    String roomID = "";
	    for (Map<String, String> canonicalAliasAndRoomID : canonicalAliasesAndRoomIDs) {
	        if (this.roomName.equals(canonicalAliasAndRoomID.get("canonical alias"))) {
	            roomID = canonicalAliasAndRoomID.get("room id");
            }
        }
        return roomID;
    }

    public void joinByID(String roomID) throws IOException {
	    this.POST("rooms/" + roomID + "/join", null);
    }

    public void sendMessage(String roomID, String messageBody) throws IOException {
	    SendMessageJson data = new SendMessageJson("m.text", messageBody);
	    this.POST("rooms/" + roomID + "/send/m.room.message", null, data);
    }

    public String sync() throws IOException {
	    return this.GET("sync", null);
    }

    public MessageJson extractLastMessage(String roomID) throws IOException {
	    String syncStr = this.sync();
	    Gson gson = new Gson();
	    JsonObject json = gson.fromJson(syncStr, JsonObject.class);
	    JsonArray messagesList = json.get("rooms").getAsJsonObject()
                                     .get("join").getAsJsonObject()
                                     .get(roomID).getAsJsonObject()
                                     .get("timeline").getAsJsonObject()
                                     .get("events").getAsJsonArray();

	    JsonElement lastMessage = messagesList.get(messagesList.size() - 1);
	    return gson.fromJson(lastMessage.toString(), MessageJson.class);
    }

    public String markRead(String roomID, String eventID) throws IOException {
	    MarkReadJson data = new MarkReadJson(eventID, eventID);
	    return this.POST("rooms/" + roomID + "/read_markers", null, data);
    }
}