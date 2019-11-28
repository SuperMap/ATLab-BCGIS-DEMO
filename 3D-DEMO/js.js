/**
 *  展示系统的界面设置
 */


$(document).ready(function () {

    // 导航栏
    $("#header").html("<ul> \
        <li><a href=\"\"><b> >>== 区块链 s3m 展示系统 ==<< </b></a></li> \
        <li><a href=\"showS3M.html\" id=\"index_bar\">显示 s3m </a></li> \
        <li><a href=\"publishS3M.html\" id=\"mappublish_bar\">发布 s3m </a></li> \
        <li><a href=\"#\" id=\"info\"></a></li> \
        </ul>");

    $("#btn_show_s3m_moudle").click(function creatTab() {
        $.ajax({
            type: 'get',
            // contentType: "application/json",
            url: 'http://172.16.15.66:8081/server_war_exploded/webapi/blockchain/history',
            // data: JSON.stringify(params),
            success: function (data) {
                let recoreds = JSON.parse(data);
                var tab = '<table border=1 width="500" cellpadding="10" align="center">'
                tab += "<caption>历史版本查询</caption>";
                tab += "<tr><th>版本号</th>\
                            <th>修改时间</th>"
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
        //初始化viewer部件
        $("#cesiumContainer").show();
        $("#option").hide();

        $.ajax({
            type: 'get',
            // contentType: "application/json",
            url: 'http://172.16.15.66:8081/server_war_exploded/webapi/blockchain?modelID=model001',
            // data: JSON.stringify(params),
            success: function (data) {
                console.log(data);
                data = "{\"Records\":[" + data + "]}";
                let recoreds = JSON.parse(data);
                var viewer = new Cesium.Viewer('cesiumContainer');
                var scene = viewer.scene;
                var widget = viewer.cesiumWidget;
                try {
                    //打开所发布三维服务下的所有图层
                    scene.camera.setView({
                        destination: new Cesium.Cartesian3(-2172108.996759282, 4375350.277824589, 4101673.0066891187),
                        orientation: {
                            heading: 4.354565476048545,
                            pitch: -0.35245897259115955,
                            roll: 9.353513519272383e-10
                        }
                    });

                    var s3mFilePath = recoreds["Records"];

                    // 定义数据展示的位置
                    var points = [116.395074412521, 40.0167102653286, 39.4119813283905];

                    // 循环输出图层
                    for (var i = 0; i < s3mFilePath.length; i++) {
                        var exam = '../s3m/' + s3mFilePath[i];
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

    // $(document).click(function(e) { // 在页面任意位置点击而触发此事件
    //     var v_id = $(e.target).attr('id');
    // })

    $('body').on('click' , '.showclass' , function(){
        // 获取鼠标点击元素的 id
        $(document).click(function(e) { // 在页面任意位置点击而触发此事件
            var v_id = $(e.target).attr('id');
            show($("#" + v_id).html().trim());
            $(document).unbind("click");
        })
    });

    // 发布地图（时间为点击）
    $("#btn_saveFile").click(function () {
        // var params = {
        //     "workspaceName": document.getElementById("publish_workspace").value,
        //     "datastoreName": document.getElementById("publish_datastore").value,
        //     "featuretypeName": document.getElementById("publish_layer").value
        // };
        $.ajax({
            type: 'get',
            // contentType: "application/json",
            url: 'http://172.16.15.66:8081/server_war_exploded/webapi/blockchain',
            // data: JSON.stringify(params),
            success: function (data) {
                console.log('data: ' + data);
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
            url: 'http://172.16.15.66:8899/bcgis/mapservice/wms/list',
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
    $("#getSaveFileName").click(function () {
        switch ($("#version_number").val()) {

            case "version_number_1":
                $("#params").show();
                $("#attributesParams").hide();
                break;
            case "version_number_2":
                break;
            case "version_number_3":
                break;

        }
    });

    // 出现一个选择框，获取选中文件夹的绝对路径   ===================== 未实现
    $("#btn_getFilePath").change(function () {
        var objFiles = document.getElementById("btn_getFilePath");
        var fileSize = objFiles.files.length;

        var params = new Array();

        for (var i = 0; i < fileSize; i++) {
            var isFileValide = true;    // 交互click和ajax之间的信息
            // 读取文件内容
            var reader = new FileReader();//新建一个FileReader
            reader.readAsBinaryString(objFiles.files[i]);//读取文件 
            // 读取文件内容放到 fileString 里面
            reader.onload = function (evt) {
                // var params = new Array();
                var fileString = evt.target.result;
                params[i] = fileString;
                console.log(i);
                // console.log("filesize: " + fileSize);
                // console.log("par: " + params.length + fileString)

                // if((params.length - 1) == fileSize ){
                //     console.log(params);
                // }

                // console.log(params);
            }
        }
        // console.log(params)

    });

    // 出现 ajax 请求，将获取的路径返回给后端，让后端完成数据的存储过程，最后返回一个值表明数据存储完毕即可
    // $("#btn_saveFile").click(function () {

    //     // var params = document.getElementById("input_getFilePath").value;

    //     // $.ajax({
    //     //     type: 'post',
    //     //     contentType: "application/json",
    //     //     url: 'http://localhost:8899/bcgis/mapservice/wms/publish',
    //     //     data: JSON.stringify(params),
    //     //     success: function (data) {
    //     //         console.log('data: ' + JSON.stringify(data));
    //     //     },
    //     //     error: function (err) {
    //     //         console.log('err: ');
    //     //         console.log(JSON.stringify(err));
    //     //     }
    //     // });

    //     alert("文件保存完毕，请点击返回")

    // });

});


// <!-- 字母 td 指表格数据（table data），即数据单元格的内容 -->
// <!-- cellpadding 定义表格的宽度 -->
// <!-- align 让表格居中显示 -->
// <!-- <table border="1" cellpadding="10" align="center">
//     <caption>历史版本查询</caption>
//     <thead>
//         <tr>
//             <th>版本号</th>
//             <th>修改时间</th>
//             <th>查询链接</th>
//         </tr>
//     </thead>
//     <tr>
//         <td>
//             Ver_1.0.0 
//         </td>
//         <td>
//             2019.11.28
//         </td>
//         <td>
//             查询
//         </td>
//     </tr>
// </table> -->


