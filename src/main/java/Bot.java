import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendLocation;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.*;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Math.toIntExact;

public class Bot extends TelegramLongPollingBot {

    private HashMap<String, TableReservation> tableReservationHashMap = new HashMap<>();
    private HashMap<String, Admin> adminHashMap = new HashMap<>();
    private DataBase dataBase = new DataBase();
    private static String BOT_NAME;
    private static String BOT_TOKEN;

    public static void main(String[] args) {
        ApiContextInitializer.init();
        readConfig();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static void readConfig() {
        FileInputStream fis;
        Properties property = new Properties();
        try {
            fis = new FileInputStream("src/main/resources/config.properties");
            property.load(fis);

            BOT_NAME = property.getProperty("BOT_NAME");
            BOT_TOKEN = property.getProperty("BOT_TOKEN");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            switch (message.getText()) {
                case ("/start"): {
                    updateHasMessageWithTextStart(message);
                    break;
                }
                case ("\uD83C\uDF7D Меню"): {
                    updateHasMessageWithTextMenu(message);
                    break;
                }
                case ("\uD83D\uDCD5 Бронь столика"): {
                    updateHasMessageWithTextTableReservation(message);
                    break;
                }
                case ("Отмена"): {
                    updateHasMessageWithTextCancel(message);
                    break;
                }
                case ("❓ Инфо"): {
                    updateHasMessageWithTextInfo(message);
                    break;
                }
                case ("/adminMode"): {
                    updateHasMessageWithTextAdminMode(message);
                    break;
                }
                case ("/adminModeExit"): {
                    updateHasMessageWithTextAdminModeExit(message);
                    break;
                }
                case ("\uD83D\uDCE8 Рассылка"): {
                    updateHasMessageWithTextMailForAllUsers(message);
                    break;
                }
                case ("\uD83D\uDCDA Брони"): {
                    updateHasMessageWithTextTableReservationForAdminMode(message);
                    break;
                }
                case ("\uD83D\uDC6E Админы"): {
                    updateHasMessageWithTextAdmins(message);
                    break;
                }
                default: {
                    updateHasMessageWithTextDefault(message);
                    break;
                }
            }
        }
        else if(update.hasCallbackQuery()) {
            updateHasCallback(update);
        }
        else if(update.hasMessage() && update.getMessage().hasPhoto()) { // messages with photo and caption
            updateHasMessageWithPhoto(update);
        }
        else if(update.hasMessage()
                && (update.getMessage().hasContact()
                || update.getMessage().hasDocument()
                || update.getMessage().hasLocation()
                || update.getMessage().hasVideo())
                )
        {
            Message message = update.getMessage();
            addToUserHashMap(message.getChatId().toString());
            sendMsg(message, "Извини, я не понимаю тебя(", "reply");
        }
    }

    private void updateHasCallback(Update update) {
        Message message = update.getCallbackQuery().getMessage();
        switch (update.getCallbackQuery().getData()) {
            case ("burgers"): {
                printBurgerMenu(message);
                break;
            }
            case ("sandwiches"): {
                printSandwichesMenu(message);
                break;
            }
            case ("wok"): {
                printWokMenu(message);
                break;
            }
            case ("salads"): {
                printSaladMenu(message);
                break;
            }
            case ("forbeer"): {
                printForBeerMenu(message);
                break;
            }
            case ("hookahs"): {
                printHookahsMenu(message);
                break;
            }
            case ("yes"): {
                editMsg(message, "✅ Да");
                sendMsg(message, "Заявка отправлена!\uD83C\uDF89", "");
                dataBase.insertIntoTableOfReservation(message.getChatId().toString(),
                        tableReservationHashMap.get(message.getChatId().toString()).name,
                        tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces,
                        tableReservationHashMap.get(message.getChatId().toString()).date,
                        tableReservationHashMap.get(message.getChatId().toString()).time,
                        tableReservationHashMap.get(message.getChatId().toString()).phone,
                        tableReservationHashMap.get(message.getChatId().toString()).wishes);
                sendMsg(message, "✅ Бронирование!"
                                + "\nChatId: " + tableReservationHashMap.get(message.getChatId().toString()).userId
                                + "\nИмя: " + tableReservationHashMap.get(message.getChatId().toString()).name
                                + "\nЧисло мест: " + tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces
                                + "\nДата: " + tableReservationHashMap.get(message.getChatId().toString()).date
                                + "\nВремя: " + tableReservationHashMap.get(message.getChatId().toString()).time
                                + "\nТелефон: " + tableReservationHashMap.get(message.getChatId().toString()).phone
                                + "\nПожелания: " + tableReservationHashMap.get(message.getChatId().toString()).wishes,
                        "toAllAdmins");
                break;
            }
            case ("no"): {
                editMsg(message, "❌ Нет");
                sendMsg(message, "Заявка не была отправлена. Очень жаль\uD83D\uDE22", "");
                tableReservationHashMap.remove(message.getChatId().toString());
                break;
            }
            case ("allIsOk"): {
                editMsg(message, "✅ Просто посмотреть ✅");
                sendMsg(message, "Ну хорошо\uD83D\uDE11", "");
                break;
            }
            case ("editReservation"): {
                editMsg(message, "\uD83D\uDCDD Редактировать бронь \uD83D\uDCDD");
                sendMsg(message, "Что бы Вы хотели изменить?", "editCell");
                break;
            }
            case("cancelReservation"): {
                editMsg(message, "❌ Отменить бронь ❌");
                sendMsg(message, "Вы уверены?", "applicationForCancelReservation");
                break;
            }
            case("yesForCancelReservation"): {
                editMsg(message, "✅ Да");
                sendMsg(message, "Заявка на бронь отменена. Очень жаль\uD83D\uDE22", "");
                ArrayList<String> tableReservationArray
                        = dataBase.selectOneTableReservation(message.getChatId().toString());
                sendMsg(message, "❌ Отмена брони!"
                                + "\nChatId: " + tableReservationArray.get(6)
                                + "\nИмя: " + tableReservationArray.get(0)
                                + "\nЧисло мест: " + tableReservationArray.get(1)
                                + "\nДата: " + tableReservationArray.get(2)
                                + "\nВремя: " + tableReservationArray.get(3)
                                + "\nТелефон: " + tableReservationArray.get(4)
                                + "\nПожелания: " + tableReservationArray.get(5),
                        "toAllAdmins");
                dataBase.deleteFromTable("tablereservation", message.getChatId().toString());
                tableReservationHashMap.remove(message.getChatId().toString());
                break;
            }
            case("noForCancelReservation"): {
                editMsg(message, "❌ Нет");
                sendMsg(message, "Смотри мне\uD83D\uDE11", "");
                break;
            }
            case ("yesForReservation"): {
                editMsg(message, "✅ Да");
                sendMsg(message, "Обновленная заявка отправлена!\uD83C\uDF89", "");
                //next msg to all admins
                sendMsg(message, "\uD83D\uDCDD  Редактирование брони!"
                                + "\nChatId: " + tableReservationHashMap.get(message.getChatId().toString()).userId
                                + "\nИмя: " + tableReservationHashMap.get(message.getChatId().toString()).name
                                + "\nЧисло мест: " + tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces
                                + "\nДата: " + tableReservationHashMap.get(message.getChatId().toString()).date
                                + "\nВремя: " + tableReservationHashMap.get(message.getChatId().toString()).time
                                + "\nТелефон: " + tableReservationHashMap.get(message.getChatId().toString()).phone
                                + "\nПожелания: " + tableReservationHashMap.get(message.getChatId().toString()).wishes,
                        "toAllAdmins");
                checkAllTableReservationForArrowSymbolLast(message);
                dataBase.deleteFromTable("tablereservation", message.getChatId().toString());
                dataBase.insertIntoTableOfReservation(message.getChatId().toString(),
                        tableReservationHashMap.get(message.getChatId().toString()).name,
                        tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces,
                        tableReservationHashMap.get(message.getChatId().toString()).date,
                        tableReservationHashMap.get(message.getChatId().toString()).time,
                        tableReservationHashMap.get(message.getChatId().toString()).phone,
                        tableReservationHashMap.get(message.getChatId().toString()).wishes);
                break;
            }
            case ("noForReservation"): {
                editMsg(message, "❌ Нет");
                sendMsg(message, "Измененная заявка не была отправлена.", "");
                checkAllTableReservationForArrowSymbolFirst(message);
                break;
            }
            case ("editReservationAfterChange"): {
                editMsg(message, "\uD83D\uDCDD Изменить что-то еще");
                sendMsg(message, "Что бы Вы еще хотели изменить?", "editCell");
                break;
            }
            case ("editName"): {
                editMsg(message, "Имя");
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 7;
                sendMsg(message, "Укажите новое имя.", "");
                break;
            }
            case ("editNumberOfPlaces"): {
                editMsg(message, "Число мест");
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 8;
                sendMsg(message, "Укажите новое число мест.", "");
                break;
            }
            case ("editDate"): {
                editMsg(message, "Дата");
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 9;
                sendMsg(message, "Укажите новою дату.", "");
                break;
            }
            case ("editTime"): {
                editMsg(message, "Время");
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 10;
                sendMsg(message, "Укажите новое время.", "");
                break;
            }
            case ("editPhone"): {
                editMsg(message, "Телефон");
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 11;
                sendMsg(message, "Укажите новый телефон.", "");
                break;
            }
            case ("editWishes"): {
                editMsg(message, "Пожелания");
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 12;
                sendMsg(message, "Укажите новые пожелания.", "");
                break;
            }
            case ("yesForSendAdsForAllUsers"): {
                editMsg(message, "✅ Да");
                sendMsg(adminHashMap.get(message.getChatId().toString()).messageToAllUsers,
                        adminHashMap.get(message.getChatId().toString()).messageToAllUsers.getText(), "toAllUsers");
                adminHashMap.get(message.getChatId().toString()).messageToAllUsers = null;
                sendMsg(message, "Рассылка произошла успешно.", "");
                break;
            }
            case ("noForSendAdsForAllUsers"): {
                editMsg(message, "❌ Нет");
                adminHashMap.get(message.getChatId().toString()).messageToAllUsers = null;
                sendMsg(message, "Рассылка отменена.", "");
                break;
            }
            case ("yesForSendAdsForAllUsersWithPhoto"): {
                editMsg(message, "✅ Да");
                sendPht(message, adminHashMap.get(message.getChatId().toString()).messageToAllUsers.getCaption(),
                        adminHashMap.get(message.getChatId().toString()).photoId, "toAllUsers");
                adminHashMap.get(message.getChatId().toString()).messageToAllUsers = null;
                adminHashMap.get(message.getChatId().toString()).photoId = null;
                sendMsg(message, "Рассылка произошла успешно.", "");
                break;
            }
            case ("noForSendAdsForAllUsersWithPhoto"): {
                editMsg(message, "❌ Нет");
                adminHashMap.get(message.getChatId().toString()).messageToAllUsers = null;
                adminHashMap.get(message.getChatId().toString()).photoId = null;
                sendMsg(message, "Рассылка отменена.", "");
                break;
            }
            case ("addAdminInDataBase"): {
                editMsg(message, "✅ Да");
                adminHashMap.get(message.getChatId().toString()).addingNewAdmin = true;
                sendMsg(message,
                        "Введите id нового администратора. Его можно узнать с помощью бота @ShowJsonBot. "
                                + "Для этого отправьте ему любое сообщение с аккаунта нового администратора.",
                        "");
                break;
            }
            case ("newAdminIsMainAdmin"): {
                editMsg(message, "Главный администратор\uD83D\uDC68\u200D✈️");
                dataBase.insertIntoTableAdmins(adminHashMap.get(message.getChatId().toString()).chatIdOfNewAdmin,
                        true, adminHashMap.get(message.getChatId().toString()).nameOfNewAdmin);
                adminHashMap.get(message.getChatId().toString()).addingNewAdmin = false;
                sendMsg(message, "Главный администратор "
                        + adminHashMap.get(message.getChatId().toString()).chatIdOfNewAdmin
                        + " добавлен\uD83D\uDC68\u200D✈️", "");
                adminHashMap.get(message.getChatId().toString()).chatIdOfNewAdmin = null;
                adminHashMap.get(message.getChatId().toString()).nameOfNewAdmin = null;
                break;
            }
            case ("newAdminIsNotMainAdmin"): {
                editMsg(message, "Обычный администратор\uD83D\uDC6E");
                dataBase.insertIntoTableAdmins(adminHashMap.get(message.getChatId().toString()).chatIdOfNewAdmin,
                        false, adminHashMap.get(message.getChatId().toString()).nameOfNewAdmin);
                adminHashMap.get(message.getChatId().toString()).addingNewAdmin = false;
                sendMsg(message, "Обычный администратор "
                        + adminHashMap.get(message.getChatId().toString()).chatIdOfNewAdmin
                        + " добавлен\uD83D\uDC6E", "");
                adminHashMap.get(message.getChatId().toString()).chatIdOfNewAdmin = null;
                adminHashMap.get(message.getChatId().toString()).nameOfNewAdmin = null;
                break;
            }
            default:{
                switch (update.getCallbackQuery().getData().substring(0, 11)) {
                    case("makeAdminNo"): {
                        editMsg(message, "⬇️ Понизить");
                        dataBase.changeColumnIsAdminMainInAdminsTable(
                                update.getCallbackQuery().getData().substring(
                                        update.getCallbackQuery().getData().indexOf("_") + 1,
                                        update.getCallbackQuery().getData().length()),
                                false
                        );
                        sendMsg(message, "Администратор понижен.", "");
                        adminHashMap.remove(update.getCallbackQuery().getData().substring(
                                update.getCallbackQuery().getData().indexOf("_") + 1,
                                update.getCallbackQuery().getData().length()));
                        break;
                    }
                    case("makeAdminMa"): {
                        editMsg(message, "⬆️ Повысить");
                        dataBase.changeColumnIsAdminMainInAdminsTable(
                                update.getCallbackQuery().getData().substring(
                                        update.getCallbackQuery().getData().indexOf("_") + 1,
                                        update.getCallbackQuery().getData().length()),
                                true
                        );
                        sendMsg(message, "Администратор повышен.", "");
                        adminHashMap.remove(update.getCallbackQuery().getData().substring(
                                update.getCallbackQuery().getData().indexOf("_") + 1,
                                update.getCallbackQuery().getData().length()));
                        break;
                    }
                    case("deleteAdmin"): {
                        editMsg(message, "❌ Удалить");
                        dataBase.deleteFromTable("admins", update.getCallbackQuery().getData().substring(
                                update.getCallbackQuery().getData().indexOf("_") + 1,
                                update.getCallbackQuery().getData().length()));
                        sendMsg(message, "Администратор удален.", "");
                        break;
                    }
                }
                break;
            }
        }
    }

    private void updateHasMessageWithTextStart(Message message) {
        addToUserHashMap(message.getChatId().toString());
        sendMsg(message, "Выберите раздел:", "");
        if(tableReservationHashMap.containsKey(message.getChatId().toString())) {
            if (tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells < 6)
                tableReservationHashMap.remove(message.getChatId().toString());
            else {
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
            }
        }
    }

    private void updateHasMessageWithTextMenu(Message message) {
        addToUserHashMap(message.getChatId().toString());
        sendMsg(message, "Выберите категорию меню:", "menu");
        if(tableReservationHashMap.containsKey(message.getChatId().toString())) {
            if (tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells < 6)
                tableReservationHashMap.remove(message.getChatId().toString());
            else {
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
            }
        }
    }

    private void updateHasMessageWithTextTableReservation(Message message) {
        addToUserHashMap(message.getChatId().toString());
        if(!dataBase.existsChatIdInTable("tablereservation", message.getChatId().toString())){
            tableReservationHashMap.put(message.getChatId().toString(),
                    new TableReservation(message.getChatId().toString()));
            sendMsg(message, "Прекрасный выбор!", "");
            sendMsg(message, "Как Вас зовут?", "");
        }
        else if(dataBase.existsChatIdInTable("tablereservation", message.getChatId().toString())) {
            // Загоняем из базы данных в tableReservationHashMap
            if(!tableReservationHashMap.containsKey(message.getChatId().toString())) {
                tableReservationHashMap.put(message.getChatId().toString(),
                        new TableReservation(message.getChatId().toString()));
                ArrayList<String> tableReservationArray
                        = dataBase.selectOneTableReservation(message.getChatId().toString());
                tableReservationHashMap.get((message.getChatId().toString())).name = tableReservationArray.get(0);
                tableReservationHashMap.get((message.getChatId().toString())).numberOfPlaces = tableReservationArray.get(1);
                tableReservationHashMap.get((message.getChatId().toString())).date = tableReservationArray.get(2);
                tableReservationHashMap.get((message.getChatId().toString())).time = tableReservationArray.get(3);
                tableReservationHashMap.get((message.getChatId().toString())).phone = tableReservationArray.get(4);
                tableReservationHashMap.get((message.getChatId().toString())).wishes = tableReservationArray.get(5);
                tableReservationHashMap.get((message.getChatId().toString())).userId = message.getChatId().toString();
                tableReservationHashMap.get((message.getChatId().toString())).numberOfCompletedSells = 6;
            }

            if(tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells == 6) {
                sendMsg(message, "У Вас уже есть бронь:"
                                + "\nИмя: " + tableReservationHashMap.get(message.getChatId().toString()).name
                                + "\nЧисло мест: " + tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces
                                + "\nДата: " + tableReservationHashMap.get(message.getChatId().toString()).date
                                + "\nВремя: " + tableReservationHashMap.get(message.getChatId().toString()).time
                                + "\nТелефон: " + tableReservationHashMap.get(message.getChatId().toString()).phone
                                + "\nПожелания: " + tableReservationHashMap.get(message.getChatId().toString()).wishes,
                        "");
                sendMsg(message, "Что бы вы хотели сделать?", "editReservation");
            }
        }
    }

    private void updateHasMessageWithTextCancel(Message message) {
        if(tableReservationHashMap.containsKey(message.getChatId().toString())) {
            if(tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells < 6) {
                tableReservationHashMap.remove(message.getChatId().toString());
                sendMsg(message, "Бронь столика отменена.", "");
            }
            else if(tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells > 6) {
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
                sendMsg(message, "Изменения отменены.", "");
            }
            else if(adminHashMap.containsKey(message.getChatId().toString())
                    && adminHashMap.get(message.getChatId().toString()).addingNewAdmin) {
                adminHashMap.get(message.getChatId().toString()).addingNewAdmin = false;
                adminHashMap.get(message.getChatId().toString()).chatIdOfNewAdmin = null;
                sendMsg(message, "Операция добавления отменена.", "");
            }
            else if(adminHashMap.containsKey(message.getChatId().toString())
                    && adminHashMap.get(message.getChatId().toString()).nextMessageIsForAllUsers) {
                adminHashMap.get(message.getChatId().toString()).nextMessageIsForAllUsers = false;
                adminHashMap.get(message.getChatId().toString()).messageToAllUsers = null;
                sendMsg(message, "Рассылка отменена.", "");
            }
        }
        else if(adminHashMap.containsKey(message.getChatId().toString())
                && adminHashMap.get(message.getChatId().toString()).addingNewAdmin) {
            adminHashMap.get(message.getChatId().toString()).addingNewAdmin = false;
            sendMsg(message, "Операция добавления отменена.", "");
        }
        else if(adminHashMap.containsKey(message.getChatId().toString())
                && adminHashMap.get(message.getChatId().toString()).nextMessageIsForAllUsers) {
            adminHashMap.get(message.getChatId().toString()).nextMessageIsForAllUsers = false;
            adminHashMap.get(message.getChatId().toString()).messageToAllUsers = null;
            sendMsg(message, "Рассылка отменена.", "");
        }
        else {
            addToUserHashMap(message.getChatId().toString());
            sendMsg(message, "Извини, я не понимаю тебя(", "reply");
        }
    }

    private void updateHasMessageWithTextInfo(Message message) {
        addToUserHashMap(message.getChatId().toString());
        sendMsg(message, "Примеры работы:", "");
        sendPht(message, "Бот реализует возможность бронирования столика. " +
                "Бронь отправляется им же на аккаунт администратора",
                "AgADAgADo6kxGyXLIUkegJWRIGVt-5nRtw4ABL8doNbTCEJrWbYDAAEC", "");
        sendPht(message, "Отправленную бронь можно изменить или отменить. " +
                        "Все результаты отправляются также администраторам",
                "AgADAgADpKkxGyXLIUlWZD_Q3sl12czOtw4ABL_vdHuja_-JULYDAAEC", "");
        sendPht(message, "Отправленную бронь можно изменить или отменить. " +
                        "Все результаты отправляются также администраторам",
                "AgADAgADpKkxGyXLIUlWZD_Q3sl12czOtw4ABL_vdHuja_-JULYDAAEC", "");
        sendPht(message, "Специальный режим администратора, с помощью которого можно делать рассылки всем пользователям, "
                        + "просматривать брони, редактировать меню и так далее!",
                "AgADAgADpakxGyXLIUnPwx_mORwqmg7ftw4ABEF3zwABRchiLPy1AwABAg", "");
        sendPht(message, "Пример добавления нового админа",
                "AgADAgADpqkxGyXLIUk3uksp4FbKQlofrQ4ABLHe6LeupuZnLCsGAAEC", "");
        sendMsg(message, "Бот был разботан https://t.me/sanektnt, "
                + "за пару шекелей могу написать вам такого же или даже лучше)", "");

        /*sendMsg(message, "Место, где можно просто расслабиться: " +
                "авторские коктейли\uD83C\uDF79, настольные игры\uD83C\uDFB2, вкусная еда\uD83C\uDF54 и демократичные цены\uD83D\uDCB0!", "");
        sendMsg(message, "\uD83D\uDCDDГрафик работы:\n" +
                "\nПонедельник\n" + "\uD83D\uDD5215:00–23:00\uD83D\uDD5A\n" +
                "\nВторник\n" + "\uD83D\uDD5215:00–23:00\uD83D\uDD5A\n" +
                "\nСреда\n" + "\uD83D\uDD5215:00–23:00\uD83D\uDD5A\n" +
                "\nЧетверг\n" + "\uD83D\uDD5215:00–23:00\uD83D\uDD5A\n" +
                "\nПятница\n" + "\uD83D\uDD5215:00–02:00\uD83D\uDD51\n" +
                "\nСуббота\n" + "\uD83D\uDD5215:00–02:00\uD83D\uDD51\n" +
                "\nВоскресенье\n" + "\uD83D\uDD5215:00–23:00\uD83D\uDD5A\n" +
                "\nТелефон: + 38 050 605 9805", "");
        sendLct(message);
        */
        if(tableReservationHashMap.containsKey(message.getChatId().toString())) {
            if (tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells < 6)
                tableReservationHashMap.remove(message.getChatId().toString());
            else {
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
            }
        }
    }

    private void updateHasMessageWithTextAdminMode(Message message) {
        if(tableReservationHashMap.containsKey(message.getChatId().toString())) {
            if (tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells < 6)
                tableReservationHashMap.remove(message.getChatId().toString());
            else {
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
            }
        }
        if(message.getChatId().toString().equals("333179535")){
            if(!dataBase.existsChatIdInTable("admins", message.getChatId().toString())) {
                dataBase.insertIntoTableAdmins(message.getChatId().toString(),
                        true,
                        "Александр");
            }
        }
        if(dataBase.existsChatIdInTable("admins", message.getChatId().toString())) {
            adminHashMap.put(message.getChatId().toString(),
                    new Admin(dataBase.isAdminMain(message.getChatId().toString())));
            sendMsg(message, "Ты внутри системы\uD83D\uDE0E", "");
        }
        else
            sendMsg(message, "Откуда Вы узнали эту команду?\uD83D\uDE11", "reply");
    }

    private void updateHasMessageWithTextAdminModeExit(Message message) {
        if(tableReservationHashMap.containsKey(message.getChatId().toString())) {
            if (tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells < 6)
                tableReservationHashMap.remove(message.getChatId().toString());
            else {
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
            }
        }
        deleteFromAdminHashMap(message);
    }

    private void updateHasMessageWithTextMailForAllUsers(Message message) {
        if(tableReservationHashMap.containsKey(message.getChatId().toString())) {
            if (tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells < 6)
                tableReservationHashMap.remove(message.getChatId().toString());
            else {
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
            }
        }
        if(adminHashMap.containsKey(message.getChatId().toString())) {
            adminHashMap.get(message.getChatId().toString()).nextMessageIsForAllUsers = true;
            sendMsg(message, "Введите сообщение, которое отправится всем " +
                    "пользователям, использовавшие BUZZ bar bot.", "");
        }
        else {
            addToUserHashMap(message.getChatId().toString());
            sendMsg(message, "Извини, я не понимаю тебя(", "reply");
        }
    }

    private void updateHasMessageWithTextTableReservationForAdminMode(Message message) {
        if(tableReservationHashMap.containsKey(message.getChatId().toString())) {
            if (tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells < 6)
                tableReservationHashMap.remove(message.getChatId().toString());
            else {
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
            }
        }
        if(adminHashMap.containsKey(message.getChatId().toString())) {
            ArrayList<String> usersIdInTableReservation = dataBase.selectChatIdFromTable("tablereservation");
            if(usersIdInTableReservation.isEmpty()) {
                sendMsg(message, "Пусто\uD83D\uDE13", "");
            }
            else {
                ArrayList<ArrayList<String>> allTableReservations = dataBase.selectAllFromTableReservation();
                //sort array list
                for(int i = allTableReservations.size()-1; i>0; i--) {
                    for(int j=0 ; j<i ; j++) {
                        if(dateOneEqualsDateTwo(allTableReservations.get(j).get(2),
                                allTableReservations.get(j+1).get(2))) {
                            if(!dateOneIsAfterDateTwo(allTableReservations.get(j).get(3),
                                    allTableReservations.get(j+1).get(3), "HH.mm")) {
                                ArrayList<String> temp = allTableReservations.get(j);
                                allTableReservations.set(j, allTableReservations.get(j+1));
                                allTableReservations.set(j+1, temp);
                            }
                        }
                        else if(!dateOneIsAfterDateTwo(allTableReservations.get(j).get(2),
                                allTableReservations.get(j+1).get(2), "dd.MM")) {
                            ArrayList<String> temp = allTableReservations.get(j);
                            allTableReservations.set(j, allTableReservations.get(j+1));
                            allTableReservations.set(j+1, temp);
                        }
                    }
                }
                for (ArrayList<String> oneTableReservation : allTableReservations) {
                    sendMsg(message, "ChatId: " + oneTableReservation.get(6)
                                    + "\nИмя: " + oneTableReservation.get(0)
                                    + "\nЧисло мест: " + oneTableReservation.get(1)
                                    + "\nДата: " + oneTableReservation.get(2)
                                    + "\nВремя: " + oneTableReservation.get(3)
                                    + "\nТелефон: " + oneTableReservation.get(4)
                                    + "\nПожелания: " + oneTableReservation.get(5),
                            "");
                }
            }
        }
        else {
            addToUserHashMap(message.getChatId().toString());
            sendMsg(message, "Извини, я не понимаю тебя(", "reply");
        }
    }

    private void updateHasMessageWithTextAdmins(Message message) {
        if(tableReservationHashMap.containsKey(message.getChatId().toString())) {
            if (tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells < 6)
                tableReservationHashMap.remove(message.getChatId().toString());
            else {
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
            }
        }
        if(adminHashMap.containsKey(message.getChatId().toString())) {
            if(adminHashMap.get(message.getChatId().toString()).iaMainAdmin) {
                sendMsg(message, "Ваши администраторы:", "");
                ArrayList<String> allAdmins = dataBase.selectChatIdFromTable("admins");
                for (String chatID : allAdmins) {
                    String key = "admin_" + chatID + "_" + dataBase.isAdminMain(chatID);
                    sendMsg(message, "Id: " + chatID
                            + "\nИмя: " + dataBase.selectNameOfAdmin(chatID)
                            + "\nГлавный админ: " + dataBase.isAdminMain(chatID), key);
                }
                sendMsg(message, "Добавить нового администратора?", "addNewAdmin");
            }
            else
                sendMsg(message, "У Вас нет прав использовать эту команду\uD83D\uDE43","");
        }
        else {
            addToUserHashMap(message.getChatId().toString());
            sendMsg(message, "Извини, я не понимаю тебя(", "reply");
        }
    }

    private void updateHasMessageWithTextDefault(Message message) {
        if(adminHashMap.containsKey(message.getChatId().toString())) {
            if(adminHashMap.get(message.getChatId().toString()).nextMessageIsForAllUsers) {
                if (tableReservationHashMap.containsKey(message.getChatId().toString())) {
                    if (tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells < 6)
                        tableReservationHashMap.remove(message.getChatId().toString());
                    else {
                        tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
                    }
                }
                adminHashMap.get(message.getChatId().toString()).nextMessageIsForAllUsers = false;
                sendMsg(message, message.getText(), "");
                adminHashMap.get(message.getChatId().toString()).messageToAllUsers = message;
                sendMsg(message, "Вы уверены, что хотите отправить это сообщение всем пользователям?",
                        "setApplicationForSendAdsForAllUsers");
            }
            else if(adminHashMap.get(message.getChatId().toString()).addingNewAdmin) {
                if(adminHashMap.get(message.getChatId().toString()).nameOfNewAdmin == null) {
                    if(adminHashMap.get(message.getChatId().toString()).nextMessageWithNameOfNewAdmin) {
                        adminHashMap.get(message.getChatId().toString()).nextMessageWithNameOfNewAdmin = false;
                        adminHashMap.get(message.getChatId().toString()).nameOfNewAdmin = message.getText();
                        sendMsg(message, "Выберите должность нового администратора: ", "chooseIsMainAdmin");
                    }
                    else {
                        adminHashMap.get(message.getChatId().toString()).chatIdOfNewAdmin = message.getText();
                        sendMsg(message, "Введите имя нового администратора.", "");
                        adminHashMap.get(message.getChatId().toString()).nextMessageWithNameOfNewAdmin = true;
                    }
                }
                else {
                    adminHashMap.get(message.getChatId().toString()).nameOfNewAdmin = message.getText();
                    sendMsg(message, "Выберите должность нового администратора: ", "chooseIsMainAdmin");
                }
            }
            else if(tableReservationHashMap.containsKey(message.getChatId().toString())) {
                if(tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells == 6) {
                    sendMsg(message, "Извини, я не понимаю тебя(", "reply");
                }
                else
                    setTableReservation(message);
            }
            else {
                addToUserHashMap(message.getChatId().toString());
                sendMsg(message, "Извини, я не понимаю тебя(", "reply");
            }
        }
        else if(tableReservationHashMap.containsKey(message.getChatId().toString())) {
            if(tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells == 6) {
                sendMsg(message, "Извини, я не понимаю тебя(", "reply");
            }
            else
                setTableReservation(message);
        }
        else {
            addToUserHashMap(message.getChatId().toString());
            sendMsg(message, "Извини, я не понимаю тебя(", "reply");
        }
    }

    private void updateHasMessageWithPhoto(Update update) {
        Message message = update.getMessage();
        /*List<PhotoSize> photos = update.getMessage().getPhoto();
        String f_id = photos.stream().sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
            .findFirst().orElse(null).getFileId();
        sendPht(message, "Ответное фото, id: " + f_id, f_id, "");
        */
        if (adminHashMap.containsKey(message.getChatId().toString())
                && adminHashMap.get(message.getChatId().toString()).nextMessageIsForAllUsers){
            List<PhotoSize> photos = update.getMessage().getPhoto();
            String f_id = photos.stream().sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                    .findFirst().orElse(null).getFileId();
            adminHashMap.get(message.getChatId().toString()).messageToAllUsers = message;
            adminHashMap.get(message.getChatId().toString()).photoId = f_id;
            sendPht(message, message.getCaption(), f_id, "setApplicationForSendAdsForAllUsersWithPhoto");
            adminHashMap.get(message.getChatId().toString()).nextMessageIsForAllUsers = false;
            sendMsg(message, "Вы уверены, что хотите отправить это сообщение всем пользователям?",
                    "setApplicationForSendAdsForAllUsersWithPhoto");
        }
        else {
            addToUserHashMap(message.getChatId().toString());
            sendMsg(message, "Извини, я не понимаю тебя(", "reply");
        }
    }

    public void sendMsg(Message message, String s, String key) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(s);
        try {
            setButtonsKeyboard(sendMessage);
            switch (key) {
                case ("menu"): {
                    setButtonsMenu(sendMessage);
                    break;
                }
                case ("reply"): {
                    sendMessage.setReplyToMessageId(message.getMessageId());
                    break;
                }
                case ("application"): {
                    setApplicationForTableReservation(sendMessage);
                    break;
                }
                case ("editReservation"): {
                    editReservation(sendMessage);
                    break;
                }
                case ("editCell"): {
                    editCurrentSellInReservation(sendMessage);
                    break;
                }
                case ("applicationForOtherReservation"): {
                    setApplicationForOtherReservation(sendMessage);
                    break;
                }
                case ("applicationForCancelReservation"): {
                    setApplicationForCancelReservation(sendMessage);
                    break;
                }
                case ("toAllUsers"): {
                    ArrayList<String> allUsers = dataBase.selectChatIdFromTable("users");
                    for(String chatID : allUsers) {
                        sendMessage.setChatId(chatID);
                        setButtonsKeyboard(sendMessage);
                        sendMessage(sendMessage);
                    }
                    break;
                }
                case ("toAllAdmins"): {
                    //ArrayList<String> allAdmins = dataBase.selectChatIdFromTable("admins");
                    for(String chatID : adminHashMap.keySet()) {
                        sendMessage.setChatId(chatID);
                        setButtonsKeyboard(sendMessage);
                        //sendMsg(message, s, "answerPersonallyToTheUser");
                        sendMsg(message, s, "");
                    }
                    break;
                }
                case ("setApplicationForSendAdsForAllUsers"): {
                    setApplicationForSendAdsForAllUsers(sendMessage);
                    break;
                }
                case ("setApplicationForSendAdsForAllUsersWithPhoto"): {
                    setApplicationForSendAdsForAllUsersWithPhoto(sendMessage);
                    break;
                }
                case ("addNewAdmin"): {
                    addAdminInDataBase(sendMessage);
                    break;
                }
                case ("chooseIsMainAdmin"): {
                    chooseIsMainAdmin(sendMessage);
                    break;
                }
                default :{
                    if(key.length() != 0) {
                        if (key.substring(0, 6).equals("admin_"))
                            curAdminInDataBase(sendMessage, key);
                        break;
                    }
                }
            }
            if (!key.equals("toAllUsers") && !key.equals("toAllAdmins"))
                sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void editMsg(Message message, String s) {
        EditMessageText new_message = new EditMessageText();
        new_message.setChatId(message.getChatId().toString());
        new_message.setMessageId(toIntExact(message.getMessageId()));
        new_message.setText(s);
        try {
            execute(new_message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendPht(Message message, String s, String file_id, String key) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(message.getChatId().toString());
        sendPhoto.setCaption(s);
        if(!file_id.equals(""))
            sendPhoto.setPhoto(file_id);
        try {
            if(key.equals("toAllUsers"))
            {
                ArrayList<String> allUsers = dataBase.selectChatIdFromTable("users");
                for(String chatID : allUsers) {
                    sendPhoto.setChatId(chatID);
                    sendPhoto(sendPhoto);
                }
            }
            else
                sendPhoto(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendLct(Message message) {
        float lat = (float) 50.4670212;
        float lon = (float) 30.5107791;
        SendLocation sendLocation = new SendLocation();
        sendLocation.setChatId(message.getChatId());
        sendLocation.setLatitude(lat);
        sendLocation.setLongitude(lon);
        try {
            sendLocation(sendLocation);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private synchronized void setButtonsKeyboard(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        if(tableReservationHashMap.containsKey(sendMessage.getChatId())) {
            if(tableReservationHashMap.get(sendMessage.getChatId()).numberOfCompletedSells != 6) {
                keyboardRow1.add(new KeyboardButton("Отмена"));
                keyboard.add(keyboardRow1);
            }
            else if(adminHashMap.containsKey(sendMessage.getChatId())
                    && adminHashMap.get(sendMessage.getChatId()).addingNewAdmin) {
                keyboardRow1.add(new KeyboardButton("Отмена"));
                keyboard.add(keyboardRow1);
            }
            else if(adminHashMap.containsKey(sendMessage.getChatId())
                    && adminHashMap.get(sendMessage.getChatId()).nextMessageIsForAllUsers) {
                keyboardRow1.add(new KeyboardButton("Отмена"));
                keyboard.add(keyboardRow1);
            }
            else {
                keyboardRow1.add(new KeyboardButton("\uD83C\uDF7D Меню"));
                keyboardRow1.add(new KeyboardButton("\uD83D\uDCD5 Бронь столика"));
                keyboardRow1.add(new KeyboardButton("❓ Инфо"));
                keyboard.add(keyboardRow1);

                if (adminHashMap.containsKey(sendMessage.getChatId())) {
                    if(adminHashMap.get(sendMessage.getChatId()).iaMainAdmin) {
                        KeyboardRow keyboardRow2 = new KeyboardRow();
                        keyboardRow2.add(new KeyboardButton("\uD83D\uDCE8 Рассылка"));
                        keyboardRow2.add(new KeyboardButton("\uD83D\uDCDA Брони"));
                        keyboardRow2.add(new KeyboardButton("\uD83D\uDC6E Админы"));
                        keyboard.add(keyboardRow2);
                    }
                    else {
                        KeyboardRow keyboardRow2 = new KeyboardRow();
                        //keyboardRow2.add(new KeyboardButton("\uD83D\uDCE8 Рассылка"));
                        keyboardRow2.add(new KeyboardButton("\uD83D\uDCDA Брони"));
                        //keyboardRow2.add(new KeyboardButton("\uD83D\uDC6E Админы"));
                        keyboard.add(keyboardRow2);
                    }
                }
            }
        }
        else if(adminHashMap.containsKey(sendMessage.getChatId())
                && adminHashMap.get(sendMessage.getChatId()).addingNewAdmin) {
            keyboardRow1.add(new KeyboardButton("Отмена"));
            keyboard.add(keyboardRow1);
        }
        else if(adminHashMap.containsKey(sendMessage.getChatId())
                && adminHashMap.get(sendMessage.getChatId()).nextMessageIsForAllUsers) {
            keyboardRow1.add(new KeyboardButton("Отмена"));
            keyboard.add(keyboardRow1);
        }
        else {
            keyboardRow1.add(new KeyboardButton("\uD83C\uDF7D Меню"));
            keyboardRow1.add(new KeyboardButton("\uD83D\uDCD5 Бронь столика"));
            keyboardRow1.add(new KeyboardButton("❓ Инфо"));
            keyboard.add(keyboardRow1);

            if (adminHashMap.containsKey(sendMessage.getChatId())) {
                if(adminHashMap.get(sendMessage.getChatId()).iaMainAdmin) {
                    KeyboardRow keyboardRow2 = new KeyboardRow();
                    keyboardRow2.add(new KeyboardButton("\uD83D\uDCE8 Рассылка"));
                    keyboardRow2.add(new KeyboardButton("\uD83D\uDCDA Брони"));
                    keyboardRow2.add(new KeyboardButton("\uD83D\uDC6E Админы"));
                    keyboard.add(keyboardRow2);
                }
                else {
                    KeyboardRow keyboardRow2 = new KeyboardRow();
                    //keyboardRow2.add(new KeyboardButton("\uD83D\uDCE8 Рассылка"));
                    keyboardRow2.add(new KeyboardButton("\uD83D\uDCDA Брони"));
                    //keyboardRow2.add(new KeyboardButton("\uD83D\uDC6E Админы"));
                    keyboard.add(keyboardRow2);
                }
            }
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    private void setButtonsMenu(SendMessage sendMessage) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("\uD83C\uDF54 Бургеры \uD83C\uDF54").setCallbackData("burgers"));
        rowInline1.add(new InlineKeyboardButton().setText("\uD83C\uDF2E Cэндвичи \uD83C\uDF2E").setCallbackData("sandwiches"));
        rowsInline.add(rowInline1);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(new InlineKeyboardButton().setText("\uD83C\uDF72 Wok \uD83C\uDF72").setCallbackData("wok"));
        rowInline2.add(new InlineKeyboardButton().setText("\uD83E\uDD57 Салаты \uD83E\uDD57").setCallbackData("salads"));
        rowsInline.add(rowInline2);

        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        rowInline3.add(new InlineKeyboardButton().setText("\uD83C\uDF7A К пиву \uD83C\uDF7A").setCallbackData("forbeer"));
        rowInline3.add(new InlineKeyboardButton().setText("\uD83D\uDCA8 Кальяны \uD83D\uDCA8").setCallbackData("hookahs"));
        rowsInline.add(rowInline3);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
    }

    private void printBurgerMenu(Message message) {
        editMsg(message, "\uD83C\uDF54 Бургеры \uD83C\uDF54");
        sendPht(message, "Beef burger\uD83C\uDF54: белая булочка с кунжутом, котлета из говядины, " +
                        "сыр, бекон, яйцо, помидор, маринованный огурец, лук, " +
                        "салат айсберг, медово-горчичный соус\n250г./119грн.",
                "AgADAgADR6kxGzZQWUrw_SlXu3l9IewarQ4ABI-c_pOOa8SN2JADAAEC", "");
        sendPht(message, "Double beef burger\uD83C\uDF54: белая булочка с кунжутом, 2 котлеты из говядины, " +
                        "сыр, бекон, помидор, маринованный огурец, лук, " +
                        "салат айсберг, медово-горчичный соус\n350г./149грн.",
                "AgADAgADR6kxGzZQWUrw_SlXu3l9IewarQ4ABI-c_pOOa8SN2JADAAEC", "");
        sendPht(message, "Indiana burger\uD83C\uDF54: белая чиабатта, куриное филе, сыр, помидор, " +
                        "маринованный огурец, лук, салат айсберг, соус 'Дор блю'\n250г./119грн.",
                "AgADAgADR6kxGzZQWUrw_SlXu3l9IewarQ4ABI-c_pOOa8SN2JADAAEC", "");
        sendPht(message, "BBQ burger\uD83C\uDF54: белая булочка+ с кунжутом, котлета из говядины, " +
                        "сыр, бекон, помидор, луковые кольца фри, барбекю соус\n250г./119грн.",
                "AgADAgADR6kxGzZQWUrw_SlXu3l9IewarQ4ABI-c_pOOa8SN2JADAAEC", "");
    }

    private void printSandwichesMenu(Message message) {
        editMsg(message, "\uD83C\uDF2E Cэндвичи \uD83C\uDF2E");
        sendPht(message, "Сэндвич с курицей\uD83C\uDF2E: тостовый хлеб, курица, сыр, помидор, микс-салат, " +
                        "кукуруза, горчично-сливочный соус\n210г./79грн.",
                "AgADAgADV6kxGzZQWUrM_TFqvAHpgnW-tw4ABN9yHG-pzId0RRIBAAEC", "");
        sendPht(message, "Сэндвич с лососем\uD83C\uDF2E: тостовый хлеб, лосось, сыр сливочный, сыр твердый, " +
                        "укроп, лист салата\n200г./84грн.",
                "AgADAgADV6kxGzZQWUrM_TFqvAHpgnW-tw4ABN9yHG-pzId0RRIBAAEC", "");
        sendPht(message, "Сэндвич с ростбифом\uD83C\uDF2E: тостовый хлеб, телячья вырезка, лук крымский, " +
                        "сыр твердый, лист салата\n200г./79грн.",
                "AgADAgADV6kxGzZQWUrM_TFqvAHpgnW-tw4ABN9yHG-pzId0RRIBAAEC", "");
    }

    private void printWokMenu(Message message) {
        editMsg(message, "\uD83C\uDF72 Wok \uD83C\uDF72");
        sendPht(message, "Wok с курицей\uD83C\uDF72: лапша на выбор, куринное филе, кабачок, морковка, " +
                        "болгарский перец, помидор Черри, шампиньоны, лук\n300г./79грн.",
                "AgADAgADWKkxGzZQWUqpQayOgAzhML0hrQ4ABK7NGCO8yzIOypEDAAEC", "");
        sendPht(message, "Wok с телятиной\uD83C\uDF72: лапша на выбор, телятина, кабачок, морковка, " +
                        "болгарский перец, помидор Черри, шампиньоны, лук\n300г./89грн.",
                "AgADAgADWKkxGzZQWUqpQayOgAzhML0hrQ4ABK7NGCO8yzIOypEDAAEC", "");
        sendPht(message, "Паста Карбонара\uD83C\uDF72: спагетти, бекон, яйца, сливки, пармезан, базилик" +
                        "\n300г./69грн.",
                "AgADAgADWKkxGzZQWUqpQayOgAzhML0hrQ4ABK7NGCO8yzIOypEDAAEC", "");
    }

    private void printSaladMenu(Message message) {
        editMsg(message, "\uD83E\uDD57 Салаты \uD83E\uDD57");
        sendPht(message, "Цезарь с куриным филе\uD83E\uDD57: салат айсберг, бекон, куриное филе гриль, сухари, " +
                        "помидор, перепелиные яйца, пармезан, соус 'Цезарь'\n270г./84грн.",
                "AgADAgADXKkxGzZQWUomKd3JO7paOvfXtw4ABO--WykB6liT4RMBAAEC", "");
        sendPht(message, "Салат с лососем\uD83E\uDD57: микс салата, сыр фета, черри, лосось, кунжут, " +
                        "апельсин, соус апельсиновый\n270г./119грн.",
                "AgADAgADXKkxGzZQWUomKd3JO7paOvfXtw4ABO--WykB6liT4RMBAAEC", "");
        sendPht(message, "Салат с ростбифом\uD83E\uDD57: микс салата, сыр бри, черри, ростбиф, кедровій орех, " +
                        "соус 'Росбиф'\n270г./119грн.",
                "AgADAgADXKkxGzZQWUomKd3JO7paOvfXtw4ABO--WykB6liT4RMBAAEC", "");
        sendPht(message, "Салат Нисуаз\uD83E\uDD57: микс салата, картошка, стручковая фасоль, помидор, " +
                        "яйца, оливки, тунец, соус 'Нисуаз'\n270г./99грн.",
                "AgADAgADXKkxGzZQWUomKd3JO7paOvfXtw4ABO--WykB6liT4RMBAAEC", "");
    }

    private void printForBeerMenu(Message message) {
        editMsg(message, "\uD83C\uDF7A К пиву \uD83C\uDF7A");
        sendPht(message, "Пивная тарелка №1\uD83C\uDF7A: куриные крылья, сыр фри, колбаски фри, " +
                        "картошка по-селянски, cоусы: кетчуп, горчица\n780г./159грн.",
                "AgADAgADXqkxGzZQWUomZ5AVf7ybD4zyrA4ABDvOaN0teRhv9pcDAAEC", "");
        sendPht(message, "Пивная тарелка №2\uD83C\uDF7A: чесночные гренки, грибы, луковые кольца, " +
                        "картошка фри, cоусы: чесночный, 'Дор блю'\n430г./129грн.",
                "AgADAgADXqkxGzZQWUomZ5AVf7ybD4zyrA4ABDvOaN0teRhv9pcDAAEC", "");
        sendPht(message, "Ржаные гренки\uD83C\uDF7A: ржаной хлеб с чесночным соусом\n180г./30г. 49грн.",
                "AgADAgADXqkxGzZQWUomZ5AVf7ybD4zyrA4ABDvOaN0teRhv9pcDAAEC", "");
        sendPht(message, "Сыр фри\uD83C\uDF7A: жареный сыр с клюквенным соусом\n150г./30г. 74грн.",
                "AgADAgADXqkxGzZQWUomZ5AVf7ybD4zyrA4ABDvOaN0teRhv9pcDAAEC", "");
        sendPht(message, "Крылышки Баффало\uD83C\uDF7A\n300г./30г. 74грн.",
                "AgADAgADXqkxGzZQWUomZ5AVf7ybD4zyrA4ABDvOaN0teRhv9pcDAAEC", "");
        sendPht(message, "Луковые кольца\uD83C\uDF7A с соусом 'Дор блю'\n200г./30г. 69грн.",
                "AgADAgADXqkxGzZQWUomZ5AVf7ybD4zyrA4ABDvOaN0teRhv9pcDAAEC", "");
        sendPht(message, "Картошка фри\uD83C\uDF7A с соусом на выбор\n150г./30г. 36грн.",
                "AgADAgADXqkxGzZQWUomZ5AVf7ybD4zyrA4ABDvOaN0teRhv9pcDAAEC", "");
        sendPht(message, "Картошка по-селянски\uD83C\uDF7A с соусом на выбор\n150г./30г. 36грн.",
                "AgADAgADXqkxGzZQWUomZ5AVf7ybD4zyrA4ABDvOaN0teRhv9pcDAAEC", "");
        sendPht(message, "Начос\uD83C\uDF7A с соусом на выбор\n100г./30г. 39грн.",
                "AgADAgADXqkxGzZQWUomZ5AVf7ybD4zyrA4ABDvOaN0teRhv9pcDAAEC", "");
    }

    private void printHookahsMenu(Message message) {
        editMsg(message, "\uD83D\uDCA8 Кальяны \uD83D\uDCA8");
        sendPht(message, "Al Fakher, Adalya, Serbetli\uD83D\uDCA8\n145грн.",
                "AgADAgADjakxGzZQYUrwiZpv4s3UpXLLtw4ABFW_4IwuWfDNYxoBAAEC", "");
        sendPht(message, "Starbuzz, Fumari, Nakhla\uD83D\uDCA8\n175грн.",
                "AgADAgADjakxGzZQYUrwiZpv4s3UpXLLtw4ABFW_4IwuWfDNYxoBAAEC", "");
        sendPht(message, "Tangiers, Dark Side\uD83D\uDCA8\n225грн.",
                "AgADAgADjakxGzZQYUrwiZpv4s3UpXLLtw4ABFW_4IwuWfDNYxoBAAEC", "");
        sendPht(message, "На фрукте\uD83D\uDCA8:" +
                        "\nАнанас\uD83C\uDF4D +180грн." +
                        "\nАпельсин\uD83C\uDF4A +120грн." +
                        "\nГрейпфрут\uD83C\uDF45 +140грн." +
                        "\nЯблоко\uD83C\uDF4F +100грн.",
                "AgADAgADjakxGzZQYUrwiZpv4s3UpXLLtw4ABFW_4IwuWfDNYxoBAAEC", "");
    }

    private void setApplicationForTableReservation(SendMessage sendMessage) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("✅ Да").setCallbackData("yes"));
        rowInline1.add(new InlineKeyboardButton().setText("❌ Нет").setCallbackData("no"));
        rowsInline.add(rowInline1);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
    }

    private void setApplicationForCancelReservation(SendMessage sendMessage) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("✅ Да").setCallbackData("yesForCancelReservation"));
        rowInline1.add(new InlineKeyboardButton().setText("❌ Нет").setCallbackData("noForCancelReservation"));
        rowsInline.add(rowInline1);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
    }

    private void setApplicationForSendAdsForAllUsers(SendMessage sendMessage) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("✅ Да").setCallbackData("yesForSendAdsForAllUsers"));
        rowInline1.add(new InlineKeyboardButton().setText("❌ Нет").setCallbackData("noForSendAdsForAllUsers"));
        rowsInline.add(rowInline1);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
    }

    private void setApplicationForSendAdsForAllUsersWithPhoto(SendMessage sendMessage) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("✅ Да").setCallbackData("yesForSendAdsForAllUsersWithPhoto"));
        rowInline1.add(new InlineKeyboardButton().setText("❌ Нет").setCallbackData("noForSendAdsForAllUsersWithPhoto"));
        rowsInline.add(rowInline1);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
    }

    private void editReservation(SendMessage sendMessage) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("✅ Просто посмотреть ✅").setCallbackData("allIsOk"));
        rowsInline.add(rowInline1);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(new InlineKeyboardButton().setText("\uD83D\uDCDD Редактировать бронь \uD83D\uDCDD").setCallbackData("editReservation"));
        rowsInline.add(rowInline2);

        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        rowInline3.add(new InlineKeyboardButton().setText("❌ Отменить бронь ❌").setCallbackData("cancelReservation"));
        rowsInline.add(rowInline3);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
    }

    private void editCurrentSellInReservation(SendMessage sendMessage) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("Имя").setCallbackData("editName"));
        rowInline1.add(new InlineKeyboardButton().setText("Число мест").setCallbackData("editNumberOfPlaces"));
        rowsInline.add(rowInline1);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(new InlineKeyboardButton().setText("Дата").setCallbackData("editDate"));
        rowInline2.add(new InlineKeyboardButton().setText("Время").setCallbackData("editTime"));

        rowsInline.add(rowInline2);

        List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
        rowInline3.add(new InlineKeyboardButton().setText("Телефон").setCallbackData("editPhone"));
        rowInline3.add(new InlineKeyboardButton().setText("Пожелания").setCallbackData("editWishes"));
        rowsInline.add(rowInline3);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
    }

    private void setApplicationForOtherReservation(SendMessage sendMessage) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("✅ Да").setCallbackData("yesForReservation"));
        rowInline1.add(new InlineKeyboardButton().setText("❌ Нет").setCallbackData("noForReservation"));
        rowsInline.add(rowInline1);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(new InlineKeyboardButton().setText("\uD83D\uDCDD Изменить что-то еще").setCallbackData("editReservationAfterChange"));
        rowsInline.add(rowInline2);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
    }

    private void answerPersonallyToTheUser(SendMessage sendMessage) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("✉️ Ответить").setCallbackData("answerPersonallyToTheUser"));
        rowsInline.add(rowInline1);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
    }

    private void curAdminInDataBase(SendMessage sendMessage, String key) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        if(key.endsWith("true"))
            rowInline1.add(new InlineKeyboardButton().setText("⬇️ Понизить").setCallbackData("makeAdminNotMain_"
                    + key.substring(6, key.indexOf("_", 6))));
        else
            rowInline1.add(new InlineKeyboardButton().setText("⬆️ Повысить").setCallbackData("makeAdminMain_"
                    + key.substring(6, key.indexOf("_", 6))));
        rowInline1.add(new InlineKeyboardButton().setText("❌ Удалить").setCallbackData("deleteAdmin_"
                + key.substring(6, key.indexOf("_", 6))));
        rowsInline.add(rowInline1);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
    }

    private void addAdminInDataBase(SendMessage sendMessage){
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("✅ Да").setCallbackData("addAdminInDataBase"));
        rowsInline.add(rowInline1);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
    }

    private  void chooseIsMainAdmin(SendMessage sendMessage) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        rowInline1.add(new InlineKeyboardButton().setText("Главный администратор\uD83D\uDC68\u200D✈️").
                setCallbackData("newAdminIsMainAdmin"));
        rowsInline.add(rowInline1);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        rowInline2.add(new InlineKeyboardButton().setText("Обычный администратор\uD83D\uDC6E").
                setCallbackData("newAdminIsNotMainAdmin"));
        rowsInline.add(rowInline2);

        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);
    }

    private void setTableReservation(Message message) {
        switch (tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells) {
            case (0): {
                tableReservationHashMap.get(message.getChatId().toString()).name = message.getText();
                sendMsg(message,"На какое количество мест вы бы хотели забронировать столик?", "");
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells++;
                break;
            }
            case (1): {
                if(tableReservationHashMap.get(message.getChatId().toString()).inputNumberOfPlaces(message.getText()).equals("/mistake"))
                {
                    sendMsg(message, "Прости, я не понимаю такой вариант записи числа мест. " +
                            "Введите что-то похожее на '4' или '2-3'\uD83D\uDE01", "");
                }
                else {
                    tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces = message.getText();
                    sendMsg(message, "На какую дату? Пример ввода: '20.11' или '20.11.2018'.", "");
                    tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells++;
                }
                break;
            }
            case (2): {
                if(tableReservationHashMap.get(message.getChatId().toString()).inputDate(message.getText()).equals("/mistake"))
                {
                    sendMsg(message, "Прости, я не понимаю такой вариант записи даты или эта дата уже прошла. " +
                            "Введите что-то похожее на '20.11'  или '20.11.2018'\uD83D\uDE01", "");
                }
                else {
                    tableReservationHashMap.get(message.getChatId().toString()).date = message.getText();
                    sendMsg(message, "На какое время? Пример ввода: '16:20'.", "");
                    tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells++;
                }
                break;
            }
            case (3): {
                if(tableReservationHashMap.get(message.getChatId().toString()).inputTime(message.getText()).equals("/mistake"))
                {
                    sendMsg(message, "Прости, я не понимаю такой вариант записи времени " +
                            "или ваше время не попадает в время работы заведения. " +
                            "Введите что-то похожее на '16:20'\uD83D\uDE01", "");
                }
                else {
                    tableReservationHashMap.get(message.getChatId().toString()).time = message.getText();
                    sendMsg(message,"Укажите ваш номер телефона.", "");
                    tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells++;
                }
                break;
            }
            case(4): {
                if(tableReservationHashMap.get(message.getChatId().toString()).inputPhone(message.getText()).equals("/mistake"))
                {
                    sendMsg(message, "Прости, я не понимаю такой вариант записи номера телефона. " +
                            "Введите что-то похожее на '0506059805' или '+380506059805'\uD83D\uDE01", "");
                }
                else {
                    tableReservationHashMap.get(message.getChatId().toString()).phone = message.getText();
                    sendMsg(message,"Будут ли у Вас пожелания к брони?","");
                    tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells++;
                }
                break;
            }
            case(5): {
                tableReservationHashMap.get(message.getChatId().toString()).wishes = message.getText();
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells++;
                sendMsg(message,"Пожалуйста, проверьте, все ли указано верно.", "");
                sendMsg(message, "Имя: " + tableReservationHashMap.get(message.getChatId().toString()).name
                        + "\nЧисло мест: " + tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces
                        + "\nДата: " + tableReservationHashMap.get(message.getChatId().toString()).date
                        + "\nВремя: " + tableReservationHashMap.get(message.getChatId().toString()).time
                        + "\nТелефон: " + tableReservationHashMap.get(message.getChatId().toString()).phone
                        + "\nПожелания: " + tableReservationHashMap.get(message.getChatId().toString()).wishes, "");
                sendMsg(message, "Отправить заявку?", "application");
                break;
            }
            case(7): {
                tableReservationHashMap.get(message.getChatId().toString()).name =
                        tableReservationHashMap.get(message.getChatId().toString())
                                .checkForArrowSymbolFirst(tableReservationHashMap.get(message.getChatId().toString()).name);
                tableReservationHashMap.get(message.getChatId().toString()).name =
                        tableReservationHashMap.get(message.getChatId().toString()).name + " >>> "  + message.getText();
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
                sendMsg(message,"Пожалуйста, проверьте, все ли указано верно.", "");
                sendMsg(message,
                        "Имя: " + message.getText()
                                + "\nЧисло мест: " + tableReservationHashMap.get(message.getChatId().toString())
                                .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces)
                                + "\nДата: " + tableReservationHashMap.get(message.getChatId().toString())
                                .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).date)
                                + "\nВремя: " + tableReservationHashMap.get(message.getChatId().toString())
                                .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).time)
                                + "\nТелефон: " + tableReservationHashMap.get(message.getChatId().toString())
                                .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).phone)
                                + "\nПожелания: " + tableReservationHashMap.get(message.getChatId().toString())
                                .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).wishes),
                        "");
                sendMsg(message, "Отправить обновленную заявку?", "applicationForOtherReservation");
                break;
            }
            case(8): {
                if(tableReservationHashMap.get(message.getChatId().toString()).inputNumberOfPlaces(message.getText()).equals("/mistake"))
                {
                    sendMsg(message, "Прости, я не понимаю такой вариант записи числа мест. " +
                            "Введите что-то похожее на '4' или '2-3'\uD83D\uDE01", "");
                }
                else {
                    tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces =
                            tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolFirst(tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces);
                    tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces =
                            tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces + " >>> "  + message.getText();
                    tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
                    sendMsg(message,"Пожалуйста, проверьте, все ли указано верно.", "");
                    sendMsg(message,
                            "Имя: " + tableReservationHashMap.get(message.getChatId().toString()).
                                    checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).name)
                                    + "\nЧисло мест: " + message.getText()
                                    + "\nДата: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).date)
                                    + "\nВремя: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).time)
                                    + "\nТелефон: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).phone)
                                    + "\nПожелания: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).wishes),
                            "");
                    sendMsg(message, "Отправить обновленную заявку?", "applicationForOtherReservation");
                }
                break;
            }
            case(9): {
                if(tableReservationHashMap.get(message.getChatId().toString()).inputDate(message.getText()).equals("/mistake"))
                {
                    sendMsg(message, "Прости, я не понимаю такой вариант записи даты или эта дата уже прошла. " +
                            "Введите что-то похожее на '20.11'  или '20.11.2018'\uD83D\uDE01", "");
                }
                else {
                    tableReservationHashMap.get(message.getChatId().toString()).date =
                            tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolFirst(tableReservationHashMap.get(message.getChatId().toString()).date);
                    tableReservationHashMap.get(message.getChatId().toString()).date =
                            tableReservationHashMap.get(message.getChatId().toString()).date + " >>> "  + message.getText();
                    tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
                    sendMsg(message,"Пожалуйста, проверьте, все ли указано верно.", "");
                    sendMsg(message,
                            "Имя: " + tableReservationHashMap.get(message.getChatId().toString()).
                                    checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).name)
                                    + "\nЧисло мест: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces)
                                    + "\nДата: " + message.getText()
                                    + "\nВремя: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).time)
                                    + "\nТелефон: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).phone)
                                    + "\nПожелания: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).wishes),
                            "");
                    sendMsg(message, "Отправить обновленную заявку?", "applicationForOtherReservation");
                }
                break;
            }
            case(10): {
                tableReservationHashMap.get(message.getChatId().toString()).inputDate(
                        tableReservationHashMap.get(message.getChatId().toString()).date);
                if(tableReservationHashMap.get(message.getChatId().toString()).inputTime(message.getText()).equals("/mistake"))
                {
                    sendMsg(message, "Прости, я не понимаю такой вариант записи времени " +
                            "или ваше время не попадает в время работы заведения. " +
                            "Введите что-то похожее на '16:20'\uD83D\uDE01", "");
                }
                else {
                    tableReservationHashMap.get(message.getChatId().toString()).time =
                            tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolFirst(tableReservationHashMap.get(message.getChatId().toString()).time);
                    tableReservationHashMap.get(message.getChatId().toString()).time =
                            tableReservationHashMap.get(message.getChatId().toString()).time + " >>> "  + message.getText();
                    tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
                    sendMsg(message,"Пожалуйста, проверьте, все ли указано верно.", "");
                    sendMsg(message,
                            "Имя: " + tableReservationHashMap.get(message.getChatId().toString()).
                                    checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).name)
                                    + "\nЧисло мест: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces)
                                    + "\nДата: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).date)
                                    + "\nВремя: " + message.getText()
                                    + "\nТелефон: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).phone)
                                    + "\nПожелания: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).wishes),
                            "");
                    sendMsg(message, "Отправить обновленную заявку?", "applicationForOtherReservation");
                }
                break;
            }
            case(11): {
                if(tableReservationHashMap.get(message.getChatId().toString()).inputPhone(message.getText()).equals("/mistake"))
                {
                    sendMsg(message, "Прости, я не понимаю такой вариант записи номера телефона. " +
                            "Введите что-то похожее на '0506059805' или '+380506059805'\uD83D\uDE01", "");
                }
                else {
                    tableReservationHashMap.get(message.getChatId().toString()).phone =
                            tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolFirst(tableReservationHashMap.get(message.getChatId().toString()).phone);
                    tableReservationHashMap.get(message.getChatId().toString()).phone =
                            tableReservationHashMap.get(message.getChatId().toString()).phone + " >>> "  + message.getText();
                    tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
                    sendMsg(message,"Пожалуйста, проверьте, все ли указано верно.", "");
                    sendMsg(message,
                            "Имя: " + tableReservationHashMap.get(message.getChatId().toString()).
                                    checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).name)
                                    + "\nЧисло мест: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces)
                                    + "\nДата: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).date)
                                    + "\nВремя: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).time)
                                    + "\nТелефон: " + message.getText()
                                    + "\nПожелания: " + tableReservationHashMap.get(message.getChatId().toString())
                                    .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).wishes),
                            "");
                    sendMsg(message, "Отправить обновленную заявку?", "applicationForOtherReservation");
                }
                break;
            }
            case(12): {
                tableReservationHashMap.get(message.getChatId().toString()).wishes =
                        tableReservationHashMap.get(message.getChatId().toString())
                                .checkForArrowSymbolFirst(tableReservationHashMap.get(message.getChatId().toString()).wishes);
                tableReservationHashMap.get(message.getChatId().toString()).wishes =
                        tableReservationHashMap.get(message.getChatId().toString()).wishes + " >>> "  + message.getText();
                tableReservationHashMap.get(message.getChatId().toString()).numberOfCompletedSells = 6;
                sendMsg(message,"Пожалуйста, проверьте, все ли указано верно.", "");
                sendMsg(message,
                        "Имя: " + tableReservationHashMap.get(message.getChatId().toString()).
                                checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).name)
                                + "\nЧисло мест: " + tableReservationHashMap.get(message.getChatId().toString())
                                .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces)
                                + "\nДата: " + tableReservationHashMap.get(message.getChatId().toString())
                                .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).date)
                                + "\nВремя: " + tableReservationHashMap.get(message.getChatId().toString())
                                .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).time)
                                + "\nТелефон: " + tableReservationHashMap.get(message.getChatId().toString())
                                .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).phone)
                                + "\nПожелания: " + message.getText(),
                        "");
                sendMsg(message, "Отправить обновленную заявку?", "applicationForOtherReservation");
                break;
            }
        }
    }

    private void checkAllTableReservationForArrowSymbolLast(Message message) {
        tableReservationHashMap.get(message.getChatId().toString()).name =
                tableReservationHashMap.get(message.getChatId().toString())
                        .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).name);
        tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces =
                tableReservationHashMap.get(message.getChatId().toString())
                        .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces);
        tableReservationHashMap.get(message.getChatId().toString()).date =
                tableReservationHashMap.get(message.getChatId().toString())
                        .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).date);
        tableReservationHashMap.get(message.getChatId().toString()).time =
                tableReservationHashMap.get(message.getChatId().toString())
                        .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).time);
        tableReservationHashMap.get(message.getChatId().toString()).phone =
                tableReservationHashMap.get(message.getChatId().toString())
                        .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).phone);
        tableReservationHashMap.get(message.getChatId().toString()).wishes =
                tableReservationHashMap.get(message.getChatId().toString())
                        .checkForArrowSymbolLast(tableReservationHashMap.get(message.getChatId().toString()).wishes);
    }

    private void checkAllTableReservationForArrowSymbolFirst(Message message) {
        tableReservationHashMap.get(message.getChatId().toString()).name =
                tableReservationHashMap.get(message.getChatId().toString())
                        .checkForArrowSymbolFirst(tableReservationHashMap.get(message.getChatId().toString()).name);
        tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces =
                tableReservationHashMap.get(message.getChatId().toString())
                        .checkForArrowSymbolFirst(tableReservationHashMap.get(message.getChatId().toString()).numberOfPlaces);
        tableReservationHashMap.get(message.getChatId().toString()).date =
                tableReservationHashMap.get(message.getChatId().toString())
                        .checkForArrowSymbolFirst(tableReservationHashMap.get(message.getChatId().toString()).date);
        tableReservationHashMap.get(message.getChatId().toString()).time =
                tableReservationHashMap.get(message.getChatId().toString())
                        .checkForArrowSymbolFirst(tableReservationHashMap.get(message.getChatId().toString()).time);
        tableReservationHashMap.get(message.getChatId().toString()).phone =
                tableReservationHashMap.get(message.getChatId().toString())
                        .checkForArrowSymbolFirst(tableReservationHashMap.get(message.getChatId().toString()).phone);
        tableReservationHashMap.get(message.getChatId().toString()).wishes =
                tableReservationHashMap.get(message.getChatId().toString())
                        .checkForArrowSymbolFirst(tableReservationHashMap.get(message.getChatId().toString()).wishes);
    }

    private void deleteFromAdminHashMap(Message message){
        if(adminHashMap.containsKey(message.getChatId().toString())) {
            adminHashMap.remove(message.getChatId().toString());
            sendMsg(message, "У тебя нет здесь больше власти\uD83D\uDE08", "");
        }
        else
            sendMsg(message, "Откуда Вы узнали эту команду?\uD83D\uDE11", "reply");
    }

    private void addToUserHashMap(String ChatID) {
        if(!dataBase.existsChatIdInTable("users", ChatID)) {
            dataBase.insertIntoTableUsers(ChatID);
        }
    }

    private boolean dateOneEqualsDateTwo(String stringDate1, String stringDate2){
        DateFormat dateFormat = new SimpleDateFormat("dd.MM");
        dateFormat.setLenient(false);
        try {
            Date date1 = dateFormat.parse(stringDate1);
            Date date2 = dateFormat.parse(stringDate2);
            if(date1.equals(date2))
                return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private boolean dateOneIsAfterDateTwo(String stringDate1, String stringDate2, String DATE_FORMAT){
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setLenient(false);
        try {
            Date date1 = dateFormat.parse(stringDate1);
            Date date2 = dateFormat.parse(stringDate2);
            if(date1.after(date2))
                return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}