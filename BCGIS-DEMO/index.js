import 'ol/ol.css';
import GeoJSON from 'ol/format/GeoJSON';
import Map from 'ol/Map';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import View from 'ol/View';
import { Fill, Stroke, Style, Text } from 'ol/style';

import { platformModifierKeyOnly } from 'ol/events/condition';
import { DragBox, Select } from 'ol/interaction';
// import {Tile as TileLayer, Vector as VectorLayer} from 'ol/layer';
// import {OSM, Vector as VectorSource} from 'ol/source';


// 成都市区 http://localhost:8080/geoserver/Test/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=Test%3A5668c664c852b2b95543b784371f0267136cb4e09b8cb4a284148d2b9f578301&maxFeatures=50&outputFormat=text%2Fjavascript
// 北京市区 http://localhost:8080/geoserver/Test/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=Test%3A6bff876faa82c51aee79068a68d4a814af8c304a0876a08c0e8fe16e5645fde4&maxFeatures=50&outputFormat=text%2Fjavascript
//参数字段  
var wfsParams = {
    service: "WFS",
    version: "1.0.0",
    request: "GetFeature",
    typeName: "Test:6bff876faa82c51aee79068a68d4a814af8c304a0876a08c0e8fe16e5645fde4",  //图层名称      
    outputFormat: "text/javascript",  //重点，不要改变  
    format_options: "callback:loadFeatures"  //回调函数声明  
};

const map = new Map({

    target: 'map-container',
    view: new View({
        // projection:"EPSG:4326",  // 该设置不适用于经纬度地图
        center: [116.5, 39.5], // 设置地图的中心值  北京中心 [116.5, 39.5]  成都中心 [103.98, 30.52]  [12993238, 4811530]
        zoom: 25   // 25 8
    })
});

