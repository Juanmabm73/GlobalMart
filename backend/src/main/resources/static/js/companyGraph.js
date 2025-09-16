const dataElements = document.querySelectorAll('[id$="-data"]'); // get all elements with id ending in -data
			
Highcharts.chart('container', {
    chart : {
        type : 'pie',
        options3d : {
            enabled : true,
            alpha : 50,
            beta : 0
        }
    },
    title : {
        text : 'Result Status'
    },
    tooltip : {
        pointFormat : '{series.name}: <b>{point.percentage:.1f}%</b>'
    },
    plotOptions : {
        pie : {
            allowPointSelect : true,
            cursor : 'pointer',
            depth : 35,
            dataLabels : {
                enabled : true,
                format : '{point.name}'
            }
        }
    },
    series : [ {
        type : 'pie',
        name : 'Ratio',
        data: [
        [ 'Deportes', parseInt(document.getElementById('Sports-data').getAttribute('data-')) ], // take the value of the data- attribute of the element with id from the DOM
        [ 'Libros', parseInt(document.getElementById('Books-data').getAttribute('data-')) ],
        [ 'Educación', parseInt(document.getElementById('Education-data').getAttribute('data-')) ],
        [ 'Casa', parseInt(document.getElementById('Home-data').getAttribute('data-')) ],
        [ 'Musica', parseInt(document.getElementById('Music-data').getAttribute('data-')) ],
        [ 'Cine', parseInt(document.getElementById('Cinema-data').getAttribute('data-')) ],
        [ 'Otros', parseInt(document.getElementById('Others-data').getAttribute('data-')) ],
        [ 'Tecnologia', parseInt(document.getElementById('Technology-data').getAttribute('data-')) ],
        [ 'Electrodomesticos', parseInt(document.getElementById('Appliances-data').getAttribute('data-')) ]
        ]
        
    } ]
});