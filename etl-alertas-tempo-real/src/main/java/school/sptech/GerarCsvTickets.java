package school.sptech;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GerarCsvTickets {

    public static void gerarCsv(JSONArray tickets) {
        String nomeArquivo = "tickets_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) +
                ".csv";

        try (FileWriter csv = new FileWriter(nomeArquivo)) {

            csv.append("ID;Descricao\n");

            for (int i = 0; i < tickets.length(); i++) {
                JSONObject issue = tickets.getJSONObject(i);
                escreverLinha(issue, csv);
            }

            System.out.println("CSV gerado: " + nomeArquivo);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void escreverLinha(JSONObject issue, FileWriter csv) throws IOException {

        String key = issue.getString("key"); // ex: AAC-290

        String description = issue
                .getJSONObject("fields")
                .optString("description", "")
                .replace("\n", "\\n")
                .replace("\r", "");

        csv.append(key).append(";").append(description).append("\n");
    }
}
