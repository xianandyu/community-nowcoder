$(function(){
    $("form").submit(check_data);
    $("input").focus(clear_error);
    $("#uploadForm").submit(upload);
});

function upload() {
    $.ajax({
        url: "http://upload-z2.qiniup.com",
        method: "post",
        processData: false,
        contentType: false,
        data: new FormData($("#uploadForm")[0]),
        success: function(data) {
            if(data && data.code == 0) {
                // 更新头像访问路径
                $.post(
                    CONSTANT_PATH + "/User/header/url",
                    {"fileName":$("input[name='key']").val()},
                    function(data) {
                        data = $.parseJSON(data);
                        if(data.code == 0) {
                            window.location.reload();
                        } else {
                            alert(data.msg);
                        }
                    }
                );
            } else {
                alert("上传失败!");
            }
        }
    });
    return false;
}

//检查密码
function check_data() {
    var pwd1 = $("#new-password").val();
    var pwd2 = $("#confirm-password").val();
    if(pwd1 != pwd2) {
        $("#confirm-password").addClass("is-invalid");
        return false;
    }
    return true;
}

//显示数据
function clear_error() {
    $(this).removeClass("is-invalid");
}