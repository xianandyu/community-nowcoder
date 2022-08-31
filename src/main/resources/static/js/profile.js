$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		$.post(
			CONSTANT_PATH + "/follow",
			{"entityType":3,"entityId":$(btn).prev().val()},
			function (data){
				data = $.parseJSON(data)
				if(data.code == 1){
					// 关注TA
					//$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
					window.location.reload()
				}else {
					alert(data.msg)
				}
			}
		)
	} else {
		$.post(
			CONSTANT_PATH + "/unfollow",
			{"entityType":3,"entityId":$(btn).prev().val()},
			function (data){
				data = $.parseJSON(data)
				if(data.code == 1){
					// 取消关注
					//$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
					window.location.reload()
				}else {
					alert(data.msg)
				}
			}
		)
	}
}