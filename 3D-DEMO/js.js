/**
 *  展示系统的界面设置
 */
$(document).ready(function () {
    // 导航栏
    $("#header").html('<ul> \
        <li><a href=""><b> >>== 区块链 s3m 展示系统 ==<< </b></a></li> \
        <li><a href="show.html" id="index_bar">显示 s3m </a></li> \
        <li><a href="publish.html" id="mappublish_bar">发布 s3m </a></li> \
        </ul>');


    $("#btn_show_s3m_moudle").click(function creatTab() {
        let modelid = $("#modelid").val();
        let s3mid = $("#sid").val();
        var tab = '<table border=1 width="500" cellpadding="10" align="center">'
        tab += "<caption>历史版本查询</caption>";
        tab += "<tr><th>版本号</th>\
                            <th>修改时间</th>";

        let params = "";
        if (s3mid != "") {
            params = "?key=" + modelid + "-" + s3mid;
        } else {
            params = "?key=" + modelid;
        }
        $.ajax({
            type: 'get',
            // contentType: "application/json",
            url: "http://localhost:8081/server_war_exploded/webapi/blockchain/history" + params,
            // data: JSON.stringify(params),
            success: function (data) {
                console.log(data);
                let recoreds = JSON.parse(data);

                for (var i = 0; i < recoreds.length; i++) {
                    tab += '<tr>'
                    for (var j = 0; j < 2; j++) {
                        if (j == 0) {
                            // target 当点击时在另外的页面打开

                            tab += "<td ><label style=\"color:blue; text-align: center;\" class=\"showclass\" id=\"show" + i + j + "\">" + i + "</label></td>"
                        } else {
                            tab += "<td >" + recoreds[i]["Timestamp"] + "</td>"
                        }
                    }
                    tab += '</tr>';
                }
                tab += '</table>';
                $("#version_list").html(tab);
            },
            error: function (err) {
                console.log('err: ');
                console.log(JSON.stringify(err));
            }
        });
    });

    function show(mapid) {
        let modelid = $("#modelid").val();
        let s3mid = $("#sid").val();
        let params = "";
        if (s3mid != "") {
            params = "?key=" + modelid + "-" + s3mid;
        } else {
            params = "?key=" + modelid;
        }

        //初始化viewer部件
        $("#cesiumContainer").show();
        $("#option").hide();
        $.ajax({
            type: 'get',
            // contentType: "application/json",
            url: "http://localhost:8081/server_war_exploded/webapi/blockchain/history" + params,
            // data: JSON.stringify(params),
            success: function (data) {
                let recoreds = JSON.parse(data);
                var viewer = new Cesium.Viewer('cesiumContainer');
                var scene = viewer.scene;
                var widget = viewer.cesiumWidget;
                viewer.scene.globe.enableLighting = true;
                try {
                    //打开所发布三维服务下的所有图层
                    // scene.camera.setView({
                    //     // estination: new Cesium.Cartesian3(-2172108.996759282, 4375350.277824589, 4101673.0066891187),
                    //     destination: new Cesium.Cartesian3(-2172200, 4375350, 4101600),
                    //     orientation: {
                    //         heading: 4.354565476048545,
                    //         pitch: -0.35245897259115955,
                    //         roll: 9.353513519272383e-10
                    //     }
                    // });
                    // Create an initial camera view
                    var initialPosition = new Cesium.Cartesian3.fromDegrees(116.3950 , 40.0172, 53.411); //116.3950 , 40.0172, 53.411
                    var initialOrientation = new Cesium.HeadingPitchRoll.fromDegrees(-155, -36, 0.0);//155, -36, 0.0 第一个参数负责模型转向
                    var homeCameraView = {
                        destination : initialPosition,
                        orientation : {
                            heading : initialOrientation.heading,
                            pitch : initialOrientation.pitch,
                            roll : initialOrientation.roll
                        }
                    };
                    // Set the initial view
                    viewer.scene.camera.setView(homeCameraView);

                    // 定义数据展示的位置
                    var points = [116.395074412521, 40.0167102653286, 5.4119813283905];//116.395074412521, 40.0167102653286, 5.4119813283905
                    var s3mFilePath = recoreds[mapid]["Record"][0]["SHash"];
                    for (var i = 0; i < s3mFilePath.length; i++) {
                        // var temp1 = ss[i];
                        var exam = 'http://localhost:8081/server_war_exploded/s3m/' + s3mFilePath[i]
                        var keymap = {};
                        keymap[exam] = [];
                        var layer = new Cesium.DynamicLayer3D(scene._context, [exam]);
                        layer.updateInterval = 500;//动态图层更新时间
                        layer.enableLocalOffset = false;//禁止模型局部偏移
                        scene.primitives.add(layer);
                        var pillarState = new Cesium.DynamicObjectState({
                            id: i,
                            longitude: points[0],
                            latitude: points[1],
                            altitude: points[2],
                            scale: new Cesium.Cartesian3(1, 1, 1)
                        });
                        keymap[exam].push(pillarState);
                        for (var key in keymap) {
                            layer.updateObjectWithModel(key, keymap[key]);
                        }
                    }
                } catch (e) {
                    if (widget._showRenderLoopErrors) {
                        var title = '渲染时发生错误，已停止渲染。';
                        widget.showErrorPanel(title, undefined, e);
                    }
                }
            }
        });
    }

    $('body').on('click', '.showclass', function () {
        // 获取鼠标点击元素的 id
        $(document).click(function (e) { // 在页面任意位置点击而触发此事件
            var v_id = $(e.target).attr('id');
            show($("#" + v_id).html().trim());
            $(document).unbind("click");
        })
    });

    // ajaxSubmit 方式，需要引用 https://github.com/jquery-form/form/blob/master/dist/jquery.form.min.js
    // function submitForm() {
    //     // jquery 表单提交
    //     $("#publish_form").ajaxSubmit(function(result) {
    //         if (result.status === true) {
    //             alert(result.msg);
    //         }else{
    //             alert(result.msg);
    //         }
    //     });
    //     return false; // 必须返回false，否则表单会自己再做一次提交操作，并且页面跳转
    // }

    $("#btn_submit").on('click', function submitForm() {
        var form = new FormData(document.getElementById("publish_form"));
        $.ajax({
            url: "http://localhost:8081/server_war_exploded/webapi/blockchain",
            type: "post",
            data: form,
            processData: false,
            contentType: false,
            success: function (data) {
                alert(data);
                // console.log(data);
                // console.log('data: ' + JSON.stringify(data));
            },
            error: function (e) {
                // alert("错误！！");
            }
        });
    });
});