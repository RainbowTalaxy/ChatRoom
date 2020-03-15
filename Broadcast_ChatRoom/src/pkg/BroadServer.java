package pkg;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service-Terminal
 * @version 1.00 2019-12-14
 * @author Talaxy
 */

public class BroadServer {

    public static final int PORT = 8192;
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final ConcurrentHashMap<String, Socket> Users = new ConcurrentHashMap<>();
    public static final ArrayBlockingQueue<String> Messages = new ArrayBlockingQueue<>(1000);


    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("Server started");
            Thread sender = new AutoSender();
            sender.start();

            while (true) {
                try {
                    Socket socket = server.accept();
                    Thread receiver = new Receiver(socket);
                    receiver.start();

                } catch (Exception e) {
                    System.out.println("Main Error!");
                }
            }

        } catch (Exception e) {
            System.out.println("Server failed");
        }

    }

}

class AutoSender extends Thread {
    @Override
    public void run() {
        while (true) {
            try {
                String message = BroadServer.Messages.take();
                for (String user: BroadServer.Users.keySet()) {
                    try {
                        String ip = user.split(" ", 2)[0];
                        Socket link = BroadServer.Users.get(user);
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(link.getOutputStream(), BroadServer.CHARSET), true);
                        out.println(message);
                    } catch (Exception e) {
                        System.out.println("Failed to send " + message + " to " + user);
                    }
                }

            } catch (Exception e) {}
        }
    }
}

class Receiver extends Thread {
    private Socket link;
    private String ip;
    private int id = 0;

    public Receiver(Socket socket) {
        link = socket;
        ip = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            while (BroadServer.Users.containsKey(ip + " " + id)) id++;
            BroadServer.Users.put(ip + " " + id, link);
            System.out.println(ip + " connected.");
            Scanner in = new Scanner(link.getInputStream(), BroadServer.CHARSET);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(link.getOutputStream(), BroadServer.CHARSET), true);
            out.println("------------- Talaxy's Broadcast Area - ver 1.0 -------------");
            out.println("[ Your IP is " + ip + " , enter 'quit' or 'exit' to exit. ]");
            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.trim().equals("quit") || line.trim().equals("exit")) break;
                BroadServer.Messages.put(ip + ": " + line);
            }
            BroadServer.Users.remove(ip + " " + id);
            System.out.println(ip + " disconnected.");

        } catch (Exception e) { }
    }
}