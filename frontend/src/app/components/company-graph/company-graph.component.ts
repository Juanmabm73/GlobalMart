import { Component } from "@angular/core";
import { GraphsService } from "../../service/graphs.service";
import * as Highcharts from 'highcharts';



@Component({
    selector: 'app-company-graph',
    templateUrl: './company-graph.component.html',
    styleUrls: [],
    standalone: false
})
export class CompanyGraphComponent {

    
    companyData: any[] = []
    pieChartData: any[] = []
    constructor(private graphService: GraphsService) { }

    ngOnInit(): void {
        
        this.loadPieChartData()
    }


    loadPieChartData(): void {
        this.graphService.getCompanyGraphData().subscribe({
          next: (data) => {
            // Transformamos los datos para que Highcharts los acepte
            this.pieChartData = data.map((item: { key: string; value: number }): [string, number] => [item.key, item.value]);  // Transformamos cada objeto en un array [key, value]
            this.renderCompanyGraph();
          },
          error: (err) => {
            console.error('Error al cargar los datos para el gráfico de pie', err);
          }
        });

        // datos como llegarian de la api 
        // this.companyData = [
        //     {
        //         "key": "Others",
        //         "value": 0
        //     },
        //     {
        //         "key": "Appliances",
        //         "value": 0
        //     },
        //     {
        //         "key": "Technology",
        //         "value": 1
        //     },
        //     {
        //         "key": "Education",
        //         "value": 0
        //     },
        //     {
        //         "key": "Music",
        //         "value": 0
        //     },
        //     {
        //         "key": "Books",
        //         "value": 0
        //     },
        //     {
        //         "key": "Home",
        //         "value": 0
        //     },
        //     {
        //         "key": "Cinema",
        //         "value": 0
        //     },
        //     {
        //         "key": "Sports",
        //         "value": 0
        //     }
        // ]
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