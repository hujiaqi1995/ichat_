const ws_url = "wss://jqhu.top/ws";
const redirect_url = "https://jqhu.top/ichat/chat.html";
const msg_and_content_url = "https://jqhu.top/ichats/msg/msgAndContact";
const login_url = "https://jqhu.top/ichats/user/login";
const captcha_id_url = "https://jqhu.top/ichats/user/captcha_id";
const captcha_src_url = "https://jqhu.top/ichats/user/captcha?captchaId=";

var current_other_uid = 0;

function init() {
    queryMsgAndContacts();
    if (window.WebSocket) {
        websocket = new WebSocket(ws_url);
        websocket.onmessage = function (event) {
            onmsg(event);
        };
        websocket.onopen = function () {
            bind();
            heartBeat.start();
        };
        websocket.onclose = function () {
            reconnect();
        };
        websocket.onerror = function () {
            reconnect();
        };
    } else {
        alert("您的浏览器不支持WebSocket协议！");
    }
}

function bind() { //上线
    if (window.WebSocket) {
        if (websocket.readyState == WebSocket.OPEN) {
            console.log("sender_id=" + sessionStorage.getItem("sender_id"));
            var bindJson = '{ "type": 1, "data": {"uid":' + sessionStorage.getItem("sender_id") + ' }}';
            websocket.send(bindJson);
        }
    }
}

//处理所有web端接收到的数据
var onmsg = function (event) {
    if (event != '') {
        heartBeat.reset();
        var resp = $.parseJSON(event.data);
        if (resp != null) {
            switch (resp.type) {
                case 1:
                    handleBindResp(resp);
                    break;

                case 2:
                    handleQueryMsgResp(resp);
                    queryUnread();
                    break;

                case 3:
                    handleSendMsgResp(resp);
                    queryUnread();
                    break;

                case 4:
                    handleReceivedMsg(resp);
                    queryUnread();
                    break;

                case 5:
                    handleLoopUnreadResp(resp);
                    break;
            }
        }
    }
};

function reconnect() {
    websocket = new WebSocket(ws_url);
    $("#ws_status").text("重新上线");
    websocket.onmessage = function (event) {
        onmsg(event);
    };

    websocket.onopen = function () {
        bind();
        heartBeat.start();
    };

    websocket.onclose = function () {
        reconnect();
    };

    websocket.onerror = function () {
        reconnect();
    };
}

function sendMsg(event) {
    event.preventDefault();
    var recipient_id = $("#recipient_id").val();
    var msg_content = $("#message-text").val();
    if(msg_content == "") {
        console.log("发送空消息！");
        return;
    }
    $("#message-text").val("");
    var sendMsgJson = '{ "type": 3, "data": {"senderUid":' + sessionStorage.getItem("sender_id") + ',"recipientUid":' + recipient_id + ', "content":"' + msg_content + '","msgType":1  }}';
    websocket.send(sendMsgJson);
}

/** 每2分钟发送一次心跳包，接收到消息或者服务端的响应又会重置来重新计时。 */
var heartBeat = {
    timeout: 120000,
    timeoutObj: null,
    serverTimeoutObj: null,
    reset: function () {
        clearTimeout(this.timeoutObj);
        clearTimeout(this.serverTimeoutObj);
        this.start();
    },
    start: function () {
        var self = this;
        this.timeoutObj = setTimeout(function () {
            var sendMsgJson = '{ "type": 0, "data": {"uid":' + sessionStorage.getItem("sender_id") + ',"timeout": 120000}}';
            websocket.send(sendMsgJson);
            self.serverTimeoutObj = setTimeout(function () {
                websocket.close();
                $("#ws_status").text("失去连接！");
            }, self.timeout)
        }, this.timeout)
    },
};

//轮询总未读
function queryUnread() {
    var queryUnreadJson = '{ "type": 5, "data": {"uid":' + sessionStorage.getItem("sender_id") + '}}';
    websocket.send(queryUnreadJson);
}

