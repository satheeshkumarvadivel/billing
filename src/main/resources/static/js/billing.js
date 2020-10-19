var invoice = {};
invoice.items = [];
sessionStorage.setItem('invoiceRowCount', 0);
$(document).ready(function () {

    // $('#searchInvoiceCustomer').focusin(function () {
    //     $('#customersListGroup').show();
    // });

    $('#customersListGroup > .list-group-item').click(function (e) {
        console.log("customer id : " + e.currentTarget.attributes.customer_id.value);
        $('#searchInvoiceCustomer').val(e.currentTarget.value);
        // $('#customersListGroup').hide();
    });

    // $('#invoiceRightPane').click(function(e) {
    //     $('#customersListGroup').hide();
    // });

    $('#productPrice').keyup(function () {
        updateTotalInvoiceItemAmount();
    });
    $('#productQuantity').keyup(function () {
        updateTotalInvoiceItemAmount();
    });

    $('#addInvoiceItemButton').click(function () {
        addInvoiceItem();
    })


    populateInvoiceItems();


});

var api_host = 'http://localhost:8000';

function populateInvoiceItems() {
    let products = getProducts();
    let listItems = '';
    products.forEach(product => {
        listItems += '<button type="button" product_id="' + product.product_id + '" product_name="' + product.product_name + '" price="' + product.price +
            '" in_stock_qty="' + product.in_stock_qty + '" class="list-group-item list-group-item-action">' + product.product_name + '</button>';
    });
    $('#itemsListGroup').html(listItems);

    $('#itemsListGroup .list-group-item').click(function (e) {
        product = {};
        product.product_id = e.currentTarget.attributes.product_id.value;
        product.product_name = e.currentTarget.attributes.product_name.value;
        product.price = e.currentTarget.attributes.price.value;
        product.in_stock_qty = e.currentTarget.attributes.in_stock_qty.value;
        showAddInvoiceItemModal(product);
    });
}

function showAddInvoiceItemModal(product) {
    $('#editProductErrorAlert').hide();
    $('#productId').val(product.product_id);
    $('#productName').val(product.product_name);
    $('#productPrice').val(product.price);
    $('#productQuantity').val(1);
    $('#inStockQty').val(product.in_stock_qty);
    $('#totalAmount').val(product.price);
    $('#addInvoiceItemModal').modal('show');
    $('#productQuantity').focus();
}

function updateTotalInvoiceItemAmount() {
    let price = parseFloat($('#productPrice').val());
    let quantity = parseInt($('#productQuantity').val());
    $('#totalAmount').val(price * quantity);
}

function addInvoiceItem() {
    if ($('#totalAmount').val() == 'NaN' || parseFloat($('#totalAmount').val()) < 0) {
        $('#addInvoiceItemErrorMsg').html('Invalid Price or Quantity');
        $('#addInvoiceItemErrorAlert').show();
        return;
    }
    if (parseInt($('#inStockQty').val()) - parseInt($('#productQuantity').val()) < 0) {
        $('#addInvoiceItemErrorMsg').html('Stock not available.');
        $('#addInvoiceItemErrorAlert').show();
        return;
    }

    let item = {};
    item.product = {};
    item.product.product_id = $('#productId').val();
    item.quantity = parseInt($('#productQuantity').val());
    item.price = parseFloat($('#totalAmount').val());

    invoice.items.push(item);
    let count = parseInt(sessionStorage.getItem('invoiceRowCount'));
    if (count == 0) {
        $('#invoice_table_body').html('');
    }
    count += 1;

    let invoiceRow = '<tr>';
    invoiceRow += '<td>' + count + '</td>';
    invoiceRow += '<td>' + $('#productName').val() + '</td>';
    invoiceRow += '<td>' + $('#productPrice').val() + '</td>';
    invoiceRow += '<td>' + $('#productQuantity').val(); + '</td>';
    invoiceRow += '<td>' + $('#totalAmount').val() + '</td><td><a href="#" class="text-danger"> Remove </a></td></tr>';

    sessionStorage.setItem('invoiceRowCount', count);
    $('#invoice_table_body').append(invoiceRow);
    $('#addInvoiceItemModal').modal('hide');

}

function getProducts(productName) {
    var products = [];
    let productUrl = api_host + "/product"
    if (productName) {
        productUrl += '?product_name=' + encodeURIComponent(productName);
    }
    $.ajax({
        url: productUrl,
        async: false,
        success: function (result) {
            products = result;
        },
        error: function (result) {
            products = [];
        }
    });
    return products;
}