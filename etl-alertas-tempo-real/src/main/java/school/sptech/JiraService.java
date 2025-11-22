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
        String nextPageToken = null;

        try {
            while (true) {
                URL url = new URL("https://autoboticssptech.atlassian.net/rest/api/3/search/jql");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.setRequestProperty("Authorization", authHeader());
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");

                JSONObject body = new JSONObject();
                body.put("jql", "project = AAC"); // seu JQL
                body.put("fields", new JSONArray().put("key").put("description"));
                body.put("maxResults", 50); // ou outro valor

                if (nextPageToken != null) {
                    body.put("nextPageToken", nextPageToken);
                }

                String bodyStr = body.toString();
                con.getOutputStream().write(bodyStr.getBytes(StandardCharsets.UTF_8));

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
                while ((line = br.readLine()) != null) {
                    responseTxt.append(line);
                }

                JSONObject json = new JSONObject(responseTxt.toString());

                // Se não houver issues, para
                if (!json.has("issues")) break;

                JSONArray issues = json.getJSONArray("issues");
                for (int i = 0; i < issues.length(); i++) {
                    todos.put(issues.getJSONObject(i));
                }

                // Pegue o token para a próxima página
                if (json.has("nextPageToken")) {
                    nextPageToken = json.getString("nextPageToken");
                } else {
                    break;
                }

                // Se o próximo token vier vazio ou null, para
                if (nextPageToken == null || nextPageToken.isBlank()) {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return todos;
    }
}

