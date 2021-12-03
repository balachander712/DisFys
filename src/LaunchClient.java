import java.io.IOException;
import java.rmi.NotBoundException;

public class LaunchClient {


    public LaunchClient() {
    }

    public void launchClients(){
        try {
            Client c = new Client();
            char[] ss = "File 1 test test END ".toCharArray();
            byte[] data = new byte[ss.length];
            for (int i = 0; i < ss.length; i++)
                data[i] = (byte) ss[i];

            c.write("file1", data);
            byte[] ret = c.read("file1");
            System.out.println("file1: " + ret +"]");

            c = new Client();
            ss = "File 1 Again Again END ".toCharArray();
            data = new byte[ss.length];
            for (int i = 0; i < ss.length; i++)
                data[i] = (byte) ss[i];

            c.write("file1", data);
            ret = c.read("file1");
            System.out.println("file1: " + ret +"]");

            c = new Client();
            ss = "File 2 test test END ".toCharArray();
            data = new byte[ss.length];
            for (int i = 0; i < ss.length; i++)
                data[i] = (byte) ss[i];

            c.write("file2", data);
            ret = c.read("file2");
            System.out.println("file2: " + ret +"]");

        } catch (NotBoundException | IOException | MessageNotFoundException e) {
            e.printStackTrace();
        }
    }


//    public static void main(String[] args) {
//
//        launchClients();
//
//    }
}
