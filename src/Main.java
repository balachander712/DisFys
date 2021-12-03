public class Main {

    public static void main(String[] args) {

        final int CLIENT_NO = 2;

//        for(int i = 0; i < CLIENT_NO; i++){
//            Thread obj = new Thread(new LaunchClient());
//            obj.start();
//        }

        LaunchClient l1 = new LaunchClient();
        l1.launchClients();

    }
}
