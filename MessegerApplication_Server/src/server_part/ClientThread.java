package server_part;

import client_part.Client;
import other.Config;
import other.Key;
import other.Message;
import other.Ping;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Map;

import static server_part.Server.getChatHistory;
import static server_part.Server.getUserList;

public class ClientThread extends Thread {

    private final static int DELAY = 30000;

    private Socket socket;
    private Message c;
    private String login;
    private int inPacks = 0;
    private int outPacks = 0;
    private boolean flag = false;
    private Timer timer;

    public ClientThread(Socket socket) {
        this.socket = socket;
        this.start();
    }

    public void run() {
        try {
            //Создаем потоки ввода-вывода для работы с сокетом
            final ObjectInputStream inputStream   = new ObjectInputStream(this.socket.getInputStream());
            final ObjectOutputStream outputStream = new ObjectOutputStream(this.socket.getOutputStream());

            //Читаем Message из потока
            this.c = (Message) inputStream.readObject();
            //Читаем логин отправителя
            this.login = this.c.getLogin();

            //Что же нам прислали?
            //Если это не регистрационное сообщение, то выводим его в чат
            // ! this.c.getMessage().equals(Config.HELLO_MESSAGE)

            if (! this.c.getMessage().equals(Config.HELLO_MESSAGE)) {
                System.out.println("[" + this.c.getLogin() + "]: " + this.c.getMessage());
                //И добавляем его к истории чата
                getChatHistory().addMessage(this.c);
            }
            else {

                String[] users = getUserList().getUsers();
                String userLogin = login;
                boolean contains = false;
                for(int i = 0; i < users.length; i++){
                    System.out.println("login# " + users[i]);
                    if (userLogin.equalsIgnoreCase(users[i])) {
                        contains = true;
                    }
                }
                // !getUserList().getOnlineUsers().containsKey(this.login)
                if (!contains) {
                    //Иначе, отправляем новичку историю чата и уведомляем всех остальных, что присоединился новый пользователь
                    outputStream.writeObject(getChatHistory());
                    this.broadcast(getUserList().getClientsList(), new Message("Server-Bot", "The user " + login + " has been connect"));
                    // Добавляем новичка в список пользователей
                }
            }
            // Добавляем новичка в список пользователей
            getUserList().addUser(login, socket, outputStream, inputStream);

            //Для ответа, указываем список доступных пользователей
            this.c.setOnlineUsers(getUserList().getUsers());
            //Передаем всем сообщение пользователя
            this.broadcast(getUserList().getClientsList(), this.c);

            //Запускаем таймер
            this.timer = new Timer(DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        //Если количество входящих пакетов от клиента равно исходящему, значит клиент еще в сети
                        if (inPacks == outPacks) {
                            outputStream.writeObject(new Ping());
                            outPacks++;
                            System.out.println(outPacks + " out");
                        } else {
                            // Иначе - вышел из сети
                            throw new SocketException();
                        }
                    } catch (SocketException ex1) {
                        System.out.println("packages not clash");
                        System.out.println(login + " disconnected!");
                        //Удаляем клиента из списка доступных и информируем всех
                        getUserList().deleteUser(login);
                        broadcast(getUserList().getClientsList(), new Message("Server-Bot", "The user " + login + " has been disconnect", getUserList().getUsers()));
                        flag = true;
                        timer.stop();
                    }  catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                }
            });

            //Начинаем пинговать клиента
            this.timer.start();
            outputStream.writeObject(new Ping());
            this.outPacks++;
            System.out.println(outPacks + " out");

            //А теперь нам остается только ждать от него сообщений
            while (true) {
                //Как только пинг пропал - заканчиваем
                if(this.flag) {
                    this.flag = false;
                    break;
                }
                this.c = (Message) inputStream.readObject();

                //Если это key
                if (c instanceof Key) {
                    System.out.println("Key message got on server: " + c.getMessage());
                    /*for (Client client : getUserList().getClientsList()) {
                        client.getThisObjectOutputStream().writeObject(c);
                    }*/
                    this.broadcast(getUserList().getClientsList(), this.c);
                }
                else {
                    //Если это ping
                    if (c.getMessage().equals("Ping")) {
                        this.inPacks++;
                        System.out.println(this.inPacks + " in");

                    }
                    else if (! c.getMessage().equals(Config.HELLO_MESSAGE)) {
                        System.out.println("[" + login + "]: " + c.getMessage());
                        getChatHistory().addMessage(this.c);

                    }
                    else {
                        outputStream.writeObject(getChatHistory());
                        this.broadcast(getUserList().getClientsList(), new Message("Server-Bot", "The user " + login + " has been connect"));
                    }

                    this.c.setOnlineUsers(getUserList().getUsers());

                    if (!(c.getMessage().equals("Ping")) && (! c.getMessage().equals(Config.HELLO_MESSAGE))) {
                        System.out.println("Send broadcast Message: " + c.getMessage() + "");
                        this.broadcast(getUserList().getClientsList(), this.c);
                    }
                }

            }

        } catch (SocketException e) {
            System.out.println(login + " disconnected!");
            getUserList().deleteUser(login);
            broadcast(getUserList().getClientsList(), new Message("Server-Bot", "The user " + login + " has been disconnect", getUserList().getUsers()));
            this.timer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void broadcast(ArrayList<Client> clientsArrayList, Message message) {
        try {
            for (Client client : clientsArrayList) {
                client.getThisObjectOutputStream().writeObject(message);
            }
        } catch (SocketException e) {
            System.out.println("in broadcast: " + login + " disconnected!");
            getUserList().deleteUser(login);
            this.broadcast(getUserList().getClientsList(), new Message("Server-Bot", "The user " + login + " has been disconnected", getUserList().getUsers()));

            timer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
