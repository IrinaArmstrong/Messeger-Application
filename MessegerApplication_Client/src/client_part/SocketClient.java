package client_part;

import other.ClientCard;
import other.Key;
import other.Message;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class SocketClient {
    private final static String address = "localhost"; // Или 127.0.0.1, это IP-адрес компьютера, где исполняется наша серверная программа
    private final static int serverPort = 5000; // здесь обязательно нужно указать порт к которому привязывается сервер

    private static ClientCard clientCard; // Карточка пользователя (учетка)

    private static String userName = "";
    static Socket socket = null;

    public static void main( String[] args ) {

        System.out.println("Вас приветствует клиент чата!\n");
        System.out.println("Введите свой ник и нажмите \"Enter\"");

        // Создаем поток для чтения с клавиатуры
        BufferedReader keyboard = new BufferedReader( new InputStreamReader( System.in ) );
        try {
// Ждем пока пользователь введет свой ник и нажмет кнопку Enter
            userName = keyboard.readLine();
            System.out.println();
        }
        catch ( IOException e ) { e.printStackTrace(); }

        clientCard = new ClientCard(userName); // Создаем учетку пользователя

        try {
            try {
                InetAddress ipAddress = InetAddress.getByName( address ); // создаем объект который отображает вышеописанный IP-адрес
                socket = new Socket( ipAddress, serverPort ); // создаем сокет используя IP-адрес и порт сервера
                clientCard.setRemotePort(serverPort);
                clientCard.setLocalPort(socket.getLocalPort());

// Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиентом
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

// Конвертируем потоки в другой тип, чтоб легче обрабатывать текстовые сообщения
                ObjectOutputStream objectOutputStream = new ObjectOutputStream( outputStream );
                ObjectInputStream objectInputStream = new ObjectInputStream( inputStream );

                new ServerListenerThread( objectOutputStream, objectInputStream, clientCard);

                //objectOutputStream.writeObject(new Message(userName, "User join to the chat(Auto-message)"));

// Генерируем скрытый и открытый ключи и отправляем открытый
                generateKey();

                objectOutputStream.writeObject(clientCard.getFriendsOpenKey()); // Отсылаем открытый ключ другому клиенту (на сервер)

// Ждем получения открытого ключа
                        //System.out.println("Waiting for key...");


// Создаем поток для чтения с клавиатуры
                String message = null;
                System.out.println("Наберите сообщение и нажмите \"Enter\"\n");

                while (true) { // Бесконечный цикл
                    message = keyboard.readLine(); // ждем пока пользователь введет что-то и нажмет кнопку Enter.
// Кодируем введенное сообщение




                    objectOutputStream.writeObject( new Message( userName, message ) ); // отсылаем введенную строку текста серверу.
                }
            } catch ( Exception e ) { e.printStackTrace(); }
        }
        finally {
            try {
                if ( socket != null ) { socket.close(); }
            } catch ( IOException e ) { e.printStackTrace(); }
        }

    }

    // Генерируем ключи
    private static void generateKey()  {
        /*
        * Здесь генерируем значения и присваиваем их нужным частям ключей
        */
        clientCard.setMySecretKey(new Key(userName, 2, 2));
        clientCard.setFriendsOpenKey(new Key(userName, 1, 1));
    }
}
