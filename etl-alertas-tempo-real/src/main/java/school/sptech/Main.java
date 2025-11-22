package school.sptech;

import org.json.JSONArray;

public class Main {
    public static void main(String[] args) {

        JSONArray tickets = JiraService.buscarTodosTicketsAAC();
        System.out.println("Total de tickets recebidos: " + tickets.length());

        GerarCsvTickets.gerarCsv(tickets);

        System.out.println("Processo concluído!");
    }
}

