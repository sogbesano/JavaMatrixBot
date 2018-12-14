import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

public class MarkovTalk implements Module {

    private MessageJson lastMessage;
    private SettingsJson settings;

    public MarkovTalk() {

    }

    public MarkovTalk(MessageJson lastMessage, SettingsJson settings) {
        this.lastMessage = lastMessage;
        this.settings = settings;
    }

    private static List<String> removeWhiteSpace(List<String> words) {
        List<String> wordsWithoutWhiteSpace = new ArrayList<>();
        for (String word : words) {
            if (!word.equals(" ")) {
                wordsWithoutWhiteSpace.add(word);
            }
        }
        return wordsWithoutWhiteSpace;
    }

    private static Map<String, List<String>> buildDictionary(String inputText) {
        List<String> inputTextSplit = Arrays.asList(inputText.split("((?<=[\\s\\.]+)|(?=[\\s\\.]+))"));
        inputTextSplit = MarkovTalk.removeWhiteSpace(inputTextSplit);
        Map<String, List<String>> dictionary = new HashMap<>();
        List<String> wordSuffixes;
        for (int i = 0; i < inputTextSplit.size(); i++) {
            wordSuffixes = new ArrayList<>();
            for (int j = 0; j < inputTextSplit.size(); j++) {
                if (i < inputTextSplit.size() - 1 && j < inputTextSplit.size() - 1) {
                    if (inputTextSplit.get(i).equalsIgnoreCase(inputTextSplit.get(j))) {
                        wordSuffixes.add(inputTextSplit.get(j + 1));
                    }
                }
            }
            dictionary.put(inputTextSplit.get(i), wordSuffixes);
        }
        return dictionary;
    }

    private static String getNextWord(Map<String, List<String>> wordsDictionary, String previousWord) {
        if(previousWord.equals("\n") || previousWord.equals(".") || previousWord.isEmpty()) {
            return "";
        }
        List<String> wordSuffixes = wordsDictionary.get(previousWord);
        if(wordSuffixes.size() != 0) {
            Random rand = new Random();
            String nextWord = wordSuffixes.get(rand.nextInt(wordSuffixes.size()));
            if (nextWord == null) {
                return "";
            }
            return nextWord;
        }
        return "";
    }

    private static Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:markov.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static String talk() {
        //String text = this.lastMessage.getBody();
        String sql = "SELECT message FROM messages";
        String text = "";
        try(Connection conn = MarkovTalk.connect();
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sql)) {
            while(rs.next()) {
                text += rs.getString("message") + "\n";
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        Map<String, List<String>> wordsDictionary = MarkovTalk.buildDictionary(text);
        Random rand = new Random();
        boolean gettingStartWord = true;
        String startWord = "";
        while (gettingStartWord) {
            List<String> wordSuffixes = (List<String>) wordsDictionary.values().toArray()[rand.nextInt(wordsDictionary.values().toArray().length)];
            if (wordSuffixes.size() > 0) {
                startWord = wordSuffixes.get(rand.nextInt(wordSuffixes.size()));
            }
            if (!startWord.equals("") || !startWord.equals(".")) {
                gettingStartWord = false;
            }
        }
        boolean generatingText = true;
        String talkText = startWord;
        String nextWord = MarkovTalk.getNextWord(wordsDictionary, startWord);
        while (generatingText) {
            nextWord = MarkovTalk.getNextWord(wordsDictionary, nextWord);
            if(!nextWord.isEmpty() || !nextWord.equals("\n")) {
                talkText += " " + nextWord;
            }
            if (nextWord.isEmpty() || nextWord.equals(".") || nextWord.equals("\n")) {
                generatingText = false;
            }
        }
        return talkText.trim();
    }

    @Override
    public List<Method> getAllMethods() {
        Class markovTalkCla = MarkovTalk.class;
        List<Method> allMethods = Arrays.asList(markovTalkCla.getDeclaredMethods());
        return allMethods;
    }

    @Override
    public String getName() {
        return "markov talk";
    }

    @Override
    public String noPrompt(String[] body) {
        String dbUrl = "jdbc:sqlite:markov.db";
        String sql = "CREATE TABLE IF NOT EXISTS messages (\n"//create table if it doesn't exist
                + " message text PRIMARY KEY\n"
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

            boolean isImage = false;
            for (String imageFileFormat : Log.imageFileFormats) {
                if (this.lastMessage != null) {
                    if (this.lastMessage.getBody().endsWith(imageFileFormat)) {
                        isImage = true;
                    }
                }
            }

            if (this.lastMessage != null && this.settings != null) {
                if (!this.lastMessage.getSender().equals("@" + this.settings.getUsername() + ":" + this.settings.getHomeServer())
                        && !this.lastMessage.getBody().startsWith(this.settings.getModulePrompt())
                        && !isImage) {
                    //insert into table or ignore error message if record already exists
                    sql = "INSERT OR IGNORE INTO messages(message) VALUES(?)";
                    PreparedStatement preparedStatement = conn.prepareStatement(sql);
                    preparedStatement.setString(1, message);
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}
