<%@ page import="java.util.*, java.io.*, act.util.*,java.sql.*,java.lang.reflect.*" 
%><%--
--%><%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
%><!doctype html>
<html lang="en-us" lang="en-us" dir="ltr" xml:lang="en-us" xmlns="http://www.w3.org/1999/xhtml">
<head>
    <link rel="shortcut icon" href="<%= request.getContextPath() %>/epcwid1/assets/favicon.png" />
    <title>Chambers County</title>
    <style> 
    html { zoom: 100%; font-size: 14px; }
    body {  
            margin: 0px; padding: 0px; width: 100%; xbackground-color: rgb(151, 151, 151);
            font-family: Arial,Helvetica,sans-serif;
            font-size: 14px;
    }
    #header { display:block;
            position: relative; margin: 0px; padding: 0px;
            height: 150px; width: 100%;
            
    }
    #header header {
            display: block; margin: 95px 0px;
            background: #0b3782;
            font-size: 14px; font-family: "Helvetica Neue",Helvetica,Arial,sans-serif;
            vertical-align: center;
            height: 45px; line-height: 20px; 
    }

    #header header .container {
            clear: none; display: block; position: relative; margin: 0px auto;
            width: 1170px; 
    }
    #header header #logo {
            display: block;  float: left; margin: -85px 10px 0px auto; padding: 0px 10px; 
            font-size: 18px; line-height: 20px;
    }
    #header header img {
            height: auto; width: 150px; max-width: 250px; vertical-align: middle;
    }


    #header header #menu { position: relative; float: left; }

    /* Primary menu */
    #header header #menu > ul { clear: both; position: relative; margin: 0px; padding: 0px; 
                                width: 100%; height: 45px; overflow: visible !important;
    }
    #header header #menu > ul > li { position: relative; float: left; display: block; margin-right: 1px;
                                vertical-align: middle;
                                list-style-type: none;
    }

    #header header #menu > ul > li > a { display: block; padding: 1px 10px 0px 10px;
                                font-size: 16px; font-family: "Open Sans","Helvetica Neue",Helvetica,Arial,sans-serif;
                                height: 44px;
                                line-height: 28px;
                                color: #fff; text-decoration: none; text-shadow: 1px 1px 1px rgba(0,0,0,0.3);
    }
    #header header #menu > ul > li > a > span { display: block; padding-top: 7px; }


    /* Primary menu - activity */
    #header header #menu > ul > li:hover, #header header #menu > ul > li.active {
                                background: #2158b6; color: #fff;
    }
    #header header #menu > ul > li.active:hover {
                                animation-name: none; animation-duration: 0.35s;
    }
    #header header #menu > ul > li:hover {
                                animation-name: fadein; animation-duration: 0.35s;
    }
    #header header #menu > ul > li:hover > a, #header header #menu > ul > li.active > a  {
                                color: #white;
    }


    /* Sub-menu */
    #header header #menu > ul > li > ul { position: absolute; z-index: 600; top: 45px; left: 0px;
                                padding: 0px 20px;
                                width: 280px; background: #2158b6;
                                display: none;
    }
    #header header #menu > ul > li > ul > li {
                                position: relative; float: left; display: block; margin-right: 1px;
                                vertical-align: middle;
                                list-style-type: none; border-bottom: 1px dotted rgba(255, 255, 255, .2); width: 100%;
                                clear: both; 
    }
    #header header #menu > ul > li > ul > li:last-child {
                                border-bottom: none;
    }
    #header header #menu > ul > li > ul > li > a { 
                                display: block; padding: 1px 10px 0px 10px;
                                font-size: 14px; font-family: "Open Sans","Helvetica Neue",Helvetica,Arial,sans-serif;
                                height: 30px; 
                                line-height: 28px;
                                color: #fff; text-decoration: none; text-shadow: 1px 1px 1px rgba(0,0,0,0.3);
    }


    /* Sum-menu - activity */
    #header header #menu > ul > li:hover > ul {
                                display: block; padding-top: 5px; padding-bottom: 5px;
                                background: #2190c4;
                                animation-name: slidein; animation-duration: 0.35s;
    }
    #header header #menu > ul > li > ul > li:hover > a, #header header #menu > ul > li > ul > li.active > a  {
                                color: black;
    }


    /* Animation definitions */
    @keyframes slidein {
            from {
                top: 75px;
            }
            to {
                top: 45px;
            }
    }
    @keyframes fadein {
            from {
                background-color: #0D4F8B;
            }
            to {
                background-color: #2190c4;
            }
    }

    #content { clear: both; margin-bottom: 40px; padding: 30px 15px 15px 15px; }
    #content { font-size: 14px; font-family: Arial, Helvetica; text-align: center; min-height: 500px; }
    .warning { color: #A30000; font-size: 1.2rem; font-style: italic; margin:30px 0px 30px; font-weight: bold; }
    h1 { margin: 0px 0px 05px; padding: 0px; }
    h3 { margin: 30px 0px 20px; padding: 0px; font-size: 1.2rem; }
    
    #footer {
        background-color: #0b3782;
    }
    </style>
    <script src="<%= request.getContextPath() %>/epcwid1/assets/jquery-3.3.1.min.js"></script>
</head>
<body>
<div id="header">
<header>
    <div class="container clearfix">
        <div id="logo">	
            <a href="http://www.co.chambers.tx.us/"><img alt="EPCWID #1" src="<%= request.getContextPath() %>/epcwid1/assets/chambers_2.png"></a>
        </div>
        <div id="menu">	
            <ul>
                <li><a href="http://www.co.chambers.tx.us/"> <span class=""> Home </span> </a></li>
                <li class="<%= (request.getRequestURI().endsWith("/search.jsp") ? "active" : "") %>">
                    <a href="<%= request.getContextPath() + "/epcwid1/search.jsp" %>"> <span class=""> Search </span> </a>
                </li>
                <!--
                <li>
                    <a href="#"> <span class=""> Option 3 </span> </a>
                    <ul>
                        <li class="active">
                            <a href="#"> <span class=""> Home </span> </a>
                        </li>
                        <li>
                            <a href="#"> <span class=""> Option 2 </span> </a>
                        </li>
                    </ul>
                </li>
                -->
            </ul>
        </div>
    </div>
</header>
</div>
<div id="content"> 
