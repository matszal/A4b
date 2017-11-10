// Fig. 27.14: TicTacToeServerTest.java
// Class that tests Tic-Tac-Toe server.
import javax.swing.JFrame;

public class TicTacToeClientTest
{
        public static void main( String[] args )
        {
            String host = "somehost";
            int port = 50000;
            TicTacToeClient application;
            //accomodate code to run with args from terminal. args[0] = ip, args[1] = port
            //intellijj set to always pass two arguments
            if(args.length == 2)
            {
                host = args[0].toString();
                port = Integer.parseInt(args[1]);
                application = new TicTacToeClient(host, port);
                System.out.println("Arguments set to host: " +host+" port "+port);

            }
            else
            {
                System.out.println("No arguments given sending default values! Host:  "+host+" port "+port);
                application = new TicTacToeClient(host, port);
            }

            application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        } // end main
} // end class TicTacToeServerTest