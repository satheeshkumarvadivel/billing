var invoice = {};
invoice.items = [];
invoice.customer = {};
invoice.customer.customer_id = 1;
sessionStorage.setItem('invoiceRowCount', 0);
sessionStorage.setItem('invoiceTotalAmount', 0);
$(document).ready(function () {

    $('#productPrice').keyup(function () {
        updateTotalInvoiceItemAmount();
    });
    $('#productQuantity').keyup(function () {
        updateTotalInvoiceItemAmount();
    });

    $('#searchInvoiceItem').keyup(function () {
        populateInvoiceItems($('#searchInvoiceItem').val());
    });

    $('#customerPhNumber').keyup(function () {
        populateCustomerList($('#customerPhNumber').val());
    });

    $('#amountReceived').keyup(function () {
        updateRemainingAmount();
    });

    $('#addInvoiceItemButton').click(function () {
        addInvoiceItem();
    });

    $('#invoiceCheckoutButton').click(function () {
        showCheckoutModal();
    });

    $('#createInvoiceButton').click(function () {
        createInvoice();
    });

    $('#invoiceCancelButton').click(function () {
        clearInvoiceData();
    });

    populateInvoiceItems();

    populateCustomerList();


});

function populateInvoiceItems(product_search) {
    let products = getProducts(product_search);
    let listItems = '';
    products.forEach(product => {
        if (product.in_stock_qty <= 0) {
            listItems += '<button type="button" product_id="' + product.product_id + '" product_name="' + product.product_name + '" price="' + product.price +
                '" in_stock_qty="' + product.in_stock_qty + '" class="list-group-item list-group-item-action d-flex justify-content-between align-items-center disabled">' + product.product_name;
        } else {
            listItems += '<button type="button" product_id="' + product.product_id + '" product_name="' + product.product_name + '" price="' + product.price +
                '" in_stock_qty="' + product.in_stock_qty + '" class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">' + product.product_name;
        }

        if (product.in_stock_qty <= 0) {
            listItems += '<span class="badge badge-secondary badge-pill">' + 'Out of Stock' + '</span></button>';
        } else if (product.in_stock_qty <= 10) {
            listItems += '<span class="badge badge-danger badge-pill">' + product.price + ' Rs</span></button>';
        } else if (product.in_stock_qty <= 50) {
            listItems += '<span class="badge badge-warning badge-pill">' + product.price + ' Rs</span></button>';
        } else {
            listItems += '<span class="badge badge-success badge-pill">' + product.price + ' Rs</span></button>';
        }

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

function populateCustomerList(customer_search) {

    let customers = getCustomers(customer_search);
    let customerList = '';
    for (let i = 0; i < customers.length; i++) {
        customerList += '<a class="dropdown-item" onclick="javascript:selectCustomer(\'' + customers[i].customer_name + ":" + customers[i].customer_id + '\')">'
            + customers[i].customer_name + '</a>';
    }
    $('#customerSearchList').html(customerList);
}

function showAddInvoiceItemModal(product) {
    $('#editProductErrorAlert').hide();
    $('#productId').val(product.product_id);
    let oldItemQty = $('#product-' + product.product_id + '-qty').html();
    if (oldItemQty) {
        let oldItemPrice = $('#product-' + product.product_id + '-price').html();
        let oldItemTotal = $('#product-' + product.product_id + '-total').html();
        $('#productQuantity').val(parseInt(oldItemQty));
        $('#productPrice').val(oldItemPrice);
        $('#totalAmount').val(oldItemTotal);
    } else {
        $('#productName').val(product.product_name);
        $('#productPrice').val(product.price);
        $('#productQuantity').val(1);
        $('#totalAmount').val(product.price);
    }
    $('#productName').val(product.product_name);
    $('#inStockQty').val(product.in_stock_qty);

    $('#addInvoiceItemModal').modal('show');
    $('#productQuantity').focus();
}

function showCheckoutModal() {
    $('#checkoutErrorAlert').hide();
    $('#checkoutSuccessAlert').hide();
    $('#payLaterDiv').hide();
    $('#createInvoiceButton').show();
    $('#printInvoiceButton').hide();
    $('#amountReceived').val('');
    if (invoice.items.length > 0) {
        $('#checkoutTotal').html(sessionStorage.getItem('invoiceTotalAmount'));
        $('#checkoutModal').modal('show');
    }
}

function clearInvoiceData() {
    invoice = {};
    invoice.items = [];
    invoice.customer = {};
    invoice.customer.customer_id = 1;
    sessionStorage.setItem('invoiceRowCount', 0);
    sessionStorage.setItem('invoiceTotalAmount', 0);
    $('#invoice_table_body').html('<tr><td colspan="6" style="text-align: center;">No Items added!</td></tr>');
    $('#totalCardTitle').html('Total: 0.0 Rs');
}

function updateRemainingAmount() {
    let amountReceived = parseFloat($('#amountReceived').val());
    let total = parseFloat(sessionStorage.getItem('invoiceTotalAmount'));
    let remaining = amountReceived - total;
    $('#checkoutRemaining').html(remaining);
    // if (remaining < 0 && invoice.customer && invoice.customer.customer_id > 0) {
    //     $('#payLaterLabel').html('Pay ' + Math.abs(remaining) + ' Rs later?');
    //     $('#payLaterDiv').show();
    // } else {
    //     $('#payLaterDiv').hide();
    // }
}

function updateTotalInvoiceItemAmount() {
    let price = parseFloat($('#productPrice').val());
    let quantity = parseInt($('#productQuantity').val());
    let totalAmount = price * quantity;
    $('#totalAmount').val(totalAmount);
}

function addInvoiceItem() {
    if ($('#totalAmount').val() == 'NaN' || parseFloat($('#totalAmount').val()) < 0 || parseFloat($('#productQuantity').val()) <= 0) {
        $('#addInvoiceItemErrorMsg').html('Invalid Price or Quantity');
        $('#addInvoiceItemErrorAlert').show();
        return;
    }
    if (parseInt($('#inStockQty').val()) - parseInt($('#productQuantity').val()) < 0) {
        $('#addInvoiceItemErrorMsg').html('Stock not available.');
        $('#addInvoiceItemErrorAlert').show();
        return;
    }


    let new_item_id = $('#productId').val();
    let i = 0;
    let isOldItem = false;
    var oldItem;
    for (i = 0; i < invoice.items.length; i++) {
        oldItem = invoice.items[i];
        if (new_item_id == oldItem.product.product_id) {
            oldItem.product.product_id = $('#productId').val();
            oldItem.quantity = parseInt($('#productQuantity').val());
            oldItem.price = parseFloat($('#productPrice').val());
            isOldItem = true;
            break;
        }
    }

    let count = parseInt(sessionStorage.getItem('invoiceRowCount'));
    if (count == 0) {
        $('#invoice_table_body').html('');
    }

    if (!isOldItem) {
        count += 1;
        let item = {};
        item.product = {};
        item.product.product_id = $('#productId').val();
        item.quantity = parseInt($('#productQuantity').val());
        item.price = parseFloat($('#productPrice').val());
        invoice.items.push(item);
        let invoiceRow = '<tr id="product-' + $('#productId').val() + '">';
        invoiceRow += '<td class="product-sno">' + count + '</td>';
        invoiceRow += '<td>' + $('#productName').val() + '</td>';
        invoiceRow += '<td id="product-' + $('#productId').val() + '-price">' + $('#productPrice').val() + '</td>';
        invoiceRow += '<td id="product-' + $('#productId').val() + '-qty">' + $('#productQuantity').val() + '</td>';
        invoiceRow += '<td id="product-' + $('#productId').val() + '-total">' + $('#totalAmount').val(); + '</td>';
        invoiceRow += '<td><a class="remove_product text-danger" product_id=' + $('#productId').val() + ' href="#" > Remove </a></td>';
        $('#invoice_table_body').append(invoiceRow);

        $('.remove_product').click(function (e) {
            removeProduct(e);
        });

    } else {
        $('#product-' + $('#productId').val() + '-price').html($('#productPrice').val());
        $('#product-' + $('#productId').val() + '-qty').html($('#productQuantity').val());
        $('#product-' + $('#productId').val() + '-total').html($('#totalAmount').val());
    }
    sessionStorage.setItem('invoiceRowCount', count);
    updateTotalCard();
    $('#addInvoiceItemModal').modal('hide');

}

function removeProduct(e) {
    let productId = e.currentTarget.attributes.product_id.value;
    for (i = 0; i < invoice.items.length; i++) {
        if (invoice.items[i].product.product_id == productId) {
            invoice.items.splice(i, 1);
        }
    }
    $('#invoice_table_body > #product-' + productId).remove();
    updateTotalCard();
    redrawSerialNumber();
}

function redrawSerialNumber() {
    let items = $('.product-sno');
    if (items) {
        for (let i = 0; i < items.length; i++) {
            items[i].innerText = i + 1;
            sessionStorage.setItem('invoiceRowCount', i + 1);
        }
    }
}

function getProducts(product_search) {
    var products = [];
    let productUrl = properties.api_host + "/product"
    if (product_search) {
        productUrl += '?product_name=' + encodeURIComponent(product_search);
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

function getCustomers(customer_search) {
    var customers = [];
    let customerUrl = properties.api_host + "/customer"
    if (customer_search) {
        customerUrl += '?search=' + encodeURIComponent(customer_search);
    }
    $.ajax({
        url: customerUrl,
        async: false,
        success: function (result) {
            customers = result;
        },
        error: function (result) {
            customers = [];
        }
    });
    return customers;
}

function createInvoice() {
    let payment_received = parseFloat(sessionStorage.getItem('invoiceTotalAmount'));
    invoice.payment_received = payment_received;
    let customer_id = $('#checkoutCustomerId').val();
    if (customer_id == '') {
        customer_id = 1;
    } else {
        invoice.customer.customer_id = customer_id;
    }

    let invoiceUrl = properties.api_host + "/invoice"
    $.ajax({
        url: invoiceUrl,
        method: 'POST',
        data: JSON.stringify(invoice),
        async: false,
        headers: {
            'Content-Type': 'application/json'
        },
        success: function (result) {
            clearInvoiceData();
            $('#checkoutSuccessAlert').show();
            $('#createInvoiceButton').hide();
            $('#printInvoiceButton').show();
            populateInvoiceItems();
        },
        error: function (result) {
            if (result.responseJSON && result.responseJSON.message) {
                $('#checkoutErrorMsg').html(result.responseJSON.message);
            } else {
                $('#checkoutErrorMsg').html("Unable to create Invoice.");
            }
            $('#checkoutErrorAlert').show();
            populateInvoiceItems();
        }
    });
}

function updateTotalCard() {
    let total = 0.0;
    let i = 0;
    for (i = 0; i < invoice.items.length; i++) {
        total += invoice.items[i].price * invoice.items[i].quantity;
    }
    sessionStorage.setItem('invoiceTotalAmount', total);
    $('#totalCardTitle').html('Total : ' + total + ' Rs');
}

function selectCustomer(customer_string) {
    let customer_name = customer_string.split(":")[0];
    let customer_id = customer_string.split(":")[1];
    $('#customerPhNumber').val(customer_name);
    $('#checkoutCustomerId').val(customer_id);
}