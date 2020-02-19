import 'ol/ol.css';
import GeoJSON from 'ol/format/GeoJSON';
import Map from 'ol/Map';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import View from 'ol/View';
import { Fill, Stroke, Style, Text } from 'ol/style';
import Overlay from 'ol/Overlay';
import { platformModifierKeyOnly } from 'ol/events/condition';
import { DragBox, Select } from 'ol/interaction';
import Modify from 'ol/interaction/Modify';
import Draw from 'ol/interaction/Draw';
import Snap from 'ol/interaction/Snap';

// 为点击获得属性----0
var container = document.getElementById('popup');
var content = document.getElementById('popup-content');
var closer = document.getElementById('popup-closer');

// 为点击获得属性----1---->获得弹窗
var overlay = new Overlay({
    element: container,
    autoPan: true,
    autoPanAnimation: {
        duration: 250
    }
});

// 为点击获得属性----2
closer.onclick = function () {
    overlay.setPosition(undefined);
    closer.blur();
    return false;
};

// 北京市区 http://localhost:8080/geoserver/Test/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=Test%3A6bff876faa82c51aee79068a68d4a814af8c304a0876a08c0e8fe16e5645fde4&maxFeatures=50&outputFormat=text%2Fjavascript
//参数字段  
var wfsParams = {
    service: "WFS",
    version: "1.0.0",
    request: "GetFeature",
    typeName: "Test:6bff876faa82c51aee79068a68d4a814af8c304a0876a08c0e8fe16e5645fde4",  //图层名称 
    // R  278934ff40e23d4a054144b495df7ca5eb0f764aa02d44f0cf02b8921539d8b1     
    // D  6bff876faa82c51aee79068a68d4a814af8c304a0876a08c0e8fe16e5645fde4
    // BL d7e94bf0c86c94579e8b564d2dea995ed3746108f98f003fb555bcd41831f885
    outputFormat: "text/javascript",  //重点，不要改变  
    format_options: "callback:loadFeatures"  //回调函数声明  
};

/**
 * 构建地图
 * TODO 1、如何自动设置地图的中心点和地图的坐标系
 */
const map = new Map({
    overlays: [overlay],
    target: 'map-container',
    view: new View({
        // projection:"EPSG:4326",  // 该设置不适用于经纬度地图
        center: [116.5, 39.5], // 设置地图的中心值  北京中心 [116.5, 39.5]  成都中心 [103.98, 30.52]  [12993238, 4811530]
        zoom: 25   // 25 8
    })
});
map.addOverlay(overlay);

/**
 * 获得地图图层 vectorSource
 */
var vectorSource = new VectorSource({
    format: new GeoJSON(),
    // url: "./data/features.json", // 可直接加载外部地图数据
    loader: function (extent, resolution, projection) {  //加载函数  
        var url = "http://localhost:8080/geoserver/wfs";
        $.ajax({
            url: url,
            data: $.param(wfsParams),   //传参  
            type: "GET",
            dataType: "jsonp",   //解决跨域的关键  
            jsonpCallback: "loadFeatures"  //回调  
        });
    },
    projection: "EPSG:4326"
});

/**
 * 获得地图 VS =====>>>>用于当有返回的feature时高亮显示地图
 */
var VS = new VectorSource({});

// 调用回调函数 数据和属性信息全在 response 里面
window.loadFeatures = function (response) {
    vectorSource.addFeatures((new GeoJSON()).readFeatures(response));  //载入要素
};

var vectorLayer = new VectorLayer({
    source: vectorSource
});
// 将图层添加到资源中
map.addLayer(vectorLayer);

/**
 * 2020.1.13新加的
 */
// // 添加 Modify 后就可以对图层进行修改了 -----》图上会有小圆点可进行修改（alt + click 删除点）
// // map.addInteraction(new Modify({
// //   source: vectorSource
// // }));

// // 实现draw交互，可以使用户画新的features并添加到数据源中。(右键点击开始画图，右键双击闭合并停止，左键双击闭合不停止)-----》》双击画图和双击获取属性重合了
// map.addInteraction(new Draw({
//   type: 'Polygon',
//   source: vectorSource
// }));

// // snap交互可以帮我在编辑和画features时候保持拓扑结构 当draw、modify和snap三种交互都被激活的时候，我们就可以在编辑数据的同时保持它原有的拓扑关系
// map.addInteraction(new Snap({
//   source: vectorSource
// }));

