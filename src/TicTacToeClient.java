/*

CLIENT

 */
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;

public class TicTacToeClient extends JFrame implements Runnable {

    private JButton restartButton;
    private JTextField idField;
    private JTextArea displayArea;
    private JPanel boardPanel, panel2;
    private Square board[][], currentSquare;
    private Socket connection;
    private DataInputStream input;
    private DataOutputStream output;
    private String ticTacToeHost;
    private int ticTacToePort;
    private char myMark;
    private boolean myTurn;
    private final char X_MARK = 'X', O_MARK = 'O';
    private boolean continueGame = false;

    // Set up user-interface and board
    public TicTacToeClient(String host, int port)
    {
        ticTacToeHost = host;
        ticTacToePort = port;

        //Container container = getContentPane();

        // set up JTextArea to display messages to user
        displayArea = new JTextArea( 4, 30 );
        displayArea.setEditable( false );
        add( new JScrollPane( displayArea ), BorderLayout.SOUTH );

        // set up panel for squares in board
        boardPanel = new JPanel();
        boardPanel.setLayout( new GridLayout( 3, 3, 0, 0 ) );

        // create board
        board = new Square[ 3 ][ 3 ];

        // When creating a Square, the location argument to the constructor
        // is a value from 0 to 8 indicating the position of the Square on
        // the board. Values 0, 1, and 2 are the first row, values 3, 4,
        // and 5 are the second row. Values 6, 7, and 8 are the third row.
        for ( int row = 0; row < board.length; row++ ) {

            for ( int column = 0; column < board[ row ].length; column++ ) {

                // create Square
                board[ row ][ column ] = new Square( ' ', row * 3 + column );
                boardPanel.add( board[ row ][ column ] );
            }
        }

        // textfield to display player's mark
        idField = new JTextField();
        idField.setEditable( false );
        add( idField, BorderLayout.NORTH );

        restartButton = new JButton("restart Game");
        restartButton.setVisible(false);
        add(restartButton, BorderLayout.EAST);
        restartButton.addActionListener(new reset());


        // set up panel to contain boardPanel (for layout purposes)
        panel2 = new JPanel();
        panel2.add( boardPanel, BorderLayout.CENTER );
        add( panel2, BorderLayout.CENTER );
        setSize(300, 255);
        setVisible(true);

        startClient();

    } // end method TicTacToe Constructor

    // Make connection to server and get associated streams.
    // Start separate thread to allow this applet to
    // continually update its output in textarea display.

    private void resetBoard() {

        for ( int row = 0; row < board.length; row++ ) {

            for ( int column = 0; column < board[ row ].length; column++ ) {

                setMark(  board[ row ][ column ],
                        (' ') );

            }
        }
        restartButton.setVisible(false);
    }
    public  void startClient()
    {
        // connect to server, get streams and start outputThread
        try {
            // make connection
            connection = new Socket( ticTacToeHost, ticTacToePort );

            // get streams
            input = new DataInputStream( connection.getInputStream() );
            output = new DataOutputStream( connection.getOutputStream() );
            restartButton.setVisible(false);
        }
        // catch problems setting up connection and streams
        catch ( IOException ioException ) {
            ioException.printStackTrace();
        }
        // create and start output thread
        ExecutorService worker = Executors.newFixedThreadPool( 1 );
        worker.execute( this ); // execute client

    } // end method start

    // control thread that allows continuous update of displayArea
    public void run()
    {
        // get player's mark (X or O)
        try {
            myMark = input.readChar();

            // display player ID in event-dispatch thread
            SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run()
                        {
                            idField.setText( "You are player \"" + myMark + "\"" );
                        }
                    }
            );

            myTurn = ( myMark == X_MARK ? true : false );

