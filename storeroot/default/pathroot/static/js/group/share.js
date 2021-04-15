$(document).ready(function(){
	//格式化日期
	Date.prototype.format = function(format){
		 var o = {
		 "M+" : this.getMonth()+1, //month
		 "d+" : this.getDate(),    //day
		 "h+" : this.getHours(),   //hour
		 "m+" : this.getMinutes(), //minute
		 "s+" : this.getSeconds(), //second
		 "q+" : Math.floor((this.getMonth()+3)/3),  //quarter
		 "S" : this.getMilliseconds() //millisecond
		 }
		 if(/(y+)/.test(format)) {
			 format=format.replace(RegExp.$1,this.getFullYear());
		 }
		 
		 for(var k in o){
			 if(new RegExp("("+ k +")").test(format)){
				 format = format.replace(RegExp.$1,RegExp.$1.length==1 ? o[k] :("00"+ o[k]).substr((""+ o[k]).length));
			 }
				 
		 }
		 return format;
	}
	Detail.init();
});
(function($){
	window.Detail={
		init:function(){
			this["cuid"]=$("#uid")//FK_RPC.invoke("getUserId",[]);
			//Login.init(this["cuid"],"@{_p.type}");
			this.voteStatus();
			this.bindEvent();
			//电子圈名我榜
			var _type = '@{_p.cate}';
			if(_type=='nomore'){
				$(".m_btn").hide();
			}
		},
		bindEvent:function(){
			var $this = this//,r = FK_RPC.invoke("countShare",['@{_p.type}']);
			//$("#countShare").text("还有"+r+"人分享了该话题>>");
			$("li a").click(function(_obj){
				//var start=$("ul li").length,obj=FK_RPC.invoke("comment",[$(this).attr("data-qid"),start-1,15]),h=[];
				if($(this).attr("class")=='join'){
					//window.location.href=$('#ctp').val()+"/mobile_app.jsp?auto=true";
					return;
				}
				if(obj.length==0){
					$(this).parent().hide();
				}else{
					for(var i=0;i<obj.length;i++){
						h.push('<li class="one_command">');
						h.push('<div class="face"><a href="/user/userpage.html?currentId='+obj[i].uid+'&tag=@{_p.tag}"><img src="http://img.mapkc.com/img/30x30f_'+obj[i].img+'"/></a></div>');
						h.push('<div class="text">');
						h.push('<div><div class="fr gray">'+new Date(obj[i].ctime).format("yyyy-MM-dd hh:mm:ss")+'</div><a href="/user/userpage.html?currentId='+obj[i].uid+'&tag=@{_p.tag}">'+obj[i].name+'</a></div>');
						
						if(obj[i].to_userId!=0){
							h.push('回复:');
							h.push('<a href="/user/userpage.html?currentId='+obj[i].to_userId+'">'+obj[i].to_uname+'</a>&nbsp;'+obj[i].content);
						}else{
							h.push('<div>'+obj[i].content+'</div>');
						}
						h.push('</div>');
						h.push('<div class="register_formddd" style="display:none">');
						h.push('<textarea class="default_textaaa" placeholder="回复:'+obj[i].name+'"></textarea>');
						h.push('<input type="button" value="提交" class="join_btn" style="cursor:pointer;cursor:hand" uid="'+obj[i].uid+'" cid="'+obj[i].id+'" qid="@{_p.qid}"/>');
						h.push('</div>');
						h.push('<div class="clear"></div>');
						h.push('</li>');
					}
					$(this).parent().parent().prev().append(h.join(""));
					$this.reply();
					if(obj.length<15){
						$(this).parent().hide();
					}
				}
			});
			//踩赞事件绑定
			$("input[type=button]").click(function(){
				var el=$(this),t=el.attr("flag");
				t=="up"&&$this.process(el,t);
				t=="down"&&$this.process(el,t);
				t=="comment"&&$this.process(el,t);
			});
			this.reply();
		},
		process:function(el,t){
			var $this = this;
			if(this["cuid"]!=0){
				this["cuid"] = uid;
				var tid=el.attr("data-tid"),ctype=el.attr("data-type"),uid=el.attr("uid");
				if(uid==$this["cuid"] && t=="up"){
					alert("不能对自己的分享进行赞");
					return;
				} if(uid==$this["cuid"] && t=="down"){
					alert("不能对自己的分享进行踩");
					return;
				}
				if(t=="comment"){
					var ingroup=eval($("#ingroup").val());
					if(!ingroup){
						alert("你还不是圈的成员,只有该");
						return;
					}
					window.location.href=$('#ctp').val()+"/g/share/comment.htm?tid="+tid;
					return;
				} else{
					var url=$("#ctp").val()+"/m/g/share/agree.htm";
					$.post(url,{"tid":tid},function(data){
						var code=data.code;
						var cid=data.data;
						if (code==0&&cid>0) {
							var n = $("."+t).find("span").text();
							$("."+t).find("span").text(parseInt(n)+1);
							t=="up" && this.setVoteStatus(1);
							t=="down" && this.setVoteStatus(-1);
						} else if (data.code>0) {
							alert(data.actionMessages);
						} else {
							alert(r);
						}
					},"json");
				}
			}else{
				window.location.href="/login?from=@url{rc.req.uri}";
			}
		},
		reply:function(){
			$("ul li").click(function(){
				var el = $(this),i=el.index();
				$(".one_command .text").each(function(index,d){
					if(i==index){
						el.find(".text").next().show();
					}else{
						$(this).next().hide();
					}
				});
			});
			$("input[class=join_btn]").click(function(){
				var el=$(this),_v = el.prev().val(),cid=el.attr("cid");
				if($.trim(_v)==""){
					alert("回复不能为空");
					return;
				}
				var url=$("#ctp").val()+"/m/g/share/reply.htm";
				$.post(url,{"cid":cid,"content":_v},function(data){
					var code=data.code;
					var cid=data.data;
					if (code==0&&cid>0) {
						el.parent().hide();
					} else if (data.code>0) {
						alert(data.actionMessages);
					} else {
						alert(r);
					}
				},"json");
				
				return false;
			});
			
		},
		voteStatus:function(){
			var obj = null;//FK_RPC.invoke("voteStatus",['@{_p.qid}']);
			this.setVoteStatus(obj);
		},
		setVoteStatus:function(obj){
			if(obj==1){
				$("input[flag=up]").val("已赞");
				$("input[flag=up]").attr("disabled","disabled");
				$("input[flag=down]").attr("disabled","disabled");
			}else if(obj==-1){
				$("input[flag=down]").val("已踩");
				$("input[flag=up]").attr("disabled","disabled");
				$("input[flag=down]").attr("disabled","disabled");
			}
		}
	}
})(jQuery);
