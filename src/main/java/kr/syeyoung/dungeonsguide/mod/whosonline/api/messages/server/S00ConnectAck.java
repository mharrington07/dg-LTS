package kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.server;

import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.AbstractMessage;
import lombok.Data;

@Data
public class S00ConnectAck implements AbstractMessage {
    public final boolean sucess;
}
