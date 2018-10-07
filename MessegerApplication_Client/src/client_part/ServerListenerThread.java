package client_part;

import other.ClientCard;
import other.Key;
import other.Message;
import other.Ping;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;

public class ServerListenerThread implements Runnable {
    private Thread thread = null;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    //private SocketClient client = null;
    private ClientCard clientCard = null; // Карточка пользователя (учетка)

    public ServerListenerThread(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, ClientCard clientCard) {
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;
        this.clientCard = clientCard;

        thread = new Thread( this );
        thread.start();
    }

    public ServerListenerThread(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        this.objectOutputStream = objectOutputStream;
        this.objectInputStream = objectInputStream;

        thread = new Thread( this );
        thread.start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message messageIn = (Message) objectInputStream.readObject();
                if ( messageIn instanceof Ping ) {
                    Ping ping = (Ping) messageIn;
                    objectOutputStream.writeObject( new Ping() );
                }
                else {
                    // Если сообщение - ключ
                    if (messageIn instanceof Key) {
                        // И логин не мой, т.е от другого клиента
                        if (!messageIn.getLogin().equals(clientCard.getUserName())) {
                            // И если мой открытай клюс еще не получен и не установлен, то устанавливаем его
                            // И отправляем в ответ открытый ключ другому пользователю
                            if (!clientCard.getMyOpenKey().isSet())  {
                                //System.out.println("[ " + "Key from another client " + messageIn.getLogin() + " ] "  + " : " + messageIn.getMessage());
                                String key1 = messageIn.getMessage().substring(0,  messageIn.getMessage().indexOf(","));
                                String key2 = messageIn.getMessage().substring(messageIn.getMessage().indexOf(",") + 1);
                                int intKey1 = Integer.parseInt(key1);
                                int intKey2 = Integer.parseInt(key2);
                                clientCard.getMyOpenKey().setKey1(intKey1);
                                clientCard.getMyOpenKey().setKey2(intKey2);
                                objectOutputStream.writeObject( new Key(clientCard.getUserName(), clientCard.getFriendsOpenKey().getKey1(),
                                    clientCard.getFriendsOpenKey().getKey1()) );
                            }
                        }
                    }
                    else {
                       /* if(messageIn.getLogin().equals("Server-Bot")) {
                            objectOutputStream.writeObject( new Key(clientCard.getUserName(), clientCard.getFriendsOpenKey().getKey1(),
                                    clientCard.getFriendsOpenKey().getKey2()) );
                        }*/
  // Здесб вставить декодирование
                        System.out.println("[ " + messageIn.getDate().toString() + " ] " + messageIn.getLogin() + " : " + messageIn.getMessage());
                        /*System.out.println("My keys are: " + " fok = [ " + clientCard.getFriendsOpenKey().getKey1() + " : " + clientCard.getFriendsOpenKey().getKey2()
                                +  " ] " + " msk = [ " + clientCard.getMySecretKey().getKey1() + " : " + clientCard.getMySecretKey().getKey2() + " ] "
                                + " mok = [ " + clientCard.getMyOpenKey().getKey1() +  " : " + clientCard.getMyOpenKey().getKey2() + " ] ");*/
                    }
                }
            }
        }
        catch ( SocketException e ) { e.getMessage(); }
        catch ( ClassNotFoundException e ) { e.getMessage(); }
        catch ( IOException e ) { e.getMessage(); }
    }
}
