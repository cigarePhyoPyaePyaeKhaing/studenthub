package com.studenthub.controller;
import com.studenthub.dao.AcademicChangeDAO;import com.studenthub.util.CsrfToken;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.*;import java.sql.*;import java.util.Locale;
@WebServlet("/profile/academic-change") public class AcademicChangeRequestServlet extends HttpServlet{
 private final AcademicChangeDAO dao=new AcademicChangeDAO();
 protected void doPost(HttpServletRequest q,HttpServletResponse p)throws IOException{
  if(!CsrfToken.isValid(q)){p.sendError(403);return;}
  try{int semester=Integer.parseInt(q.getParameter("semester"));String section=q.getParameter("sectionName")==null?"":q.getParameter("sectionName").trim().toUpperCase(Locale.ROOT),reason=q.getParameter("reason")==null?"":q.getParameter("reason").trim();if(semester<1||semester>10||!section.matches("[A-Z0-9][A-Z0-9 -]{0,19}")||reason.length()<10||reason.length()>1000)throw new IllegalArgumentException();dao.create((Long)q.getSession().getAttribute("userId"),semester,section,reason);q.getSession().setAttribute("flash","Academic change request submitted for administrator review.");}
  catch(IllegalStateException e){q.getSession().setAttribute("flashError",e.getMessage());}
  catch(IllegalArgumentException e){q.getSession().setAttribute("flashError","Enter a valid semester, section, and reason of 10-1000 characters.");}
  catch(SQLException e){q.getServletContext().log("Academic change request failed: "+e.getClass().getName());q.getSession().setAttribute("flashError","The request service is temporarily unavailable.");}
  p.sendRedirect(q.getContextPath()+"/profile");
 }
}
