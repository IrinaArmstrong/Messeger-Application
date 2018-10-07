package other;

/*
* Класс для обмена ключами при шифровании RSA.
* Необходимо отправить ДО начала омена сообщения между клиентами.
*/

public class Key extends Message {

    // Число для ключа 1
    private int key1 = 0;
    // Число для ключа 2
    private int key2 = 0;

    // Конструктор класса
    public Key(String login, int key1, int key2) {
        super(login, "");
        this.key1 = key1;
        this.key2 = key2;
        String message = new String(key1 + "," + key2 );
        super.setMessage(message);
    }

    // Метод для получения первой части ключа
    public int getKey1() {
        return key1;
    }

    // Метод для получения второй части ключа
    public int getKey2() {
        return key2;
    }

    // Метод для установки первой части ключа
    public void setKey1(int key1)  {
        this.key1 = key1;
    }

    // Метод для установки второй части ключа
    public void setKey2(int key2)  {
        this.key2 = key2;
    }

    // Проверяем, установлен ли ключ
    public boolean isSet() {
        if (this.key1 == 0 && this.key2 == 0)  {
            return false;
        }
        return true;
    }
    @Override
    public String getMessage() {
        return new String( key1 + "," + key2 );
    }
}
