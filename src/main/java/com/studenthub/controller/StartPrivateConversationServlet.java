package com.studenthub.controller;
import com.studenthub.dao.PrivateMessageDAO;import com.studenthub.util.CsrfToken;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.*;
@WebServlet(name="StartPrivateConversationServlet",urlPatterns="/messages/start")
public class StartPrivateConversationServlet extends HttpServlet{
 private final PrivateMessageDAO dao=new PrivateMessageDAO();
 @Override protected void doPost(HttpServletRequest q,HttpServletResponse r)throws IOException{if(!CsrfToken.isValid(q)){r.sendError(403);return;}try{long me=(Long)q.getSession().getAttribute("userId"),target=Long.parseLong(q.getParameter("targetUserId"));long id=dao.findOrCreate(me,target);r.sendRedirect(q.getContextPath()+"/messages?conversationId="+id);}catch(IllegalArgumentException e){r.sendError(400);}catch(Exception e){getServletContext().log("Private conversation start failed: "+e.getClass().getName());r.sendError(500);}}
}
