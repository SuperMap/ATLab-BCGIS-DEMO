
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
    $("#btn_saveFile").click(function () {

        var params = document.getElementById("input_getFilePath").value;

        // $.ajax({
        //     type: 'post',
        //     contentType: "application/json",
        //     url: 'http://localhost:8899/bcgis/mapservice/wms/publish',
        //     data: JSON.stringify(params),
        //     success: function (data) {
        //         console.log('data: ' + JSON.stringify(data));
        //     },
        //     error: function (err) {
        //         console.log('err: ');
        //         console.log(JSON.stringify(err));
        //     }
        // });

        alert("文件保存完毕，请点击返回")

    });

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


