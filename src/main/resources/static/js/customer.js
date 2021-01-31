$(document).ready(function () {

    $('#saveCustomerButton').click(function () {
        addNewCustomer();
    });

    $('#updateCustomerButton').click(function () {
        editCustomer();
    });

    $('#deleteCustomerConfirm').click(function (e) {
        console.log('delete customer');
        deleteCustomer(e.currentTarget.attributes.deletecustomerid.value);
    });

    $('#searchCustomerButton').click(function () {
        displayCustomer($('#searchCustomerInput').val());
    });

    $('#searchCustomerInput').keypress(function (e) {
        if (e.keyCode == 13) {
            displayCustomer($('#searchCustomerInput').val());
        }
    });

    $('#clearCustomerButton').click(function () {
        $('#searchCustomerInput').val('');
        displayCustomer('');
    });

    $('#inputCompany').change(function () {
        $('#inputCompany').removeClass('is-invalid');
    });

    $('#addCustomerModal').on('show.bs.modal', function (e) {
        $('#addCustomerErrorAlert').hide();
        $('#inputCompany').removeClass('is-invalid');
        $('#inputCompany').val('');
        $('#inputCustomer').val('');
        $('#inputAddress').val('');
        $('#inputPhone').val('');
		$('#inputEmail').val('');
		$('#inputOutstanding').val('');
    });

    displayCustomer();

});

function displayCustomer(inputCompany) {
    let customers = getCustomers(inputCompany);
    let tableBody = '';
    let i = 1;
    if (typeof customers == 'object') {
        customers.forEach(customer => {
        	tableBody += '<tr><th scope="row">' + i + '</th><td>' + customer.company_name + '</td><td>' + customer.customer_name + '</td><td>' + customer.contact_number_1 + '</td><td>' + customer.email + '</td><td>' + customer.address + '</td><td>' + customer.outstanding_amount + '</td>' +
                '<td> <a href="#" style="padding-right: 10px;" onClick=\'openEditCustomerModal(' + JSON.stringify(customer) + ')\'> Edit </a> <a href="#" class="text-danger" onClick=\'showDeleteCustomerModal(' + JSON.stringify(customer) + ')\'> Delete </a></td></tr>';
            i = i + 1;
        });
    }
    $('#customers_table_body').html(tableBody);
}

function openEditCustomerModal(customer) {
    $('#editCustomerModal').modal('show');
    $('#editCustomerErrorAlert').hide();
    $('#editInputCompany').val(customer.company_name);
    $('#editInputCustomer').val(customer.customer_name);
    $('#editInputAddress').val(customer.address);
    $('#editInputPhone').val(customer.contact_number_1);
	$('#editInputEmail').val(customer.email);
	$('#editInputOutstanding').val(customer.outstanding_amount);
    $('#editCustomerId').val(customer.customer_id);
}

function editCustomer() {
	let customer = {};
    customer.company_name = $('#editInputCompany').val();
    customer.customer_name = $('#editInputCustomer').val();
	customer.contact_number_1 = $('#editInputPhone').val();
    customer.address = $('#editInputAddress').val();
	customer.email = $('#editInputEmail').val();
	customer.outstanding_amount = $('#editInputOutstanding').val();
    customer.customer_id = $('#editCustomerId').val();
    if (validateCustomer(customer)) {
        updateCustomer(customer);
    
    }
}

function showDeleteCustomerModal(customer) {
    console.log("Delete Customer : " + customer);
    $('#deleteCustomerModal').modal('show');
    $('#deleteCustomerErrorAlert').hide();
    $('#deleteCustomerConfirm').attr('deleteCustomerId', customer.customer_id);
}

function deleteCustomer(customer_id) {
    $('#deleteCustomerErrorAlert').hide();
    $.ajax({
        url: properties.api_host + "/customer/" + customer_id,
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        },
        async: false,
        success: function (result) {
            $('#deleteCustomerModal').modal('hide');
            displayCustomer();
        },
        error: function (result) {
            $('#deleteCustomerErrorMsg').html("Unable to delete customer.");
            $('#deleteCustomerErrorAlert').show();
        }
    });

}

function getCustomers(inputCompany) {
    var customers = [];
    let customerUrl = properties.api_host + "/customer"
    if (inputCompany) {
        customerUrl += '?search=' + encodeURIComponent(inputCompany);
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

function addNewCustomer() {
    let customer = {};
    customer.company_name = $('#inputCompany').val();
    customer.customer_name = $('#inputCustomer').val();
	customer.contact_number_1 = $('#inputPhone').val();
    customer.address = $('#inputAddress').val();
	customer.email = $('#inputEmail').val();
	customer.outstanding_amount = $('#inputOutstanding').val();
   
    if (validateCustomer(customer)) {
        createCustomer(customer);
    }
}

function validateCustomer(customer) {
    if (typeof customer != 'undefined') {
        if (typeof customer.customer_name != 'undefined') {
            if (customer.customer_name.trim() == '') {
                $('#inputCompany').addClass('is-invalid');
                $('#editInputCompany').addClass('is-invalid');
                return false;
            }
        } else {
            $('#inputCompany').addClass('is-invalid');
            return false;
        }
        return true;
    }
    return false;
}

function createCustomer(customer) {
    $('#addCustomerErrorAlert').hide();
    $.ajax({
        url: properties.api_host + "/customer",
        method: 'POST',
        data: JSON.stringify(customer),
        headers: {
            'Content-Type': 'application/json'
        },
        async: false,
        success: function (result) {
            $("#addCustomerModal").modal('toggle');
            displayCustomer();
        },
        error: function (result) {
            if (result.responseJSON && result.responseJSON.message) {
                $('#addCustomerErrorMsg').html(result.responseJSON.message);
            } else {
                $('#addCustomerErrorMsg').html("Unable to create customer.");
            }
            $('#addCustomerErrorAlert').show();
        }
    });
}

function updateCustomer(customer) {
    $('#editCustomerErrorAlert').hide();
    console.log(customer);
    $('#editCustomerErrorAlert').hide();
    $.ajax({
        url: properties.api_host + "/customer/" + customer.customer_id,
        method: 'PUT',
        data: JSON.stringify(customer),
        headers: {
            'Content-Type': 'application/json'
        },
        async: false,
        success: function (result) {
            $("#editCustomerModal").modal('toggle');
            displayCustomer();
        },
        error: function (result) {
            if (result.responseJSON && result.responseJSON.message) {
                $('#editCustomerErrorMsg').html(result.responseJSON.message);
            } else {
                $('#editCustomerErrorMsg').html("Unable to save product information.");
            }
            $('#editCustomerErrorAlert').show();
        }
    });
}

