import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Cioran implements Module {

    public static String cioran() throws IOException {
        URL url = new URL("https://en.wikiquote.org/wiki/Emil_Cioran");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder cioranHtmlStr = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            cioranHtmlStr.append(line);
        }

        br.close();
        Document doc = Jsoup.parse(cioranHtmlStr.toString());
        Elements liLInks = doc.getElementsByTag("li");
        List<Element> quoteLinks = new ArrayList<>();
        int i = 0;
        for(Element liLink : liLInks) {
            if(i > 14 && i < liLInks.size() - 77) {
                quoteLinks.add(liLink);
            }
            i++;
        }
        Random random = new Random();
        int choice = random.nextInt(quoteLinks.size());
        String cioranQuote = quoteLinks.get(choice).text();
        return cioranQuote;
    }

    @Override
    public List<Method> getAllMethods() {
        Class cioranClass = Cioran.class;
        List<Method> allMethods = Arrays.asList(cioranClass.getDeclaredMethods());
        return allMethods;
    }

    @Override
    public String getName() {
        return "cioran";
    }

    @Override
    public String noPrompt(String[] body) {
        return "";
    }
}
