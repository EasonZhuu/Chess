package client;

import model.GameData;

public interface ServerMessageObserver {
    void loadGame(GameData game);

    void showNotification(String message);

    void showError(String errorMessage);
}
