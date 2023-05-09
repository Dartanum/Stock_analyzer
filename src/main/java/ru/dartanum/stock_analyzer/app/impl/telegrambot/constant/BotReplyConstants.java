package ru.dartanum.stock_analyzer.app.impl.telegrambot.constant;

public interface BotReplyConstants {
    String MSG_START = "Привет! Я - бот для анализа акций!";
    String MSG_INCORRECT_DATA = "Не могу разобрать... Повторите попытку";
    String MSG_ACCESS_DENIED = "Эта команда доступна только администратору!";
    //---------------------------ADMIN---------------------------
    String MSG_TEMPLATE_CODE_SENT = "На номер %s был отправлен код подтверждения. Отправь его сюда в формате '1.2.3.4.5'";
    String MSG_ERROR_DURING_SIGN_IN = "Бот не смог начать процесс входа в аккаунт. Повторите попытку";
    String MSG_SUCCESS_SYSTEM_SETUP = "Система сконфигурирована!";
    String MSG_ERROR_DURING_ENTER_VERIFICATION_CODE = "Произошла ошибка при отправке или проверке кода верификации. Повторите попытку";
    //-----------------------NEWS ANALYSIS-----------------------
    String MSG_TEMPLATE_ENTER_DAY_COUNT = "Введи за сколько дней проанализировать записи (от %d до %d)";
    String MSG_DAY_COUNT_SAVED = "Для начала анализа новостей нажмите \"Старт\"";
    String MSG_START_COLLECTING_DATA = "Сбор данных...";
    String MSG_ERROR_DURING_COLLECTING_DATA = "Произошла ошибка во время сбора данных";
    String MSG_END_COLLECTING_DATA = "Сбор данных завершен.\nЗапускаю анализ...";
    //---------------------TECHNICAL ANALYSIS---------------------
    String MSG_ENTER_TICKER_NAME = "Введите название тикера";
    String MSG_UNDEFINED_TICKER_NAME = "Не могу найти тикер с таким названием. Попробуйте снова";
}
