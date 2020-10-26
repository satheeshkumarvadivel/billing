$(document).ready(function () {

    $('#saveProductButton').click(function () {
        addNewProduct();
    });

    $('#updateProductButton').click(function () {
        editProduct();
    });

    $('#deleteProductConfirm').click(function (e) {
        console.log('delete product');
        deleteProduct(e.currentTarget.attributes.deleteproductid.value);
    });

    $('#searchProductButton').click(function () {
        displayInvoices($('#searchProductInput').val());
    });

    $('#searchProductInput').keypress(function (e) {
        if (e.keyCode == 13) {
            displayInvoices($('#searchProductInput').val());
        }
    });

    $('#clearProductButton').click(function () {
        $('#searchProductInput').val('');
        displayInvoices('');
    });

    $('#productName').change(function () {
        $('#productName').removeClass('is-invalid');
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

var api_host = 'http://localhost:8000';

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
                '<td> <a href="#" style="padding-right: 10px;" onClick=\'openViewInvoiceModal(' + JSON.stringify(invoice.invoice_id) + ')\'> View </a></td></tr>';
            i = i + 1;
        });
    }
    $('#invoice_table_body').html(tableBody);
}

function openViewInvoiceModal(invoice_id) {
    alert(invoice_id);
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

function showDeleteProductModal(product) {
    console.log("Delete Product : " + product);
    $('#deleteProductModal').modal('show');
    $('#deleteProductErrorAlert').hide();
    $('#deleteProductConfirm').attr('deleteProductId', product.product_id);
}

function deleteProduct(product_id) {
    $('#deleteProductErrorAlert').hide();
    $.ajax({
        url: api_host + "/product/" + product_id,
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        },
        async: false,
        success: function (result) {
            $('#deleteProductModal').modal('hide');
            displayInvoices();
        },
        error: function (result) {
            $('#deleteProductErrorMsg').html("Unable to delete product.");
            $('#deleteProductErrorAlert').show();
        }
    });

}

function getInvoices(search) {
    var invoices = [];
    let invoiceUrl = api_host + "/invoice"
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

function addNewProduct() {
    let product = {};
    product.product_name = $('#productName').val();
    product.price = $('#productPrice').val();
    product.in_stock_qty = $('#productQuantity').val();
    product.description = $('#description').val();
    if (validateProduct(product)) {
        createProduct(product);
    }
}

function validateProduct(product) {
    if (typeof product != 'undefined') {
        if (typeof product.product_name != 'undefined') {
            if (product.product_name.trim() == '') {
                $('#productName').addClass('is-invalid');
                $('#editProductName').addClass('is-invalid');
                return false;
            }
        } else {
            $('#productName').addClass('is-invalid');
            return false;
        }
        return true;
    }
    return false;
}

function createProduct(product) {
    $('#addProductErrorAlert').hide();
    $.ajax({
        url: api_host + "/product",
        method: 'POST',
        data: JSON.stringify(product),
        headers: {
            'Content-Type': 'application/json'
        },
        async: false,
        success: function (result) {
            $("#addProductModal").modal('toggle');
            displayInvoices();
        },
        error: function (result) {
            if (result.responseJSON && result.responseJSON.message) {
                $('#addProductErrorMsg').html(result.responseJSON.message);
            } else {
                $('#addProductErrorMsg').html("Unable to create Product.");
            }
            $('#addProductErrorAlert').show();
        }
    });
}

function updateProduct(product) {
    $('#editProductErrorAlert').hide();
    console.log(product);
    $('#editProductErrorAlert').hide();
    $.ajax({
        url: api_host + "/product/" + product.product_id,
        method: 'PUT',
        data: JSON.stringify(product),
        headers: {
            'Content-Type': 'application/json'
        },
        async: false,
        success: function (result) {
            $("#editProductModal").modal('toggle');
            displayInvoices();
        },
        error: function (result) {
            if (result.responseJSON && result.responseJSON.message) {
                $('#editProductErrorMsg').html(result.responseJSON.message);
            } else {
                $('#editProductErrorMsg').html("Unable to save product information.");
            }
            $('#editProductErrorAlert').show();
        }
    });
}

