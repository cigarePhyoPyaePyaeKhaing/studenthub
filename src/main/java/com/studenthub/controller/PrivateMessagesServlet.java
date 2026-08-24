package com.studenthub.controller;
import com.studenthub.dao.PrivateMessageDAO;import com.studenthub.model.PrivateConversation;import com.studenthub.util.CsrfToken;import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.*;import java.util.*;
@WebServlet(name="PrivateMessagesServlet",urlPatterns="/messages")
public class PrivateMessagesServlet extends HttpServlet{
 private final PrivateMessageDAO dao=new PrivateMessageDAO();
 @Override protected void doGet(HttpServletRequest q,HttpServletResponse r)throws IOException,ServletException{long me=(Long)q.getSession().getAttribute("userId");try{List<PrivateConversation> list=dao.list(me);q.setAttribute("conversations",list);Long id=parse(q.getParameter("conversationId"));if(id!=null){if(!dao.isParticipant(id,me)){r.sendError(403);return;}q.setAttribute("selectedConversation",list.stream().filter(x->x.conversationId()==id).findFirst().orElse(null));q.setAttribute("privateMessages",dao.messages(id,me,0));dao.markRead(id,me);}q.setAttribute("csrfToken",CsrfToken.getOrCreate(q.getSession()));q.getRequestDispatcher("/WEB-INF/views/messages/index.jsp").forward(q,r);}catch(Exception e){getServletContext().log("Private messages load failed: "+e.getClass().getName());r.sendError(500);}}
 private Long parse(String v){try{long x=Long.parseLong(v);return x>0?x:null;}catch(Exception e){return null;}}
}
