import {NDEFMessage, NFC, NFCCard, nfcCard, NFCReader} from "./nfc-common";
import {readAllMetadataFiles} from "./metadata-reader";

const nfc = new NFC();

// Read all metadata files before starting NFC operations
const albums = readAllMetadataFiles();

let currentAlbumIndex = 0;
let currentAlbum = albums[currentAlbumIndex];

function logWaitingToWrite() {
    console.log(`Waiting to write: ${currentAlbum.artist} - ${currentAlbum.title}`);
}

nfc.on('reader', (reader: NFCReader) => {

    console.log(`${reader.reader.name} device attached`);

    logWaitingToWrite();

    reader.on('card', async (card: NFCCard) => {
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

            if (preparationWrite) {
                currentAlbumIndex++;
                currentAlbum = albums[currentAlbumIndex];
                if (currentAlbumIndex == albums.length) {
                    console.log('All Albums have been written successfully.');
                    process.exit(0);
                }
            }

            logWaitingToWrite();

        } catch (err: unknown) {
            console.error(`error when reading data. we'll try again...`, err);
            logWaitingToWrite();
        }
    });

    reader.on('card.off', (card: NFCCard) => {
        // console.log(`${reader.reader.name} card removed`, card);
    });

    reader.on('error', (err: Error) => {
        // console.log(`${reader.reader.name} an error occurred`, err);
    });

    reader.on('end', () => {
        // console.log(`${reader.reader.name} device removed`);
    });

});

nfc.on('error', (err: Error) => {
    console.log('an error occurred', err);
});
