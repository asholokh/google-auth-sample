$("#btnCancelRegister").click(function () {
    window.location.replace("index.html");
});

$("#btnCancelLogin").click(function () {
    window.location.replace("index.html");
});

$("#btnRegisterDone").click(function () {
    window.location.replace("index.html");
});

$("#btnLogin").click(function () {
    $.post("/authenticate/" + $("#login").val() + "/" + $("#password").val(), function (data, status) {
        if (data == 'AUTHENTICATED') {
            window.location.replace("secured.html");
        } else if (data == "REQUIRE_TOKEN_CHECK") {
            $("#modalLoginCheckToken").modal('show');
        } else if (data == "FAILED") {
            $('.alert').alert();
        }
    });
});

$("#btnRegister").click(function () {
    $.post("/register/" + $("#login").val() + "/" + $("#password").val(), function (data, status) {
        if (status == 'success') {
            $("#tokenQr").attr("src", "https://zxing.org/w/chart?cht=qr&chs=250x250&chld=M&choe=UTF-8&chl=otpauth://totp/2FaExample.com?secret=" + data + "&issuer=2FaExample");
            $("#tokenValue").text(data);
            $("#modalRegister").modal('show');
        }
    });
});

$("#btnTokenVerify").click(function () {
    $.post("/authenticate/token/" + $("#login").val() + "/" + $("#password").val() + "/" + $("#loginToken").val(), function (data, status) {
        if (data == 'AUTHENTICATED') {
            window.location.replace("secured.html");
        } else if (data == "FAILED") {
            $('.alert').alert();
        }
    });
});