const dataElements = JSON.parse(document.getElementById('orderPrices-data').dataset.orderprices);
    const size = dataElements.length;
    const index = [];
    for (let i = 1; i <= size; i++) {
        index.push("Order " + i );
    }
    console.log("DATOS" + dataElements);

    Highcharts.chart('container', {
        chart: {
            type: 'line'
        },
        title: {
            text: 'Gasto últimos pedidos'
        },
        
        xAxis: {
            // order ID
            categories: index
        },
        yAxis: {
            title: {
                text: 'Gasto en euros'
            }
        },
        plotOptions: {
            line: {
                dataLabels: {
                    enabled: true
                },
                enableMouseTracking: true
            }
        },
        series: [{
            name: 'Línea de gastos en pedidos',
            data: dataElements
        }, ]
    });