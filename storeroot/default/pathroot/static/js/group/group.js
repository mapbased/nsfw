$("div.click_more").find("a").on("click", function() {  
	var url=$(this).attr("url");
	url=url.replace("//","/");
	var start=$('ul.list').find('li.item').length+1;
    $.post(url, {"start": start}, function (data) {
           $('ul.list').append(data);
        }, 'html');
});
$("a.save").on("click", function() {
	var url=$(this).attr("url");
	var form=$("#groupForm");
	var action=form.attr("action");
	var data=form.serialize();
	$.post(action, data, function(data) {
		alert(data.msg);
		location.href=url+"?id="+data.gid;
	},"json");
});
