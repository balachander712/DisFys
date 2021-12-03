import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;

public class LaunchClient{


    public LaunchClient() {
    }

    public void launchClients(){
        try {
            Client c = new Client();
            File file1 = new File("/home/madman/Documents/DisFys/file1");
            BufferedReader br = new BufferedReader(new FileReader(file1));
            String st;
            String str = null;
            while ((st = br.readLine()) != null) {
                //System.out.println(st);
                str += st;
            }
            char[] ss = str.toCharArray();
            byte[] data = new byte[ss.length];
            for (int i = 0; i < ss.length; i++)
                data[i] = (byte) ss[i];

            c.write("file1", data);
            byte[] ret = c.read("file1");
            System.out.println("file1: " + ret +"]");

            c = new Client();
            ss = "Testing File 1 Again ---> END ".toCharArray();
            data = new byte[ss.length];
            for (int i = 0; i < ss.length; i++)
                data[i] = (byte) ss[i];

            c.write("file1", data);
            ret = c.read("file1");
            System.out.println("file1: " + ret +"]");

            File file2 = new File("/home/madman/Documents/DisFys/file2");
            BufferedReader br1 = new BufferedReader(new FileReader(file2));
            String st1;
            String str1 = null;
            while ((st1 = br.readLine()) != null) {
                //System.out.println(st);
                str1 += st1;
            }

            c = new Client();
            ss = "Testing File 1 ---> END!!! ".toCharArray();
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

//    @Override
//    public void run() {
//        try {
//            Client c = new Client();
//            char[] ss = "File 1 test test END ".toCharArray();
//            byte[] data = new byte[ss.length];
//            for (int i = 0; i < ss.length; i++)
//                data[i] = (byte) ss[i];
//
//            c.write("file1", data);
//            byte[] ret = c.read("file1");
//            System.out.println("file1: " + ret +"]");
//
//            c = new Client();
//            ss = "File 1 Again Again END ".toCharArray();
//            data = new byte[ss.length];
//            for (int i = 0; i < ss.length; i++)
//                data[i] = (byte) ss[i];
//
//            c.write("file1", data);
//            ret = c.read("file1");
//            System.out.println("file1: " + ret +"]");
//
//            c = new Client();
//            ss = "File 2 test test END ".toCharArray();
//            data = new byte[ss.length];
//            for (int i = 0; i < ss.length; i++)
//                data[i] = (byte) ss[i];
//
//            c.write("file2", data);
//            ret = c.read("file2");
//            System.out.println("file2: " + ret +"]");
//
//        } catch (NotBoundException | IOException | MessageNotFoundException e) {
//            e.printStackTrace();
//        }
//    }


//    public static void main(String[] args) {
//
//        launchClients();
//
//    }
}
