/**
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 **/
 /*global document,Image*/

var source, id, canvas, context;
var overviewUrl, rect, imageObj, drag;

function setUrlParameters() {
    var pageUrl = window.location.search.substring(1);
    var parameters = pageUrl.split('&');

    $.each(parameters, function(index, value) {
        var parameter = value.split("=");
        if(parameter[0] === "id") {
            id = parameter[1];
        } else if(parameter[0] === "source") {
            source = parameter[1];
        }
    });
    overviewUrl = "/services/catalog/sources/" + source + "/" + id + "?transform=resource&qualifier=overview";
}

function mouseDown(e) {
    rect.startX = e.pageX - this.offsetLeft;
    rect.startY = e.pageY - this.offsetTop;
    drag = true;
}

function mouseUp() {
    drag = false;
}

function draw() {
    context.beginPath();
    context.lineWidth="4";
    context.strokeStyle="#f0e786";
    context.rect(rect.startX, rect.startY, rect.w, rect.h);
    context.stroke();
}

function mouseMove(e) {
    if (drag) {
        rect.w = (e.pageX - this.offsetLeft) - rect.startX;
        rect.h = (e.pageY - this.offsetTop) - rect.startY ;
        context.drawImage(imageObj, 0, 0, imageObj.width, imageObj.height, 0, 0, canvas.width, canvas.height);
        draw();
    }
}

function drawImage(srcImageUrl) {
    rect = {};
    imageObj = new Image();
    imageObj.src = srcImageUrl;

    canvas = document.getElementById('canvas');
    context = canvas.getContext('2d');

    imageObj.onload = function() {
        $('canvas').attr('height', this.height);
        $('canvas').attr('width', this.width);
        context.drawImage(imageObj, 0, 0, imageObj.width, imageObj.height, 0, 0, canvas.width, canvas.height);
    };
    canvas.addEventListener('mousedown', mouseDown, false);
    canvas.addEventListener('mouseup', mouseUp, false);
    canvas.addEventListener('mousemove', mouseMove, false);
}

function setOnClickListeners() {
    $('.chip-image').on('click', function() {
        var x = rect.startX;
        var y = rect.startY
        var w = rect.w;
        var h = rect.h;
        var chipUrl = "/services/catalog/sources/" + source + "/" + id + "?transform=chip&qualifier=overview&x=" + x + "&y=" + y + "&w=" + w + "&h=" + h ;
        $('.chip-image').attr('href',chipUrl);
    });

    $('.reset').on('click', function() {
        drawImage(overviewUrl);
    });
}

$(document).ready(function() {
    setUrlParameters();
    drawImage(overviewUrl);
    setOnClickListeners();
});


