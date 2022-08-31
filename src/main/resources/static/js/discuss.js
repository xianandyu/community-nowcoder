$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

//置顶
function setTop(){
    $.post(
        CONSTANT_PATH + "/discuss/top",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data);
            if(data.code == 1){
                $("#topBtn").text(data.type==1?'已置顶':'置顶');
            }else {
                alert(data.msg)
            }
        }
    )
}

//加精
function setWonderful(){
    $.post(
        CONSTANT_PATH + "/discuss/wonderful",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data);
            if(data.code == 1){
                $("#wonderfulBtn").text(data.status==1?'已加精':'加精');
            }else {
                alert(data.msg)
            }
        }
    )
}

//删除
function setDelete(){
    $.post(
        CONSTANT_PATH + "/discuss/delete",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data);
            if(data.code == 1){
                alert(data.msg + "1秒后返回主页")
                setTimeout(function(){
                    location.href = CONSTANT_PATH + "/index";
                }, 1000);
            }else {
                alert(data.msg)
            }
        }
    )
}

function like(btn,entityType,entityId,entityUserId,postId){
    $.post(
        CONSTANT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function (data){
            data = $.parseJSON(data);
            if(data.code == 1){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
            }else {
                alert(data.msg)
            }
        }
    )
}