
// Submits the booking of a tire replacement time.
document.getElementById("submit_booking").addEventListener("click", function() {
    var beginTime = document.getElementById("maintenance_time").value;
    var vehicleType = document.getElementById("car_type_pick").value;
    var workshopName = document.getElementById("workshop_pick").value;

    // Fetch the local timezone offset for the selected workshop
    fetch(`/api/timezone-offset?workshop=${workshopName}`)
        .then(response => response.json())
        .then(data => {
            var timezoneOffset = data.timezoneOffset;

            // Create a Date object from the beginTime
            var localDateTime = new Date(beginTime);

            // Adjust the time to the correct timezone offset
            var offsetInMinutes = timezoneOffset * 60; // Convert hours to minutes

            if (!localDateTime.getTime()) { // Ensures valid parameters for post request
                alert("Please pick a time slot before booking.");
                return;
            }

            var adjustedDateTime = new Date(localDateTime.getTime() + offsetInMinutes * 60000);

            // Converting to ISO string including timezone offset.
            var isoStringWithOffset = adjustedDateTime.toISOString().slice(0, 16) + getTimeZoneOffsetString(timezoneOffset);

            var url = "/book";

            fetch(url, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
                body: new URLSearchParams({
                    beginTime: isoStringWithOffset,
                    vehicleType: vehicleType,
                    workshopName: workshopName
                })
            })
            .then(response => response.text())  
                // Convert the response to text
            .then(text => {
                // Display the response text in the specified element
                document.getElementById("post_response_text").innerText = text;
            })
            .catch(error => {
                console.error('Error:', error.message);
                document.getElementById("post_response_text").innerText = "Something went wrong. Try again.";
            });
        });
});

// Helper function to format timezone offset as "+HH:MM" or "-HH:MM"
function getTimeZoneOffsetString(offset) {
    var sign = offset >= 0 ? "+" : "-";
    var absOffset = Math.abs(offset);
    var hours = Math.floor(absOffset);
    var minutes = (absOffset - hours) * 60;
    return sign + String(hours).padStart(2, '0') + ":" + String(minutes).padStart(2, '0');
}

// Displays a table of tire replacement time slots available for booking.
document.getElementById("submit_filters").addEventListener("click", function() {
    var startTime = document.getElementById("free_timeslot_start").value;
    var endTime = document.getElementById("free_timeslot_end").value;
    var vehicleType = document.getElementById("car_type_filter_pick").value;
    var workshopName = document.getElementById("workshop_filter_pick").value;

    if (!startTime || !endTime) { // Ensures valid parameters for get request.
        alert("Please fill in all the fields before searching.");
        return;
    }

    var url = "/filter?beginTime=" + encodeURIComponent(startTime) + "&endTime=" +
              encodeURIComponent(endTime) + "&vehicleTypes=" + encodeURIComponent(vehicleType) +
              "&workshopPick=" + encodeURIComponent(workshopName);

    fetch(url, {
        method: "GET",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
        },
    })
    .then(response => {
        var tbody = document.querySelector("#results_table tbody");
        var resultsTable = document.getElementById("results_table");
        var responseText = document.getElementById("get_response_text");

        if (response.status === 200) { 
            return response.json().then(data => {
                // Setup for elements to be entered.
                tbody.innerHTML = "";
                resultsTable.style.visibility = "visible";
                responseText.innerText = "";

                
                data.forEach(item => {
                    var date = new Date(item.tireReplacementTime);

                    // Format the date and time for display (local time)
                    var formattedDate = date.toLocaleDateString("en-GB", {
                        day: "2-digit",
                        month: "short",
                        year: "numeric"
                    });
                    var formattedTime = date.toLocaleTimeString("en-GB", {
                        hour: "2-digit",
                        minute: "2-digit"
                    });

                    // Get local ISO format (yyyy-mm-ddTHH:mm) for populateForm(row) function.
                    var localISO = date.getFullYear() + '-' +
                                    String(date.getMonth() + 1).padStart(2, '0') + '-' +
                                    String(date.getDate()).padStart(2, '0') + 'T' +
                                    String(date.getHours()).padStart(2, '0') + ':' +
                                    String(date.getMinutes()).padStart(2, '0');

                    // Capitalize the first letter of the workshop name for display.
                    var displayWorkshopName = item.workshopName.charAt(0).toUpperCase() + item.workshopName.slice(1);

                    // Add a space after every comma.
                    var formattedVehicleTypes = item.vehicleTypesServiced.replace(/,/g, ', ');

                    // Insert the row with the display value and data-* attribute.
                    var row = `<tr onclick="populateForm(this)">
                        <td data-original="${item.workshopName}">${displayWorkshopName}</td>
                        <td>${item.workshopAddress}</td>
                        <td data-iso="${localISO}">${formattedDate} ${formattedTime}</td>
                        <td>${formattedVehicleTypes}</td>
                    </tr>`;
                    tbody.insertAdjacentHTML('beforeend', row);
                    
                });
            });
        } else if (response.status === 204) { // If the response was of type "no content".
            tbody.innerHTML = "";
            resultsTable.style.visibility = "hidden";
            responseText.innerText = "No workshops can meet these conditions";
        } else { // Server errors.
            tbody.innerHTML = "";
            resultsTable.style.visibility = "hidden";
            console.log("Error:" + response);
            responseText.innerText = "Something went wrong. Try again.";
        }
    })
    .catch(error => { // Malformed response, that have an error thrown when parsed.
        var responseText = document.getElementById("get_response_text");

        console.error('Error:', error);
        resultsTable.style.visibility = "hidden";
        responseText.innerText = "Something went wrong. Try again.";
    });
});

