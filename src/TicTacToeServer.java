// Fig. 18.8: TicTacToeServer.java
// This class maintains a game of Tic-Tac-Toe for two client applets.
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.Arrays;
import javax.swing.*;

public class TicTacToeServer extends JFrame {
    private char[] board;
    private JTextArea outputArea;
    private Player[] players;
    private int port;
    private ServerSocket server;
    private int currentPlayer;
    private final int PLAYER_X = 0, PLAYER_O = 1;
    private final char X_MARK = 'X', O_MARK = 'O';

    boolean continueGame = false;
    private int counter = 0;
    private int firstPressed =0;

    boolean startNewGame = true;
    boolean gameFinished = false;

    // set up tic-tac-toe server and GUI that displays messages
    public TicTacToeServer(int port_)
    {
        super( "Tic-Tac-Toe Server" );
        port = port_;


        board = new char[ 9 ];
        players = new Player[ 2 ];
        currentPlayer = PLAYER_X;

        // set up ServerSocket
        try {
            server = new ServerSocket( port, 2 );
            System.out.println("waiting for connection on port "+ port);
        }

        // process problems creating ServerSocket
        catch( IOException ioException ) {
            ioException.printStackTrace();
            System.exit( 1 );
        }

        // set up JTextArea to display messages during execution
        outputArea = new JTextArea();
        getContentPane().add( outputArea, BorderLayout.CENTER );
        outputArea.setText( "Server awaiting connections\n" );

        setSize( 300, 300 );
        setVisible( true );

    } // end TicTacToeServer constructor

