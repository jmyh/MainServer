package server;

import workwithfiles.Parser;
import workwithfiles.Remover;
import workwithfiles.Writer;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable {

    private final static File fileRecords=new File("history.json");
    private final static File dir=new File("files");

    private Socket socketText;
    private PrintWriter printWriter;
    private Scanner scanner;

    private Socket socketFile;

    private static Logger logger= Logger.getLogger(Server.class.getName());

    Server(Socket socketText,Socket socketFile) throws IOException {
        this.socketText=socketText;
        this.printWriter=new PrintWriter(socketText.getOutputStream(),true);
        this.scanner=new Scanner(socketText.getInputStream());

        this.socketFile=socketFile;
        //this.fileIS=new BufferedInputStream(socketFile.getInputStream());
        //this.fileOS=new BufferedOutputStream(socketFile.getOutputStream());
    }
    @Override
    public void run() {
        Message message=new Message(scanner.nextLine());
        boolean close=false;

        while(!close) {

            synchronized(fileRecords) {
                switch(message.getMessage()) {
                    case Message.MY_MESSAGES: getHistoryOfMessages(message);break;
                    case Message.DELETE: deleteRecord(message); break;
                    case Message.ALL_MESSAGES: getAllMessages(message);break;
                    case Message.SEND_FILE: writeFile();break;
                    case Message.GET_FILE: sendFile(dir,message);break;
                    case Message.EXIT:closeConnection(message); close =true; break;
                    default:saveMessage(message);
                }

            }
            //next message
            if(!close)
                message=new Message(scanner.nextLine());
        }

    }

    private void sendFile(File dir,Message message) {
        //todo защита от скачаивания файла который не существует

        logger.info(message.getLogin()+": Получение файла ("+message.getId()+")...");
        Parser parser=new Parser(fileRecords);
        Message fileRecord=parser.getFileRecord(message);
        File sendFile=null;
        if(fileRecord!=null) {

            sendFile = new File(dir.getAbsolutePath(), fileRecord.getMessage());
            if (sendFile.exists()) {
                try(BufferedOutputStream fileOS=new BufferedOutputStream(socketFile.getOutputStream())) {
                    printWriter.println(new Message("login",Message.SEND_FILE));
                    printWriter.println(new Message("login", sendFile.getName()));
                    //read file from disk
                    byte[] buffer = Files.readAllBytes(sendFile.toPath());
                    //send file to server
                    fileOS.write(buffer, 0, buffer.length);
                    fileOS.flush();
                    logger.info(message.getLogin()+": Файл отправлен ("+message.getId()+", "+sendFile.getName() +")...");
                } catch (IOException e) {
                    logger.log(Level.ALL,"Ошибка при отправке файла: "+sendFile.getName()+"\n"+e.getStackTrace());
                }
            } else {
                logger.info("Существует только запись о файле: " + fileRecord.getMessage());
                printWriter.println(new Message("login",Message.ERROR));
                printWriter.println(new Message("login", "Существует только запись о файле: " + fileRecord.getMessage()));
            }
        } else {
            logger.info("Файл не существует: " +message.getId());
            printWriter.println(new Message("login",Message.ERROR));
            printWriter.println(new Message("login", "Файл не существует: " +message.getId()));
        }
    }

    private void writeFile() {
        Message message=new Message(scanner.nextLine());
        message.markAsFile();
        String fileName=message.getMessage();

        logger.info(message.getLogin()+": Запись файла ("+fileName+")... ");
        Writer writer=new Writer(fileRecords);

        //read input stream and writing to file
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream();
                BufferedInputStream fileIS=new BufferedInputStream(socketFile.getInputStream())) {
            int result;
            while ((result=fileIS.read())!= -1) {
                buf.write((byte) result);
            }
            //set new file name to message
            message.setMessage(writer.writeFile(dir,fileName,buf));
            //write message about file to fileRecords
            writer.writeMessage(message);
        } catch (IOException e) {
            logger.log(Level.ALL,"Ошибка при записи в буфер: "+message.getMessage()+"\n"+e.getStackTrace());
        }
        logger.info(message.getLogin()+": Файл записан ("+message.getMessage()+").");
    }

    private void getAllMessages(Message message) {
        logger.info(message.getLogin()+": Получение истории сообщений всех пользователей...");
        Parser parser=new Parser(fileRecords);
        List<Message> list=parser.getAllOfMessages();
        if(list!=null) {
            list.forEach(printWriter::println);

            //mark about the end list
            printWriter.println(new Message("login", Message.END));
            logger.info(message.getLogin() + ": История сообщений получена.");
        } else {
            logger.info(message.getLogin() + ": История сообщений пуста.");
            printWriter.println(new Message("login", Message.END));
        }
    }

    private void closeConnection(Message message) {

        try {
            printWriter.close();
            scanner.close();

            socketText.close();
            socketFile.close();
        } catch (IOException e) {
            logger.info("Ошибка при закрытии соединения с "+message.getLogin()+".\n"+e.getStackTrace());
        }
        logger.info(": Соединение с "+message.getLogin()+" закрыто.");

    }

    private void saveMessage(Message message) {
        logger.info(message.getLogin()+": Сохранение сообщения ("+message.getMessage()+")... ");
        Writer writer=new Writer(fileRecords);
        writer.writeMessage(message);
        logger.info(message.getLogin()+": Сообщение сохранено ("+message.getMessage()+").");
    }

    private void deleteRecord(Message message) {
        logger.info(message.getLogin()+": Удаление сообщения ("+message.getId()+")... ");
        Remover remover=new Remover(fileRecords);
        String result=remover.deleteRecord(message,dir);
        printWriter.println(result);
        logger.info(message.getLogin()+": "+result);
    }

    private void getHistoryOfMessages(Message message) {
        logger.info(message.getLogin()+": Получение истории сообщений (файлов)...");
        Parser parser=new Parser(fileRecords);
        List<Message> list=parser.getHistoryOfMessages(message);
        if(list!=null) {
            list.forEach(printWriter::println);

            //mark about the end list
            printWriter.println(new Message("login", Message.END));
            logger.info(message.getLogin() + ": История сообщений (файлов) получена.");
        } else {
            logger.info(message.getLogin() + ": История сообщений (файлов) пуста.");
            printWriter.println(new Message("login", Message.END));
        }
    }
}