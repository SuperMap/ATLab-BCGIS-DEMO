$(document).ready(function () {

    // 导航栏
    $("#header").html("<ul> \
        <li><a href=\"\"><b> >>== 区块链GIS系统DEMO ==<< </b></a></li> \
        <li><a href=\"index.html\" id=\"index_bar\">显示地图</a></li> \
        <li><a href=\"MapPublish.html\" id=\"mappublish_bar\">发布地图</a></li> \
        <li><a href=\"MapList.html\" id=\"maplist_bar\">地图列表</a></li> \
        <li><a href=\"#\" id=\"info\"></a></li> \
        </ul>");

    // 发布地图
    $("#publish_btn").click(function () {
        var params = {
            "workspaceName": document.getElementById("publish_workspace").value,
            "datastoreName": document.getElementById("publish_datastore").value,
            "featuretypeName": document.getElementById("publish_layer").value  
        };
        $.ajax({
            type: 'post',
            contentType: "application/json",
            url: 'http://localhost:8899/bcgis/mapservice/wms/publish',
            data: JSON.stringify(params),
            success: function (data) {
                console.log('data: ' + JSON.stringify(data));
            },
            error: function (err) {
                console.log('err: ');
                console.log(JSON.stringify(err));
            }
        });
    });

    // 查询已发布的地图
    $("#maplist_btn").click(function () {
        $.ajax({
            type: 'get',
            url: 'http://localhost:8899/bcgis/mapservice/wms/list',
            data: {
                workspaceName: document.getElementById("maplist_workspace").value,
                datastoreName: document.getElementById("maplist_datastore").value,
            },
            success: function (data) {
                console.log('data: ');
                console.log(data);
                document.getElementById("maps").innerHTML = JSON.stringify(data);
            },
            error: function (err) {
                console.log('err: ');
                console.log(err);
            }
        });
    });
});