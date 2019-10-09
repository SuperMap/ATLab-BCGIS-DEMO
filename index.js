import 'ol/ol.css';
import GeoJSON from 'ol/format/GeoJSON';
import Map from 'ol/Map';
import VectorLayer from 'ol/layer/Vector';
import VectorSource from 'ol/source/Vector';
import View from 'ol/View';
import { Fill, Stroke, Style, Text } from 'ol/style';
import {fromLonLat} from 'ol/proj';

// http://127.0.0.1:8070/geoserver/D/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=D%3AD&maxFeatures=50&outputFormat=application%2Fjson

const map = new Map({
    target: 'map-container',
    view: new View({
        center: fromLonLat([116.5, 40.18]),
        zoom: 9
    })
});

var vectorSource = new VectorSource({
    format: new GeoJSON(),
    url: './data/features.json',
});

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


var sendFeatureInfo = function (pixel) {
    var feature = map.forEachFeatureAtPixel(pixel, function (feature) {
        return feature;
    });

    if (feature) {
        var params = {
            "bufferRadius":2,
            "fid":[feature.getId()]
        };
        $.ajax({
            type: 'post',
            contentType: "application/json",
            url: 'http://localhost:8899/bcgis/mapservice/buffer/bufferAnalysis',
            data: JSON.stringify(params),
            success: function (data) {
                // var vectorSource = new VectorSource({
                //     format: new GeoJSON(),
                //     url: './data/features.json',
                //     projection: 'EPSG:4326'
                // });
                // var vectorLayer = new VectorLayer({
                //     source: vectorSource
                // });
                // map.addLayer(vectorLayer);
                console.log('data: ' + JSON.stringify(data));
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

map.on('click', function (evt) {
    sendFeatureInfo(evt.pixel);
});