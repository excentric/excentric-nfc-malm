import * as http from 'http';
import {getGreenText} from "./nfc-common";

export function makeSonosRequest(sonosRequest: string, zone: string, successCallback?: () => void): void {

    let path = '/' + encodeURIComponent(zone) + '/' + sonosRequest;

    const options = {
        hostname: 'bb',
        port: 5005,
        path: path,
        method: 'GET'
    };

    console.log(`Making request to GET http://${options.hostname}:${options.port}/${path}`)

    const request = http.request(options, (result) => {
        console.log(`Status Code: ${getGreenText(result.statusCode?.toString())}\n`);

        let data = '';

        result.on('data', (chunk) => {
            data += chunk;
        });

        result.on('end', () => {
            // console.log('Response:');
            // console.log(data);

            if (successCallback) {
                successCallback();
            }
        });
    });

    request.on('error', (error) => {
        console.error(`Error: ${error.message}`);
    });

    request.end();
}