// // 清除要素
const clear = document.getElementById('clear');
clear.addEventListener('click', function() {
    vectorSource.clear();
    VS.clear();
});
// 下载GeoJson格式的数据
const format = new GeoJSON({featureProjection: 'EPSG:3857'});
const download = document.getElementById('download');
vectorSource.on('change', function() {
  const features = vectorSource.getFeatures();
  const json = format.writeFeatures(features);
  download.href = 'data:text/json;charset=utf-8,' + json;
});

// 设置新加图层VS的图层颜色
var highlightStyle = new Style({
    // 设置边框颜色
    stroke: new Stroke({
        color: '#f00',
        width: 1
    }),
    // 设置填充颜色
    fill: new Fill({
        color: '#ff0',
    }),
    // 设置里面字体的颜色
    text: new Text({
        font: '12px Calibri,sans-serif',
        fill: new Fill({
            color: '#ff0'
        }),
        stroke: new Stroke({
            color: '#f0f',
            width: 3
        })
    })
});

/**
 * 高亮显示单个属性
 */
// var featureOverlay = new VectorLayer({
//     source: new VectorSource(),
//     map: map,
//     style: function (feature) {
//         // highlightStyle.getText().setText(feature.get('AdminCode'));   // 属性在feature里面，首先需要拿出来，然后实现
//         return highlightStyle;
//     }
// });

/**
 * 双击获取图层属性
 */
map.on('dblclick', function (evt) {

    var coordinate = evt.coordinate;
    var pixel = map.getEventPixel(evt.originalEvent);
    var feature = map.forEachFeatureAtPixel(pixel, function (feature) {
        return feature;
    });
    if (feature) {
        // 得到属性的键
        var keys = feature.getKeys();
        // 得到属性值
        var json = {};
        for (var i = 1; i < keys.length; i++) {
            var key = keys[i];
            var value = feature.get(key);
            json[key] = value;
        }
        content.innerHTML = JSON.stringify(json);
        console.log(json);
        overlay.setPosition(coordinate);
    }
});

// /**
//  * 这个是为移动鼠标高亮显示准备的，暂时未用到-----》》》》》暂时找到的就是这里的问题
//  */
// var highlight;
// var displayFeatureInfo = function (pixel) {
//     // 得到feature，里面的value为属性
//     var feature = map.forEachFeatureAtPixel(pixel, function (feature) {
//         return feature;
//     });

//     var info = document.getElementById('info');
//     if (feature) {
//         info.innerHTML = "ID : " + feature.getId();
//     }

//     if (feature !== highlight) {
//         if (highlight) {
//             featureOverlay.getSource().removeFeature(highlight);
//         }
//         if (feature) {
//             featureOverlay.getSource().addFeature(feature);
//         }
//         highlight = feature;
//     }
// };

/**
 * 鼠标移动高亮显示---->>>太占内存了，暂时不用
 */
// map.on('pointermove', function (evt) {
//     if (evt.dragging) {
//         return;
//     }
//     var pixel = map.getEventPixel(evt.originalEvent);
//     displayFeatureInfo(pixel);
// });

// 点击图像事件获取feture
var sendSelectedFeatureInfo = function (evt) {
    var feature = map.forEachFeatureAtPixel(evt.pixel, function (feature) {
        return feature;
    });
    if (feature) {
        selectedList[selectedList.length] = feature.getId();
    }
    // if (feature !== highlight) {
    //     if (feature) {
    //         featureOverlay.getSource().addFeature(feature);
    //     }
    //     highlight = feature;
    // }
};

var selectedList = [];
map.on('singleclick', function (evt) {
    switch ($("#analysis_type_select").val()) {
        case "buffer":
            sendFeatureInfo(evt);
            break;
        case "union":
            sendSelectedFeatureInfo(evt);
            break;
        case "intersection":
            sendSelectedFeatureInfo(evt);
            break;
        case "attributes":
            sendFeatureInfoAttributes(evt);
            break;
        default:
            break;
    }
});


// ------------------------》》》》  以下为缓冲区等数据分析和查询，如果不用，不会涉及到，不占内存，暂时不考虑 《《《《---------------

/**
 * 缓冲区分析==========》》》》选择好 featureID 传输给后端，返回geometry
 */
