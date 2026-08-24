package com.studenthub.model;
import org.junit.jupiter.api.Test;import java.time.LocalDateTime;import static org.junit.jupiter.api.Assertions.*;
class PrivateMessageReceiptTest{
 private final LocalDateTime now=LocalDateTime.of(2026,8,24,10,0);
 @Test void persistedMessageIsSent(){assertEquals("SENT",new PrivateMessage(1,1,2,"Hi",now,null,null,null).getStatus());}
 @Test void fetchedMessageIsDelivered(){assertEquals("DELIVERED",new PrivateMessage(1,1,2,"Hi",now,now,null,null).getStatus());}
 @Test void visibleMessageIsSeen(){assertEquals("SEEN",new PrivateMessage(1,1,2,"Hi",now,now,now,null).getStatus());}
}
