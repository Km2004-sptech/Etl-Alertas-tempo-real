package school.sptech;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

public class JiraService {

    private static final String EMAIL = "autobotics.sptech@gmail.com";
    private static final String API_TOKEN = "";
    private static final String JIRA_SEARCH_URL =
            "https://autoboticssptech.atlassian.net/rest/api/3/search/jql";

    private static String authHeader() {
        String auth = EMAIL + ":" + API_TOKEN;
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    /**
     * Busca todos os tickets do projeto AAC, paginando até acabar.
     */
    public static JSONArray buscarTodosTicketsAAC() {
        JSONArray todos = new JSONArray();
        int startAt = 0;

        try {
            while (true) {

                String endpoint = JIRA_SEARCH_URL
                        + "?jql=project=AAC"
                        + "&fields=key,description"
                        + "&maxResults=50"
                        + "&startAt=" + startAt;

                URL url = new URL(endpoint);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", authHeader());
                con.setRequestProperty("Accept", "application/json");

                int status = con.getResponseCode();
                System.out.println("STATUS HTTP → " + status);

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                (status >= 200 && status < 300)
                                        ? con.getInputStream()
                                        : con.getErrorStream(),
                                StandardCharsets.UTF_8
                        )
                );

                StringBuilder responseTxt = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) responseTxt.append(line);

                JSONObject json = new JSONObject(responseTxt.toString());

                if (!json.has("issues")) break;

                JSONArray issues = json.getJSONArray("issues");

                System.out.println("JSON RETORNADO:");
                System.out.println(json.toString());

                // adiciona ao array final
                for (int i = 0; i < issues.length(); i++) {
                    todos.put(issues.getJSONObject(i));
                }

                boolean isLast = json.optBoolean("isLast", true);
                if (isLast || issues.length() == 0) break;

                startAt += issues.length();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return todos;
    }
}

