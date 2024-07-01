import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done = false;
    private ExecutorService pool;


    @Override
    public void run(){
        try{
            server = new ServerSocket(8000);
            pool = Executors.newCachedThreadPool();
            connections = new ArrayList<>();
            while(!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                System.out.println("CHECK: "+ connections);
                pool.execute(handler);
            }
        }catch(Exception ex) {
            shutdown();
        }
    }

    public void broadcast(String msg){
        for(ConnectionHandler ch: connections){
            ch.sendMessage(msg);
        }
    }

    public void shutdown(){
        try{
            done = true;
            if(!server.isClosed()){
                server.close();
            }
            for(ConnectionHandler ch: connections){
                ch.shutdown();
            }
        }catch(IOException ex){
            // PASS
        }
    }

    class ConnectionHandler implements Runnable{

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public ConnectionHandler(Socket client){
            this.client = client;
        }

        @Override
        public void run(){
            try{
//                out.println("\n_______Wecome To Server ______\n");
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                nickname = in.readLine();
                System.out.println(nickname + " has connected");
                broadcast(nickname + ", joined the SERVER.");
                String msg;
                while((msg = in.readLine()) != null){
                    if(msg.startsWith("/nick")){
                        String[] messaeSplit = msg.split(" ");
                        if(messaeSplit.length == 2){
                            broadcast("BROADCAST: "  + nickname + " Changed their name to -> " + messaeSplit[1]);
                            System.out.println("TERMINAL: " + nickname + " Changed their name to -> " + messaeSplit[1]);
                            nickname =  messaeSplit[1];
                            out.println("Successully changed name to :" + messaeSplit[1]);
                        }else{
                            System.out.println("NO nickname provided");
                        }
                    }else if(msg.startsWith("/quit")){
                        // TODO
                        broadcast(nickname + ": Left the server.");
                        shutdown();
                    }else{
                        broadcast(nickname + ": " + msg);
                    }
                }
            }catch(IOException ex){
                // TODO: handle
                shutdown();
            }
        }

        public void sendMessage(String msg){
            out.println(msg);
        }

        public  void shutdown(){
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            }catch(IOException ex){
                //PASS
            }
        }

    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

}
