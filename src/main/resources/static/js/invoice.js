$(document).ready(function () {

    $('#deleteProductConfirm').click(function (e) {
        console.log('delete product');
        deleteProduct(e.currentTarget.attributes.deleteproductid.value);
    });

    $('#addProductModal').on('show.bs.modal', function (e) {
        $('#addProductErrorAlert').hide();
        $('#productName').removeClass('is-invalid');
        $('#productName').val('');
        $('#productPrice').val('');
        $('#productQuantity').val('');
        $('#description').val('');
    });

    displayInvoices();

});


function displayInvoices(search) {
    let invoices = getInvoices(search);
    let tableBody = '';
    let i = 1;
    if (typeof invoices == 'object') {
        invoices.forEach(invoice => {
            if (invoice.customer.customer_name == 'null') {
                invoice.customer.customer_name = '';
            }
            let date = new Date(invoice.invoice_date);
            let invoice_date = date.getDate() + "/" + date.getMonth() + "/" + date.getFullYear() + " " + (date.getHours() % 12) + ":" + date.getMinutes() + (date.getHours() >= 12 ? ' PM' : ' AM');
            tableBody += '<tr><th scope="row">' + invoice.invoice_id + '</th><td>' + invoice_date + '</td><td>' + invoice.customer.customer_name + '</td><td>' + invoice.customer.contact_number_1 + '</td><td>' + invoice.price + '</td>' +
                '<td> <a href="#" style="padding-right: 10px;" onClick=\'showInvoiceDetails(' + JSON.stringify(invoice.invoice_id) + ')\'> View </a></td></tr>';
            i = i + 1;
        });
    }
    $('#invoice_table_body').html(tableBody);
}

function showInvoiceDetails(invoice_id) {
    getInvoiceDetails(invoice_id);
}

function getInvoiceDetails(invoice_id) {
    var invoices = [];
    let invoiceUrl = properties.api_host + "/invoice/" + invoice_id;
    $.ajax({
        url: invoiceUrl,
        async: false,
        success: function (result) {
            openViewInvoiceModal(result[0]);
        },
        error: function (result) {
            alert("ERROR: Unable to get invoice details.");
        }
    });
}


function openViewInvoiceModal(invoice) {
    $('#viewInvoiceModal').modal('show');
    

}

function editProduct() {
    let product = {};
    product.product_id = $('#editProductId').val();
    product.product_name = $('#editProductName').val();
    product.price = $('#editProductPrice').val();
    product.in_stock_qty = $('#editProductQuantity').val();
    product.description = $('#editDescription').val();
    if (validateProduct(product)) {
        updateProduct(product);
    }
}

function getInvoices(search) {
    var invoices = [];
    let invoiceUrl = properties.api_host + "/invoice"
    if (search) {
        invoiceUrl += '?search=' + encodeURIComponent(search);
    }
    $.ajax({
        url: invoiceUrl,
        async: false,
        success: function (result) {
            invoices = result;
        },
        error: function (result) {
            invoices = [];
        }
    });
    return invoices;
}
