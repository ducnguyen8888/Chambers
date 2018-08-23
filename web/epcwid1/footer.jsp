<%@ page import="java.util.*, java.io.*, act.util.*,java.sql.*,java.lang.reflect.*" 
%><%--
--%><%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
%>
</div>
<div id="footer" style="clear:both; background-color: #0b3782;padding:10px 20px 30px;">
    <div id="footerLinks"> 
        <a style=" color : #fff; font-size:12px;margin-left: 10px;" href="<%= request.getContextPath() %>/epcwid1/gen/terms.jsp">Terms of Use</a>
        <a style=" color : #fff; font-size:12px;margin-left:10px;" href="<%= request.getContextPath() %>/epcwid1/gen/privacypolicy.jsp">Privacy Policy</a>
    </div>
</div>
</body>
</html> 
