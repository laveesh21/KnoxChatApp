import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done = false;

    @Override
    public void run(){

        try{
            client = new Socket("127.0.0.1", 8000);
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            System.out.println("CLIENT CHECK: " + client);

            InputHandler inHandler = new InputHandler();
            Thread t = new Thread(inHandler);
            t.start();

            String inMessage;
            while((inMessage = in.readLine())!=null){
                System.out.println(inMessage);
            }

        }catch(IOException ex){
            System.err.println("Error connecting to server: " + ex.getMessage());
        } finally {
            shutdown();
        }

    }

    public void shutdown(){
        done = true;
        try{
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }
        }catch (IOException ex){
            System.err.println("Error shutting down: " + ex.getMessage());
        }
    }


    class InputHandler implements Runnable{

        @Override
        public void run(){
            try{
                System.out.println("\n_______Wecome To Server ______\n");
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Enter Nickname: ");
                String nickname = inReader.readLine();
                out.println(nickname);
                while(!done){
                    String message = inReader.readLine();
                    if(message.equals("/quit")){
                        inReader.close();
                        shutdown();
                    }else{
                        out.println(message);
                    }
                }
            }catch (IOException ex){
                // TODO: handle
                shutdown();
            }
        }

    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

}
