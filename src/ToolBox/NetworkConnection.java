package ToolBox;

import javafx.scene.image.Image;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.function.Consumer;

public class NetworkConnection {
    private static final Logger logger = Logger.getLogger(NetworkConnection.class.getName());

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final ConnectionHandler connection = new ConnectionHandler();
    public Consumer<Serializable> receiveCallBack;
    public Consumer<Exception> errorCallBack;
    public String ip;
    public boolean isServer;
    public int port;
    public int timeout;
    public int maxRetries;
    private volatile boolean running = false;

    public NetworkConnection(Consumer<Serializable> receiveCallBack,
                             Consumer<Exception> errorCallBack,
                             String ip, boolean isServer, int port) {
        this(receiveCallBack, errorCallBack, ip, isServer, port, 5000, 3);
    }

    public NetworkConnection(Consumer<Serializable> receiveCallBack,
                             Consumer<Exception> errorCallBack,
                             String ip, boolean isServer, int port,
                             int timeout, int maxRetries) {
        this.receiveCallBack = receiveCallBack;
        this.errorCallBack = errorCallBack;
        this.ip = ip;
        this.isServer = isServer;
        this.port = port;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
    }

    public void openConnection() {
        running = true;
        executor.submit(connection);
    }

    public void sendData(Serializable data) {
        executor.submit(() -> {
            try {
                synchronized (connection.outputStream) {
                    connection.outputStream.writeObject(data);
                }
            } catch (IOException e) {
                handleException(e);
            }
        });
    }

    public void sendImage(Image image) {
        executor.submit(() -> {
            try {
                synchronized (connection.outputStream) {
                    connection.outputStream.defaultWriteObject();
                    connection.outputStream.writeObject(image);
                }
            } catch (IOException e) {
                handleException(e);
            }
        });
    }

    public void closeConnection() {
        running = false;
        try {
            if (connection.socket != null && !connection.socket.isClosed()) {
                connection.socket.close();
            }
        } catch (IOException e) {
            handleException(e);
        }
        executor.shutdownNow();
    }

    private void handleException(Exception e) {
        logger.log(Level.WARNING, "Network error", e);
        if (errorCallBack != null) {
            errorCallBack.accept(e);
        }
    }

    private class ConnectionHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream outputStream;

        @Override
        public void run() {
            int attempts = 0;
            while (running && attempts <= maxRetries) {
                try {
                    if (isServer) {
                        try (ServerSocket server = new ServerSocket(port)) {
                            server.setSoTimeout(timeout);
                            socket = server.accept();
                        }
                    } else {
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(ip, port), timeout);
                    }
                    socket.setSoTimeout(timeout);
                    outputStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

                    while (running) {
                        try {
                            Serializable data = (Serializable) inputStream.readObject();
                            receiveCallBack.accept(data);
                        } catch (SocketTimeoutException e) {
                            // allow loop to check running flag
                        }
                    }
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    handleException(e);
                    attempts++;
                }
            }
        }
    }
}
