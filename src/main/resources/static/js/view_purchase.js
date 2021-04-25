var app = new Vue({
    el: '#app',
    data: {
        purchases: [],
        currentPurchase: {},
        alertMessage: '',
        alertClass: 'alert alert-danger alert-dismissible fade show'
    },
    methods: {
        getPurchases: async function (e) {
            if (e) {
                if (e && e.type != 'click' && e.keyCode != 13) {
                    return;
                }
            }
            let url = properties.api_host + "/purchase?";
            let search = $('#purchaseSearch').val();
            let from = $('#fromDateInput').val();
            let to = $('#toDateInput').val();
            if (search) {
                url += "&search=" + encodeURIComponent(search)
            }
            if (from && to) {
                url += '&date=' + encodeURIComponent(from + "," + to);
            }
            let res = await fetch(url);
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

        clearPurchaseSearch: function () {
            $('#purchaseSearch').val('');
            $('#fromDateInput').val('');
            $('#toDateInput').val('');
            this.getPurchases();
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
        },
        initDatePicket: function () {
            $("#fromDateInput").datepicker({
                dateFormat: "yy-mm-dd"
            });
            $("#toDateInput").datepicker({
                dateFormat: "yy-mm-dd"
            });
        }

    },

    created: function () {
        this.getPurchases();
    },

    mounted: function () {
        this.initDatePicket();
    }
})