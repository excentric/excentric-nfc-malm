import {makeSonosRequest} from "./sonos-api-client";
import {getGreenText, getRedText, logReaderAttached, NDEFMessage, NFC, NFCCard, nfcCard, NFCReader} from "./nfc-common";
import settings from "./settings";

const nfc = new NFC();

nfc.on('reader', (reader: NFCReader) => {
    logReaderAttached(reader);

    reader.on('card', async (card: NFCCard) => {
        try {
            nfcCard.parseInfo(await reader.read(0, 20));

            if (nfcCard.isFormatedAsNDEF() && nfcCard.hasReadPermissions() && nfcCard.hasNDEFMessage()) {
                const NDEFRawMessage: Buffer = await reader.read(4, nfcCard.getNDEFMessageLengthToRead()); // starts reading in block 0 until 6
                const NDEFMessage: NDEFMessage[] = nfcCard.parseNDEF(NDEFRawMessage);

                let sonosCommand = NDEFMessage[0].text;

                console.log(`Detected Sonos Command: ${getGreenText(sonosCommand)}`,);

                if (sonosCommand.startsWith("applemusic")) {
                    console.log(`Clearing queue before playing...`,);
                    await makeSonosRequest("clearqueue", settings.sonosRoom);
                    await new Promise(resolve => setTimeout(resolve, 100));
                }

                await makeSonosRequest(sonosCommand, settings.sonosRoom);
            } else {
                console.log('Could not parse anything from this tag');
            }

        } catch (err: unknown) {
            console.error(`${getRedText("Error:")} when reading data. Keep trying...`, err);
        }
    });
});

nfc.on('error', (err: Error) => {
    console.log('an error occurred', err);
});
