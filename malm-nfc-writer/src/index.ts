/**
 * HTTP GET request example
 */

import * as http from 'http';

// Function to make a GET request
export function makeSonosRequest(sonosRequest: string, successCallback?: () => void): void {
    const options = {
        hostname: 'bb',
        port: 5005,
        path: '/Office/' + sonosRequest,
        method: 'GET'
    };

    const request = http.request(options, (res) => {
        console.log(`Status Code: ${res.statusCode}`);

        let data = '';

        // A chunk of data has been received
        res.on('data', (chunk) => {
            data += chunk;
        });

        // The whole response has been received
        res.on('end', () => {
            console.log('Response:');
            console.log(data);

            // Call the callback function if provided
            if (successCallback) {
                successCallback();
            }
        });
    });

    // Handle errors
    request.on('error', (error) => {
        console.error(`Error: ${error.message}`);
    });

    // End the request
    request.end();
}
