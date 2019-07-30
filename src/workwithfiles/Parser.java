package workwithfiles;

import server.Message;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class Parser {
    private File file;
    private Message removedMessage=null;
    private static Logger logger= Logger.getLogger(Parser.class.getName());

    public Parser(File file) {
        this.file = file;
    }

    //indetification next index
    public int nextId(Message message) {
        int id = 1;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            Optional<Message> optional=reader.lines()
                    .filter(s->!(s==null||s.equals("")||s.equals("\n")))
                    .filter(s -> Message.getLogin(s).equals(message.getLogin()))
                    .map(Message::new)
                    .max(Comparator.comparingInt(Message::getId));
            if(optional.isPresent())
                id=optional.get().getId()+1;
        } catch (FileNotFoundException e) {
            logger.log(Level.ALL,"Не найден файл: "+file+"\n"+e.getStackTrace());
        } catch (IOException e) {
            logger.log(Level.ALL,"Ошибка при чтении из файла: "+file+"\n"+e.getStackTrace());
        }

        return id;
    }

    //exclude string with specific id

    public List<String> findAndRemoveString(Message message) {//todo разбить на 2 метода
        List<String> resultMessages=new ArrayList<>();
        List<Message> listMessages=new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<Message> record;

            listMessages= reader.lines().map(Message::new)
                    .collect(Collectors.toList());

            //get record
            record=listMessages.stream()
                    .filter(m -> m.getLogin().equals(message.getLogin()) && m.getId() == message.getId())
                    .collect(Collectors.toList());
            //record exists then delete this record from list
            if (record.size()!=0) {
                 removedMessage=record.get(0);
                 resultMessages=listMessages.stream()
                    .filter(m -> !(m.getLogin().equals(message.getLogin()) && m.getId() == message.getId()))
                    .map(Message::toString)
                    .collect(Collectors.toList());
                 return resultMessages;
            } else
                return null;


        } catch (FileNotFoundException e) {
            logger.log(Level.ALL,"Не найден файл: "+file+"\n"+e.getStackTrace());
        } catch (IOException e) {
            logger.log(Level.ALL,"Ошибка при чтении из файла: "+file+"\n"+e.getStackTrace());
        }
        return null;
    }

    public Message getLastRemovedMessage() {return removedMessage;}

    public List<Message> getHistoryOfMessages(Message message) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            if(file.exists())
                return reader.lines().filter(s -> Message.getLogin(s).equals(message.getLogin())).map(s -> new Message(s)).collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            logger.log(Level.ALL,"Не найден файл: "+file+"\n"+e.getStackTrace());
        } catch (IOException e) {
            logger.log(Level.ALL,"Ошибка при чтении из файла: "+file+"\n"+e.getStackTrace());
        }

        return null;

    }

    public List<Message> getAllOfMessages() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            if(file.exists())
                return reader.lines().map(s -> new Message(s)).collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            logger.log(Level.ALL,"Не найден файл: "+file+"\n"+e.getStackTrace());
        } catch (IOException e) {
            logger.log(Level.ALL,"Ошибка при чтении из файла: "+file+"\n"+e.getStackTrace());
        }

        return null;
    }

    public Message getFileRecord(Message message) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            if(file.exists()) {

                List<String> list = reader.lines()
                        .filter(s -> (Message.getLogin(s).equals(message.getLogin()) && (Message.getId(s) == message.getId())))
                        .collect(Collectors.toList());
                if(list.size()!=0) {
                    List<Message> listMessages=list.stream().map(Message::new).collect(Collectors.toList());
                    return listMessages.get(0);
                }
                //.map(s -> new Message(s)).collect(Collectors.toList());
            } else return null;
        } catch (FileNotFoundException e) {
            logger.log(Level.ALL,"Не найден файл: "+file+"\n"+e.getStackTrace());
        } catch (IOException e) {
            logger.log(Level.ALL,"Ошибка при чтении из файла: "+file+"\n"+e.getStackTrace());
        }

        return null;
    }
}


