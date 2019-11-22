import 'ol/ol.css';
import GeoJSON from 'ol/format/GeoJSON';
import Map from 'ol/Map';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import View from 'ol/View';
import { Fill, Stroke, Style, Text } from 'ol/style';

// http://127.0.0.1:8070/geoserver/D/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=D%3AtempfeaturesType&maxFeatures=50&outputFormat=text%2Fjavascript
// http://127.0.0.1:8070/geoserver/testFabric/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=testFabric%3AtempfeaturesType&maxFeatures=50&outputFormat=application%2Fjson

// http://127.0.0.1:8070/geoserver/testFabric/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=testFabric%3AtempfeaturesType&maxFeatures=50&outputFormat=text%2Fjavascript

//参数字段  
var wfsParams = {
    service: "WFS",
    version: "1.0.0",
    request: "GetFeature",
    typeName: "testFabric:tempfeaturesType",  //图层名称     
    outputFormat: "text/javascript",  //重点，不要改变  
    format_options: "callback:loadFeatures"  //回调函数声明  
};

const map = new Map({
    target: 'map-container',
    view: new View({
        center: [116.5, 39.5], // [116.5, 40.18]
        zoom: 3
    })
});

var vectorSource = new VectorSource({
    format: new GeoJSON(),
    url: "./data/features.json",
    // loader: function (extent, resolution, projection) {  //加载函数  
    //     var url = "http://localhost:8070/geoserver/wfs";
    //     $.ajax({
    //         url: url,
    //         data: $.param(wfsParams),   //传参  
    //         type: "GET",
    //         dataType: "jsonp",   //解决跨域的关键  
    //         jsonpCallback: "loadFeatures"  //回调  

    //     });
    // },
    projection: "EPSG:4326"
});

//回调函数使用
window.loadFeatures = function (response) {
    vectorSource.addFeatures((new GeoJSON()).readFeatures(response));  //载入要素
};

var vectorLayer = new VectorLayer({
    source: vectorSource
});
map.addLayer(vectorLayer);


var highlightStyle = new Style({
    stroke: new Stroke({
        color: '#f00',
        width: 1
    }),
    fill: new Fill({
        color: 'rgba(255,0,0,0.1)'
    }),
    text: new Text({
        font: '12px Calibri,sans-serif',
        fill: new Fill({
            color: '#000'
        }),
        stroke: new Stroke({
            color: '#f00',
            width: 3
        })
    })
});

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
    } else {
        info.innerHTML = '&nbsp;';
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

$("#analysis_btn").click(function () {
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

var selectedList = [];
map.on('click', function (evt) {
    switch ($("#analysis_type_select").val()) {
        case "buffer":
            sendFeatureInfo(evt);
            break;
        case "union":
            sendSelectedFeatureInfo(evt);
            break;
        default:
            break;
    }
});