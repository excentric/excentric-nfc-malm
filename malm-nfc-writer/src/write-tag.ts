import {NFC, nfcCard, NFCReader, NFCCard, NDEFMessage} from "./nfc-common";
import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';

// Define the AlbumMetadata interface based on the Kotlin class
interface AlbumMetadata {
    mbids: string[];
    title: string;
    artist?: string;
    year?: number;
    appleMusicAlbumId?: string;
}

const nfc = new NFC();

// Function to read all metadata files
function readAllMetadataFiles(): AlbumMetadata[] {
    const userHome = os.homedir();
    const metadataDir = path.join(userHome, '.malm', 'metadata');
    const metadataFiles: AlbumMetadata[] = [];

    try {
        // Check if directory exists
        if (!fs.existsSync(metadataDir)) {
            console.error(`Metadata directory does not exist: ${metadataDir}`);
            return [];
        }

        // Read all JSON files in the directory
        const files = fs.readdirSync(metadataDir)
            .filter(file => file.endsWith('.json'));

        for (const file of files) {
            const filePath = path.join(metadataDir, file);
            const fileContent = fs.readFileSync(filePath, 'utf8');
            const metadata = JSON.parse(fileContent) as AlbumMetadata;

            // Print filename without extension and title
            const filenameWithoutExt = path.basename(file, '.json');
            console.log(`${filenameWithoutExt}: ${metadata.title}`);

            metadataFiles.push(metadata);
        }
    } catch (error) {
        console.error('Error reading metadata files:', error);
    }

    return metadataFiles;
}

// Read all metadata files before starting NFC operations
const allMetadata = readAllMetadataFiles();

nfc.on('reader', (reader: NFCReader) => {

    console.log(`${reader.reader.name} device attached`);

    reader.on('card', async (card: NFCCard) => {

        console.log(`card detected`, card);

        try {
            const cardHeader: Buffer = await reader.read(0, 20);

            const tag = nfcCard.parseInfo(cardHeader);

            const message: NDEFMessage[] = [
                {type: 'text', text: 'applemusic/now/album:1673857120', language: 'en'},
            ];

            // Prepare the buffer to write on the card
            const rawDataToWrite = nfcCard.prepareBytesToWrite(message);

            // Write the buffer on the card starting at block 4
            const preparationWrite: boolean = await reader.write(4, rawDataToWrite.preparedData);

            // Success !
            if (preparationWrite) {
                console.log('Data have been written successfully.');
                process.exit(0); // Exit the process after successful write
            }

        } catch (err: unknown) {
            console.error(`error when reading data`, err);
            process.exit(1); // Exit with error code
        }
    });

    reader.on('card.off', (card: NFCCard) => {
        console.log(`${reader.reader.name} card removed`, card);
    });

    reader.on('error', (err: Error) => {
        console.log(`${reader.reader.name} an error occurred`, err);
    });

    reader.on('end', () => {
        console.log(`${reader.reader.name} device removed`);
    });

});

nfc.on('error', (err: Error) => {
    console.log('an error occurred', err);
});
