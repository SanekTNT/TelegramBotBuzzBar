import org.telegram.telegrambots.api.objects.Message;

public class Admin {

    public boolean nextMessageIsForAllUsers;
    public boolean iaMainAdmin;
    public boolean addingNewAdmin;
    public boolean nextMessageWithNameOfNewAdmin;
    public Message messageToAllUsers;
    public String photoId;
    public String chatIdOfNewAdmin;
    public String nameOfNewAdmin;

    Admin(boolean isMain) {
        nextMessageIsForAllUsers = false;
        iaMainAdmin = isMain;
        addingNewAdmin = false;
        nextMessageWithNameOfNewAdmin = false;
    }
}