// 获得地图
var vectorSource = new VectorSource({
    format: new GeoJSON(),
    // url: "./data/beijing.json",
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

window.loadFeatures = function (response) {
    vectorSource.addFeatures((new GeoJSON()).readFeatures(response));  //载入要素
};

var vectorLayer = new VectorLayer({
    source: vectorSource
});
map.addLayer(vectorLayer);

var highlightStyle = new Style({
    stroke: new Stroke({
        color: '#f0f',
        width: 1
    }),
    fill: new Fill({
        color: 'rgba(255，0，255, 0.1)'
    }),
    text: new Text({
        font: '12px Calibri,sans-serif',
        fill: new Fill({
            color: '#f0f'
        }),
        stroke: new Stroke({
            color: '#f0f',
            width: 3
        })
    })
});

// 这个时定义高亮显示的  把 下面注释掉之后，在选中的时候就不会出现高亮显示额
var featureOverlay = new VectorLayer({
    source: new VectorSource(),
    map: map,
    style: function (feature) {
        highlightStyle.getText().setText(feature.get('name'));
        return highlightStyle;
    }
});

var highlight;
var displayFeatureInfo = function (pixel) {
    var feature = map.forEachFeatureAtPixel(pixel, function (feature) {
        return feature;
    });

    var info = document.getElementById('info');
    if (feature) {
        info.innerHTML = feature.getId() + ': ' + feature.get('name');
        console.log(feature);
    } else {
        // info.innerHTML = '&nbsp';
    }

    if (feature !== highlight) {
        if (highlight) {
            featureOverlay.getSource().removeFeature(highlight);
        }
        if (feature) {
            featureOverlay.getSource().addFeature(feature);
        }
        highlight = feature;
    }
};

map.on('pointermove', function (evt) {
    if (evt.dragging) {
        return;
    }
    var pixel = map.getEventPixel(evt.originalEvent);
    displayFeatureInfo(pixel);
});

var sendSelectedFeatureInfo = function (evt) {
    var feature = map.forEachFeatureAtPixel(evt.pixel, function (feature) {
        return feature;
    });

    if (feature) {
        selectedList[selectedList.length] = feature.getId();
    }

    if (feature !== highlight) {
        if (feature) {
            featureOverlay.getSource().addFeature(feature);
        }
        highlight = feature;
    }
};

var selectedList = [];
map.on('click', function (evt) {
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
            sendFeatureInfo(evt);
            break;
        case "spatial":
            sendFeatureInfo(evt);
            break;
        default:
            break;
    }
});

// 缓冲区分析
var sendFeatureInfo = function (evt) {
    var feature = map.forEachFeatureAtPixel(evt.pixel, function (feature) {
        return feature;
    });

    if (feature) {
        let bufferRadius = $("#analysis_input").val();
        console.log(bufferRadius);
        var params = {
            "bufferRadius": bufferRadius,
            "fid": [feature.getId()]
        };
        $.ajax({
            type: 'post',
            contentType: "application/json",
            url: 'http://localhost:8899/bcgis/mapservice/buffer/bufferAnalysis',
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
    }

    if (feature !== highlight) {
        if (highlight) {
            featureOverlay.getSource().removeFeature(highlight);
        }
        if (feature) {
            featureOverlay.getSource().addFeature(feature);
        }
        highlight = feature;
    }
};

// 联合分析
$("#analysis_btn_union").click(function () {
    var params = {
        "fid": selectedList
    };
    selectedList = [];

    $.ajax({
        type: 'post',
        contentType: "application/json",
        url: 'http://localhost:8899/bcgis/mapservice/buffer/unionAnalysis',
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

// 叠加分析
$("#analysis_btn_intersect").click(function () {
    var params = {
        "fid": selectedList
    };
    selectedList = [];

    $.ajax({
        type: 'post',
        contentType: "application/json",
        url: 'http://localhost:8899/bcgis/mapservice/buffer/intersectionAnalysis',
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

// 属性查询
$("#analysis_btn_attributes").click(function () {
    // 可选取多个参数
    let attributes = $("#attributes_analysis_input").val();
    console.log(attributes);
    var params = {
        "attributes": attributes,
        "fid": "5668c664c852b2b95543b784371f0267136cb4e09b8cb4a284148d2b9f578301" // 后期优化需自动获取
    };

    $.ajax({
        type: 'post',
        contentType: "application/json",
        url: 'http://localhost:8899/bcgis/mapservice/query/attributes',
        data: JSON.stringify(params),
        success: function (data) {
            vectorSource.addFeatures((new GeoJSON()).readFeatures(data));
            var vectorLayer2 = new VectorLayer({
                source: vectorSource,
                style: highlightStyle
            });
            map.addLayer(vectorLayer2);
        },
        error: function (err) {
            console.log('err: ');
            console.log(JSON.stringify(err));
        }
    });

    if (feature !== highlight) {
        if (highlight) {
            featureOverlay.getSource().removeFeature(highlight);
        }
        if (feature) {
            featureOverlay.getSource().addFeature(feature);
        }
        highlight = feature;
    }
});

// 空间查询
$("#analysis_btn_spatial").click(function () {
    let minX = $("#spatial_analysis_minX").val();
    let minY = $("#spatial_analysis_minY").val();
    let maxX = $("#spatial_analysis_maxX").val();
    let maxY = $("#spatial_analysis_maxY").val();
    var params = {
        "minX": minX,
        "minY": minY,
        "maxX": maxX,
        "maxY": maxY,
        "fid": "5668c664c852b2b95543b784371f0267136cb4e09b8cb4a284148d2b9f578301"
    };

    $.ajax({
        type: 'post',
        contentType: "application/json",
        url: 'http://localhost:8899/bcgis/mapservice/query/spatial',
        data: JSON.stringify(params),
        success: function (data) {
            vectorSource.addFeatures((new GeoJSON()).readFeatures(data));
            var vectorLayer2 = new VectorLayer({
                source: vectorSource,
                // style: highlightStyle
            });
            map.addLayer(vectorLayer2);
        },
        error: function (err) {
            console.log('err: ');
            console.log(JSON.stringify(err));
        }
    });

    if (feature !== highlight) {
        if (highlight) {
            featureOverlay.getSource().removeFeature(highlight);
        }
        if (feature) {
            featureOverlay.getSource().addFeature(feature);
        }
        highlight = feature;
    }
});


// 框选查询
$("#query_btn_intersect").click(function () {

    // 增加地图框选功能
    // a normal select interaction to handle click
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
            "fid": "5668c664c852b2b95543b784371f0267136cb4e09b8cb4a284148d2b9f578301"
        };
        console.log(params);
        $.ajax({
            type: 'post',
            contentType: "application/json",
            url: 'http://localhost:8899/bcgis/mapservice/query/spatial',
            data: JSON.stringify(params),
            success: function (data) {
                vectorSource.addFeatures((new GeoJSON()).readFeatures(data));
                var vectorLayer2 = new VectorLayer({
                    source: vectorSource,
                    // style: highlightStyle  // 这个增加的图层显示效果是将全部的数据进行图层叠加
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









