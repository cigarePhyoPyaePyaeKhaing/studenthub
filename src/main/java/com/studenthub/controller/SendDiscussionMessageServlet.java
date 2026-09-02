package com.studenthub.controller;

import com.studenthub.model.DiscussionScope;
import com.studenthub.service.DiscussionService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "SendDiscussionMessageServlet", urlPatterns = "/discussions/messages")
@MultipartConfig(maxFileSize=com.studenthub.util.UploadPolicy.VIDEO_MAX,maxRequestSize=com.studenthub.util.UploadPolicy.MULTIPART_MAX)
public class SendDiscussionMessageServlet extends HttpServlet {
    private final DiscussionService service = new DiscussionService();

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, jakarta.servlet.ServletException {
        request.setCharacterEncoding("UTF-8");
        boolean json = acceptsJson(request);
        if (!CsrfToken.isValid(request)) { if(json)fail(request,response,true,HttpServletResponse.SC_FORBIDDEN,"Your security token expired. Refresh and try again.");else response.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        String scope = DiscussionScope.fromRequest(request.getParameter("scope")).name();
        Long roomId = parsePositiveLong(request.getParameter("roomId"));
        if (roomId == null && "ADMIN".equals(String.valueOf(request.getSession().getAttribute("role")))) {
            Object selected = request.getSession().getAttribute("selectedDiscussionRoomId");
            if (selected instanceof Long value && value > 0) roomId = value;
        }
        String redirect = "/discussions?scope=" + scope + (roomId == null ? "" : "&roomId=" + roomId);
        com.studenthub.util.AttachmentRequest.Result attachment=com.studenthub.util.AttachmentRequest.read(request.getPart("attachment"));
        if(!attachment.valid()){fail(request,response,json,HttpServletResponse.SC_BAD_REQUEST,attachment.error());if(!json)response.sendRedirect(request.getContextPath()+redirect);return;}
        try {
            DiscussionService.OperationResult result = service.send(
                    (Long) request.getSession().getAttribute("userId"), scope, request.getParameter("message"),attachment.upload(),roomId);
            if(!result.successful()){com.studenthub.util.AttachmentRequest.discard(attachment.upload());fail(request,response,json,HttpServletResponse.SC_BAD_REQUEST,result.message());if(!json)response.sendRedirect(request.getContextPath()+redirect);return;}
            if(json){writeJson(response,HttpServletResponse.SC_OK,"{\"success\":true,\"redirectUrl\":\""+request.getContextPath()+redirect+"\"}");return;}
            request.getSession().setAttribute("flash", result.message());
        } catch (SecurityException exception) {
            com.studenthub.util.AttachmentRequest.discard(attachment.upload());
            if(json)fail(request,response,true,HttpServletResponse.SC_FORBIDDEN,"You do not have access to this discussion.");else response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (SQLException exception) {
            com.studenthub.util.AttachmentRequest.discard(attachment.upload());
            getServletContext().log(String.format("Discussion send failed: stage=insert scope=%s role=%s sqlState=%s errorCode=%d exception=%s",
                    scope, request.getSession().getAttribute("role"), exception.getSQLState(), exception.getErrorCode(), exception.getClass().getName()));
            fail(request,response,json,HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"The message could not be sent right now.");
            if(json)return;
        }
        response.sendRedirect(request.getContextPath() + redirect);
    }

    private boolean acceptsJson(HttpServletRequest request){return request.getHeader("Accept")!=null&&request.getHeader("Accept").contains("application/json");}
    private void fail(HttpServletRequest request,HttpServletResponse response,boolean json,int status,String message)throws IOException{if(json){writeJson(response,status,"{\"success\":false,\"message\":\""+escape(message)+"\"}");}else{request.getSession().setAttribute("flashError",message);}}
    private void writeJson(HttpServletResponse response,int status,String body)throws IOException{response.setStatus(status);response.setContentType("application/json");response.setCharacterEncoding("UTF-8");response.getWriter().write(body);}
    private String escape(String value){return value==null?"Message could not be sent.":value.replace("\\","\\\\").replace("\"","\\\"").replace("\r"," ").replace("\n"," ");}
    private Long parsePositiveLong(String value){if(value==null||value.isBlank())return null;try{long parsed=Long.parseLong(value);return parsed>0?parsed:null;}catch(NumberFormatException ignored){return null;}}
}