var sendFeatureInfo = function (evt) {
    var feature = map.forEachFeatureAtPixel(evt.pixel, function (feature) {
        return feature;
    });

    if (feature) {
        let bufferRadius = $("#analysis_input").val();
        var params = {
            "bufferRadius": bufferRadius,
            "fid": [feature.getId()]
        };
        $.ajax({
            type: 'post',
            contentType: "application/json",
            url: 'http://localhost:8899/bcgis/mapservice/Analysis/buffer',
            data: JSON.stringify(params),
            success: function (data) {
                console.log(data.JSON)
                VS.addFeatures((new GeoJSON()).readFeatures(data));
                var vectorLayer2 = new VectorLayer({
                    source: VS,
                    style: highlightStyle
                });
                map.addLayer(vectorLayer2);
            },
            error: function (err) {
                console.log('err: ');
                console.log(JSON.stringify(err));
            }
        });
    }

    // if (feature !== highlight) {
    //     if (highlight) {
    //         featureOverlay.getSource().removeFeature(highlight);
    //     }
    //     if (feature) {
    //         featureOverlay.getSource().addFeature(feature);
    //     }
    //     highlight = feature;
    // }
};


/**
 * 联合分析==========》》》》选择好 featureID 传输给后端，返回geometry
 */
$("#analysis_btn_union").click(function () {
    var params = {
        "fid": selectedList
    };
    selectedList = [];

    $.ajax({
        type: 'post',
        contentType: "application/json",
        url: 'http://localhost:8899/bcgis/mapservice/Analysis/union',
        data: JSON.stringify(params),
        success: function (data) {
            VS.addFeatures((new GeoJSON()).readFeatures(data));
            var vectorLayer2 = new VectorLayer({
                source: VS,
                style: highlightStyle
            });
            map.addLayer(vectorLayer2);
        },
        error: function (err) {
            console.log('err: ');
            console.log(JSON.stringify(err));
        }
    });
});

/**
 * 叠加分析 ==========》》》》选择好 featureID 传输给后端，返回geometry
 */
$("#analysis_btn_intersect").click(function () {
    var params = {
        "fid": selectedList
    };
    selectedList = [];

    $.ajax({
        type: 'post',
        contentType: "application/json",
        url: 'http://localhost:8899/bcgis/mapservice/Analysis/intersection',
        data: JSON.stringify(params),
        success: function (data) {
            vectorSource.addFeatures((new GeoJSON()).readFeatures(data));
            var vectorLayer2 = new VectorLayer({
                source: vectorSource
            });
            map.addLayer(vectorLayer2);
        },
        error: function (err) {
            console.log('err: ');
            console.log(JSON.stringify(err));
        }
    });
});

/**
 * 属性查询第一步 =======》》》根据 fid 获取当前地图具体有哪些属性，然后以表格的形式展现出
 */
var sendFeatureInfoAttributes = function (evt) {
    var fid = wfsParams.typeName;
    var params = {
        "fid": fid
    };
    $.ajax({
        type: 'post',
        contentType: "application/json",
        url: 'http://localhost:8899/bcgis/mapservice/query/attributes',
        data: JSON.stringify(params),
        success: function (data) {
            var keys = [];
            var values = [];
            for (var key in data) {
                keys.push(key);
                values.push(data[key]);
            }
            // 创建表格展现查询得到的属性
            var rows = keys.length;     // 行数
            var column = 2;             // 列数
            var tab = '<table border=1 width="500" cellpadding="10" align="center" id="mytable">'
            tab += "<caption>属性查询表</caption>";
            tab += "<tr><th>Key</th>\
                                <th>Value</th>"
            for (var i = 0; i < rows; i++) {
                tab += '<tr>'
                for (var j = 0; j < column; j++) {
                    if (j == 0) {
                        // tab += "<td ><label style=\"color:blue; text-align: center;\" >" + keys[i] + "</label></td>"
                        tab += "<td >" + keys[i] + "</td>"
                    } else {
                        tab += "<td ><input  value=\"" + values[i] + "\"   id=\"" + keys[i] + "\">" + "</input></td>"
                    }
                }
                tab += '</tr>';
            }
            tab += '</table>';
            // 显示表格
            div1.innerHTML = tab
            $("#analysis_type1_attributes").hide();
            $("#analysis_btn_attributes").show();
        },
    });
};

/**
 * 属性查询第二步 =======》》》根据表单获取查询条件组成的Json字符串，传输给后端查询得到空间几何信息
 */
