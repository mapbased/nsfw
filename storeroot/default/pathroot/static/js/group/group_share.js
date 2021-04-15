$("input.share").click(function(){
	var gid=$(this).attr("gid");
	var url=$(this).attr("url");
	var detail=$('#share-detail').val();
    $.post(url, {"share.gid": gid,"share.title":detail}, function (data) {
           alert(data.msg);
        }, 'html');
})
