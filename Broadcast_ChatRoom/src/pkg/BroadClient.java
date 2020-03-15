package pkg;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Client Terminal
 * @version 1.00 2019-12-14
 * @author Talaxy
 */

public class BroadClient {

    public static final int PORT = 8192;
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static boolean unDone = true;

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.print("Please enter the server IP: ");
            String server = sc.nextLine();
            Socket link = new Socket(server, PORT);
            System.out.println("Server connected!");
            Thread viewer = new Viewer(link);
            Thread sender = new Sender(link);
            viewer.start();
            sender.start();

        } catch (Exception e) {
            System.out.println("Cannot connect to the Server!");
        }
    }
}

class Viewer extends Thread {
    private Socket link;

    public Viewer(Socket socket) {
        link = socket;
    }

    @Override
    public void run() {
        try {
            Scanner in = new Scanner(link.getInputStream(), BroadClient.CHARSET);
            while (BroadClient.unDone && in.hasNextLine()) {
                System.out.println(in.nextLine());
            }

        } catch (Exception e) {}
    }
}

class Sender extends Thread {
    private Socket link;

    public Sender(Socket socket) {
        link = socket;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(link.getOutputStream(), BroadClient.CHARSET), true);
            Scanner in = new Scanner(System.in, BroadClient.CHARSET);
            while (BroadClient.unDone && in.hasNextLine()) {
                String line = in.nextLine();
                if (line.trim().equals("quit") || line.trim().equals("exit")) {
                    BroadClient.unDone = false;
                }
                out.println(line);
            }
            sleep(1000);

        } catch (Exception e) {}
        System.exit(0);

    }
}