import {NDEFMessage, NFC, NFCCard, nfcCard, NFCReader, logReaderAttached, getGreenText} from "./nfc-common";
import {AlbumMetadata, readAllMetadataFiles} from "./metadata-reader";

const nfc = new NFC();

function getColouredAlbumDetails(currentAlbum: AlbumMetadata) {
    return `${getGreenText(currentAlbum.artist)} - \x1b[32m${currentAlbum.title}\x1b[0m (\x1b[32m${currentAlbum.year}\x1b[0m)`
}

function logWaitingToWrite() {
    console.log(`Present card to write: ${getColouredAlbumDetails(currentAlbum)}`);
}

// Read all metadata files before starting NFC operations
const albums = readAllMetadataFiles();

for (const album of albums) {
    console.log(`${album.slot}: ${getColouredAlbumDetails(album)}`);
}

let currentAlbumIndex = 0;
let currentAlbum = albums[currentAlbumIndex];

nfc.on('reader', (reader: NFCReader) => {
    logReaderAttached(reader);

    logWaitingToWrite();

    reader.on('card', async (card: NFCCard) => {
        try {
            const cardHeader: Buffer = await reader.read(0, 20);

            const tag = nfcCard.parseInfo(cardHeader);

            const message: NDEFMessage[] = [
                {type: 'text', text: `applemusic/now/album:${currentAlbum.appleMusicAlbumId}`, language: 'en'},
                // {type: 'text', text: `playpause`, language: 'en'},
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
            console.error(`error when writing data. we'll try again...`, err);
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
