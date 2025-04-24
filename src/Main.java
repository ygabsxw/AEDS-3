import java.util.Scanner;

import model.MovieMenu;


public class Main {

public static void main(String[] args) {

    Scanner console;

    try {
        console = new Scanner(System.in);
        int opcao;
        do {
            System.out.println("\n\nAEDsIII");
            System.out.println("-------");
            System.out.println("> Início");
            System.out.println("\n1 - Arquivo Sequencial");
            System.out.println("2 - Arquivo Indexado - Hash");
            System.out.println("3 - Arquivo Indexado - Arvore B");
            System.out.println("4 - Arquivo Indexado - Lista Invertida");
            System.out.println("0 - Sair");

            System.out.print("\nOpção: ");
            try {
                opcao = Integer.valueOf(console.nextLine());
            } catch(NumberFormatException e) {
                opcao = -1;
            }

            String fileType = "";
            switch(opcao) {
                case 1:
                    fileType = "sequential";
                    break;
                case 2:
                    fileType = "hash";
                    break;
                case 3:
                    fileType = "btree";
                    break;
                case 4:
                    fileType = "inverted";
                    break;
                default:
                    System.out.println("Invalid option!");
                    return;
            }
            MovieMenu menu = new MovieMenu(fileType);
            menu.menu();

        } while (opcao != 0);



    } catch(Exception e) {
        e.printStackTrace();
    }

}

}