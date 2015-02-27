package edu.upenn.cis.cis455;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
       throws java.io.IOException
  {
    response.setContentType("text/html");
    HttpSession s = request.getSession(true);
    PrintWriter out = response.getWriter();
    out.println("<html><head><title>Test</title></head><body>");
    out.println("Scheme: ["+request.getScheme()+"]<br>");
    out.println("ServerName: ["+request.getServerName()+"]<br>");
    out.println("ServerPort: ["+request.getServerPort()+"]<br>");
    out.println("ContextPath: ["+request.getContextPath()+"]<br>");
    out.println("ServletPath: ["+request.getServletPath()+"]<br>");
    out.println("RequestURL: ["+request.getRequestURL()+"]<br>");
    out.println("RequestURI: ["+request.getRequestURI()+"]<br>");
    out.println("PathInfo: ["+request.getPathInfo()+"]<br>");
    out.println("QueryString: ["+request.getQueryString()+"]<br>");
    out.println("Header: ["+request.getHeader("Accept-Language")+"]<br>");
    
    out.println("</body></html>");
  }
}
  
