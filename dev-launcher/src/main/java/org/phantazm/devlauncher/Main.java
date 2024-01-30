package org.phantazm.devlauncher;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds to ./dev-launcher.jar. Necessary due to IntelliJ's poor support for running Docker containers, the need to
 * support both Windows and Linux hosts, and wanting to avoid having developers run commands every time they wish to
 * launch the server. This code starts the Gradle build process and manages the lifecycle of any necessary Docker
 * containers.
 * <p>
 * Some limitations:
 * <ul>
 *    <li>The Docker container cannot use a pseudo-TTY (--tty or tty: true) because we would not be able to connect to
 *    it in this manner. This could be fixed by using an external PTY library (like pty4j), but this would bloat the
 *    size of the final jar, which is less than ideal as it is checked into VCS. Additionally, pty4j does not play nice
 *    with johnrengelmen's `shadow` plugin.</li>
 *    <li>Because of the above concerns, jline will spit out a warning to the console.</li>
 *    <li>You will only see output from the server, not any other container. You can attach to the proxy and database
 *    specifically through manually running `docker attach [container-name]`.</li>
 * </ul>
 * <p>
 * Justification:
 * <ul>
 *     <li>It is not possible to consistently have intelliJ run `docker attach` in its terminal after
 *     `docker compose up -d`. This is possibly due to a bug.</li>
 *     <li>Other methods of accomplishing this, such as the "Run script" configuration, are not platform-independent.
 *     </li>
 *     <li>Docker can be run as an external tool, which can be added to the "Run before" section of any IntelliJ run
 *     configuration, but this would not be portable. IntelliJ would also NOT store the external tool configuration in
 *     the .run folder.</li>
 *     <li>It is additionally desirable to always run `docker compose down` after the server terminates, regardless of
 *     if it did so erroneously (with a non-zero exit code), to prevent the containers from unnecessarily staying up.
 *     IntelliJ would not do this.</li>
 * </ul>
 * <p>
 * The resulting jar file can be run from the command line using {@code java -jar dev-launcher.jar} to launch in normal
 * mode, or {@code java -jar dev-launcher.jar -d} to launch in debug mode.
 * <p>
 * Two program arguments are recognized by this class: {@code -d} and {@code -g}. {@code -d} launches in debug mode,
 * {@code -g} signifies that all the following arguments should be passed to the Gradle wrapper as program arguments.
 * If -g is not specified, the following default arguments are passed to Gradle:
 * {@code -PskipBuild=snbt-builder,dev-launcher,velocity -w phantazm-server:copyJar}
 */
public class Main {
    private static final String[] DEFAULT_GRADLE_ARGS = new String[]{
        "-PskipBuild=snbt-builder,dev-launcher,velocity", "-w", "phantazm-server:copyJar"
    };

    private static final String[] DOCKER_COMPOSE_UP_NODEBUG = new String[]{
        "docker", "compose", "up", "-d"
    };

    private static final String[] DOCKER_COMPOSE_UP_DEBUG = new String[]{
        "docker", "compose", "--profile", "debug", "up", "-d"
    };

    private static final String[] DOCKER_ATTACH_NODEBUG = new String[]{
        "docker", "attach", "phantazm-server-1"
    };

    private static final String[] DOCKER_ATTACH_DEBUG = new String[]{
        "docker", "attach", "phantazm-server_debug-1"
    };

    private static final String[] DOCKER_COMPOSE_DOWN_NODEBUG = new String[]{
        "docker", "compose", "down"
    };

    private static final String[] DOCKER_COMPOSE_DOWN_DEBUG = new String[]{
        "docker", "compose", "--profile", "debug", "down"
    };

