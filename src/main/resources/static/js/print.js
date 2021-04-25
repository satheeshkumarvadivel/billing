(function ($) {
    $(function () {
        $(document).ready(function () {
            let invoice = JSON.parse(localStorage.getItem('current_invoice'));
            renderInvoicePrintPage(invoice);
        });

        function renderInvoicePrintPage(invoice) {
            let urlParams = new URLSearchParams(window.location.search);
            let quote = urlParams.get('quote');
            $('#invoice_id').html(invoice.invoice_id);
            if (quote == 'true') {
                $('#company_info').html('<h2>QUOTATION</h2>');
                $('#invoice_id').hide();
            }
            $('#customer_name').html(invoice.customer.customer_name);
            $('#invoice_date').html(formatDate(invoice.invoice_date));


            var rowHtml = "";
            let total = 0;
            for (let i = 0; i < invoice.items.length; i++) {
                rowHtml += '<tr><td>' + (i + 1) + '</td><td>' + invoice.items[i].product.product_name +
                    '</td><td>' + invoice.items[i].quantity + '</td><td>' + invoice.items[i].price + '</td><td>' + invoice.items[i].total + '</td></tr>';
                total += invoice.items[i].total;
            }
            rowHtml += '<tr><td class="lastrow"></td><td class="lastrow"></td><td class="lastrow"></td><td class="lastrow" style="font-weight: bold; text-align: right;">TOTAL : </td><td class="lastrow" style="font-weight: bold;">' + total + '</td></tr>';
            $('#invoiceBody').html(rowHtml);

        }

        function formatDate(oldDate) {
            let dateArray = oldDate.split('-');
            return dateArray[2].split(" ")[0] + '/' + dateArray[1] + '/' + dateArray[0];
        }

    });



})(jQuery); // end of jQuery name space