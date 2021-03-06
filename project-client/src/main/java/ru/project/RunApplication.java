package ru.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.factory.ObjectFactory;
import ru.project.entity.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class RunApplication {

    public static void main(String[] args) {
        ObjectFactory.getInstance();
        String insert = "insert into public.users(id,first_name,last_name,age) values(1,'Ivan','Ivanov',256);";
        ObjectFactory.getDataBase().execute(insert);

        new Server().bootstrap();
    }
}

class Server {
    private AsynchronousServerSocketChannel server;
    private final static int BUFFER_SIZE = 256;
    private final static String HEADERS =
            "HTTP/1.1 200 OK\n" +
                    "Server: name\n" +
                    "Content-Type: text/html\n" +
                    "Content-Length: %s\n" +
                    "Connection: close\n\n";

    public void bootstrap() {
        try {
            server = AsynchronousServerSocketChannel.open();
            server.bind(new InetSocketAddress("127.0.0.1", 8080));

            while (true) {
                Future<AsynchronousSocketChannel> future = server.accept();
                handleClient(future);
            }

        } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Future<AsynchronousSocketChannel> future) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        System.out.println("new client thread");

//        AsynchronousSocketChannel clientChannel = future.get(30, TimeUnit.SECONDS);
        AsynchronousSocketChannel clientChannel = future.get();

        User user = null;
        try {
            user = ObjectFactory.getDataBase().getObject(User.class);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        while (clientChannel != null && clientChannel.isOpen()) {
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            StringBuilder builder = new StringBuilder();
            boolean keepReading = true;
            while (keepReading) {
                clientChannel.read(buffer).get();

                int position = buffer.position();
                keepReading = position == BUFFER_SIZE;
                byte[] array = keepReading
                        ? buffer.array()
                        : Arrays.copyOfRange(buffer.array(), 0, position);

                builder.append(new String(array));

                buffer.clear();
            }

            String table = new ObjectMapper().writeValueAsString(user);
            String body = String.format("<html><body>%s</body></html>", table);
            String page = String.format(HEADERS, body.length()) + body;
            ByteBuffer response = ByteBuffer.wrap(page.getBytes());
            clientChannel.write(response);
            clientChannel.close();
        }
    }
}
