import {makeSonosRequest} from "./sonos-api-client";
import {getGreenText, getRedText, logReaderAttached, NDEFMessage, NFC, NFCCard, nfcCard, NFCReader} from "./nfc-common";
import settings from "./settings";
import notifier from 'node-notifier';

const nfc = new NFC();

function desktopNotify(title: string, message: string) {
    notifier.notify({
        title: title,
        message: message,
        sound: false,
        wait: false
    });
}

nfc.on('reader', (reader: NFCReader) => {
    logReaderAttached(reader);

    reader.on('card', async (card: NFCCard) => {
        try {
            const cardHeader: Buffer = await reader.read(0, 20);
            nfcCard.parseInfo(cardHeader);

            if (nfcCard.isFormatedAsNDEF() && nfcCard.hasReadPermissions() && nfcCard.hasNDEFMessage()) {
                const NDEFRawMessage: Buffer = await reader.read(4, nfcCard.getNDEFMessageLengthToRead()); // starts reading in block 0 until 6
                const NDEFMessage: NDEFMessage[] = nfcCard.parseNDEF(NDEFRawMessage);

                let sonosCommand = NDEFMessage[0].text;

                console.log(`Detected Sonos Command: ${getGreenText(sonosCommand)}`,);

                if (sonosCommand.startsWith("applemusic")) {
                    desktopNotify(`Playing album on ${settings.sonosRoom}`, `${sonosCommand}`);

                    console.log(`Clearing queue before playing...`,);
                    await makeSonosRequest("clearqueue", settings.sonosRoom);
                    await new Promise(resolve => setTimeout(resolve, 100));
                } else if (sonosCommand.startsWith("playpause")) {
                    desktopNotify(`Play / Pause on ${settings.sonosRoom}`, `${sonosCommand}`);
                }

                await makeSonosRequest(sonosCommand, settings.sonosRoom);

            } else {
                console.log('Could not parse anything from this tag: \n The tag is either empty, locked, has a wrong NDEF format or is unreadable.');
            }

        } catch (err: unknown) {
            desktopNotify(`Error reading NFC`, `Please try again...`);

            console.error(`${getRedText("Error:")} when reading data. Keep trying...`, err);
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
