class Chart {
    constructor(url, btnIdPrefix, refresh) {
        this.url = url;
        if (btnIdPrefix) {
            var offsetMin = $('#' + btnIdPrefix + '_interval').val();
            this.timeStart = (moment() - (offsetMin * 60000)).valueOf();
            this.setButtons(btnIdPrefix);
        } else {
            this.timeStart = null;
        }
        this.refresh = refresh;
        this.timeEnd = 0;
        this.chart = null;
        this.add = false;
        this.timeOut = null;
    }

    calcTimeEnd() {
        return (this.timeEnd == 0 ? moment().valueOf() : this.timeEnd);
    }

    requestLiveData() {
        var self = this;
        $.ajax({
            url: self.url + '&tstart=' + self.timeStart + '&tend=' + self.calcTimeEnd(),
            success: this.onRequestLiveData,
            context: this,
            cache: false
        });
    }

    onRequestLiveData(data) {
        var chart = this.chart;
        Object.keys(data).forEach(key => {
            var series = this.chart.get(key);
        var shift = series.data.length > 200; // shift if the series is longer than 20
        var points = data[key];
        if (points.length > 0) {
            this.timeStart = this.timeStart > points[points.length - 1][0] ? this.timeStart : points[points.length - 1][0];
            var i = 0;
            // add the points
            if (this.add) {
                for (i; i < points.length; i += 1) {
                    // console.info(points[i][0]);
                    series.addPoint(points[i], false, shift);
                }
            } else {
                series.setData(points);
            }
        }
    })
        ;
        chart.redraw();
        // call it again after one second
        if (this.timeOut) {
            clearTimeout(this.timeOut);
            this.timeOut = null;
        }
        if (this.timeEnd == 0) {
            this.add = true;
            var self = this;
            var to = setTimeout(function () {
                self.requestLiveData();
            }, this.refresh);
            this.timeOut = to;
        }
    }

    requestData() {
        $.ajax({
            url: this.url,
            success: this.onRequestData,
            context: this,
            cache: false
        });
    }

    onRequestData(data) {
        Object.keys(data).forEach(id => {
            var series = this.chart.get(id);
        var points = data[id];
        if (points) {
            series.setData(points, true);
        }
    })
        ;
    }

    timeShift(interval, step) {
        var msInt = interval * 60000;
        if (step == 2) {
            this.timeEnd = 0;
            this.timeStart = (moment() - msInt).valueOf();
        } else if (step == 1) {
            this.timeEnd += this.timeEnd == 0 ? 0 : msInt;
            this.timeStart = this.timeEnd == 0 ? (moment() - msInt).valueOf() : this.timeEnd - msInt;
        } else if (step == 0) {
            this.timeStart = this.timeEnd == 0 ? (moment() - msInt).valueOf() : this.timeEnd - msInt;
        } else {
            this.timeEnd = this.timeEnd == 0 ? (moment() - msInt).valueOf() : this.timeEnd - msInt;
            this.timeStart = this.timeEnd - msInt;
        }
        this.add = false;
        this.requestLiveData();
    }

    setButtons(idPrefix) {
        var self = this;
        $('#' + idPrefix + '_back').click(function () {
            self.timeShift($('#' + idPrefix + '_interval').val(), -1);
        });
        $('#' + idPrefix + '_interval').click(function () {
            self.timeShift($('#' + idPrefix + '_interval').val(), 0);
        });
        $('#' + idPrefix + '_forward').click(function () {
            self.timeShift($('#' + idPrefix + '_interval').val(), 1);
        });
        $('#' + idPrefix + '_live').click(function () {
            self.timeShift($('#' + idPrefix + '_interval').val(), 2);
        });
    }
}