            // receive messages sent to client and output them
            while ( true ) {
                processMessage( input.readUTF() );
            }

        } // end try

        // process problems communicating with server
        catch ( IOException ioException ) {
            ioException.printStackTrace();
        }

    }  // end method run

    // process messages received by client
    private void processMessage( String message )
    {
        if(message.equals("player1"))
        {
            myTurn = true;
            myMark = X_MARK;
        }
        else if(message.equals("player2"))
        {
            myTurn = true;
            myMark = O_MARK;
        }
        else  if(message.equals("game over"))
        {
            SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run()
                        {
                            restartButton.setVisible(true);
                        }
                    }
            );

            if(myTurn == false)
            {
                displayMessage( myMark + " won!\n" );
            }
            else
            {
                displayMessage( myMark + " lost!\n" );
            }
        }
        // valid move occurred
        else   if ( message.equals( "Valid move." ) ) {
            displayMessage( "Valid move, please wait.\n" );
            setMark( currentSquare, myMark );
        }

        // invalid move occurred
        else if ( message.equals( "Invalid move, try again" ) ) {
            displayMessage( message + "\n" );
            myTurn = true;
        }

        // opponent moved
        else if ( message.equals( "Opponent moved" ) ) {

            // get move location and update board
            try {
                int location = input.readInt();
                int row = location / 3;
                int column = location % 3;

                setMark(  board[ row ][ column ],
                        ( myMark == X_MARK ? O_MARK : X_MARK ) );
                displayMessage( "Opponent moved. Your turn.\n" );
                myTurn = true;

            } // end try

            // process problems communicating with server
            catch ( IOException ioException ) {
                ioException.printStackTrace();
            }

        }
        else if (message.equals("Draw")){
            try{
                int location = input.readInt();
                if (location != -1){
                    int row = location / 3;
                    int column = location % 3;
                    setMark(  board[ row ][ column ],( myMark == X_MARK ? O_MARK : X_MARK ) );
                }

                System.out.println("Player "+myMark+" Draw");

                SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run()
                            {
                                restartButton.setVisible(true);
                            }
                        }
                );
                /*SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        displayMessage( "Draw.\n" );
                        restartButton.setVisible(true);
                    }
                });*/

            } // end try
            catch ( IOException ioException ) {
                ioException.printStackTrace();
            }
            //return false;
        }
        // simply display message
        else
            displayMessage( message + "\n" );

    } // end method processMessage

    // utility method called from other threads to manipulate
    // outputArea in the event-dispatch thread
    private void displayMessage( final String messageToDisplay )
    {
        // display message from event-dispatch thread of execution
        SwingUtilities.invokeLater(
                new Runnable() {  // inner class to ensure GUI updates properly

                    public void run() // updates displayArea
                    {
                        displayArea.append( messageToDisplay );
                        displayArea.setCaretPosition(
                                displayArea.getText().length() );
                    }

                }  // end inner class

        ); // end call to SwingUtilities.invokeLater
    }

    // utility method to set mark on board in event-dispatch thread
    private void setMark( final Square squareToMark, final char mark )
    {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run()
                    {
                        squareToMark.setMark( mark );
                    }
                }
        );
    }

    // send message to server indicating clicked square
    public void sendClickedSquare( int location )
    {
        if ( myTurn )
        {
            // send location to server
            try {
                output.writeInt( location );
                output.flush();
                myTurn = false;
            }

            // process problems communicating with server
            catch ( IOException ioException ) {
                ioException.printStackTrace();
            }
        }
    }

    // set current Square
    public void setCurrentSquare( Square square )
    {
        currentSquare = square;
    }

    public class reset implements ActionListener {

        public void actionPerformed(ActionEvent evt) {


            try {
                output.writeInt(-1);
                output.flush();
                resetBoard();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



    // private inner class for the squares on the board
    private class Square extends JPanel {
        private char mark;
        private int location;

        public Square( char squareMark, int squareLocation )
        {
            mark = squareMark;
            location = squareLocation;

            addMouseListener(
                    new MouseAdapter() {
                        public void mouseReleased( MouseEvent e )
                        {
                            setCurrentSquare( Square.this );
                            sendClickedSquare( getSquareLocation() );
                        }
                    }
            );

        } // end Square constructor

        // return preferred size of Square
        public Dimension getPreferredSize()
        {
            return new Dimension( 30, 30 );
        }

        // return minimum size of Square
        public Dimension getMinimumSize()
        {
            return getPreferredSize();
        }

        // set mark for Square
        public void setMark( char newMark )
        {
            mark = newMark;
            repaint();
        }

        // return Square location
        public int getSquareLocation()
        {
            return location;
        }

        // draw Square
        public void paintComponent( Graphics g )
        {
            super.paintComponent( g );

            g.drawRect( 0, 0, 29, 29 );
            g.drawString( String.valueOf( mark ), 11, 20 );
        }

    } // end inner-class Square

} // end class TicTacToeClient