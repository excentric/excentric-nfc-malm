import * as fs from 'fs';
import * as path from 'path';

interface Settings {
    sonosRoom: string;
    sonosApi: {
        hostname: string;
        port: number;
    };
}

// Load settings from the settings.json file
const settingsPath = path.resolve(__dirname, '../settings.json');
let settings: Settings;

try {
    const settingsData = fs.readFileSync(settingsPath, 'utf8');
    settings = JSON.parse(settingsData);
} catch (error) {
    console.error('Error loading settings:', error);
    // Fallback to default settings if file can't be read
    settings = {
        sonosRoom: 'Playroom Sonos',
        sonosApi: {
            hostname: 'sonos-api',
            port: 5005
        }
    };
}

export default settings;
