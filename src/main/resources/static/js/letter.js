$(function(){
	$("#sendBtn").click(send_letter);
	$(".closeMessage").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");

	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		CONSTANT_PATH + "/letter/send",
		{"toName":toName,"content":content},
		function (data){
			data = $.parseJSON(data);
			if(data.code == 1){
				$("#hintBody").text("发送成功!");
			}else {
				$("#hintBody").text(data.msg);
			}

			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload()
			}, 2000);
		}
	)
}

function delete_msg() {
	// TODO 删除数据
	var id = $("#commentId").val();
	$.post(
		CONSTANT_PATH + "/letter/delete",
		{"id":id},
		function (data){
			data = $.parseJSON(data);
			if(data.code == 1){
				$(this).parents(".media").remove();
			}else {
				alert(data.msg);
			}

			setTimeout(function(){
				location.reload()
			}, 100);
		}
	)
}