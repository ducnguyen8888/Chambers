<%@ page import="act.app.security.*,java.util.*,act.util.*,java.nio.file.*,java.io.*" 
         contentType="text/html;charset=windows-1252"
%><%--
javax.servlet.forward.context_path	/minimal
javax.servlet.forward.servlet_path	/_system/restricted/login.jsp
javax.servlet.forward.request_uri	/minimal/_system/restricted/success.jsp
javax.servlet.forward.path_info	    null
javax.servlet.forward.query_string	abc=123
--%><%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");

    StringBuffer buffer = new StringBuffer();


    String requestedURI     = request.getRequestURI();
    String postURI          = request.getRequestURI().replaceAll(".*/([^/]*)$","$1");
    String failureReason    = "";


    String [] configurationFiles = (new ClientWebsiteConfiguration(pageContext)).listAvailableConfigurations();
    Arrays.sort(configurationFiles);
%><%!
    boolean isDefined(String val) { return val != null && val.length() > 0; }
    String nvl(String... values) {
        if ( values != null ) 
            for ( String value : values ) {
                if ( value != null ) return value;
            }
        return "";
    }
%><!DOCTYPE html>
<html lang="en-us">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252"/>
        <title>Deployment Information</title>
        <style>
            html { 
                    background: url("<%= request.getContextPath() %>/resources/images/stock-photo-198802483.jpg") no-repeat center center fixed; 
            
                    -webkit-background-size: cover;
                    -moz-background-size: cover;
                    -o-background-size: cover;
                    background-size: cover;
                    font-family: 'Arial Narrow', Arial, sans-serif; font-size: 16px;
                }
            .login-form {
                background:rgba(19, 35, 47, .9);
                max-width:400px; margin: 40px auto; padding: 40px;
                border-radius:4px;
                box-shadow: 0 4px 10px 4px rgba(19, 35, 47, .3); color: white;
                padding: 35px; text-overflow: ellipse;
            }

            .login-form h1 {
                margin:0 0 40px;
                text-align:center; color: #ffffff;
                font-weight: light;
            }

            .instruction-form {
                background:rgba(19, 35, 47, .9);
                max-width:750px; margin: 40px auto; padding: 20px;
                border-radius:4px;
                box-shadow: 0 4px 10px 4px rgba(19, 35, 47, .3); color: white;
                padding: 15px; text-overflow: ellipse;
            }

            .instruction-form h1 {
                margin:0 0 25px;
                text-align:center; color: #ffffff;
                font-weight: light;
            }

            label {
                position: absolute;
                left:13px;
                transform:translateY(12px) translateX(5px);
                color: rgba(160, 179, 176, .5);
                transition: all 0.25s ease;
                -webkit-backface-visibility: hidden;
                pointer-events: none;
                font-size:22px;
            }
            label .req {
                margin:2px;
            }

            label.active {
                transform:translateY(53px);
                left:2px; font-size:14px;
            }

            label.highlight {
                color: #f0f0f0; 
            }

            input, textarea {
                font-size:22px; color: #ffffff; 
                display:block; width:95%; height:100%;
                padding:10px 15px;
                background:none; background-image:none;
                border:1px solid lightgrey; border-radius:2px;
                transition: border-color .25s ease, box-shadow .25s ease;
                &:focus {
                    outline:0;
                    border-color: #1ab188; color:blue;
                }
            }
            .field-wrap {
                position:relative;
                margin-bottom:40px;
            }

            .button {
                border:0;
                outline:none;
                border-radius:0;
                padding:15px 0;
                font-size:1.5rem; font-weight: bold; color: #ffffff;
                letter-spacing:.1em;
                background: #1ab188;
                transition:all.5s ease;
                -webkit-appearance: none;
                cursor: pointer;
            }
            .button:hover, .button:focus { background:lightblue; }

            .button-block {
                display:block;
                width:100%;
            }
            input.valueChanged { border-color: #A30000; color: #A30000; background-color: #d8d8d8; }
            input.valueChanged.noValue { background-color: transparent; }

            </style>
    <script src="<%= request.getContextPath() %>/resources/js/jquery-3.2.1.min.js"></script>
    <script>
        $(function() {
            $("input").focus(function() {
                var label = $(this).prev("label");
                    if ( $(this).val() === "" ) {
                        label.removeClass("highlight");
                    } else {
                        label.addClass("highlight");
                    }
            });
            $("input").keyup(function() { 
                var label = $(this).prev("label");
                    if ( $(this).val() === "" ) {
                        label.removeClass("active highlight");
                    } else {
                        label.addClass("active highlight");
                    }
            }).keyup();
            $("input").change(function() {
                if ( $(this).val() != $(this).prop("defaultValue") ) {
                    $(this).addClass("valueChanged");
                    if ( $(this).val().length == 0 ) 
                        $(this).addClass("noValue");
                    else
                        $(this).removeClass("noValue");
                } else
                    $(this).removeClass("valueChanged").removeClass("noValue");
            });
            $(".configurationFile").click(function() {
                console.log("Config file clicked: " + $(this).prop("id"));
                console.log("Action: " + $("#altConfiguration").prop("action"));
                $("#fileName").val($(this).prop("id"));
                $("#altConfiguration").submit();
                console.log("Should have submitted");
                });
        });
        document.addEventListener("keyup", (event) => {
            const keyName = event.key;
            const ctrlKeyPressed = event.ctrlKey;
            if ( ctrlKeyPressed && keyName == "Home" ) {
                var parser = location;
                location = parser.origin + parser.pathname.replace(/(\/[^\/]*\/[^\/]*\/).*/,"$1default.jsp");
                return;

                // Reference fields
                // var parser = location;
                // parser.protocol // => "http:"
                // parser.host     // => "example.com:3000"
                // parser.hostname // => "example.com"
                // parser.port     // => "3000"
                // parser.pathname // => "/pathname/"
                // parser.hash     // => "#hash"
                // parser.search   // => "?search=test"
                // parser.origin   // => "http://example.com:3000"
                //
                // KeyUp/KeyDown Events
                // event.ctrlKey    event.altKey    event.shiftKey
                // true/false: is the specified key pressed

            }
        }, false);

    </script>
    <style>
        .commandHeader { position: fixed; z-index: 1000; top: 0px; left: 0px; right: 0px; height: 50px; background-color: #2f2f2f; font-size: 12px; }
        .commandHeader a { display: inline-block; padding: 10px 15px; font-size: 1.1rem; text-decoration: none; color: white; }
        .commandHeader a:hover { text-decoration: underline; color: lightblue; }
        .alternateFiles button {
                padding:5px 0; width: 100px; cursor: pointer; border-radius: 6px;
                font-size:0.8rem; font-weight: normal; margin: 4px 5px;
                text-transform:uppercase;
                letter-spacing:.1em;
                background: #1ab188;
                transition:all.5s ease;
                -webkit-appearance: none;
        }
    </style>
</head>
<body>
<div class="commandHeader">
    <a href="<%= request.getContextPath() %>/restricted/login.jsp"> &#8612; Exit </a>
</div>
<div style="position:relative;width:58%; margin:0px; text-align: center; vertical-align: top; padding: auto; padding-top: 50px; display: inline-block;">
    <div class="instruction-form" >
        <h1><%= request.getContextPath() %> Administration</h1>

        <div style="margin:0px 20px 20px 20px;background-color: #1e1e1e;padding:10px 20px 20px;border-radius:4px;">
            <h2 style="xcolor:lightblue;xtext-decoration:underline;"> Select Command Option </h2>
            <span style="xcolor:lightblue;"></span>
        </div>

        <% if ( isDefined(failureReason) || buffer.length() > 0 ) { %>
            <div style="color:red;margin:0px 20px 20px 20px;font-weight: bold;padding:10px 20px 20px;background-color: #eeeeee;border-radius:4px;">
                <%= failureReason %>
                <pre style="text-align: left;"><%= buffer.toString() %></pre>
            </div>
        <% } %>
    </div>
    <div class="instruction-form alternateFiles" >
        <form id="altConfiguration" action="clientWebsiteConfiguration.jsp" method="post">
        <input type="hidden" id="fileName" name="siteName" value="">
        </form>
        <h2 style="margin-top: 0px;">Available Client Website Configuration Files</h2>
        <% for ( String file : configurationFiles ) { %>
            <button id="<%= file %>" class="configurationFile button"> <%= file %> </button>
        <% } %>
    </div>
</div>

<div style="position:relative;width:38%;margin:0px; text-align: center; padding: auto; padding-top: 50px; display: inline-block;min-width: 450px;">
    <div class="login-form">
            <div id="login">
              <h1>Options</h1>



            <p>&nbsp;</p>

            <form action="systemInformation.jsp" method="post">
                <button id="systemButton" class="button button-block"/>System Information</button>
            </form>
            <p>&nbsp;</p>
            <form action="jndi.jsp" method="post">
                <button id="jndiButton" class="button button-block"/>JNDI Context</button>
            </form>
            <p>&nbsp;</p>
            <form action="clientWebsiteConfiguration.jsp" method="post">
                <button id="configurationButton" class="button button-block"/>Site Configuration</button>
            </form>
            <p>&nbsp;</p>
            </div>
    </div>
</div>

</html>
