<#include "/org/alfresco/repository/admin/admin-template.ftl" />

<!--
    Copyright (c) Ascensio System SIA 2019. All rights reserved.
    http://www.onlyoffice.com
-->

<@page title=msg("onlyoffice-config.title") readonly=true>

</form>
<div class="column-full">
   <@section label=msg("onlyoffice-config.doc-section") />

   <form id="docservcfg" action="${url.service}" method="POST" accept-charset="utf-8">
      <div class="control field">
         <label class="label" for="onlyurl">${msg("onlyoffice-config.doc-url")}</label>
         <br/>
         <input class="value" id="onlyurl" name="url" size="35" placeholder="http://docserver/" title="${msg('onlyoffice-config.doc-url-tooltip')}" pattern="http(s)?://.*" value="${callbackurl}" />
      </div>
      <div class="control field">
         <label class="label" for="jwtsecret">${msg("onlyoffice-config.jwt-secret")}</label>
         <br/>
         <input class="value" id="jwtsecret" name="url" size="35" value="${jwtsecret}" />
      </div>
      <div class="control field">
         <input class="value" id="onlycert" name="cert" type="checkbox" ${cert} />
         <label class="label" for="onlycert">${msg("onlyoffice-config.ignore-ssl-cert")}</label>
      </div>
      <input id="postonlycfg" type="button" value="${msg('onlyoffice-config.save-btn')}"/>
   </form>
   <br>
   <span data-saved="${msg('onlyoffice-config.saved')}" data-error="${msg('onlyoffice-config.error')}" id="onlyresponse" class="message hidden"></span>
</div>

<script type="text/javascript">//<![CDATA[
   (function() {
      var url = document.getElementById("onlyurl");
      var cert = document.getElementById("onlycert");
      var jwts = document.getElementById("jwtsecret");
      var jwth = document.getElementById("jwtheader");

      var form = document.getElementById("docservcfg");
      var btn = document.getElementById("postonlycfg");
      var msg = document.getElementById("onlyresponse");

      var doPost = function(obj) {
         var xhr = new XMLHttpRequest();
         xhr.open("POST", form.action, true);
         xhr.setRequestHeader("Content-type", "application/json");
         xhr.setRequestHeader("Accept", "application/json");
         xhr.overrideMimeType("application/json");

         xhr.onload = function () { callback(xhr); };

         xhr.send(JSON.stringify(obj));
      };

      var callback = function(xhr) {
         btn.disabled = false;

         if (xhr.status != 200) {
               showMessage(msg.dataset.error + " status code " + xhr.status, true);
               return;
         }

         var response = JSON.parse(xhr.response);

         if (!response.success) {
               showMessage(msg.dataset.error + " " + response.message, true);
               return;
         }

         showMessage(msg.dataset.saved);
      };

      var parseForm = function() {
         var obj = {};

         var reg = RegExp(url.pattern);
         if (!reg.test(url.value)) { return null; }

         obj.url = url.value.trim();
         obj.cert = cert.checked.toString();
         obj.jwtsecret = jwts.value.trim();
         obj.jwtheader = jwth.value.trim();

         return obj;
      };

      var hideMessage = function() {
         msg.classList.add("hidden");
         msg.classList.remove("error");
         msg.innerText = "";
      };

      var showMessage = function(message, error) {
         if (error) {
               msg.classList.add("error");
         }

         msg.innerText = message;
         msg.classList.remove("hidden");
      };

      btn.onclick = function() {
         hideMessage();
         if (btn.disabled) return;

         var obj = parseForm();
         if (!obj) return;

         btn.disabled = true;
         doPost(obj);
      };
   })();
//]]></script>
</@page>