import {getGreenText, logReaderAttached, NDEFMessage, NFC, NFCCard, nfcCard, NFCReader} from "./nfc-common";
import {AlbumMetadata, readAllMetadataFiles} from "./metadata-reader";

const args = process.argv.slice(2);

// Parse the first optional argument if provided
let mode: string | undefined;
let value: string;
const nfc = new NFC();
const albums = readAllMetadataFiles();
let currentAlbumIndex = 0;


if (args.length > 0) {
    const arg = args[0];
    const match = arg.match(/^([^:]+):(.+)$/);

    if (!match) {
        console.error('Error: First argument must be in the format "$mode:$value"');
        process.exit(1);
    }

    mode = match[1];
    value = match[2];
}

switch (mode) {
    case "amid":
        appleMusicIdMode()
        break
    case "action":
        actionMode()
        break
    default:
        albumMode()
        break
}

function getColouredAlbumDetails(currentAlbum: AlbumMetadata) {
    return `${getGreenText(currentAlbum.artist)} - \x1b[32m${currentAlbum.title}\x1b[0m (\x1b[32m${currentAlbum.year}\x1b[0m)`
}

function getAppleMusicPlayNowAction(appleMusicAlbumId: string) {
    return `applemusic/now/album:${appleMusicAlbumId}`;
}


function appleMusicIdMode() {
    startNfcWriter(
        () => {
            console.log(`Present card to write action: ${getGreenText(getAppleMusicPlayNowAction(value))}`);
        },
        () => {
            return value;
        },
        () => {
            console.log('Action has been written successfully.');
            process.exit(0);
        },
    );
}

function actionMode() {
    startNfcWriter(
        () => {
            console.log(`Present card to write action: ${getGreenText(value)}`);
        },
        () => {
            return value;
        },
        () => {
            console.log('Action has been written successfully.');
            process.exit(0);
        },
    );
}

function albumMode() {

    for (const album of albums) {
        console.log(`${album.slot}: ${getColouredAlbumDetails(album)}`);
    }

    startNfcWriter(
        () => {
            console.log(`Present card to write: ${getColouredAlbumDetails(albums[currentAlbumIndex])}`);
        },
        () => {
            return getAppleMusicPlayNowAction(albums[currentAlbumIndex].appleMusicAlbumId || "")
        },
        () => {
            currentAlbumIndex++;
            if (currentAlbumIndex == albums.length) {
                console.log('All Albums have been written successfully.');
                process.exit(0);
            }
        },
    );
}

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

    });

    nfc.on('error', (err: Error) => {
        console.log('an error occurred', err);
    });
}
