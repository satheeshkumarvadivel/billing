$(document).ready(function () {

    $('#saveProductButton').click(function () {
        addNewProduct();
    });

    $('#productName').change(function () {
        $('#productName').removeClass('is-invalid');
    });

    displayProducts();


});

var products = [{ "product_id": 1, "product_name": "Shakthi Chilli Powder 200g", "desctiption": null, "price": 15.0, "in_stock_qty": 1000 }, { "product_id": 2, "product_name": "Shakthi Sambar Powder 100g", "desctiption": null, "price": 20.0, "in_stock_qty": 250 }, { "product_id": 3, "product_name": "Shakthi Rasam Powder 100g", "desctiption": null, "price": 17.0, "in_stock_qty": 450 }, { "product_id": 4, "product_name": "Shakthi Turmeric Powder 50g", "desctiption": null, "price": 22.0, "in_stock_qty": 500 }];

function displayProducts() {
    let products = getProducts();
    let tableBody = '';
    let i = 1;
    if (typeof products == 'object') {
        products.forEach(product => {
            tableBody += '<tr><th scope="row">' + i + '</th><td>' + product.product_name + '</td><td></td><td>' + product.price + '</td><td>' + product.in_stock_qty + '</td><td></td></tr>';
            i = i + 1;
        });
    }
    $('#products_table_body').html(tableBody);
}

function getProducts() {

    return products;



    // $.get("http://localhost:8000/product", function (data, status) {
    //     if (status == 200) {

    //     }
    // });
}

function addNewProduct() {
    let product = {};
    product.product_name = $('#productName').val();
    product.price = $('#productPrice').val();
    product.in_stock_qty = $('#productQuantity').val();
    product.desctiption = $('#description').val();
    validateProduct(product);
    saveProduct(product);
}

function validateProduct(product) {
    if (typeof product != 'undefined') {
        if (typeof product.product_name != 'undefined') {
            if (product.product_name.trim() == '') {
                $('#productName').addClass('is-invalid');
            }
        } else {
            $('#productName').addClass('is-invalid');
        }
    }
}

function saveProduct(product) {
    products.push(product);
    displayProducts();
}