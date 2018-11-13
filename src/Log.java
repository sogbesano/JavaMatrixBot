import java.lang.reflect.Method;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Log implements Module {

    private MessageJson lastMessage;
    private SettingsJson settings;

    public Log(MessageJson lastMessage, SettingsJson settings) {
        this.lastMessage = lastMessage;
        this.settings = settings;
    }

    public Log() {

    }

    public static String lget(List<Object> params) throws SQLException {
        String date = "";
        for (Object param : params) {
            date += param;
        }
        String messages = String.format("unable to find a message dated %s", date);
        if (params.size() == 0) {
            String dbUrl = "jdbc:sqlite:log.db";
            Connection conn = null;
            conn = DriverManager.getConnection(dbUrl);
            String sql = "SELECT message, date, sender FROM messages ORDER BY RANDOM() LIMIT 1";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                messages = resultSet.getString("date") + " "
                        + " " + resultSet.getString("sender")
                        + " " + resultSet.getString("message") + "\n";
            }
        } else if (params.size() == 1) {
            if (date.matches("\\d{2}/\\d{2}/\\d{4}")) {//is a valid date string
                String dbUrl = "jdbc:sqlite:log.db";
                Connection conn = null;
                conn = DriverManager.getConnection(dbUrl);
                String sql = String.format("SELECT message, date, sender FROM messages WHERE date LIKE '%s'" +
                        " ORDER BY RANDOM() LIMIT 1", date);
                Statement statement = conn.createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                    messages = resultSet.getString("date") + " "
                            + " " + resultSet.getString("sender")
                            + " " + resultSet.getString("message") + "\n";
                }
            }
        }
        return messages;
    }

    @Override
    public List<Method> getAllMethods() {
        Class logClass = Log.class;
        List<Method> allMethods = Arrays.asList(logClass.getDeclaredMethods());
        return allMethods;
    }

    @Override
    public String getName() {
        return "log";
    }

    @Override
    public String noPrompt(String[] body) {
        String dbUrl = "jdbc:sqlite:log.db";
        String sql = "CREATE TABLE IF NOT EXISTS messages (\n"//create table if it doesn't exist
                + " message text PRIMARY KEY,\n"
                + " date text NOT NULL,\n"
                + " sender text NOT NULL\n"
                + ");";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl);
            Statement statement = conn.createStatement();
            statement.execute(sql);
            String message = "";
            for (String partOfMessage : body) {
                message += partOfMessage + " ";
            }
            message = message.substring(0, message.length() - 1);//remove end space
            //check if message already exists in messages table
            String query = "SELECT (count(*) > 0) as found FROM messages WHERE message LIKE ?";
            PreparedStatement pst = conn.prepareStatement(query);
            boolean foundMessage = false;
            if (lastMessage != null) {
                pst.setString(1, lastMessage.getBody());
                try (ResultSet rs = pst.executeQuery()) {
                    // Only expecting a single result
                    if (rs.next()) {
                        foundMessage = rs.getBoolean(1); // "found" column
                    }
                }
            }
            if (this.lastMessage != null && this.settings != null) {
                if (!this.lastMessage.getSender().equals("@" + this.settings.getUsername() + ":" + this.settings.getHomeServer())
                        && !this.lastMessage.getBody().startsWith(this.settings.getModulePrompt())
                        && !foundMessage) {
                    sql = "INSERT INTO messages(message,date,sender) VALUES(?, ?, ?)";//insert into table
                    PreparedStatement preparedStatement = conn.prepareStatement(sql);
                    preparedStatement.setString(1, message);
                    LocalDateTime ldt = LocalDateTime.now();
                    String formattedDate = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH).format(ldt);
                    preparedStatement.setString(2, formattedDate.replaceAll("-", "/"));
                    preparedStatement.setString(3, this.lastMessage.getSender());
                    if (!this.lastMessage.getSender().equals("@" + this.settings.getUsername() + ":" + this.settings.getHomeServer())) {
                        preparedStatement.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
}
