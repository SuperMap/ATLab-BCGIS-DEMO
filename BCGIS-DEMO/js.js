
/**
 *  展示系统的界面设置   
 */

$(document).ready(function () {

    // 导航栏
    $("#header").html("<ul> \
        <li><a href=\"\"><b> >>== 区块链GIS系统DEMO ==<< </b></a></li> \
        <li><a href=\"index.html\" id=\"index_bar\">显示地图</a></li> \
        <li><a href=\"MapPublish.html\" id=\"mappublish_bar\">发布地图</a></li> \
        <li><a href=\"MapList.html\" id=\"maplist_bar\">地图列表</a></li> \
        <li><a href=\"#\" id=\"info\"></a></li> \
        </ul>");

    // 发布地图（时间为点击）
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

    // 分析的选择，当选择不同按钮时出现不同的显示效果
    $("#analysis_type_select").click(function () {
        switch ($("#analysis_type_select").val()) {

            case "buffer":
                $("#params").show();

                $("#attributesParams").hide();
                $("#analysis_btn_union").hide();
                $("#analysis_btn_intersect").hide();
                $("#spatialParams").hide();
                $("#attributes_type_label").hide();
                break;

            case "union":
                $("#analysis_btn_union").show();

                $("#params").hide();
                $("#attributesParams").hide();
                $("#analysis_btn_intersect").hide();
                $("#spatialParams").hide();
                $("#attributes_type_label").hide();
                break;

            case "intersection":
                $("#analysis_btn_intersect").show();

                $("#params").hide();
                $("#attributesParams").hide();
                $("#analysis_btn_union").hide();
                $("#spatialParams").hide();
                $("#attributes_type_label").hide();
                break;

            case "attributes":
                $("#attributesParams").show();
                $("#attributes_analysis_input").show();

                $("#params").hide();
                $("#analysis_btn_union").hide();
                $("#analysis_btn_intersect").hide();
                $("#spatialParams").hide();
                $("#attributes_type_label").hide();
                break;

            case "spatial":
                $("#spatialParams").show();
                $("#attributes_type_label").show();

                $("#attributesParams").hide();
                $("#attributes_analysis_input").hide();
                $("#params").hide();
                $("#analysis_btn_union").hide();
                $("#analysis_btn_intersect").hide();
                break;

            case "query":

                $("#query_btn_intersect").show();

                $("#spatialParams").hide();
                $("#attributes_type_label").hide();
                $("#attributesParams").hide();
                $("#attributes_analysis_input").hide();
                $("#params").hide();
                $("#analysis_btn_union").hide();
                $("#analysis_btn_intersect").hide();
                break;

        }
    });
});