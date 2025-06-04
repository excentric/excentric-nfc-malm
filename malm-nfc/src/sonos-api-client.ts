import * as http from 'http';
import {getGreenText} from "./nfc-common";
import settings from "./settings";

export function makeSonosRequest(sonosRequest: string, zone: string): Promise<void> {
    return new Promise((resolve, reject) => {
        let path = '/' + encodeURIComponent(zone) + '/' + sonosRequest;

        const options = {
            hostname: settings.sonosApi.hostname,
            port: settings.sonosApi?.port || 5005,
            path: path,
            method: 'GET'
        };

        console.log(`Making request to GET http://${options.hostname}:${options.port}${path}`)

        const request = http.request(options, (result) => {
            console.log(`Status Code: ${getGreenText(result.statusCode?.toString())}\n`);

            let data = '';

            result.on('data', (chunk) => {
                data += chunk;
            });

            result.on('end', () => {
                console.log(`Response: ${data}`);
                resolve();
            });
        });

        request.on('error', (error) => {
            console.error(`Error: ${error.message}`);
            reject(error);
        });

        request.end();
    });
}
