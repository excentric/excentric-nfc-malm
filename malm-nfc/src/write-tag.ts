import {getGreenText, logReaderAttached, NDEFMessage, NFC, NFCCard, nfcCard, NFCReader} from "./nfc-common";
import {AlbumMetadata, readAllMetadataFiles} from "./metadata-reader";

const args = process.argv.slice(2);

const nfc = new NFC();

function getColouredAlbumDetails(currentAlbum: AlbumMetadata) {
    return `${getGreenText(currentAlbum.artist)} - \x1b[32m${currentAlbum.title}\x1b[0m (\x1b[32m${currentAlbum.year}\x1b[0m)`
}

const albums = readAllMetadataFiles();
let currentAlbumIndex = 0;

for (const album of albums) {
    console.log(`${album.slot}: ${getColouredAlbumDetails(album)}`);
}

startNfcWriter(
    () => {
        console.log(`Present card to write: ${getColouredAlbumDetails(albums[currentAlbumIndex])}`);
    },
    () => {
        return `applemusic/now/album:${albums[currentAlbumIndex].appleMusicAlbumId}`
    },
    () => {
        currentAlbumIndex++;
        if (currentAlbumIndex == albums.length) {
            console.log('All Albums have been written successfully.');
            process.exit(0);
        }
    },
);

function startNfcWriter(onWaitingForWrite: () => void, nfcText: () => string, onSuccess: () => void) {
    nfc.on('reader', (reader: NFCReader) => {
        logReaderAttached(reader);
        onWaitingForWrite();

        reader.on('card', async (card: NFCCard) => {
            try {
                const cardHeader: Buffer = await reader.read(0, 20);

                const tag = nfcCard.parseInfo(cardHeader);

                const message: NDEFMessage[] = [{type: 'text', text: nfcText(), language: 'en'}];
                const rawDataToWrite = nfcCard.prepareBytesToWrite(message);

                // Write the buffer on the card starting at block 4
                const writeSuccess: boolean = await reader.write(4, rawDataToWrite.preparedData);

                if (writeSuccess) {
                    onSuccess()
                }

                onWaitingForWrite();
            } catch (err: unknown) {
                console.error(`Error when writing data. we'll try again...`, err);
                onWaitingForWrite();
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
}

