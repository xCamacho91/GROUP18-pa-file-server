import java.util.Scanner;

public class MainClient {

    public static void main ( String[] args ) throws Exception {

        Scanner usrInput = new Scanner ( System.in );
        System.out.println ( "Insert your name:" );
        String userName = usrInput.nextLine ( );

        Client client = new Client ( 8000 , userName);
        client.execute ( );
    }

}
