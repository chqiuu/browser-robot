package com.chqiuu.browser.robot.server.common.constant;

import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Constant {
    public static List<WebSocketSession> SOCKET_SESSIONS = Collections.synchronizedList(new ArrayList<>());
}
