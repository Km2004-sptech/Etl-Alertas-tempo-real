package school.sptech;

import org.json.JSONArray;

public class Main {
    public static void main(String[] args) {

        JSONArray tickets = JiraService.buscarTodosTicketsAAC();
        GerarCsvTickets.gerarCsv(tickets);

        System.out.println("Processo concluído!");
    }
}

