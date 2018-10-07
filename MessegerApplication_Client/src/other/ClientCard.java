package other;

/*
* Класс - персональная карточка пользователя (учетка).
* Хранит персональные данные на каждого пользователя сети.
*/

public class ClientCard {

    private String userName; // Имя поьзователя
    private int localPort; // Номер клиентского порта
    private int remotePort; // Номер серверного порта
    private Key myOpenKey; // Открытый ключ, который получаем от другого клиента
    private Key friendsOpenKey; // Открытый ключ, который отправляем другому клиенту
    private  Key mySecretKey; // Секретный ключ, который сгенерировали

    /*
    * Конструктор карточки
    */
    public ClientCard(String userName) {
        this.userName = userName;
        this.myOpenKey = new Key(userName, 0, 0);
        this.friendsOpenKey = new Key(userName,0, 0);
        this.mySecretKey = new Key(userName,0, 0);
    }

    public  String getUserName() {
        return userName;
    }

    public synchronized void setUserName(String userName) {
        this.userName = userName;
    }

    public  int getLocalPort() {
        return localPort;
    }

    public  synchronized void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public  int getRemotePort() {
        return remotePort;
    }

    public  synchronized void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public  Key getMyOpenKey() {
        return myOpenKey;
    }

    public  synchronized void setMyOpenKey(Key myOpenKey) {
        this.myOpenKey = myOpenKey;
    }

    public  Key getFriendsOpenKey() {
        return friendsOpenKey;
    }

    public  synchronized void setFriendsOpenKey(Key friendsOpenKey) {
        this.friendsOpenKey = friendsOpenKey;
    }

    public  Key getMySecretKey() {
        return mySecretKey;
    }

    public  synchronized void setMySecretKey(Key mySecretKey) {
        this.mySecretKey = mySecretKey;
    }
}
