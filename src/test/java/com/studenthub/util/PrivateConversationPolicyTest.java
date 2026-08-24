package com.studenthub.util;
import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class PrivateConversationPolicyTest{
 @Test void pairIsCanonical(){assertEquals(new PrivateConversationPolicy.Pair(3,9),PrivateConversationPolicy.normalize(9,3));}
 @Test void samePairIsStable(){assertEquals(PrivateConversationPolicy.normalize(2,7),PrivateConversationPolicy.normalize(7,2));}
 @Test void selfMessageRejected(){assertThrows(IllegalArgumentException.class,()->PrivateConversationPolicy.normalize(4,4));}
 @Test void participantsOnly(){assertTrue(PrivateConversationPolicy.participant(2,2,7));assertFalse(PrivateConversationPolicy.participant(5,2,7));}
 @Test void clientTokenMustBeUuid(){assertTrue(PrivateConversationPolicy.validClientId("550e8400-e29b-41d4-a716-446655440000"));assertFalse(PrivateConversationPolicy.validClientId("retry-me"));}
}
