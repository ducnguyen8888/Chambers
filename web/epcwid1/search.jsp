<%@ page import="java.util.*" 
%><%@ include file="/epcwid1/configuration.inc" %><%--
--%><%
%><%@ include file="header.jsp" %>
    <style> 
        fieldset { border-color: transparent; xbackground-color: cyan;}
        legend { margin: 0 auto; }
        #property-search-block { clear:both; position: relative; margin: 2rem auto auto auto; width: 58rem; height: 43rem; overflow: none;
        xborder: 1px solid red; }
        #property-search-block div.content { position: absolute; top: 2rem; bottom: 0px; width: 100%;  
                                        background-color: whitesmoke;  border: 1px solid black;   
                                        border-top-right-radius: 8px 8px; border-bottom-right-radius: 6px; border-bottom-left-radius: 6px;
                                        }

        #property-search-block div.tab { display: inline; clear: none; float: left; z-index: 4; } 
        #property-search-block div.tab input[type="radio"] { visibility: hidden; position: absolute; z-index: -1; }
        #property-search-block div.tab:first-child label { border-top-left-radius: 6px; } 
        #property-search-block div.tab:last-child label { border-top-right-radius: 6px; } 
        #property-search-block div.tab label { display: table-cell; position: relative; z-index: 2;
                                        height: 1rem; width: 8rem; max-width: 8rem; padding: .6rem .3rem .3rem .3rem;
                                        font-family: Arial, Helvetica, sans-serif; font-weight: bold; text-align: center; font-size: 0.8rem; line-height: 1.0rem;
                                        background-color: midnightblue;
                                        background: -webkit-gradient(linear, left top, left bottom, from(midnightblue), to(#0078a5));
                                        background: -moz-linear-gradient(top,  midnightblue,  #0078a5);
                                        background: -webkit-linear-gradient(top,  midnightblue,  #0078a5);
                                        background: linear-gradient(to bottom,  midnightblue 0%,#0078a5 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
                                        background: -ms-linear-gradient(to bottom,  midnightblue 0%,#0078a5 100%);
                                        filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='midnightblue', endColorstr='#0078a5',GradientType=0 ); /* IE6-9 */
                                        color: whitesmoke; border: 1px solid midnightblue;  
                                        }

        #property-search-block div.tab input[type="radio"] ~ label {
                                        font-weight: normal; text-align: center; font-size: 1.0rem; line-height: 1.0rem;
                                        background: none; background-color: darkgrey; color: black;
                                        border-left-color: whitesmoke; 
                                        }
        #property-search-block div.tab input[type="radio"]:not(:checked) ~ label:hover { 
                                        background-color: dimgrey; color: whitesmoke; cursor: pointer;
                                        }

        #property-search-block div.tab input[type="radio"]:checked ~ label { 
                                        font-style: italic; font-weight: bold; color: #A30000; 
                                        background-color: whitesmoke;  
                                        border-top: 2px solid black; border-bottom: 1px solid whitesmoke; border-right: 1px solid black; cursor: default;
                                        }


        #property-search-block div.tab input[type="radio"] ~ div.tab-content { 
                                        display: none; position: absolute; left: 0px; right: 0px; bottom: 0px; top: 2.5rem;
                                        padding: 1rem; text-align: center; font-size: 1.0rem;
                                        }
        #property-search-block div.tab input[type="radio"]:checked ~ div.tab-content { display: block; }

        #property-search-block .search-textinput {
                                        margin: 0; padding: 5px 10px; width: 20rem;
                                        font-family: Arial, Helvetica, sans-serif; font-size: 1.1rem;
                                        border:1px solid #0076a3; border-right:0px; border-top-left-radius: 6px; border-bottom-left-radius: 6px;
                                        background: white url("search.png") no-repeat 6px 8px; outline: none;
                                        }
        #property-search-block input[type="submit"] {
                                        margin: 0; padding: 5px 15px;
                                        font-family: Arial, Helvetica, sans-serif; font-size:14px; font-size: 1.1rem; color: #ffffff;
                                        outline: none; cursor: pointer; text-align: center; text-decoration: none;
                                        border: solid 1px #0076a3; border-right:0px; border-top-right-radius: 4px; border-bottom-right-radius: 4px;
                                        background-color: midnightblue;
                                        background: -webkit-gradient(linear, left top, left bottom, from(midnightblue), to(#0078a5));
                                        background: -moz-linear-gradient(top,  midnightblue,  #0078a5);
                                        background: -webkit-linear-gradient(top,  midnightblue,  #0078a5);
                                        background: linear-gradient(to bottom,  midnightblue 0%,#0078a5 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
                                        background: -ms-linear-gradient(to bottom,  midnightblue 0%,#0078a5 100%);
                                        filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='midnightblue', endColorstr='#0078a5',GradientType=0 ); /* IE6-9 */
                                        }
        #property-search-block input[type="submit"].text-button { padding: 3px 5px; font-size: 0.7rem; border-radius: 4px; margin-left: 3px; margin-right: 3px; }
        #property-search-block input[type="submit"]:hover {
                                        background-color: #00adee;
                                        background: -webkit-gradient(linear, left top, left bottom, from(#00adee), to(#0078a5));
                                        background: -moz-linear-gradient(top,  #00adee,  #0078a5);
                                        background: -webkit-linear-gradient(top,  #00adee,  #0078a5);
                                        background: linear-gradient(to bottom,  #00adee 0%,#0078a5 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
                                        background: -ms-linear-gradient(to bottom,  #00adee 0%,#0078a5 100%);
                                        filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#00adee', endColorstr='#0078a5',GradientType=0 ); /* IE6-9 */
                                        }
        #property-search-block input[type="submit"]:active {
                                        background-color: #0078a5;
                                        background: -webkit-gradient(linear, left top, left bottom, from(#0078a5), to(#00adee));
                                        background: -moz-linear-gradient(top,  #0078a5,  #00adee);
                                        background: -webkit-linear-gradient(top,  #0078a5,  #00adee);
                                        background: linear-gradient(to bottom, #0078a5 100%,  #00adee 0%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
                                        background: -ms-linear-gradient(to bottom, #0078a5 100%,  #00adee 0%);
                                        filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#0078a5', endColorstr='#0078a5',GradientType=0 ); /* IE6-9 */
                                        }
        /* Fixes submit button height problem in Firefox */
        .search-button::-moz-focus-inner { border: 0; }


        /* Optional search criteria input */
        #property-search-block input[type="button"] {
                                        margin: 0; padding: 5px 15px;
                                        font-family: Arial, Helvetica, sans-serif; font-size:14px; font-size: 1.1rem; color: #ffffff;
                                        outline: none; cursor: pointer; text-align: center; text-decoration: none;
                                        border: solid 1px #0076a3; border-right:0px; border-top-left-radius: 4px; border-bottom-left-radius: 4px;
                                        background-color: #0078a5; cursor: default;
                                        }
        #property-search-block .secondary-textinput{
                                        margin: 0; padding: 5px 5px 5px 15px;
                                        font-family: Arial, Helvetica, sans-serif; font-size: 1.1rem;
                                        border:1px solid #0076a3; border-left:0px; border-top-right-radius: 6px; border-bottom-right-radius: 6px;
                                        outline: none;
                                        }

        #property-search-block div.tab-content div.search-example > table { clear: both;  text-align: center; margin-left: auto; 
                                    margin-right: auto; margin-top: 1rem; margin-bottom: 1.5rem; padding: 4px; border-collapse: collapse; 
                                    }
        #property-search-block div.tab-content div.search-example > table * { padding: 15px; }

        #property-search-block div.tab-content { color: midnightblue; font-family: Arial, Helvetica, sans-serif; font-size: 1.4rem; }
        #property-search-block div.tab-content h2 { font-size: 1.7rem; font-weight: bold; clear: both; margin: 0px 0px 15px 0px; color: #A30000; }

        #property-search-block .search-criteria-block { margin: 2.2rem; }

        #property-search-block div.tab-content div.search-selection-note { clear: both; margin-bottom: 25px; color: black; font-style: italic; font-weight: bold; }
        #property-search-block div.tab-content div.search-description { clear: both; margin-bottom: 35x; }
        #property-search-block div.tab-content div.search-secondary-info { clear: both; margin-bottom: 45px; }
        #property-search-block div.tab-content div.search-example { clear: both; margin-bottom: 30px; }


        hr.fade-hr { 
                                    border: 0; width: 60%; xfloat:right; height: 1px; 
                                    background-image: -moz-linear-gradient(left, #f0f0f0, #8c8b8b, #f0f0f0);
                                    background-image: -webkit-linear-gradient(left, #f0f0f0, #8c8b8b, #f0f0f0);
                                    background-image: linear-gradient(left, #f0f0f0, #8c8b8b, #f0f0f0);
                                    background-image: -ms-linear-gradient(left, #f0f0f0, #8c8b8b, #f0f0f0);
                                    background-image: -o-linear-gradient(left, #f0f0f0, #8c8b8b, #f0f0f0); 
                                    }


        #property-search-block div.tab .anchor { display: table-cell; position: relative; z-index: 1; border-top-left-radius: 6px;
                                        height: 1.2rem; width: 11rem; max-width: 11rem; padding: 10px 10px 2px 5px;
                                        font-family: Arial, Helvetica, sans-serif; font-weight: bold; text-align: right; font-size: 1.0rem; line-height: 1.0rem;
                                        background-color: midnightblue;
                                        color: whitesmoke; border: 1px solid midnightblue;  
                                        }


    /* clear background w/ full-size border - small "select search type" label */
    .sm-default	#property-search-block div.content {
                    background-color: transparent;
                    }
    .sm-default #property-search-block div.tab input[type="radio"]:checked ~ label {
                    background-color: transparent; 
                    border-bottom-color: white;
                    }

    /* clear background w/ full-size border - small "select search type" label */
    .lg-default	#property-search-block div.content {
                    background-color: transparent;
                    }
    .lg-default #property-search-block div.tab input[type="radio"]:checked ~ label {
                    background-color: transparent; 
                    border-bottom-color: white;
                    }
    .lg-default #property-search-block div.tab .anchor {
                    height: 7.0rem;
                    }

    /* clear background w/ no border - large "select search type" label */
    .lg-tabonly #property-search-block div.content {
                    border: none; background-color: transparent;
                    }
    .lg-tabonly #property-search-block div.tab input[type="radio"]:checked ~ label {
                    background-color: transparent; 
                    border-bottom-color: white;
                    }
    .lg-tabonly #property-search-block div.tab .anchor {
                    height: 7.0rem;
                    border-bottom-left-radius: 6px;
                    }


    /* clear background w/ no border - small "select search type" label */
    .sm-tabonly	#property-search-block div.content {
                    border: none; background-color: transparent;
                    }
    .sm-tabonly #property-search-block div.tab input[type="radio"]:checked ~ label {
                    background-color: transparent; 
                    border-bottom-color: white;
                    }
    .sm-tabonly #property-search-block div.tab .anchor {
                    height: 1.2rem;
                    border-bottom-left-radius: 6px;
                    }



    /* default background w/ header border - large "select search type" label */
    .lg-header-block #property-search-block div.content {
                    height: 5.7rem;
                    }
    .lg-header-block #property-search-block div.tab .anchor {
                    height: 7.0rem;
                    border-bottom-left-radius: 6px;
                    }


    /* default background w/ header border - large "select search type" label */
    .sm-header-block #property-search-block div.content {
                    height: 5.7rem;
                    }
    .sm-header-block #property-search-block div.tab .anchor {
                    height: 1.2rem;
                    }









    /* clear background w/ full-size border - small "select search type" label */
    .background-clear	#property-search-block div.content {
                    background-color: transparent;
                    }
    .background-clear #property-search-block div.tab input[type="radio"]:checked ~ label {
                    background-color: transparent; 
                    border-bottom-color: white;
                    }

    /* grey background w/ full-size border - small "select search type" label */
    .background-grey	#property-search-block div.content {
                    background-color: whitesmoke;
                    }
    .background-grey #property-search-block div.tab input[type="radio"]:checked ~ label {
                    background-color: whitesmoke; 
                    }

    /* default background w/ header border - large "select search type" label */
    .label-large #property-search-block div.tab .anchor {
                    height: 7.0rem;
                    border-bottom-left-radius: 6px;
                    }


    /* default background w/ header border - large "select search type" label */
    .label-small #property-search-block div.tab .anchor {
                    height: 1.2rem;
                    }




    .border-none #property-search-block div.content {
                    border: none; 
                    }

    .border-short #property-search-block div.content {
                    border: 1px solid black; 
                    }
    .border-short #property-search-block div.content {
                    height: 5.7rem;
                    }

    .border-full #property-search-block div.content {
                    border: 1px solid black; 
                    }
    .border-full #property-search-block div.content {
                    height: auto;
                    }


    .tab-large #property-search-block div.tab label { 
                                    height: 42px; top: -36px; border-top-left-radius: 15px; padding-top: 20px;
                                    }


    .tab-location-low #property-search-block div.tab label { 
                                    top: 32px; 
                                    }
    .tab-location-low #property-search-block div.tab .tab-content { 
                                    margin-top: 32px; 
                                    }
    .tab-location-low #property-search-block div.tab:last-child label { border-right: none; border-top-right-radius: 0px;} 
    .tab-location-low #property-search-block div.tab input[type="radio"]:checked ~ label { border-right: none; border-top: 1px solid black;} 




    xxbody {
      animation: colorchange 20s infinite; /* animation-name followed by duration in seconds*/
         /* you could also use milliseconds (ms) or something like 2.5s */
      -webkit-animation: colorchange 20s infinite; /* Chrome and Safari */
    }

    @keyframes colorchange
    {
      0%   {background: white;}
      25%  {background: #8f8f8f;}
      50%  {background: #f6f6f6;}
      75%  {background: #8f8f8f;}
      100% {background: #fafafa;}
    }

    @-webkit-keyframes colorchange /* Safari and Chrome - necessary duplicate */
    {
      0%   {background: white;}
      25%  {background: #8f8f8f;}
      50%  {background: #f6f6f6;}
      75%  {background: #8f8f8f;}
      100% {background: #fafafa;}
    }




    </style>


    <fieldset><legend><h1>Find Your Property Tax Balance</h1></legend>
    <div id="property-search-block">

            <div class="content"></div>
            <div class="tabs-block">
                <div class="tab">
                    <!--<label for="nameSearch" style="border-top-left-radius: 8px; text-align: right;width: 140px; padding-left: 0px; "> Select Search Type: </label>-->
                    <div class="anchor">Select Search Type: </div>
                </div>
                <div class="tab">
                    <input type="radio" id="nameSearch" name="search-type" value="name" checked aria-label="search by name">
                    <label class="tab-label" for="nameSearch"> Name Search </label>
                    <div class="tab-content">
                    <form class="search-form" action="searchResults.jsp" method="post">
                        <input type="hidden" name="search-type" value="name">
                        <h2> Search by Owner Name </h2>
                        <div class="search-selection-note">
                            To select a different search type select from the tabs above.
                        </div>
                        <div class="search-description">
                            Enter the owner name (last name first) to search for and press the <input type="submit" value="search" class="search-button text-button"> button. 
                            <div class="search-criteria-block">
                                <input type="search" class="search-textinput" placeholder="Enter your search here" name="criteria" size="20" maxlength="120" 
                                        aria-label="name search criteria" autofocus><input type="submit" value="search" class="search-button">
                            </div>
                            <hr class="fade-hr"><br>
                        </div>
                        <div class="search-secondary-info">
                            You may enter additional name or address search criteria to filter the results of your search. 
                            <div class="search-term-input" style="margin-top: 20px;margin-bottom: 30px;">
                                <input type="button" value="optional:" class="secondary-button" disabled><input aria-label="optional name or address criteria"
                                type="search" class="secondary-textinput" placeholder="Enter additional name or address criteria" name="altCriteria" size="38" maxlength="120">
                            </div>
                        </div>
                        <div class="search-example">
                            <table border="1">
                                <tr> <th> To Search For: </th> <th> Enter the following: </th>
                                </tr>
                                <tr>
                                    <td> Mary Smith </td>
                                    <td> <strong> smith mary </strong></td>
                                </tr>
                            </table>
                            Owner names are typically recorded with the last name first.
                        </div>
                    </form>
                    </div>
                </div>
                <div class="tab">
                    <input type="radio" id="addressSearch" name="search-type" value="address" aria-label="search by address">
                    <label class="tab-label" for="addressSearch"> Address Search </label>
                    <div class="tab-content"> 
                        <form class="search-form" action="searchResults.jsp" method="post">
                            <input type="hidden" name="search-type" value="address">
                            <h2> Search by Property Address </h2>
                            <div class="search-selection-note">
                                To select a different search type select from the tabs above.
                            </div>
                            <div class="search-description">
                                Enter the property address to search for and press the <input type="submit" value="search" class="search-button text-button"> button. 
                                <div class="search-criteria-block">
                                    <input type="search" class="search-textinput" placeholder="Enter your search here" name="criteria" size="20" maxlength="120" aria-label="address search criteria"
                                        ><input type="submit" value="search" class="search-button">
                                </div>
                                <hr class="fade-hr"><br>
                            </div>
                            <div class="search-secondary-info">
                                You may enter additional name or address search criteria to filter the results of your search. 
                                <div class="search-term-input" style="margin-top: 20px;margin-bottom: 30px;">
                                    <input type="button" value="optional:" class="secondary-button" disabled><input aria-label="optional name or address criteria"
                                    type="search" class="secondary-textinput" placeholder="Enter additional name or address criteria" name="altCriteria" size="38" maxlength="120">
                                </div>
                            </div>

                        <div class="search-example">
                            <table border="1">
                                <tr> <th> To Search For: </th> <th> Enter the following: </th>
                                </tr>
                                <tr>
                                    <td> 8907 Park St (property location) </td>
                                    <td> <strong> 8907 park </strong></td>
                                </tr>
                            </table>
                            Avoid including terms like <span style="font-style:italic;">"street"</span>, <span style="font-style:italic;">"lane"</span>,
                            <span style="font-style:italic;">"boulevard"</span>, or similar designations<br>in your search. These may be abbreviated or shortened by the Appraisal District.
                        </div>
                        </form>
                    </div>
                </div>

                <div class="tab">
                    <input type="radio" id="accountSearch" name="search-type" value="account" aria-label="search by account number">
                    <label class="tab-label" for="accountSearch"> Account Search </label>
                    <div class="tab-content"> 
                        <form class="search-form" action="searchResults.jsp" method="post">
                            <input type="hidden" name="search-type" value="account">
                        <h2> Search by Property Account Number </h2>
                        <div class="search-selection-note">
                            To select a different search type select from the tabs above.
                        </div>
                        <div class="search-description">
                            Enter the property account number to search for and press the <input type="submit" value="search" class="search-button text-button"> button. 
                            <div class="search-criteria-block">
                                <input type="search" class="search-textinput" placeholder="Enter your search here" name="criteria" size="20" maxlength="120" aria-label="account search criteria"
                                    ><input type="submit" value="search" class="search-button">
                            </div>
                            <hr class="fade-hr"><br>
                        </div>

                        <div class="search-example">
                            <table border="1">
                                <tr> <th> To Search For: </th> <th> Enter the following: </th>
                                </tr>
                                <tr>
                                    <td> account number 8560020030040906 </td>
                                    <td> <strong> 8560020030040906 </strong></td>
                                </tr>
                            </table>
                        </div>
                        </form>
                    </div>
                </div>

                <div class="tab">
                    <input type="radio" id="cadSearch" name="search-type" value="cad" aria-label="search by appraisal district number">
                    <label class="tab-label" for="cadSearch"> CAD Search </label>
                    <div class="tab-content"> 
                        <form class="search-form" action="searchResults.jsp" method="post">
                            <input type="hidden" name="search-type" value="cad">
                        <h2> Search by CAD Reference Number </h2>
                        <div class="search-selection-note">
                            To select a different search type select from the tabs above.
                        </div>
                        <div class="search-description">
                            Enter the CAD reference number to search for and press the <input type="submit" value="search" class="search-button text-button"> button. 
                            <div class="search-criteria-block">
                                <input type="search" class="search-textinput" placeholder="Enter your search here" name="criteria" size="20" maxlength="120" aria-label="CAD search criteria"
                                    ><input type="submit" value="search" class="search-button">
                            </div>
                            <hr class="fade-hr"><br>
                        </div>

                        <div class="search-example">
                            <table border="1">
                                <tr> <th> To Search For: </th> <th> Enter the following: </th>
                                </tr>
                                <tr>
                                    <td> CAD Reference Number R212884 </td>
                                    <td> <strong> R212884  </strong></td>
                                </tr>
                            </table>
                            The CAD reference number is the property account number used by the Appraisal District. <br>
                            This account number may be different than the property account number used by the<br> Tax Office.
                        </div>
                        </form>
                    </div>
                </div>
                <!--
                <div class="tab">
                    <input type="radio" id="fidoSearch" name="search-type" value="fido" aria-label="search by fiduciary requests">
                    <label class="tab-label" for="fidoSearch"> FIDO Search </label>
                    <div class="tab-content"> 
                        <form class="search-form" action="searchResults.jsp">
                            <input type="hidden" name="search-type" value="fido">
                        <h2> Search by Fiduciary Requests </h2>
                        <div class="search-selection-note">
                            To select a different search type select from the tabs above.
                        </div>
                        <div class="search-description">
                            Enter the Fiduciary number to search for and press the <input type="submit" value="search" class="search-button text-button"> button. 
                            <div class="search-criteria-block">
                                <input type="search" class="search-textinput" placeholder="Enter your search here" name="criteria" size="20" maxlength="120" aria-label="fiduciary search criteria"
                                    ><input type="submit" value="search" class="search-button">
                            </div>
                            <hr class="fade-hr"><br>
                        </div>
                        <div class="search-secondary-info">
                            You may enter additional name or address search criteria to filter the results of your search. 
                            <div class="search-term-input" style="margin-top: 20px;margin-bottom: 30px;">
                                <input type="button" value="optional:" class="secondary-button" disabled><input aria-label="optional name or address criteria"
                                type="search" class="secondary-textinput" placeholder="Enter additional name or address criteria" name="altCriteria" size="38" maxlength="120">
                            </div>
                        </div>

                        <div class="search-example">
                            <table border="1">
                                <tr> <th> To Search For: </th> <th> Enter the following: </th>
                                </tr>
                                <tr>
                                    <td> all accounts with statements requested by<br>fiduciary number 2066 (Washington Mutual) </td>
                                    <td> <strong> 2066 </strong></td>
                                </tr>
                            </table>
                            The Fiduciary number is assigned by the Tax Office and is a business related search. <br>
                            This number is not used by individual tax payers.
                            <br><br>
                            <span style="color: #A30000;font-weight: bold;">Important:</span> <i> Fiduciary searches may take several minutes to complete</i>
                        </div>
                        </form>
                    </div>
                </div>
                -->

            </div>

    </div>
    </fieldset>

<%@ include file="footer.jsp" %>