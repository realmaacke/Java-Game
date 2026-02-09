package builder.base;

public class Main {
    public static void main(String[] args) {
        Window app;
        app = new Window(Config.width, Config.height, Config.title);
        app.run();
    }
}
