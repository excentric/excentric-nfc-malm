import {NFC, nfcCard, NFCReader, NFCCard, NDEFMessage} from "./nfc-common";

const nfc = new NFC();

nfc.on('reader', (reader: NFCReader) => {

    console.log(`${reader.reader.name}  device attached`);

    reader.on('card', async (card: NFCCard) => {

        console.log(`card detected`, card);

        try {
            const cardHeader: Buffer = await reader.read(0, 20);

            const tag = nfcCard.parseInfo(cardHeader);

            const message: NDEFMessage[] = [
                { type: 'text', text: 'applemusic/now/album:1673857120', language: 'en' },
            ];

            // Prepare the buffer to write on the card
            const rawDataToWrite = nfcCard.prepareBytesToWrite(message);

            // Write the buffer on the card starting at block 4
            const preparationWrite: boolean = await reader.write(4, rawDataToWrite.preparedData);

            // Success !
            if (preparationWrite) {
                console.log('Data have been written successfully.');
            }

        } catch (err: unknown) {
            console.error(`error when reading data`, err);
        }
    });

    reader.on('card.off', (card: NFCCard) => {
        console.log(`${reader.reader.name}  card removed`, card);
    });

    reader.on('error', (err: Error) => {
        console.log(`${reader.reader.name}  an error occurred`, err);
    });

    reader.on('end', () => {
        console.log(`${reader.reader.name}  device removed`);
    });

});

nfc.on('error', (err: Error) => {
    console.log('an error occurred', err);
});
