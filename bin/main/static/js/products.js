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
        displayProduct($('#searchProductInput').val());
    });

    $('#searchProductInput').keypress(function (e) {
        if (e.keyCode == 13) {
            displayProduct($('#searchProductInput').val());
        }
    });

    $('#clearProductButton').click(function () {
        $('#searchProductInput').val('');
        displayProduct('');
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

    displayProduct();

});

var api_host = 'http://localhost:8000';

function displayProduct(productName) {
    let products = getProducts(productName);
    let tableBody = '';
    let i = 1;
    if (typeof products == 'object') {
        products.forEach(product => {
        	if (product.description == 'null') {
        		product.description = '';
        	}
            tableBody += '<tr><th scope="row">' + i + '</th><td>' + product.product_name + '</td><td>' + product.description + '</td><td>' + product.price + '</td><td>' + product.in_stock_qty + '</td>' +
                '<td> <a href="#" style="padding-right: 10px;" onClick=\'openEditProductModal(' + JSON.stringify(product) + ')\'> Edit </a> <a href="#" class="text-danger" onClick=\'showDeleteProductModal(' + JSON.stringify(product) + ')\'> Delete </a></td></tr>';
            i = i + 1;
        });
    }
    $('#products_table_body').html(tableBody);
}

function openEditProductModal(product) {
    $('#editProductModal').modal('show');
    $('#editProductErrorAlert').hide();
    $('#editProductName').val(product.product_name);
    $('#editProductPrice').val(product.price);
    $('#editProductQuantity').val(product.in_stock_qty);
    $('#editDescription').val(product.description);
    $('#editProductId').val(product.product_id);
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
            displayProduct();
        },
        error: function (result) {
            $('#deleteProductErrorMsg').html("Unable to delete product.");
            $('#deleteProductErrorAlert').show();
        }
    });

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
            displayProduct();
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
            displayProduct();
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