//通过websocket长连来查询消息
function queryMsg(event) {
    $('.chat-thread').empty();
    $("#self-chat-li-style").remove();
    $("#other-chat-li-style").remove();
    var button = $(event.relatedTarget);
    var recipient_id = button.data('recipient_id');
    var recipient_name = button.data('recipient_name');
    var modal = $("#chatModal");
    modal.find('.modal-title').text('给' + recipient_name + '发送信息：');
    modal.find("#recipient_id").val(recipient_id);
    modal.find("#recipient_name").val(recipient_name);

    var queryMsgJson = '{ "type": 2, "data": {"ownerUid":' + sessionStorage.getItem("sender_id") + ',"otherUid":' + recipient_id + ' }}';
    websocket.send(queryMsgJson);

    current_other_uid = recipient_id;
    var tbodyID = $(event.relatedTarget).parent().parent().parent().attr("id");
    if (tbodyID == "contactsMsgBody") {
        $(event.relatedTarget).parent().prev().text(0);
    }
}

//处理上线bind请求，主要是回显通知用户
function handleBindResp(resp) {
    var status = resp.status;
    if (status != "" && status == 'success') {
        $("#ws_status").text("欢迎回来，" + sessionStorage.getItem("nick") + "! ");
    } else {
        $("#ws_status").text("未上线!");
    }
}

//处理查询消息的响应，界面联动
function handleQueryMsgResp(resp) {
    var jsonObject = JSON.parse(resp.data);
    var ul_pane = $('.chat-thread');

    if (jsonObject != null) {
        // console.log("收到消息：" + JSON.stringify(jsonObject));

        var owner_uid = jsonObject.owner.uid;
        var owner_avatar = jsonObject.owner.avatar;

        var other_uid = jsonObject.other.uid;
        var other_avatar = jsonObject.other.avatar;

        var msgList = jsonObject.messageList;
        for (let i = 0; i < msgList.length; i++) {
            var li_msg = $('<li></li>');//创建一个li
            var relation_type = msgList[i].type;

            li_msg.attr("mid", msgList[i].mid);
            li_msg.text(msgList[i].content);
            li_msg.attr("other_uid", other_uid);
            li_msg.attr("create_time", msgList[i].createTime);
            li_msg.attr("avatar", 'url(/static/images/' + owner_avatar + ')');

            if (relation_type == 0 && owner_uid == sessionStorage.getItem("sender_id")) {
                li_msg.attr("id", "self-chat-li");
            } else if (relation_type == 1 && owner_uid == sessionStorage.getItem("sender_id")) {
                li_msg.attr("id", "other-chat-li");
            }
            ul_pane.append(li_msg);
        }
        $('.chat-thread').animate({scrollTop: $('.chat-thread').prop("scrollHeight")}, 100);

        $("<style id='self-chat-li-style'>#self-chat-li:before{background-image:url('/static/images/" + owner_avatar + "')}</style>").appendTo('head');
        $("<style id='other-chat-li-style'>#other-chat-li:before{background-image:url('/static/images/" + other_avatar + "')}</style>").appendTo('head');
    }
}


