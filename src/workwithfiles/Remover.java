package workwithfiles;

import server.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Remover {

    private File file;
    private static Logger logger= Logger.getLogger(Remover.class.getName());


    public Remover(File file) {
        this.file=file;
    }

    public String deleteRecord(Message message, File dir) {
        Message removedMessage=null;
        if (file.exists()) {
            Parser parser = new Parser(file);
            List<String> list=parser.findAndRemoveString(message);
            if (list!=null) {
                try {
                    Files.write(file.toPath(), (Iterable<String>) list::iterator);
                } catch (IOException e) {
                    logger.log(Level.ALL, "Ошибка при записи в файл: " + file + "\n" + e.getStackTrace());
                }
                removedMessage=parser.getLastRemovedMessage();
                if(removedMessage!=null && removedMessage.isFile()) {
                    File deleteFile=new File(dir.getAbsolutePath(),removedMessage.getMessage());
                    deleteFile.delete();
                    return "Файл был удален (" + message.getId() + ")";
                }
                return "Сообщение было удалено (" + message.getId() + ")";
            }
            else if(removedMessage!=null&&removedMessage.getMessage()!=null) return "Выбранный файл не существует (" + message.getId() + ")";
        }
        return "Выбранное сообщение не существует (" + message.getId() + ")";
    }



}