    private static final Process DUMMY_PROCESS = new Process() {
        @Override
        public OutputStream getOutputStream() {
            return null;
        }

        @Override
        public InputStream getInputStream() {
            return null;
        }

        @Override
        public InputStream getErrorStream() {
            return null;
        }

        @Override
        public int waitFor() {
            return 0;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {

        }
    };

    private static final Object lock = new Object();

    private static volatile Process currentProcess;
    private static volatile boolean shuttingDown;
    private static volatile BufferedWriter serverProcessWriter;
    private static volatile boolean lastWasStop;

    private static Process proc(String[] cmd, boolean isServerProcess) throws IOException {
        if (shuttingDown) {
            return DUMMY_PROCESS;
        }

        Process proc;
        synchronized (lock) {
            if (shuttingDown) {
                return DUMMY_PROCESS;
            }

            Process process = (proc = new ProcessBuilder().command(cmd)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT).start());

            if (isServerProcess) {
                currentProcess = process;

                OutputStream outputStream = process.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

                serverProcessWriter = writer;

                // this indicates to the server process to start
                writer.write("start\n");
                writer.flush();

                asyncTransferStandardInputTo(writer);
            }
        }

        return proc;
    }

    private static void asyncTransferStandardInputTo(BufferedWriter writer) {
        Console console = System.console();
        Thread thread = new Thread(() -> {
            try (writer) {
                BufferedReader inputReader = new BufferedReader(console == null ? new InputStreamReader(System.in) :
                    console.reader());
                while (true) {
                    String line = inputReader.readLine();
                    if (line == null) {
                        break;
                    }

                    writer.write(line);
                    writer.write('\n');
                    writer.flush();

                    lastWasStop = line.equals("stop");
                }
            } catch (IOException e) {
                System.err.println("[ERROR] An exception occurred when trying to connect to the server process " +
                    "standard input! It will likely not be possible to send commands to it.");
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private static boolean isDebug(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-g")) {
                return false;
            }

            if (arg.equalsIgnoreCase("-d")) {
                return true;
            }
        }

        return false;
    }

    private static String[] gradlewArgs(String[] args) {
        boolean argMode = false;
        List<String> argBuilder = null;
        int i = 0;
        for (String arg : args) {
            if (argMode) {
                if (argBuilder == null) {
                    argBuilder = new ArrayList<>(args.length - i);
                }

                argBuilder.add(arg);
            } else if (arg.equalsIgnoreCase("-g")) {
                if (i == args.length - 1) {
                    argBuilder = new ArrayList<>(0);
                }

                argMode = true;
            }

            i++;
        }

        String[] gradlewArgs = argBuilder == null ? DEFAULT_GRADLE_ARGS : argBuilder.toArray(String[]::new);
        String[] finalArgs = new String[gradlewArgs.length + 1];
        finalArgs[0] = Path.of(".", "gradlew").toString();
        System.arraycopy(gradlewArgs, 0, finalArgs, 1, gradlewArgs.length);
        return finalArgs;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Files.newOutputStream(Path.of(".override.env"), StandardOpenOption.CREATE, StandardOpenOption.APPEND)
            .close();

        boolean debug = isDebug(args);

        String[] gradlewArgs = gradlewArgs(args);
        String[] upCommand = debug ? DOCKER_COMPOSE_UP_DEBUG : DOCKER_COMPOSE_UP_NODEBUG;
        String[] attachCommand = debug ? DOCKER_ATTACH_DEBUG : DOCKER_ATTACH_NODEBUG;
        String[] downCommand = debug ? DOCKER_COMPOSE_DOWN_DEBUG : DOCKER_COMPOSE_DOWN_NODEBUG;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (lock) {
                Process process = currentProcess;
                shuttingDown = true;
                if (process == null) {
                    return;
                }

                System.out.println("[INFO] Shutting down!");
                BufferedWriter writer = serverProcessWriter;
                if (writer != null && !lastWasStop) {
                    try {
                        writer.write("stop\n");
                        writer.flush();
                    } catch (IOException e) {
                        System.err.println("[ERROR] Errored while trying to send stop command to server process!");
                    }
                }

                try {
                    new ProcessBuilder().command(downCommand).inheritIO().start();
                } catch (IOException ignored) {
                }

                currentProcess = null;
            }
        }));

        int code;
        if ((code = proc(gradlewArgs, false).waitFor()) != 0) {
            System.err.println("[ERROR] Gradle build returned a non-zero exit code (" + code + ")");
            return;
        }

        if ((code = proc(upCommand, false).waitFor()) != 0) {
            System.err.println("[ERROR] Docker Compose returned a non-zero exit code (" + code + ")");
            return;
        }

        if ((code = proc(attachCommand, true).waitFor()) != 0) {
            System.err.println("[ERROR] Server process returned a non-zero exit code (" + code + ")");
        }
    }
}