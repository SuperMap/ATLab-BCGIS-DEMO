




//     <script type="text/javascript">
//     function onload(Cesium) {
//         //初始化viewer部件
//         var viewer = new Cesium.Viewer('cesiumContainer');
//         var scene = viewer.scene;
//         var widget = viewer.cesiumWidget;
//         $('#loadingbar').remove();
//         try {
//             // 相机视角
//             scene.camera.setView({
//                 destination: new Cesium.Cartesian3(-2172108.996759282, 4375350.277824589, 4101673.0066891187), //  看的高度 现在调低
//                 orientation: {                  // -2171551.3227830417   4375506.2101590205
//                     heading: 4.354565476048545,
//                     pitch: -0.35245897259115955,
//                     roll: 9.353513519272383e-10
//                 }

//             });

//             // 将 s3m 文件位置信息（可放多个，后面循环读取）
//             var s3mFilePath = [
//                 './SampleData/models/testBim/KMLModels/GeoModel3D%23_515362609.s3m',
//                 './SampleData/models/testBim/KMLModels/GeoModel3D%23_515435356.s3m',
//             ];

//             // 定义数据展示的位置
//             var points = [116.395074412521, 40.0167102653286, 39.4119813283905];

//             // 循环输出图层
//             for (var i = 0; i < s3mFilePath.length; i++) {
//                 var exam = s3mFilePath[i];
//                 var keymap = {};
//                 keymap[exam] = [];
//                 var layer = new Cesium.DynamicLayer3D(scene._context, [exam]);
//                 layer.updateInterval = 500;//动态图层更新时间
//                 layer.enableLocalOffset = false;//禁止模型局部偏移
//                 scene.primitives.add(layer);
//                 var pillarState = new Cesium.DynamicObjectState({
//                     id: i,
//                     longitude: points[0],
//                     latitude: points[1],
//                     altitude: points[2],
//                     scale: new Cesium.Cartesian3(1, 1, 1)
//                 });
//                 keymap[exam].push(pillarState);
//                 for (var key in keymap) {
//                     layer.updateObjectWithModel(key, keymap[key]);
//                 };
//             }
//         } catch (e) {
//             if (widget._showRenderLoopErrors) {
//                 var title = '渲染时发生错误，已停止渲染。';
//                 widget.showErrorPanel(title, undefined, e);
//             }
//         }
//     }
// </script>
