var app = new Vue({
    el: '#app',
    data: {
        purchases: [],
        currentPurchase: {},
        alertMessage: '',
        alertClass: 'alert alert-danger alert-dismissible fade show'
    },
    methods: {
        getPurchases: async function () {
            let res = await fetch(properties.api_host + "/purchase");
            if (res.status == 200) {
                let purchase = await res.json();
                purchase = purchase.map((item) => {
                    let date = new Date(item.purchase_date);
                    item.purchase_date = date.getDate() + "/" + (date.getMonth() + 1) + "/" + date.getFullYear();
                    return item;
                });
                this.purchases = purchase;
            } else {
                this.alertClass = 'alert alert-danger alert-dismissible fade show';
                this.alertMessage = "ERROR: Unable to load Purchases";
            }
        },
        viewPurchase: function (purchase_id) {
            this.getPurchaseById(purchase_id);
        },

        getPurchaseById: async function (id) {
            let res = await fetch(properties.api_host + "/purchase/" + id);
            if (res.status == 200) {
                this.currentPurchase = await res.json();
                $('#viewPurchaseModal').modal('show');
            } else {
                this.alertClass = 'alert alert-danger alert-dismissible fade show';
                this.alertMessage = "ERROR: Unable to load Purchases Details";
            }
        },

        showAddPurchase: function () {
            document.location.href = "/purchases";
        },
        dismissAlert: function () {
            this.alertMessage = '';
        }

    },

    created: function () {
        this.getPurchases();
    }
})