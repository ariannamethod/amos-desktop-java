package ToolBox;

import javafx.scene.image.Image;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * NetworkConnection handles asynchronous communication using an {@link ExecutorService}.
 * It supports callbacks for received data, connection status changes and errors.
 */
public class NetworkConnection implements Closeable {
    private final ExecutorService connectionExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService sendExecutor = Executors.newSingleThreadExecutor();
    private final Consumer<Serializable> receiveCallBack;
    private final Consumer<Boolean> statusCallback;
    private final Consumer<Exception> errorCallback;
    private final String ip;
    private final boolean isServer;
    private final int port;

    private volatile boolean running;
    private Socket socket;
    private ServerSocket serverSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public NetworkConnection(Consumer<Serializable> receiveCallBack,
                             Consumer<Boolean> statusCallback,
                             Consumer<Exception> errorCallback,
                             String ip, boolean isServer, int port) {
        this.receiveCallBack = receiveCallBack;
        this.statusCallback = statusCallback;
        this.errorCallback = errorCallback;
        this.ip = ip;
        this.isServer = isServer;
        this.port = port;
    }

    /**
     * Opens the connection and starts listening asynchronously.
     */
    public void openConnection() {
        running = true;
        connectionExecutor.submit(this::runConnection);
    }

    private void runConnection() {
        while (running) {
            try {
                startSocket();
                if (statusCallback != null) statusCallback.accept(true);
                listen();
            } catch (Exception e) {
                if (errorCallback != null) errorCallback.accept(e);
                if (statusCallback != null) statusCallback.accept(false);
                closeResources();
                if (running) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private void startSocket() throws IOException {
        if (isServer) {
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
        } else {
            socket = new Socket(ip, port);
        }
        socket.setTcpNoDelay(true);
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
    }

    private void listen() throws IOException, ClassNotFoundException {
        while (running && !socket.isClosed()) {
            Serializable data = (Serializable) inputStream.readObject();
            receiveCallBack.accept(data);
        }
    }

    /**
     * Sends serializable data asynchronously.
     */
    public void sendData(Serializable data) {
        sendExecutor.submit(() -> {
            try {
                if (outputStream != null) {
                    outputStream.writeObject(data);
                    outputStream.flush();
                }
            } catch (IOException e) {
                if (errorCallback != null) errorCallback.accept(e);
            }
        });
    }

    /**
     * Sends an image asynchronously.
     */
    public void sendImage(Image image) {
        sendData(image);
    }

    /**
     * Closes the connection and shuts down executors.
     */
    public void closeConnection() {
        running = false;
        closeResources();
        connectionExecutor.shutdownNow();
        sendExecutor.shutdown();
    }

    private void closeResources() {
        tryClose(inputStream);
        tryClose(outputStream);
        tryClose(socket);
        tryClose(serverSocket);
    }

    private void tryClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void close() throws IOException {
        closeConnection();
    }
}
