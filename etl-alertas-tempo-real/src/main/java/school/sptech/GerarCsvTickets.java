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

            csv.append("ID;Key;Descricao\n");

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

        String id = issue.optString("id", "");
        String key = issue.optString("key", "");

        // Trata a descrição corretamente (pode ser string ou ADF JSON)
        String description = extrairDescricao(issue);

        // Formatação e escape
        description = description
                .replace("\n", "\\n")
                .replace("\r", "")
                .replace(";", ","); // evita quebrar o CSV

        csv.append(id).append(";")
                .append(key).append(";")
                .append(description)
                .append("\n");
    }

    /**
     * Extrai a descrição, mesmo quando ela está no formato Atlassian Document Format (ADF)
     */
    private static String extrairDescricao(JSONObject issue) {

        Object descObj = issue.getJSONObject("fields").opt("description");

        if (descObj == null) {
            return "";
        }

        // Caso venha como string (raro mas possível)
        if (descObj instanceof String) {
            return (String) descObj;
        }

        // Caso venha como objeto ADF (o mais comum)
        if (descObj instanceof JSONObject) {
            try {
                JSONObject description = (JSONObject) descObj;

                // O texto normalmente está em:
                // description.content[0].content[i].text
                StringBuilder sb = new StringBuilder();

                JSONArray contentLvl1 = description.optJSONArray("content");
                if (contentLvl1 != null && contentLvl1.length() > 0) {

                    JSONArray contentLvl2 =
                            contentLvl1.getJSONObject(0).optJSONArray("content");

                    if (contentLvl2 != null) {
                        for (int i = 0; i < contentLvl2.length(); i++) {
                            JSONObject node = contentLvl2.getJSONObject(i);
                            if (node.has("text")) {
                                sb.append(node.getString("text")).append("\n");
                            }
                        }
                    }
                }

                String texto = sb.toString().trim();

                // Se não extraiu nenhum texto, pelo menos retorna o bruto
                return texto.isEmpty() ? description.toString() : texto;

            } catch (Exception e) {
                // Se der erro ao parsear, salva o JSON bruto
                return descObj.toString();
            }
        }

        // Caso seja algum formato diferente
        return descObj.toString();
    }
}
