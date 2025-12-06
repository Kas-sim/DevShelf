package core;

public class Launcher {
    public static void main(String[] args) {
        // Try to load a JavaFX class explicitly before launching
        try {
            Class.forName("javafx.application.Application");
        } catch (ClassNotFoundException e) {
            System.err.println("JavaFX not found on classpath!");
        }

        GuiMain.main(args);
    }
}