//处理发消息的发送方的响应
function handleSendMsgResp(resp) {
    // console.log("收到发送的响应消息：" + resp.data);
    var jsonObject = JSON.parse(resp.data);
    var ul_pane = $('.chat-thread');
    var li_current = $('<li></li>');//创建一个li

    var other_uid = jsonObject.other.uid;
    var other_avatar = jsonObject.other.avatar;
    var other_nick = jsonObject.other.nick;

    var mid = jsonObject.messageList[0].mid;
    var content = jsonObject.messageList[0].content;
    var createTime = jsonObject.messageList[0].createTime;

    li_current.attr("id", "self-chat-li");
    li_current.text(content);
    li_current.attr("mid", mid);
    li_current.attr("other_uid", other_uid);
    li_current.attr("create_time", createTime);
    li_current.attr("avatar", 'url(/static/images/' + other_avatar + ')');
    ul_pane.append(li_current);
    $('.chat-thread').animate({scrollTop: $('.chat-thread').prop("scrollHeight")});
    
    var currentContactsTR = $("#contactsMsgBody tr");
    var flag1 = false;
    if (currentContactsTR) {
        $.each(currentContactsTR, function (i, contactTR) {
            var chat_uid = $(contactTR).attr("chat_uid");
            if (chat_uid == other_uid) {
                $(contactTR).children(":nth-child(3)").text(content);
                flag1 = true;
            }
        });
    }

    if (!flag1) {
        var td_images = "<td><img width='50px' src='/static/images/" + other_avatar + "'/></td>";
        var td_otherName = "<td>" + other_nick + "</td>";
        var td_content = "<td>" + content + "</td>";
        var td_convUnread = "<td>0</td>";
        var td_button = "<td><button type='button' class='btn btn-info' data-toggle='modal' data-target='#chatModal' data-recipient_id='"
            + other_uid + "' data-recipient_name='" + other_nick + "'>和他聊天</button></td>";
        var tr_html = "<tr chat_uid='" + other_uid + "'>" + td_images + td_otherName + td_content + td_convUnread + td_button + "</tr>";
        $("#contactsMsgBody").prepend(tr_html);
    }
}

//处理接收到的推送消息，主要是最近联系人界面和聊天界面的展示
function handleReceivedMsg(pushedMsg) {
    var jsonRecipient = pushedMsg.data;
    var tid = pushedMsg.tid;
    // console.log("jsonRecipient " + JSON.stringify(jsonRecipient));
    if (tid != "") {
        var ackJson = '{ "type": 6, "data": {"tid":' + tid + ' }}';
        websocket.send(ackJson);
    }
    var ul_pane = $('.chat-thread');
    var li_current = $('<li></li>');//创建一个li
    var flag2 = false;

    var owner_avatar = jsonRecipient.owner.avatar;
    var owner_uid = jsonRecipient.owner.uid;
    var owner_name = jsonRecipient.owner.nick;

    var mid = jsonRecipient.messageList[0].mid;
    var content = jsonRecipient.messageList[0].content;

    if(owner_uid== current_other_uid) {
        li_current.attr("id", "other-chat-li");
        li_current.text(content);
        li_current.attr("mid", mid);
        li_current.attr("other_uid", owner_uid);
        li_current.attr("avatar", 'url(/static/images/' + owner_uid + ')');
        ul_pane.append(li_current);
        $('.chat-thread').animate({scrollTop: $('.chat-thread').prop("scrollHeight")});
    }
    

    var currentContactsTR = $("#contactsMsgBody tr");

    $.each(currentContactsTR, function (i, contactTR) {
        var chat_uid = $(contactTR).attr("chat_uid");
        if (chat_uid == owner_uid) {
            $(contactTR).children(":nth-child(3)").text(content);
            var unread = parseInt($(contactTR).children(":nth-child(4)").text());
            $(contactTR).children(":nth-child(4)").text(unread + 1);
            flag2 = true;
        }
    });

    if (flag2 == false) {
        var td_images = "<td><img width='50px' src='/static/images/" + owner_avatar + "'/></td>";
        var td_otherName = "<td>" + owner_name + "</td>";
        var td_content = "<td>" + content + "</td>";
        var td_convUnread = "<td>1</td>";
        var td_button = "<td><button type='button' class='btn btn-info' data-toggle='modal' data-target='#chatModal' data-recipient_id='"
            + owner_uid + "' data-recipient_name='" + owner_name + "'>和他聊天</button></td>";
        var tr_html = "<tr chat_uid='" + owner_uid + "'>" + td_images + td_otherName + td_content + td_convUnread + td_button + "</tr>";
        $("#contactsMsgBody").prepend(tr_html);
    }
}

//处理轮询总未读的响应
function handleLoopUnreadResp(resp) {
    var totalUnreadData = resp.data;
    $("#totalUnread").text(totalUnreadData.unread);
}