$("#analysis_btn_attributes").click(function () {
    // var form = new FormData(document.getElementById("queryProp"));
    var table = document.getElementById("mytable");
    var json = {};
    for (var i = 1; i < table.rows.length; i++) {
        for (var j = 0; j < table.rows[i].cells.length; j++) {
            if (j == 0) {
                var key = table.rows[i].cells[j].innerHTML;
                var value = document.getElementById(key).value;
                if (value.length != 0) {
                    json[key] = value;
                }
            }
        }
    }

    // 将得到的数据传输到后端解析
    var params = json;
    var fid = wfsParams.typeName;
    params["fid"] = fid;
    console.log(params);
    $.ajax({
        type: 'post',
        contentType: "application/json",
        url: 'http://localhost:8899/bcgis/mapservice/query/attributes',
        data: JSON.stringify(params),
        success: function (data) {
            VS.addFeatures((new GeoJSON()).readFeatures(data)); // VS为重新定义的图层，增加feature可高亮显示
            var vectorLayer2 = new VectorLayer({
                source: VS,
                style: highlightStyle
            });
            map.addLayer(vectorLayer2);
        },
        error: function (err) {
            console.log('err: ');
            console.log(JSON.stringify(err));
        }
    });
});

/**
 * 空间查询======》》》》以框选的方式得到查询范围，传输给后端返回查询得到的空间几何信息
 */
$("#query_btn_intersect").click(function () {

    // 增加地图框选功能
    var select = new Select();
    map.addInteraction(select);
    var selectedFeatures = select.getFeatures();
    // a DragBox interaction used to select features by drawing boxes
    var dragBox = new DragBox({
        condition: platformModifierKeyOnly
    });
    map.addInteraction(dragBox);
    dragBox.on('boxstart', function (evt) {
        var minX = evt.coordinate[0];
        var maxY = evt.coordinate[1];
        document.getElementById("minX").innerHTML = minX;
        document.getElementById("maxY").innerHTML = maxY;
    });

    // 加入fid
    var fid = wfsParams.typeName;
    dragBox.on('boxend', function (evt) {

        var maxX = evt.coordinate[0];
        var minY = evt.coordinate[1];
        var minX = document.getElementById("minX").innerText;
        var maxY = document.getElementById("maxY").innerText;

        var params = {
            "minX": minX,
            "minY": minY,
            "maxX": maxX,
            "maxY": maxY,
            "fid": fid
        };
        console.log(params);
        $.ajax({
            type: 'post',
            contentType: "application/json",
            url: 'http://localhost:8899/bcgis/mapservice/query/spatial',
            data: JSON.stringify(params),
            success: function (data) {
                VS.addFeatures((new GeoJSON()).readFeatures(data));
                var vectorLayer2 = new VectorLayer({
                    source: VS,
                    style: highlightStyle  // 这个增加的图层显示效果是将全部的数据进行图层叠加
                });
                map.addLayer(vectorLayer2);
            },
            error: function (err) {
                console.log('err: ');
                console.log(JSON.stringify(err));
            }
        });

        var rotation = map.getView().getRotation();
        var oblique = rotation % (Math.PI / 2) !== 0;
        var candidateFeatures = oblique ? [] : selectedFeatures;
        var extent = dragBox.getGeometry().getExtent();
        vectorSource.forEachFeatureIntersectingExtent(extent, function (feature) {
            candidateFeatures.push(feature);
        });

        if (oblique) {
            var anchor = [0, 0];
            var geometry = dragBox.getGeometry().clone();
            geometry.rotate(-rotation, anchor);
            var extent$1 = geometry.getExtent();
            candidateFeatures.forEach(function (feature) {
                var geometry = feature.getGeometry().clone();
                geometry.rotate(-rotation, anchor);
                if (geometry.intersectsExtent(extent$1)) {
                    selectedFeatures.push(feature);
                }
            });
        }

    });

    dragBox.on('boxstart', function () {
        selectedFeatures.clear();
    });

    var infoBox = document.getElementById('info');

    selectedFeatures.on(['add', 'remove'], function () {
        var names = selectedFeatures.getArray().map(function (feature) {
            return feature.get('name');
        });
        if (names.length > 0) {
            infoBox.innerHTML = names.join(', ');
        } else {
            infoBox.innerHTML = 'No countries selected';
        }
    });
});