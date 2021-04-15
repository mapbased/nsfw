/* Riot v2.0.11, @license MIT, (c) 2015 Muut Inc. + contributors */
(function(){var e={version:"v2.0.11",settings:{}};"use strict";e.observable=function(e){e=e||{};var t={},n=0;e.on=function(r,i){if(typeof i=="function"){i._id=typeof i._id=="undefined"?n++:i._id;r.replace(/\S+/g,function(e,n){(t[e]=t[e]||[]).push(i);i.typed=n>0})}return e};e.off=function(n,r){if(n=="*")t={};else{n.replace(/\S+/g,function(e){if(r){var n=t[e];for(var i=0,o;o=n&&n[i];++i){if(o._id==r._id){n.splice(i,1);i--}}}else{t[e]=[]}})}return e};e.one=function(t,n){if(n)n.one=1;return e.on(t,n)};e.trigger=function(n){var r=[].slice.call(arguments,1),i=t[n]||[];for(var o=0,u;u=i[o];++o){if(!u.busy){u.busy=1;u.apply(e,u.typed?[n].concat(r):r);if(u.one){i.splice(o,1);o--}else if(i[o]!==u){o--}u.busy=0}}return e};return e};(function(e,t){if(!this.top)return;var n=location,r=e.observable(),i=window,o;function u(){return n.hash.slice(1)}function f(e){return e.split("/")}function a(e){if(e.type)e=u();if(e!=o){r.trigger.apply(null,["H"].concat(f(e)));o=e}}var c=e.route=function(e){if(e[0]){n.hash=e;a(e)}else{r.on("H",e)}};c.exec=function(e){e.apply(null,f(u()))};c.parser=function(e){f=e};i.addEventListener?i.addEventListener(t,a,false):i.attachEvent("on"+t,a)})(e,"hashchange");var t=function(t,n,r){return function(i){n=e.settings.brackets||t;if(r!=n)r=n.split(" ");return i&&i.test?n==t?i:RegExp(i.source.replace(/\{/g,r[0].replace(/(?=.)/g,"\\")).replace(/\}/g,r[1].replace(/(?=.)/g,"\\")),i.global?"g":""):r[i]}}("{ }");var n=function(){var e={},n=/({[\s\S]*?})/,r=/(['"\/]).*?[^\\]\1|\.\w*|\w*:|\b(?:(?:new|typeof|in|instanceof) |(?:this|true|false|null|undefined)\b|function *\()|([a-z_]\w*)/gi;return function(t,n){return t&&(e[t]=e[t]||i(t))(n)};function i(e,r){r=(e||t(0)+t(1)).replace(t(/\\{/),"￰").replace(t(/\\}/),"￱").split(t(n));return new Function("d","return "+(!r[0]&&!r[2]&&!r[3]?o(r[1]):"["+r.map(function(e,t){return t%2?o(e,1):'"'+e.replace(/\n/g,"\\n").replace(/"/g,'\\"')+'"'}).join(",")+'].join("")').replace(/\uFFF0/g,t(0)).replace(/\uFFF1/g,t(1))+";")}function o(e,n){e=e.replace(/\n/g," ").replace(t(/^[{ ]+|[ }]+$|\/\*.+?\*\//g),"");return/^\s*[\w- "']+ *:/.test(e)?"["+e.replace(/\W*([\w- ]+)\W*:([^,]+)/g,function(e,t,n){return n.replace(/[^&|=!><]+/g,u)+'?"'+t.trim()+'":"",'})+'].join(" ")':u(e,n)}function u(e,t){e=e.trim();return!e?"":"(function(v){try{v="+(e.replace(r,function(e,t,n){return n?"(d."+n+"===undefined?window."+n+":d."+n+")":e})||"x")+"}finally{return "+(t?'!v&&v!==0?"":v':"v")+"}}).call(d)"}}();function r(e){var n={val:e},r=e.split(/\s+in\s+/);if(r[1]){n.val=t(0)+r[1];r=r[0].slice(t(0).length).trim().split(/,\s*/);n.key=r[0];n.pos=r[1]}return n}function i(e,t,n){var r={};r[e.key]=t;if(e.pos)r[e.pos]=n;return r}function o(e,t,o){d(e,"each");var u=e.outerHTML,f=e.previousSibling,c=e.parentNode,l=[],s=[],m;o=r(o);function v(e,t,n){l.splice(e,0,t);s.splice(e,0,n)}t.one("update",function(){c.removeChild(e)}).one("premount",function(){if(c.stub)c=t.root}).on("update",function(){var e=n(o.val,t);if(!e)return;if(!Array.isArray(e)){var r=JSON.stringify(e);if(r==m)return;m=r;p(s,function(e){e.unmount()});l=[];s=[];e=Object.keys(e).map(function(t){return i(o,t,e[t])})}p(h(l,e),function(e){var t=l.indexOf(e),n=s[t];if(n){n.unmount();l.splice(t,1);s.splice(t,1)}});var d=c.childNodes,g=[].indexOf.call(d,f);p(e,function(n,r){var f=e.indexOf(n,r),p=l.indexOf(n,r);f<0&&(f=e.lastIndexOf(n,r));p<0&&(p=l.lastIndexOf(n,r));if(p<0){if(!m&&o.key)n=i(o,n,f);var h=new a({tmpl:u},{before:d[g+1+f],parent:t,root:c,loop:true,item:n});return v(f,n,h)}if(o.pos&&s[p][o.pos]!=f){s[p].one("update",function(e){e[o.pos]=f});s[p].update()}if(f!=p){c.insertBefore(d[g+p+1],d[g+f+1]);return v(f,l.splice(p,1)[0],s.splice(p,1)[0])}});l=e.slice()})}function u(e,t,n){g(e,function(e){if(e.nodeType==1){p(e.attributes,function(n){if(/^(name|id)$/.test(n.name))t[n.value]=e})}})}function f(e,n,r){function i(e,n,i){if(n.indexOf(t(0))>=0){var o={dom:e,expr:n};r.push(m(o,i))}}g(e,function(e){var t=e.nodeType;if(t==3&&e.parentNode.tagName!="STYLE")i(e,e.nodeValue);if(t!=1)return;var r=e.getAttribute("each");if(r){o(e,n,r);return false}p(e.attributes,function(t){var n=t.name,r=n.split("__")[1];i(e,t.value,{attr:r||n,bool:r});if(r){d(e,n);return false}});var u=w[e.tagName.toLowerCase()];if(u){u=new a(u,{root:e,parent:n});return false}})}function a(t,r){var i=e.observable(this),o=b(r.opts)||{},a=v(t.tmpl),c=r.parent,l=r.loop,d=[],g=r.root,h=r.item,y={},w;m(this,{parent:c,root:g,opts:o},h);p(g.attributes,function(e){y[e.name]=e.value});function x(e){p(Object.keys(y),function(e){o[e]=n(y[e],c||i)})}this.update=function(e,t){m(i,e,h);x();i.trigger("update",h);s(d,i,h);i.trigger("updated")};this.unmount=function(){var e=l?w:g,t=e.parentNode;if(t){if(c)t.removeChild(e);else while(g.firstChild)g.removeChild(g.firstChild);i.trigger("unmount");c&&c.off("update",i.update).off("unmount",i.unmount);i.off("*")}};function k(){i.trigger("premount");if(l){w=a.firstChild;g.insertBefore(w,r.before||null)}else{while(a.firstChild)g.appendChild(a.firstChild)}if(g.stub)i.root=g=c.root;c&&c.on("update",i.update).one("unmount",i.unmount);i.trigger("mount")}x();u(a,this);if(t.fn)t.fn.call(this,o);f(a,this,d);this.update();k()}function c(e,t,n,r,i){n[e]=function(e){e=e||window.event;e.which=e.which||e.charCode||e.keyCode;e.target=e.target||e.srcElement;e.currentTarget=n;e.item=i;if(t.call(r,e)!==true){e.preventDefault&&e.preventDefault();e.returnValue=false}var o=i?r.parent:r;o.update()}}function l(e,t,n){if(e){e.insertBefore(n,t);e.removeChild(t)}}function s(e,t,r){p(e,function(e){var i=e.dom,o=e.attr,u=n(e.expr,t);if(u==null)u="";if(e.value===u)return;e.value=u;if(!o)return i.nodeValue=u;d(i,o);if(typeof u=="function"){c(o,u,i,t,r)}else if(o=="if"){var f=e.stub;if(u){f&&l(f.parentNode,f,i)}else{f=e.stub=f||document.createTextNode("");l(i.parentNode,i,f)}}else if(/^(show|hide)$/.test(o)){if(o=="hide")u=!u;i.style.display=u?"":"none"}else if(o=="value"){i.value=u}else if(o.slice(0,4)=="riot"){o=o.slice(5);u?i.setAttribute(o,u):d(i,o)}else{if(e.bool){i[o]=u;if(!u)return;u=o}if(typeof u!="object")i.setAttribute(o,u)}})}function p(e,t){for(var n=0,r=(e||[]).length,i;n<r;n++){i=e[n];if(i!=null&&t(i,n)===false)n--}return e}function d(e,t){e.removeAttribute(t)}function m(e,t,n){t&&p(Object.keys(t),function(n){e[n]=t[n]});return n?m(e,n):e}function v(e){var t=e.trim().slice(1,3).toLowerCase(),n=/td|th/.test(t)?"tr":t=="tr"?"tbody":"div",r=document.createElement(n);r.stub=true;r.innerHTML=e;return r}function g(e,t){if(e){if(t(e)===false)g(e.nextSibling,t);else{e=e.firstChild;while(e){g(e,t);e=e.nextSibling}}}}function h(e,t){return e.filter(function(e){return t.indexOf(e)<0})}function b(e){function t(){}t.prototype=e;return new t}var y=[],w={};function x(e){var t=document.createElement("style");t.innerHTML=e;document.head.appendChild(t)}function k(e,t,n){var r=w[t];if(r&&e){r=new a(r,{root:e,opts:n});y.push(r);return r.on("unmount",function(){y.splice(y.indexOf(r),1)})}}e.tag=function(e,t,n,r){if(typeof n=="function")r=n;else if(n)x(n);w[e]={name:e,tmpl:t,fn:r}};e.mount=function(e,t,n){if(e=="*")e=Object.keys(w).join(", ");if(typeof t=="object"){n=t;t=0}var r=[];function i(e){var i=t||e.tagName.toLowerCase(),o=k(e,i,n);if(o)r.push(o)}if(e.tagName){i(e);return r[0]}else{p(document.querySelectorAll(e),i);return r}};e.update=function(){return p(y,function(e){e.update()})};e.mountTo=e.mount;(function(t){var n=("allowfullscreen,async,autofocus,autoplay,checked,compact,controls,declare,default,"+"defaultchecked,defaultmuted,defaultselected,defer,disabled,draggable,enabled,formnovalidate,hidden,"+"indeterminate,inert,ismap,itemscope,loop,multiple,muted,nohref,noresize,noshade,novalidate,nowrap,open,"+"pauseonexit,readonly,required,reversed,scoped,seamless,selected,sortable,spellcheck,translate,truespeed,"+"typemustmatch,visible").split(",");var r="area,base,br,col,command,embed,hr,img,input,keygen,link,meta,param,source,track,wbr".split(",");var i={jade:C};var o={coffeescript:b,none:k,cs:b,es6:y,typescript:w,livescript:x,ls:x};var u=/^<([\w\-]+)>(.*)<\/\1>/gim,f=/=({[^}]+})([\s\/\>])/g,a=/([\w\-]+)=(["'])([^\2]+?)\2/g,c=/{\s*([^}]+)\s*}/g,l=/^<([\w\-]+)>([^\x00]*[\w\/}]>$)?([^\x00]*?)^<\/\1>/gim,s=/<script(\s+type=['"]?([^>'"]+)['"]?)?>([^\x00]*?)<\/script>/gm,p=/<style(\s+type=['"]?([^>'"]+)['"]?)?>([^\x00]*?)<\/style>/gm,d=/<!--.*?-->/g,m=/<([\w\-]+)([^>]*)\/\s*>/g,v=/^\s*\/\/.*$/gm,g=/\/\*[^\x00]*?\*\//gm;function h(t,i,o){var u=e.util.brackets;t=t.replace(/\s+/g," ");t=t.trim().replace(d,"");t=t.replace(u(f),'="$1"$2');t=t.replace(a,function(e,t,r,i){if(i.indexOf(u(0))>=0){t=t.toLowerCase();if(/style|src/.test(t))t="riot-"+t;else if(n.indexOf(t)>=0)t="__"+t}return t+'="'+i+'"'});if(i.expr){t=t.replace(u(c),function(e,t){var n=E(t,i,o).trim().replace(/\r?\n|\r/g,"").trim();if(n.slice(-1)==";")n=n.slice(0,-1);return u(0)+n+u(1)})}t=t.replace(m,function(e,t,n){var i="<"+t+(n?" "+n.trim():"")+">";if(r.indexOf(t.toLowerCase())==-1)i+="</"+t+">";return i});t=t.replace(/'/g,"\\'");t=t.replace(u(/\\{|\\}/g),"\\$&");if(i.compact)t=t.replace(/> </g,"><");return t}function b(e){return require("coffee-script").compile(e,{bare:true})}function y(e){return require("babel").transform(e,{blacklist:["useStrict"]}).code}function w(e){return require("typescript-simple")(e)}function x(e){return require("LiveScript").compile(e,{bare:true,header:false})}function k(e){return e}function C(e){return require("jade").render(e,{pretty:true})}function O(e){e=e.replace(v,"").replace(g,"");var t=e.split("\n"),n="";t.forEach(function(e,r){var i=e.trim();if(i[0]!="}"&&i.indexOf("(")>0&&i.indexOf("function")==-1){var o=/[{}]/.exec(i.slice(-1)),u=o&&/(\s+)([\w]+)\s*\(([\w,\s]*)\)\s*\{/.exec(e);if(u&&!/^(if|while|switch|for)$/.test(u[2])){t[r]=u[1]+"this."+u[2]+" = function("+u[3]+") {";if(o[0]=="}"){t[r]+=" "+i.slice(u[0].length-1,-1)+"}.bind(this)"}else{n=u[1]}}}if(e.slice(0,n.length+1)==n+"}"){t[r]=n+"}.bind(this);";n=""}});return t.join("\n")}function E(e,t,n){var r=t.parser||(n?o[n]:O);if(!r)throw new Error('Parser not found "'+n+'"');return r(e,t)}function T(e,t){var n=i[e];if(!n)throw new Error('Template parser not found "'+e+'"');return n(t)}function j(e,t){e=e.replace(/\s+/g," ");e=e.trim();e=e.replace(/'/g,"\\'");return e}function L(e,t,n,r){return"riot.tag('"+e+"', '"+t+"'"+(n?", '"+n+"'":"")+", function(opts) {"+r+"\n});"}function S(t,n){n=n||{};if(n.brackets)e.settings.brackets=n.brackets;if(n.template)t=T(n.template,t);t=t.replace(u,function(e,t,r){return L(t,h(r,n),"","")});return t.replace(l,function(e,t,r,i){r=r||"";var o=n.type;if(!i.trim()){r=r.replace(s,function(e,t,n,r){if(n)o=n.replace("text/","");i=r;return""})}var u="";var f="css";r=r.replace(p,function(e,t,n,r){if(n)f=n.replace("text/","");u=r;return""});return L(t,h(r,n,o),j(u,f),E(i,n,o))})}if(t){this.riot=require(process.env.RIOT||"../riot");return module.exports={html:h,compile:S}}var N=document,q,_;function A(e,t){var n=new XMLHttpRequest;n.onreadystatechange=function(){if(n.readyState==4&&n.status==200)t(n.responseText)};n.open("GET",e,true);n.send("")}function $(e){var t=/[ \t]+/.exec(e);if(t)e=e.replace(new RegExp("^"+t[0],"gm"),"");return e}function F(e){var t=N.createElement("script"),n=N.documentElement;t.text=S(e);n.appendChild(t);n.removeChild(t)}function H(e){var t=N.querySelectorAll('script[type="riot/tag"]');[].map.call(t,function(n,r){var i=n.getAttribute("src");function o(n){F(n);if(r+1==t.length){q.trigger("ready");_=true;e&&e()}}return i?A(i,o):o($(n.innerHTML))})}e.compile=function(t,n){if(typeof t=="string"){if(t.trim()[0]=="<"){var r=$(S(t));if(!n)F(r);return r}else{return A(t,function(e){var t=$(S(e));F(t);n&&n(t,e)})}}if(typeof t!="function")t=undefined;if(_)return t&&t();if(q){t&&q.on("ready",t)}else{q=e.observable();H(t)}};var M=e.mount;e.mount=function(t,n,r){var i;e.compile(function(){i=M(t,n,r)});return i};e.mountTo=e.mount})(!this.top);e.util={brackets:t,tmpl:n};if(typeof exports==="object")module.exports=e;else if(typeof define==="function"&&define.amd)define(function(){return e});else this.riot=e})();