// Function to populate booking form inputs using the values in a clicked row.
function populateForm(row) {
    // Extract values from the clicked row.
    var workshopName = row.querySelector('td[data-original]').dataset.original;
    var dateTimeISO = row.querySelector('td[data-iso]').dataset.iso;
    var vehicleTypes = row.cells[3].innerText.split(', ').map(type => type.trim());

    document.getElementById('maintenance_time').value = dateTimeISO;

    var carTypeSelect = document.getElementById('car_type_pick');
    carTypeSelect.value = vehicleTypes[0].toLowerCase();

    var workshopSelect = document.getElementById('workshop_pick');
    workshopSelect.value = workshopName.toLowerCase();
}

// Function to fetch configuration values and populate the select elements.
function populateOptions() {
    fetch('/api/config')
        .then(response => response.json())
        .then(data => {
            const serversSelect = document.getElementById('workshop_pick');
            const carTypesSelect = document.getElementById('car_type_pick');
            const carTypeFilterSelect = document.getElementById('car_type_filter_pick');
            const workshopFilterSelect = document.getElementById('workshop_filter_pick');

            // Populate workshop options.
            data.servers.forEach(server => {
                const option = document.createElement('option');
                option.value = server.toLowerCase();
                option.textContent = server.charAt(0).toUpperCase() + server.slice(1);
                serversSelect.appendChild(option);

                // Populate workshop filter.
                const filterOption = document.createElement('option');
                filterOption.value = server.toLowerCase();
                filterOption.textContent = server.charAt(0).toUpperCase() + server.slice(1);
                workshopFilterSelect.appendChild(filterOption);
            });

            // Populating car types.
            data.carTypes.forEach(type => {
                const option = document.createElement('option');
                option.value = type.toLowerCase();
                option.textContent = type.charAt(0).toUpperCase() + type.slice(1);
                carTypesSelect.appendChild(option);

                // Populating car type filter.
                const filterOption = document.createElement('option');
                filterOption.value = type.toLowerCase();
                filterOption.textContent = type.charAt(0).toUpperCase() + type.slice(1);
                carTypeFilterSelect.appendChild(filterOption);
            });
        })
        .catch(error => console.error('Error fetching configuration:', error));
}

// Call the function to populate options.
document.addEventListener('DOMContentLoaded', populateOptions);