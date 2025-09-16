import { Component } from "@angular/core";
import { GraphsService } from "../../service/graphs.service";
import * as Highcharts from 'highcharts';



@Component({
    selector: 'app-user-graph',
    templateUrl: './user-graph.component.html',
    styleUrls: ['./user-graph.component.css'],
    standalone: false
})
export class UserGraphComponent {

    orderPrices: number[] = [];
    companyData: any[] = []
    pieChartData: any[] = []
    constructor(private graphService: GraphsService) { }

    ngOnInit(): void {
        // funcion que depende de la url desde la que se llame cargue un tipo de grafico u otro
        this.loadGraphData()
    }


    loadGraphData(): void {
        this.graphService.getUserGraphData().subscribe(data => {
            console.log("datos user graph: " + data)
            this.orderPrices = data;
            this.renderUserChart()
        });
        

    }

    renderUserChart(): void {
        const size = this.orderPrices.length;
        const index = [];
        console.log("Orderprices" + this.orderPrices)
        for (let i = 1; i <= size; i++) {
            index.push("Order " + i);
        }

        Highcharts.chart({
            chart: {
                renderTo: 'container',
                type: 'line'
            },
            title: {
                text: 'Gasto últimos pedidos'
            },
            xAxis: {
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
                type: 'line',
                name: 'Línea de gastos en pedidos',
                data: this.orderPrices
            }]
        });
    }


    loadPieChartData(): void {
        // this.graphService.getCompanyGraphData().subscribe({
        //   next: (data) => {
        //     // Transformamos los datos para que Highcharts los acepte
        //     this.pieChartData = data.map((item: { key: string; value: number }): [string, number] => [item.key, item.value]);  // Transformamos cada objeto en un array [key, value]
        //     this.renderCompanyGraph();
        //   },
        //   error: (err) => {
        //     console.error('Error al cargar los datos para el gráfico de pie', err);
        //   }
        // });

        // datos como llegarian de la api 
        this.companyData = [
            {
                "key": "Others",
                "value": 0
            },
            {
                "key": "Appliances",
                "value": 0
            },
            {
                "key": "Technology",
                "value": 1
            },
            {
                "key": "Education",
                "value": 0
            },
            {
                "key": "Music",
                "value": 0
            },
            {
                "key": "Books",
                "value": 0
            },
            {
                "key": "Home",
                "value": 0
            },
            {
                "key": "Cinema",
                "value": 0
            },
            {
                "key": "Sports",
                "value": 0
            }
        ]
        this.pieChartData = this.companyData.map((item: { key: string; value: number }): [string, number] => [item.key, item.value]);  // Transformamos cada objeto en un array [key, value]
        console.log(this.pieChartData)
        this.renderCompanyGraph()
    }

    renderCompanyGraph(): void {


        Highcharts.chart('container', {
            chart: {
                type: 'pie',
                options3d: {
                    enabled: true,
                    alpha: 50,
                    beta: 0
                }
            },
            title: {
                text: 'Result Status'
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    depth: 35,
                    dataLabels: {
                        enabled: true,
                        format: '{point.name}'
                    }
                }
            },
            series: [{
                type: 'pie',
                name: 'Ratio',
                data: this.pieChartData

            }]
        });

    }
}