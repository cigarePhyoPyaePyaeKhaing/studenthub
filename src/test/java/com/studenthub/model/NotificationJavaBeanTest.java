package com.studenthub.model;

import org.junit.jupiter.api.Test;

import java.beans.Introspector;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class NotificationJavaBeanTest {
    @Test
    void exposesEveryPropertyUsedByNotificationsJspAsAJavaBeanGetter() throws Exception {
        Set<String> readableProperties = Arrays.stream(
                        Introspector.getBeanInfo(Notification.class).getPropertyDescriptors())
                .filter(property -> property.getReadMethod() != null)
                .map(property -> property.getName())
                .collect(Collectors.toSet());

        assertTrue(readableProperties.containsAll(Set.of(
                "notificationId", "type", "title", "message", "linkUrl", "read", "createdLabel")));
    }

    @Test
    void beanGettersPreserveRecordValuesAndReadState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 15, 21, 30);
        Notification item = new Notification(42L, "COMMENT", "Title", "Message",
                "/posts/comments?postId=9", true, createdAt);

        assertAll(
                () -> assertEquals(42L, item.getNotificationId()),
                () -> assertEquals("COMMENT", item.getType()),
                () -> assertEquals("Title", item.getTitle()),
                () -> assertEquals("Message", item.getMessage()),
                () -> assertEquals("/posts/comments?postId=9", item.getLinkUrl()),
                () -> assertTrue(item.isRead()),
                () -> assertEquals("Aug 16, 2026 · 4:00 AM", item.getCreatedLabel()),
                () -> assertTrue(item.read()),
                () -> assertEquals(42L, item.notificationId())
        );
    }
}
