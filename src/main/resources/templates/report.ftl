<#-- @ftlvariable name="snapshotTotals" type="java.util.Map" -->
<#-- @ftlvariable name="book" type="com.lpi.budgy.domain.Book" -->
<#-- @ftlvariable name="snapshots" type="com.lpi.budgy.domain.Snapshot[]" -->
<#-- @ftlvariable name="title" type="java.lang.String" -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>${title}</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://www.gstatic.com/charts/loader.js"></script>
    <script>
        google.charts.load('current', {packages: ['corechart']});
        google.charts.setOnLoadCallback(() => {
            drawChartByAccount()
        });

        function drawChartByAccount() {
            const chartData = JSON.parse('${chartDataByAccountJson?no_esc}');
            for (const row of chartData.slice(1)) {
                row[0] = new Date(row[0])
            }
            const data = google.visualization.arrayToDataTable(chartData);

            let formatter = new google.visualization.NumberFormat({fractionDigits: 0}
            );
            for (let i = 1; i < chartData[0].length; i++) {
                formatter.format(data, i);
            }

            const options = {
                hAxis: {format: 'MMM d, y'}, // Date format
                vAxis: {minValue: 0}, // Min value on the vertical axis
                seriesType: 'bars',
                isStacked: true,
// Additional series for the totals (the first column doesn’t count in the // indices, hence the length minus 2 and not minus 1)
                series: {[chartData[0].length - 2]: {type: 'line'}}
            };
            const chart = new google.visualization.ComboChart(document.getElementById('chart_by_account')
            );
            chart.draw(data, options);
        }
    </script>
</head>
<body class="container mx-auto bg-gray-50 pb-16">
<h1 class="mt-8 mb-8 text-2xl text-blue-800">${title}</h1>

<div class="bg-white shadow-2xl p-8 rounded-xl">
    <table class="w-full">
        <thead>
        <tr class="text-blue-800 border-b-4 border-blue-800">
            <!-- Empty cell over two columns -->
            <th colspan="2">&nbsp;</th>
            <!-- Loop over the snapshots -->
            <#list snapshots as snapshot>
                <th class="text-right">
                    <!-- Formatted snapshot’s date -->
                    ${snapshot.date.toString()?date("yyyy-MM-dd")?string.medium}
                </th>
            </#list>
        </tr>
        </thead>
        <tbody>
        <#list book.institutions as institution>
            <tr class="border-y font-bold">
                <td colspan="${snapshots?size + 2}" class="pt-4">${institution.name}</td>
            </tr>
            <#list book.accountsIn(institution) as account>
                <tr>
                    <td class="w-6 text-center">
                        <#if (account.metadata.riskLevel)??>${account.metadata.riskLevel.symbol}<#else> </#if>
                    </td>
                    <td>${account.name}</td>
                    <#list snapshots as snapshot>
                        <td class="text-right tabular-nums">
                            <#if (snapshot.assetBalance(account))??>
                                ${snapshot.assetBalance(account).toValue(book.mainCurrency,snapshot.date)?round}
                            <#else>-
                            </#if>
                        </td>
                    </#list>
                </tr>
            </#list>
        </#list>
        </tbody>
        <tfoot>
        <!-- This row is here for styling concerns only -->
        <tr class="pt-4 border-t">
            <td>&nbsp;</td>
        </tr>
        <tr class="border-t-4 text-blue-800 border-blue-800 font-bold">
            <td colspan="2">Total</td>
            <#list snapshots as snapshot>
                <td class="text-right tabular-nums">
                    ${snapshotTotals[snapshot.date]?round}
                </td>
            </#list>
        </tr>
        </tfoot>
    </table>
</div>
<div class="w-full bg-white rounded-xl shadow-2xl mt-8 overflow-hidden">
    <h2 class="px-8 pt-8 text-2xl text-blue-800">By account</h2>
    <div id="chart_by_account" style="height: 400px"></div>
</div>
</body>
</html>