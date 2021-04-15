$("input.invite").click(function(){
	var uid=$(this).attr("uid");
	var gid=$(this).attr("gid");
	var url=$(this).attr("url");
    $.post(url, {"uids": uid,"id":gid}, function (data) {
           if(data.checklogin){
        	   window.location.href="/login?from"+encodeURIComponent(window.location);
           }
           if(data.allow){
	   			window.location.href="/login?from"+window.location;
           }else{
        	   setTimeout(function(){
        		   window.location.href="/login?from"+window.location;
        	   },1000);
           }
        }, 'json');
})
$("a.join").click(function(){
	var gid=$(this).attr("gid");
	var url=$(this).attr("url");
	$.post(url, {"id":gid}, function (data) {
		 if(data.code==2003){
			 alert("你还沒登录,请您先登录");
			 var __from=encodeURIComponent(window.location);
      	   window.location.href="/login?from="+__from;
      	   return;
         }
		 if(data.exception){
			 alert(data.msg);
			 return;
		 }
		 if(data.data.allow){
				alert("恭喜您,加入成功");
	   			window.location.href="/g/share.htm?gid="+gid;
        }else{
        	alert("您的申请己提交，请等待审核...");
        	$(this).css("backgroud-color","#ccc");
        	$("a.join").unbind();
        }
	}, 'json');
})
$("a.exit").click(function(){
	var gid=$(this).attr("gid");
	var url=$(this).attr("url");
	$.post(url, {"id":gid}, function (data) {
		alert("退出成功");
		window.location.href="/group/my.htm";
	}, 'json');
})