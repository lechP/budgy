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
                            <#if (snapshot.accountBalance(account))??>
                                ${snapshot.accountBalance(account).toValue(book.mainCurrency,snapshot.date)?round}
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
</body>
</html>