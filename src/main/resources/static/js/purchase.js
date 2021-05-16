var app = new Vue({
    el: '#app',
    data: {
        customers: [],
        selectedCustomerId: 0,
        selectedCustomerName: '',
        products: [],
        selectedProducts: [{
            product_id: 0
        }],
        checkoutTotal: 0,
        amountPaid: 0,
        alertMessage: '',
        alertClass: 'alert alert-danger alert-dismissible fade show'
    },
    methods: {
        getProducts: async function () {
            let res = await fetch(properties.api_host + "/product");
            if (res.status == 200) {
                this.products = await res.json();
            } else {
                this.alertClass = 'alert alert-danger alert-dismissible fade show';
                this.alertMessage = "ERROR: Unable to load Products";
            }
        },
        getCustomers: async function () {
            let res = await fetch(properties.api_host + "/customer?type=PURCHASE");
            if (res.status == 200) {
                this.customers = await res.json();
            } else {
                this.alertClass = 'alert alert-danger alert-dismissible fade show';
                this.alertMessage = "ERROR: Unable to load Customers";
            }
        },
        selectCustomer: function (selected) {
            this.selectedCustomerId = selected.customer_id;
            this.selectedCustomerName = selected.customer_name;
        },
        selectProduct: function (event, selected) {
            let prevProductId = event.currentTarget.parentElement.previousElementSibling.getAttribute("productId");
            let isExisting = false;
            this.selectedProducts.forEach((product) => {
                if (selected.product_id == product.product_id) {
                    document.getElementById(selected.product_id).focus();
                    isExisting = true;
                    return;
                }
            });

            if (isExisting) {
                return;
            }

            this.selectedProducts = this.selectedProducts.map((product) => {
                if (product.product_id == 0 || product.product_id == prevProductId) {
                    product.product_id = selected.product_id;
                    product.product_name = selected.product_name;
                    product.quantity = 1;
                    product.price = selected.price;
                    product.total = selected.price;
                }
                return product;
            });
            let total = 0;
            this.selectedProducts.forEach((product) => {
                total += product.total;
            });
            this.checkoutTotal = total;
            this.amountPaid = total;
        },
        updateTotal: function (productId) {
            this.selectedProducts = this.selectedProducts.map((product) => {
                if (productId == product.product_id) {
                    product.total = product.price * product.quantity;
                }
                return product;
            });

            let total = 0;
            this.selectedProducts.forEach((product) => {
                total += product.total;
            });
            this.checkoutTotal = total;
            this.amountPaid = total;
        },
        addRow: function () {
            this.selectedProducts = [...this.selectedProducts, { product_id: 0 }];
        },
        removeRow: function (productId) {
            this.selectedProducts = this.selectedProducts.filter((product) => {
                return product.product_id != productId;
            });
            if (this.selectedProducts.length == 0) {
                this.addRow();
            }
        },
        clearSelectedProducts: function () {
            this.selectedProducts = [{
                product_id: 0
            }];
            this.amountPaid = 0;
            this.selectedCustomerId = 0;
            this.selectedCustomerName = '';
            this.checkoutTotal = 0;
        },
        showPayLaterModal: function () {
            if (this.amountPaid != this.checkoutTotal) {
                $('#payLaterConfirmationModal').modal('show');
            } else {
                this.createPurchase();
            }
        },
        createPurchase: async function () {
            $('#payLaterConfirmationModal').modal('hide');
            let purchase = {};
            if (this.selectedCustomerId != 0) {
                purchase.customer_id = this.selectedCustomerId;
            }
            purchase.amount_paid = this.amountPaid;
            purchase.items = [];
            this.selectedProducts.forEach((product) => {
                item = {};
                item.item_type = 'PRODUCT';
                item.product_id = product.product_id;
                item.quantity = product.quantity;
                item.price = product.price;
                purchase.items.push(item);
            });

            let res = await fetch(properties.api_host + "/purchase", {
                method: 'POST',
                headers: {
                    'content-type': 'application/json'
                },
                body: JSON.stringify(purchase)
            });
            if (res.status > 299) {
                let error = await res.json();
                this.alertMessage = 'ERROR: ' + error.message;
                this.alertClass = 'alert alert-danger alert-dismissible fade show';
            } else {
                this.clearSelectedProducts();
                this.alertClass = 'alert alert-success alert-dismissible fade show';
                this.alertMessage = "Success: Purchase created!";
            }

        },
        dismissAlert: function () {
            this.alertMessage = '';
        }
    },

    created: function () {
        this.getProducts();
        this.getCustomers();
    }
})