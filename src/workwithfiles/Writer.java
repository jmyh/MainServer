package workwithfiles;

import server.Message;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Writer {

    private static Logger logger = Logger.getLogger(Writer.class.getName());
    private File file;

    public Writer(File file) {
        this.file = file;
    }

    public void writeMessage(Message message) {
        //file isExist()
        int id;
        try {
            if (file.exists()) {
                Parser parser = new Parser(file);
                id = parser.nextId(message);
                message.setId(id);
                Files.write(file.toPath(), (message.toString() + "\n").getBytes(), StandardOpenOption.APPEND);
            } else {
                message.setId(1);
                Files.write(file.toPath(), (message.toString() + "\n").getBytes(), StandardOpenOption.CREATE);
            }
        } catch (IOException e) {
            logger.log(Level.ALL, "Ошибка при записи в файл: " + file + "\n" + e.getStackTrace());
        }
    }

    public String writeFile(File dir, String fileName, ByteArrayOutputStream buf) {
        File uploadFile = new File(dir.getAbsolutePath(), fileName);

             if (dir.exists()) {
                 if (uploadFile.exists())
                     uploadFile = new File(uploadFile.getParent(), createNameForDuplicateFile(uploadFile));
             } else dir.mkdir();

            try(FileOutputStream fos=new FileOutputStream(uploadFile)) {
                buf.writeTo(fos);
                buf.close();
            } catch (FileNotFoundException e) {
                logger.log(Level.ALL,"Ошибка при записи файла: "+uploadFile.getName()+"\n"+e.getStackTrace());
            } catch (IOException e) {
                logger.log(Level.ALL,"Ошибка при записи файла: "+uploadFile.getName()+"\n"+e.getStackTrace());
            }
        return uploadFile.getName();
    }

    private String createNameForDuplicateFile(File file) {
        String name[]=new String[2];
        String finalName;
        //check have file name extension
        if(file.getName().contains(".")) {
            name=file.getName().split("\\.");
        } else {
            name[0] = file.getName(); //name
            name[1]=""; //extension
        }
        for(int i=1;i<file.getParentFile().list().length+1;i++) {
            finalName=name[0]+"("+i+")."+name[1];
            if (!(new File(file.getParent(),finalName).exists()))
                return finalName;
            }
        return null;
    }
}

