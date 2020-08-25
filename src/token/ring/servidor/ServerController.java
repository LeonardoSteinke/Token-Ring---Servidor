package token.ring.servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Leonardo Steinke
 */
public class ServerController {

    private String TOKEN = "BB8";
    private ServerSocket serverLogin;
    private ServerSocket serverSend;
    private Socket socket;
    private List<Socket> process;
    private String content = null;

    int PORT1;
    int PORT2;
    aguardaConeccao aguarda;

    private Server server;

    ServerController(int PORT1, int PORT2, Server server) throws IOException {
        process = new ArrayList<>();
        this.PORT1 = PORT1;
        this.PORT2 = PORT2;
        this.server = server;
        serverLogin = new ServerSocket(PORT1);
        serverSend = new ServerSocket(PORT2);
    }

    public void conections() {
        aguarda = new aguardaConeccao();
        aguarda.start();
    }

    public void iniciarTokenRing() {
        circulaToken circula = new circulaToken();
        circula.start();

    }

    private class aguardaConeccao extends Thread {

        @Override
        public void run() {
            while (true) {
                System.out.println("Aguardando conexão...");
                try (Socket conn = serverLogin.accept();) {
                    System.out.println("Conectado com: " + conn.getInetAddress().getHostName());

                    process.add(conn);
                    ObjectOutputStream saida = new ObjectOutputStream(conn.getOutputStream());
                    saida.writeInt(process.size());
                    saida.writeInt(PORT2 + process.size());
                    saida.flush();
                    saida.close();

                    conn.close();
                } catch (Exception e) {

                }
            }
        }
    }

    private class circulaToken extends Thread {

        @Override
        public void run() {
            System.out.println("Token Ring Iniciado");
            while (true) {
                try {
                    socket = new Socket("localhost", (PORT2 + 1));
                    ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
                    saida.writeUTF(TOKEN);
                    saida.flush();
                    saida.close();
                    socket.close();
                } catch (Exception ex) {
                }

                System.out.println("Aguardando conexão...");
                try (Socket conn = serverSend.accept();) {
                    System.out.println("Conectado com: " + conn.getInetAddress().getHostName());
                    ObjectInputStream entrada = new ObjectInputStream(conn.getInputStream());
                    TOKEN = entrada.readUTF();
                    System.out.println("TOKEN: " + TOKEN);
                    try {
                        content = entrada.readUTF();
                        System.out.println("Conteúdo: " + content);

                    } catch (Exception e) {
                        System.out.println("Não tem Conteúdo");
                    }
                    entrada.close();
                    conn.close();
                } catch (Exception e) {
                }
            }
        }

    }

}
