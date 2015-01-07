import java.awt.*;

/**
 * Separate thread to control the mouse, allowing the main thread to still listen for commands
 * <p/>
 * Created by Ian on 1/6/2015.
 */
public class MouseController extends Thread {

    int x;
    int y;
    int scroll;

    Robot robot;

    public MouseController(Robot robot, int x, int y) {
        this.robot = robot;
        this.x = x;
        this.y = y;
        scroll = 0;
        this.start();
    }

    public void run() {
        while ((x != 0) || (y != 0)) {
            Point currentLocation = MouseInfo.getPointerInfo().getLocation();
            int newX = currentLocation.x + x;
            int newY = currentLocation.y - y;
            robot.mouseMove(newX, newY);
            robot.delay(10);
        }
        while (scroll != 0) {
            robot.mouseWheel(scroll);
            robot.delay(100);
        }
    }

    public void changeVelocities(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public void stopMoving() {
        x = 0;
        y = 0;
    }

    public void changeScrolling(int x) {
        scroll = x;
    }

    public void stopScrolling() {
        scroll = 0;
    }


}
