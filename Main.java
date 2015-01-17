/**
 * Main execution class
 * <p/>
 * Created by Ian on 1/5/2015.
 */
public class Main {
    public static void main(String[] args) {
        while (true) {
            try {
                Daemon daemon = new Daemon();
                daemon.run();
                new CommandListener();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
