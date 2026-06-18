package com.studentlife.StudentLifeAPIs.Service;

public interface OneSignalService {

    void sendPushToUser(String playerId, String title, String message, Long referenceId, String link);
}
