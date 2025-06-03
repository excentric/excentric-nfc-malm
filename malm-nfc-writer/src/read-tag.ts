import {makeSonosRequest} from "./sonos-api-client";
import {getGreenText, getRedText, logReaderAttached, NDEFMessage, NFC, NFCCard, nfcCard, NFCReader} from "./nfc-common";

const nfc = new NFC();

nfc.on('reader', (reader: NFCReader) => {
    logReaderAttached(reader);

    reader.on('card', async (card: NFCCard) => {
        try {
            const cardHeader: Buffer = await reader.read(0, 20);
            nfcCard.parseInfo(cardHeader);

            if (nfcCard.isFormatedAsNDEF() && nfcCard.hasReadPermissions() && nfcCard.hasNDEFMessage()) {
                const NDEFRawMessage: Buffer = await reader.read(4, nfcCard.getNDEFMessageLengthToRead()); // starts reading in block 0 until 6
                const NDEFMessage: NDEFMessage[] = nfcCard.parseNDEF(NDEFRawMessage);

                console.log(`Detected Sonos Command: ${getGreenText(NDEFMessage[0].text)}`,);

                makeSonosRequest(NDEFMessage[0].text)
            } else {
                console.log('Could not parse anything from this tag: \n The tag is either empty, locked, has a wrong NDEF format or is unreadable.');
            }

        } catch (err: unknown) {
            console.error(`${getRedText("Error:")} when reading data. Keep trying...`);
        }
    });

    reader.on('card.off', (card: NFCCard) => {
        // console.log(`${reader.reader.name}  card removed`, card);
    });

    reader.on('error', (err: Error) => {
        // console.log(`${reader.reader.name}  an error occurred`, err);
    });

    reader.on('end', () => {
        // console.log(`${reader.reader.name}  device removed`);
    });

});

nfc.on('error', (err: Error) => {
    console.log('an error occurred', err);
});
