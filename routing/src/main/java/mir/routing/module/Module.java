package mir.routing.module;

import mir.routing.constants.Constants;
import mir.routing.exception.PortNotFoundException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class Module {
    private static Socket clientSocket, sendSocket;
    private static ServerSocket serverSocket;
    private static DataInputStream inClient;

    private static int port;

    private static void sendToAnotherModule(String message, int port) throws IOException {
        try {
            sendSocket = new Socket("localhost", port);

            DataOutputStream out = new DataOutputStream(sendSocket.getOutputStream());

            out.writeUTF(message);
            out.flush();
        } finally {
            sendSocket.close();
        }
    }

    private static int getSendPort(int mti) throws PortNotFoundException {
            switch (port) {
                case Constants.Ports.ACQUIRER_MODULE:
                case Constants.Ports.ISSUER_MODULE:
                    return Constants.Ports.PLATFORM_MODULE;
                case Constants.Ports.PLATFORM_MODULE:
                    if (mti == 0100) {
                        return Constants.Ports.ISSUER_MODULE;
                    } else /*if (mti == 0110)*/ {
                        return Constants.Ports.ACQUIRER_MODULE;
                    }
                default:
                    // TODO: Change to Exception type, not RuntimeException.
                    throw new PortNotFoundException("There's no module with port provided");
            }
    }

    public static void start() {
        try {
            try {
                // Порт отправки сообщенний в Parsing Module = 8011.
                serverSocket = new ServerSocket(port, 1);

                // Запуск сервера и ожидание подключения к нему.
                /* DEBUG */
                System.out.println("Server started.");

                while (true) {
                    clientSocket = serverSocket.accept();

                    try {
                        // region 1. Input
                        // Получение потока ввода/вывода клиентского сокета, по которым отправляются/получаются сообщения.
                        inClient = new DataInputStream(clientSocket.getInputStream());

                        // Получение сообщения от клиента.
                        String encodedMessage = inClient.readUTF();

                        /* DEBUG */
                        System.out.println(String.format("Recieved: %s\n", encodedMessage));
                        // endregion

                        // region 2. Parse & Check
//                        Router router = new Router(); // Router object from Parsing module. // TODO: прикрутить ParsingModule.
//                        ParsedMessage parsedMessage = router.getParsedMessage(encodedMessage); // TODO: прикрутить ParsingModule.

                        boolean isValid = /*CheckerModule.check(parsedMessage)*/ true; // TODO: прикрутить CheckerModule.

                        // endregion

                        if (isValid) {
                            // region 3. Edit; 4. Save to DB; 5. Form new message.

//                            String encodedMessage = router.fromEncodedMessage(parsedMessage); // TODO: прикрутить ParsingModule.

                            // endregion

                            // region 6. Send

                            int sendPort = getSendPort(0100/*parsedMessage.mti*/);
                            sendToAnotherModule(encodedMessage, sendPort);

                            /* DEBUG */
                            System.out.println(String.format("Sent: %s; to: %d", encodedMessage, sendPort));

                            // endregion
                        } else if (!isValid && port == Constants.Ports.ACQUIRER_MODULE) {
                            // Отправка неправильного сообщения отправителю.
                            sendToAnotherModule(String.format("Message received is invalid: %s", encodedMessage), Constants.Ports.ROUTING_MODULE);
                        }

                    } finally {
                        clientSocket.close();
                        inClient.close();
                    }
                }
            } finally {
                serverSocket.close();
                System.out.println("Server closed.");
            }
        } catch (IOException ex) {
            // TODO: отреагировать по-нормальному.
            System.out.println("Некая какая-то проблема на стороне сервера.");
        } catch (PortNotFoundException ex) {

        }
    }

    public static void main(String[] args) {
        start();
    }

    public Module(int port) {
        this.port = port;
    }
}
