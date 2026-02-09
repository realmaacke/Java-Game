package builder.base;

import builder.Game;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private long window;
    private Game game;

    // FPS TITLE
    private double lastTime;
    private int frames;

    // builder.base.Window Settings
    private int window_width, window_height;
    private String window_title;

    public Window (int width, int height, String title) {
        this.window_width = width;
        this.window_height = height;
        this.window_title = title;

        game = new Game();
    }

    public void run() {
        System.out.println("LWJGL STARTING: " + Version.getVersion() + "!");

        init();

        lastTime = glfwGetTime();
        frames = 0;

        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private  void  init () {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to init GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Width, height, title, monitor, share
        window = glfwCreateWindow(
                this.window_width,
                this.window_height,
                this.window_title,
                NULL,
                NULL
        );

        glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            glViewport(0, 0, w, h);
            WindowContext.width = w;
            WindowContext.height = h;
        });


        if (window == NULL) {
            throw new RuntimeException("Failed to create the window, obj:" + window);
        }

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        WindowContext.window = window;

        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            WindowContext.mouseX = xpos;
            WindowContext.mouseY = ypos;
        });


        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // V-sync

        glfwShowWindow(window);
    }

    private void loop() {
        GL.createCapabilities(); // Apparently a critical line
        glViewport(0,0,window_width,window_height);

        glClearColor(0.53f, 0.81f, 0.92f, 1f);

        game.init();

        while ( !glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            double currentTime = glfwGetTime();
            frames++;

            if (currentTime - lastTime >= 1.0) {
                glfwSetWindowTitle(
                        window,
                        window_title + " | FPS: " + frames
                );

                frames = 0;
                lastTime = currentTime;
            }


            game.update();
            game.render();


            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

}
