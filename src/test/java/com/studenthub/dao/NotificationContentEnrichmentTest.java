package com.studenthub.dao;

import com.studenthub.model.Notification;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NotificationContentEnrichmentTest {

    @Test
    void extractPostIdHandlesStandardAndCustomLinkFormats() {
        assertEquals(42L, NotificationDAO.extractPostId("/posts/comments?postId=42"));
        assertEquals(101L, NotificationDAO.extractPostId("/posts/comments?source=home&postId=101"));
        assertEquals(88L, NotificationDAO.extractPostId("/posts/88"));
        assertEquals(77L, NotificationDAO.extractPostId("postId=77"));
        assertEquals(99L, NotificationDAO.extractPostId("/announcements?post_id=99"));
        assertNull(NotificationDAO.extractPostId("/announcements"));
        assertNull(NotificationDAO.extractPostId("/deadlines"));
        assertNull(NotificationDAO.extractPostId(null));
        assertNull(NotificationDAO.extractPostId(""));
    }

    @Test
    void enrichesAnnouncementNotificationWithFullBodyContent() throws Exception {
        NotificationDAO dao = new NotificationDAO();
        LocalDateTime now = LocalDateTime.now();

        List<Notification> items = new ArrayList<>();
        items.add(new Notification(1L, "ANNOUNCEMENT", "Farewell Dinner", "A new announcement was published.",
                "/posts/comments?postId=10", false, now));
        items.add(new Notification(2L, "DEADLINE", "Assignment 1", "A new deadline was added.",
                "/deadlines", false, now));
        items.add(new Notification(3L, "ANNOUNCEMENT", "Missing Post", "A new announcement was published.",
                "/posts/comments?postId=999", false, now));

        String burmeseAndEnglishText = "Farewell Dinner 2026 ကို လာမည့် တနင်္ဂနွေနေ့တွင် ကျင်းပမည်ဖြစ်ပါသည်။\n" +
                "Please bring your student ID and register before 5:00 PM.\n" +
                "Venue: Main Auditorium (Room 301).";

        Map<Long, String> postsDb = new HashMap<>();
        postsDb.put(10L, burmeseAndEnglishText);

        Connection mockConnection = createMockPostsConnection(postsDb);

        Method enrichMethod = NotificationDAO.class.getDeclaredMethod("enrichAnnouncementContents", Connection.class, List.class);
        enrichMethod.setAccessible(true);
        enrichMethod.invoke(dao, mockConnection, items);

        // First item (ANNOUNCEMENT with existing post 10) must be enriched with actual full body
        assertEquals("Farewell Dinner", items.get(0).getTitle());
        assertEquals(burmeseAndEnglishText, items.get(0).getMessage());

        // Second item (DEADLINE) must remain untouched
        assertEquals("DEADLINE", items.get(1).getType());
        assertEquals("A new deadline was added.", items.get(1).getMessage());

        // Third item (ANNOUNCEMENT with missing post 999) must keep fallback message
        assertEquals("Missing Post", items.get(2).getTitle());
        assertEquals("A new announcement was published.", items.get(2).getMessage());
    }

    private Connection createMockPostsConnection(Map<Long, String> postsDb) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        String sql = (String) args[0];
                        if (sql.contains("FROM posts WHERE post_id IN")) {
                            return createMockPreparedStatement(postsDb);
                        }
                    }
                    return null;
                });
    }

    private PreparedStatement createMockPreparedStatement(Map<Long, String> postsDb) {
        List<Long> queriedIds = new ArrayList<>();
        return (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("setLong".equals(name)) {
                        queriedIds.add((Long) args[1]);
                        return null;
                    }
                    if ("executeQuery".equals(name)) {
                        List<Map.Entry<Long, String>> results = new ArrayList<>();
                        for (Long id : queriedIds) {
                            if (postsDb.containsKey(id)) {
                                results.add(Map.entry(id, postsDb.get(id)));
                            }
                        }
                        return createMockResultSet(results);
                    }
                    return null;
                });
    }

    private ResultSet createMockResultSet(List<Map.Entry<Long, String>> results) {
        final int[] index = {-1};
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("next".equals(name)) {
                        index[0]++;
                        return index[0] < results.size();
                    }
                    if ("getLong".equals(name)) {
                        String col = (String) args[0];
                        if ("post_id".equals(col)) {
                            return results.get(index[0]).getKey();
                        }
                    }
                    if ("getString".equals(name)) {
                        String col = (String) args[0];
                        if ("title".equals(col)) {
                            return "Farewell Dinner";
                        }
                        if ("content".equals(col)) {
                            return results.get(index[0]).getValue();
                        }
                    }
                    return null;
                });
    }
}