    // wait for two connections so game can be played
    public void execute()
    {
        while (true)
        {
            if (startNewGame)
            {
                // wait for each client to connect
                for (int i = 0; i < players.length; i++)
                {

                    // wait for connection, create Player, start thread
                    try
                    {
                        players[i] = new Player(server.accept(), i);
                        players[i].start();

                    }

                    // process problems receiving connection from client
                    catch (IOException ioException)
                    {
                        ioException.printStackTrace();
                        System.exit(1);
                    }
                }
                // Player X is suspended until Player O connects.
                // Resume player X now.
                synchronized (players[PLAYER_X])
                {
                    players[PLAYER_X].setSuspended(false);
                    players[PLAYER_X].notify();
                }
                startNewGame = false;


            } else
            {
                try
                {
                    Thread.sleep(1);
                } catch (java.lang.InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }  // end method execute
    }

    // utility method called from other threads to manipulate
    // outputArea in the event-dispatch thread
    private void displayMessage( final String messageToDisplay )
    {
        // display message from event-dispatch thread of execution
        SwingUtilities.invokeLater(
                new Runnable() {  // inner class to ensure GUI updates properly

                    public void run() // updates outputArea
                    {
                        outputArea.append( messageToDisplay );
                        outputArea.setCaretPosition(
                                outputArea.getText().length() );
                    }

                }  // end inner class

        ); // end call to SwingUtilities.invokeLater
    }

    // Determine if a move is valid. This method is synchronized because
    // only one move can be made at a time.
    public synchronized boolean validateAndMove( int location, int player )
    {
        boolean moveDone = false;

        // while not current player, must wait for turn
        while ( player != currentPlayer ) {

            // wait for turn
            try {
                wait();
            }
            // catch wait interruptions
            catch( InterruptedException interruptedException ) {
                interruptedException.printStackTrace();
            }
        }
        // if location not occupied, make move
        if ( !isOccupied( location ) )
        {
            //counter++;
            // set move in board array
            board[ location ] = currentPlayer == PLAYER_X ? X_MARK : O_MARK;
            //gameFinished = isGameOver();

            // change current player
            currentPlayer = ( currentPlayer + 1 ) % 2;

            // let new current player know that move occurred
            players[ currentPlayer ].otherPlayerMoved( location );

            notify(); // tell waiting player to continue

            // tell player that made move that the move was valid
            System.out.println(counter);
            if (counter == 9)
            {
                //isDraw = true;
                players[currentPlayer].sendDraw(-1);
                currentPlayer = (currentPlayer + 1) % 2;
                players[currentPlayer].sendDraw(location);
                counter =0;
                //isGameOver();
                //System.out.println("set a flagh for draw");

            }
            return true;
        }

        // tell player that made move that the move was not valid
        else
            return false;

    } // end method validateAndMove


    // determine whether location is occupied
    public boolean isOccupied( int location )
    {
        if ( board[ location ] == X_MARK || board [ location ] == O_MARK )
            return true;
        else
            return false;
    }

    // place code in this method to determine whether game over
    public boolean isGameOver()
    {

        for (int i = 0; i < 3; i++)
        {
            if ((board[i] == X_MARK) && (board[i+3] == X_MARK) && (board[i+(3*2)] == X_MARK)) {

                return true;
            }
            if ((board[i] == O_MARK) && (board[i+3] == O_MARK) && (board[i+(3*2)] == O_MARK)) {

                return true;
            }
        }
        for (int i = 0; i < 9; i+=3)
        {
            if ((board[i] == X_MARK) && (board[i + 1] == X_MARK) && (board[i + 2] == X_MARK)) {

                return true;
            }
            if  ((board[i] == O_MARK) && (board[i+1] == O_MARK) && (board[i+2] == O_MARK)) {

                return true;
            }
        }

        if ((board[0] == O_MARK) && (board[4] == O_MARK) && (board[8] == O_MARK))
        {

            return true;
        }
        else if ((board[2] == O_MARK) && (board[4] == O_MARK) && (board[6] == O_MARK))
        {

            return true;
        }
        if ((board[0] == X_MARK) && (board[4] == X_MARK) && (board[8] == X_MARK))
        {

            return true;
        }
        else if ((board[2] == X_MARK) && (board[4] == X_MARK) && (board[6] == X_MARK))
        {

            return true;
        }

        return false;

    }

    public static void main( String args[] )
    {

        int port = 54321;
        if (args.length >0)
        {
            port = Integer.parseInt(args[0]);
            System.out.println("port argument is : "+args[0]);

        }
        System.out.println("Using port : "+port);


        TicTacToeServer application = new TicTacToeServer(port);
        application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        application.execute();
    }

    // private inner class Player manages each Player as a thread
    private class Player extends Thread {
        private Socket connection;
        private DataInputStream input;
        private DataOutputStream output;
        private int playerNumber;
        private char mark;
        protected boolean suspended = true;

        // set up Player thread
        public Player( Socket socket, int number )
        {
            playerNumber = number;

            // specify player's mark
            mark = ( playerNumber == PLAYER_X ? X_MARK : O_MARK );

            connection = socket;

            // obtain streams from Socket
            try {
                input = new DataInputStream( connection.getInputStream() );
                output = new DataOutputStream( connection.getOutputStream() );
            }

            // process problems getting streams
            catch( IOException ioException ) {
                ioException.printStackTrace();
                System.exit( 1 );
            }

        } // end Player constructor

        // send message that other player moved
        public void otherPlayerMoved( int location )
        {
            // send message indicating move
            try {
                output.writeUTF( "Opponent moved" );
                output.writeInt( location );
            }

            // process problems sending message
            catch ( IOException ioException ) {
                ioException.printStackTrace();
            }
        }

        public void sendDraw(int location)
        {
            try {
                output.writeUTF( "Draw" );
                output.writeInt( location );
            }
            catch ( IOException ioException ) {
                ioException.printStackTrace();
            }
        }

        // control thread's execution
        public void run()
        {
            // send client message indicating its mark (X or O),
            // process messages from client
            try {
                displayMessage( "Player " + ( playerNumber ==
                        PLAYER_X ? X_MARK : O_MARK ) + " connected\n" );

                output.writeChar( mark ); // send player's mark

                // send message indicating connection
                output.writeUTF( "Player " + ( playerNumber == PLAYER_X ?
                        "X connected\n" : "O connected, please wait\n" ) );

                // if player X, wait for another player to arrive
                if ( mark == X_MARK ) {
                    output.writeUTF( "Waiting for another player" );

                    // wait for player O
                    try {
                        synchronized( this ) {
                            while ( suspended )
                                wait();
                        }
                    }

                    // process interruptions while waiting
                    catch ( InterruptedException exception ) {
                        exception.printStackTrace();
                    }

                    // send message that other player connected and
                    // player X can make a move
                    output.writeUTF( "Other player connected. Your move." );
                }
                while(true)
                {
                    // while game not over
                    while (!isGameOver())
                    {

                        if (input.available() > 0)
                        {
                            // get move location from client
                            //if input available
                            int location = input.readInt();

                            // check for valid move
                            if (validateAndMove(location, playerNumber))
                            {
                                displayMessage("\nlocation: " + location);
                                output.writeUTF("Valid move.");
                            } else
                                output.writeUTF("Invalid move, try again");
                        }
                    }

                    output.writeUTF("game over");

                    int reset = input.readInt();

                    synchronized (this)
                    {
                        if (reset == -1)
                        {
                            firstPressed++;
                            if (firstPressed == 1)
                            {
                                System.out.println("player 1 pressed button");
                                output.writeUTF("player1");
                                currentPlayer = playerNumber;
                                Arrays.fill(board, ' ');
                            } else
                            {
                                System.out.println("player 2 pressed button");
                                output.writeUTF("player2");
                                firstPressed = 0;
                                playerNumber = (currentPlayer + 1) % 2;

                            }
                        }
                    }
                }

               // connection.close(); // close connection to client
               // System.out.println("Connection closed");

            } // end try

            // process problems communicating with client
            catch( IOException ioException ) {
                ioException.printStackTrace();
                System.exit( 1 );
            }

        } // end method run

        // set whether or not thread is suspended
        public void setSuspended( boolean status )
        {
            suspended = status;
        }

    } // end class Player

} // end class TicTacToeServer