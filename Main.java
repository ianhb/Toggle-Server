/**
 * Main execution class
 * <p/>
 * Created by Ian on 1/5/2015.
 */
public class Main {
    public static void main(String[] args) {
        CommandListener cm = null;
        while (true) {
            try {
                Daemon daemon = new Daemon();
                daemon.run();
                cm = new CommandListener(daemon.ip);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
