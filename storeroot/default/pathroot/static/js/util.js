(function($){
	window.Login={
		init:function(uid,t){
			this["cuid"] = uid;
			this["type"] = t;
			this["height"]=$(document).height();
			this.loginHtml();
		},
		loginHtml:function(){
			var h=[];
			h.push('<div style="display:none" id="login-win" class="float_login">');
			h.push('<div class="float_title"><span class="fr"><a style="cursor:pointer;cursor:hand" id="win-close">X</a></span>登录电子圈</div>');
				h.push('<div class="float_form">');
					h.push('<form>');
						h.push('<div>电子圈账号：<input type="text" name="account" placeholder="邮箱/手机号码"/></div>');
						h.push('<div>登录密码：<input type="password" name="passwd"/></div>');
						h.push('<div class="m_btn">');
							h.push('<input type="button" value="登录" class="join_btn"/>');
						h.push('</div>');
					h.push('</form>');
					h.push('<a href="/register/register?type='+this["type"]+'">还没有账号？立即注册</a>');
				h.push('</div>');
			h.push('</div>');
			h.push('<div style="display:none" class="float_shadow" id="shadow"></div>');
			$("body").append(h.join(""));
			this.bindEvent();
		},
		login:function(a,p){//登陆操作
			var r = FK_RPC.invoke("login",[a,p]),js=$.parseJSON(r);
			
			if(js.msg!=""){
				alert(js.msg);
			}else{
				this["cuid"]=js.uid;
				this["cb"](js.uid);
				this.showOrHideLogin("hide");
			}
		},
		showOrHideLogin:function(t){//隐藏或显示登陆框
			if(t=="show"){
				if($("#login-win").is(":hidden")){
					$("#login-win").show();
					$("#login-win").css({"top":$(document).scrollTop(),"margin-top":"0px"});
					$("#shadow").css("height",this["height"]).show();
				}
			}else if(t=="hide"){
				$("#login-win").hide();
				$("#shadow").hide();
			}
		},
		islogin:function(callback){//判断是否登陆
            //当前用户id
            if(this["cuid"]==0){
                this["cb"]=callback;
                this.showOrHideLogin("show");
                return;
            }else{
                callback();
            }
        },
		bindEvent:function(){
			var $this = this;
			//关闭弹出的登陆框
			$("#win-close").click(function(){
				$("#login-win").hide();
				$("#shadow").hide();
			});
			
			$(".join_btn").click(function(){
				var a = $("input[name=account]").val(),p=$("input[name=passwd]").val();
				if($.trim(a)==""){
					alert("电子圈账号不能为空");
					return;
				}
				if($.trim(p)==""){
					alert("登录密码不能为空");
					return;
				}
				$this.login(a,p);
			})
		}
	}
	/*.star{
		float:left;margin-right:2px;width:19px;height:19px;background: url('/static/img/dianping/xing_grey.png') no-repeat;cursor:pointer
	}*/
	$.fn.extend({
		
		star:function(options){
			/*要改变样式，在函数的参数列表中加入light,grey,如:
			 * $(".a").star({light:"class1","grey":"class2"});
			 */
			$(this).each(function(){
				var _p = $(this);
				var device = (/android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/i.test(navigator.userAgent.toLowerCase()));
			    var clickEvtName = device ? 'touchstart' : 'mousedown';
			    var moveEvtName = device? 'touchmove': 'mousemove';
			    var overEvtName = device? 'touchover': 'mouseover';
			    var outEvtName = device? 'touchout': 'mouseout';
				//先产生5颗星
				var opts = $.extend({},options);
				var iCount = opts.num||5;
				var frontText = opts.frontText||"";
				var _light = opts.light||"xing_light";
				var _grey = opts.grey||"xing_grey";
				var _html = [];
				_html.push(frontText);
				for(var i=0;i<iCount;i++){
					_html.push('<span class="'+_grey+' showstar" index="'+(i+1)+'" flag="unclick"></span>');
				}
				_p.append(_html.join(""));
				_p.find(".showstar").bind(overEvtName,function(){
					var index = $(this).attr("index");
					for(var i=parseInt(index);i>0;i--){
						if(_p.find("span[index="+i+"]").attr("flag")=='unclick'){
							//_p.find("span[index="+i+"]").css("background","url('/static/img/dianping/xing_light.png') no-repeat");
							_p.find("span[index="+i+"]").removeClass(_grey);
							_p.find("span[index="+i+"]").addClass(_light);
						}
					}
				});
				_p.find(".showstar").bind(outEvtName,function(){
					var index = $(this).attr("index");
					_p.find(".showstar").each(function(i,d){
						if($(this).attr("flag")=='unclick'){
							//_p.find("span[index="+(i+1)+"]").css("background","url('/static/img/dianping/xing_grey.png') no-repeat");
							_p.find("span[index="+(i+1)+"]").removeClass(_light);
							_p.find("span[index="+(i+1)+"]").addClass(_grey);
						}
					})
				});
				_p.find(".showstar").bind(clickEvtName,function(){
					var index = $(this).attr("index");
					_p.find(".showstar").each(function(i,d){
						if(i<parseInt(index)){
							//_p.find("span[index="+(i+1)+"]").css("background","url('/static/img/dianping/xing_light.png') no-repeat");
							_p.find("span[index="+(i+1)+"]").removeClass(_grey);
							_p.find("span[index="+(i+1)+"]").addClass(_light);
							$(this).attr("flag","click");
						}else{
							//_p.find("span[index="+(i+1)+"]").css("background","url('/static/img/dianping/xing_grey.png') no-repeat");
							_p.find("span[index="+(i+1)+"]").removeClass(_light);
							_p.find("span[index="+(i+1)+"]").addClass(_grey);
							$(this).attr("flag","unclick");
						}
					});
				});
			});
		},
		showstar:function(options){
			var opts = $.extend({},options);
			var isShowScore = typeof(opts.showScore)=='undefined'?true:opts.showScore;
			$(this).each(function(i,d){
				var score = $(this).attr("data-score"),_s = parseFloat(score),_si = parseInt(_s),_html=[],f=true;
				for(var i=0;i<5;i++){
					if(_si>=(i+1)){
						_html.push('<span class="xing_light"></span>');
					}else if(_s>_si && f){
						f=!1;
						_html.push('<span class="xing_half"></span>');
					}else{
						_html.push('<span class="xing_grey"></span>');
					}
				}
				if(isShowScore){
					_html.push('&nbsp;'+_s);
				}
				if(typeof($(this).attr("data-text"))!='undefined'){
					_html.push('&nbsp;'+$(this).attr("data-text"));
				}
				$(this).html(_html.join(""));
			});
		}
	});
	
})(jQuery);