function queryMsgAndContacts() {
    console.log("token:" + sessionStorage.getItem("token"));
    $.ajax({
        url: msg_and_content_url,
        type: "GET",
        beforeSend: function (XMLHttpRequest) {
            XMLHttpRequest.setRequestHeader("Authorization", "Bearer " + sessionStorage.getItem("token"));
        },
        success: function (returnData) {
            var msgContact = returnData.data.msgContact;
            var total_unread = msgContact.totalUnread;

            $("#totalUnread").text(total_unread);
            for (var i = 0; i < msgContact.msgList.length; i++) {
                var avatar = msgContact.msgList[i].avatar;
                var other_nick = msgContact.msgList[i].nick;
                var other_uid = msgContact.msgList[i].uid;
                var content = msgContact.msgList[i].content;
                var unread = msgContact.msgList[i].unread;

                var td_images = "<td><img width='50px' src='/static/images/" + avatar + "'/></td>";
                var td_otherName = "<td>" + other_nick + "</td>";
                var td_content = "<td>" + content + "</td>";
                var td_convUnread = "<td>" + unread + "</td>";
                var td_button = "<td><button type='button' class='btn btn-info' data-toggle='modal' data-target='#chatModal' data-recipient_id='"
                    + other_uid + "' data-recipient_name='" + other_nick + "'>和他聊天</button></td>";
                var tr_html = "<tr chat_uid='" + other_uid + "'>" + td_images + td_otherName + td_content + td_convUnread + td_button + "</tr>";
                $("#contactsMsgBody").prepend(tr_html);
            }

            // 联系人部分
            var contact_vo = returnData.data.contact;
            var contactList = contact_vo.contacts;

            for (var i = 0; i < contactList.length; i++) {
                var avatar = contactList[i].avatar;
                var nick = contactList[i].nick;
                var other_uid = contactList[i].uid;

                var td_images = "<td><img width='50px' src='/static/images/" + avatar + "'/></td>";
                var td_name = "<td>" + nick + "</td>";
                var td_button = "<td><button type='button' class='btn btn-info' data-toggle='modal' " +
                    "data-target='#chatModal' data-recipient_id='" + other_uid + "' data-recipient_name='" + nick + "' >和他聊天</button></td>>"
                var tr_html = "<tr>" + td_images + td_name + td_button + "</tr>";
                $("#contactsBody").prepend(tr_html);
            }
        },
        error: function (data) {
            alert("发生异常！");
            console.log(data);
        }
    });
}

document.onkeydown = function(e) {
    var theEvent = window.event || e;
    var code = theEvent.keyCode || theEvent.which;
    if (code == 13) {
        $("#loginbutton").click();
    }
};

$(document).ready(function () {
    $("#loginbutton").click(function () {
        console.log($('#login-form').serialize()+"&captchaId="+sessionStorage.getItem("captcha_id"));
        $.ajax({
            type: "POST",
            dataType: "json",
            url: login_url,
            data: $('#login-form').serialize()+"&captchaId="+sessionStorage.getItem("captcha_id"),
            success: function (result) {
                if (result.code == 200) {
                    console.log("登录成功："+result);
                    sessionStorage.setItem("token", result.data.token);
                    sessionStorage.setItem("sender_id", result.data.owner.uid);
                    sessionStorage.setItem("nick", result.data.owner.nick);
                    window.location.href=redirect_url;
                } else {
                    $("#error_reason").text(result.message);
                }
            },
            error : function(data) {
                alert("发生异常！");
                console.log(data);
            },
            
        });
        
    })
});

function getCaptcha() {
    $.ajax({
        url: captcha_id_url,
        type: "GET",
        success:function(res){
            var captcha_id = res.data;
            $("#captchaId").text(captcha_id);
            sessionStorage.setItem("captcha_id", captcha_id);
            document.getElementById("captcha_image").src=captcha_src_url+captcha_id+"&"+ Math.random(); 
        }
    });
}