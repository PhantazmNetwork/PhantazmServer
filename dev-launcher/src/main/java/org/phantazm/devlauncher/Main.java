package org.phantazm.devlauncher;

import java.io.*;

/**
 * Builds to ./dev-launcher.jar. Necessary due to IntelliJ's poor support for running Docker containers, the need to
 * support both Windows and Linux hosts, and wanting to avoid having developers run commands every time they wish to
 * launch the server.
 * <p>
 * Some limitations:
 * <ul>
 *    <li>The Docker container cannot use a pseudo-TTY (--tty or tty: true) because we would not be able to connect to
 *    it in this manner.This could be fixed by using an external PTY library (like pty4j), but this would bloat the size
 *    of the final jar, which is less than ideal as it is checked into VCS. Additionally, pty4j does not play nice with
 *    johnrengelmen's `shadow` plugin.</li>
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
 */
public class Main {
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

    private static Process proc(String env, boolean isAttach) throws IOException {
        if (shuttingDown) {
            return DUMMY_PROCESS;
        }

        String[] command = System.getenv(env).split(" ");

        Process proc;
        synchronized (lock) {
            if (shuttingDown) {
                return DUMMY_PROCESS;
            }

            Process process = (proc = new ProcessBuilder().command(command)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT).redirectError(ProcessBuilder.Redirect.INHERIT).start());

            if (isAttach) {
                currentProcess = process;

                OutputStream outputStream = process.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

                // this indicates to the server process to start
                writer.write("start");
                writer.newLine();
                writer.flush();

                asyncTransferStandardInputTo(writer);
            }
        }

        return proc;
    }

    private static void asyncTransferStandardInputTo(BufferedWriter writer) {
        Thread thread = new Thread(() -> {
            try (writer) {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    String line = inputReader.readLine();
                    if (line == null) {
                        break;
                    }

                    writer.write(line);
                    writer.newLine();
                    writer.flush();
                }
            } catch (IOException e) {
                System.err.println("[ERROR] An exception occurred when trying to connect to the server process " +
                    "standard input! It will likely not be possible to send commands to it.");
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (lock) {
                Process process = currentProcess;
                shuttingDown = true;
                if (process == null) {
                    return;
                }

                System.out.println("[INFO] Shutting down!");

                try {
                    new ProcessBuilder().command(System.getenv("DOCKER_COMPOSE_DOWN_CMD").split(" "))
                        .inheritIO().start();
                } catch (IOException ignored) {
                }

                currentProcess = null;
            }
        }));

        int code;
        if ((code = proc("DOCKER_COMPOSE_UP_CMD", false).waitFor()) != 0) {
            System.err.println("[ERROR] Docker Compose returned a non-zero exit code (" + code + ")");
            return;
        }

        if ((code = proc("DOCKER_ATTACH_CMD", true).waitFor()) != 0) {
            System.err.println("[ERROR] Server process returned a non-zero exit code (" + code + ")");
        }
    }
}