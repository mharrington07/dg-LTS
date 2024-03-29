package kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.server;

import kr.syeyoung.dungeonsguide.mod.whosonline.api.messages.AbstractMessage;
import lombok.Data;

@Data
public class S01IsOnlineAck implements AbstractMessage {
    public final boolean is_online;
    public final String uuid;
    public final String nonce;

}
